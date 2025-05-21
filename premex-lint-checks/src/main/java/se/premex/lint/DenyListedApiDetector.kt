package se.premex.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.FileOption
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LocationType.NAME
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.android.utils.forEach
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.util.isConstructorCall
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import se.premex.lint.DenyListedEntry.Companion.MatchAll
import java.io.File
import java.util.EnumSet
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Deny-listed APIs that we don't want people to use.
 *
 * Adapted from https://gist.github.com/JakeWharton/1f102d98cd10133b03a5f374540c327a
 */
internal class DenyListedApiDetector : Detector(), SourceCodeScanner, XmlScanner {
    private lateinit var config: DenyListConfig
    override fun beforeCheckRootProject(context: Context) {
        super.beforeCheckRootProject(context)
        config = loadBlocklist(context)
    }

    override fun getApplicableUastTypes() = config.applicableTypes()
    override fun createUastHandler(context: JavaContext) = config.visitor(context)

    override fun getApplicableElements() = config.applicableLayoutInflaterElements.keys
    override fun visitElement(context: XmlContext, element: Element) =
        config.visitor(context, element)

    private class DenyListConfig(entries: List<DenyListedEntry>) {
        private class TypeConfig(entries: List<DenyListedEntry>) {
            @Suppress("UNCHECKED_CAST") // Safe because of filter call.
            val functionEntries =
                entries.groupBy { it.functionName }.filterKeys { it != null }
                        as Map<String, List<DenyListedEntry>>

            @Suppress("UNCHECKED_CAST") // Safe because of filter call.
            val referenceEntries =
                entries.groupBy { it.fieldName }.filterKeys { it != null }
                        as Map<String, List<DenyListedEntry>>
        }

        private val typeConfigs =
            entries.groupBy { it.className }.mapValues { (_, entries) -> TypeConfig(entries) }

        val applicableLayoutInflaterElements =
            entries
                .filter { it.functionName == "<init>" }
                .filter {
                    it.arguments == null ||
                            it.arguments == listOf(
                        "android.content.Context",
                        "android.util.AttributeSet"
                    )
                }
                .groupBy { it.className }
                .mapValues { (cls, entries) ->
                    entries.singleOrNull() ?: error("Multiple two-arg init rules for $cls")
                }

        fun applicableTypes() =
            listOf<Class<out UElement>>(
                UCallExpression::class.java,
                UImportStatement::class.java,
                UQualifiedReferenceExpression::class.java,
            )

        fun visitor(context: JavaContext) =
            object : UElementHandler() {
                override fun visitCallExpression(node: UCallExpression) {
                    val function = node.resolve() ?: return

                    val className = function.containingClass?.qualifiedName
                    val typeConfig = typeConfigs[className] ?: return

                    val functionName =
                        if (node.isConstructorCall()) {
                            "<init>"
                        } else {
                            // Kotlin compiler mangles function names that use inline value types as parameters by
                            // suffixing them
                            // with a hyphen.
                            // https://github.com/Kotlin/KEEP/blob/master/proposals/inline-classes.md#mangling-rules
                            function.name.substringBefore("-")
                        }

                    val deniedFunctions =
                        typeConfig.functionEntries.getOrDefault(functionName, emptyList()) +
                                typeConfig.functionEntries.getOrDefault(MatchAll, emptyList())

                    deniedFunctions.forEach { denyListEntry ->
                        if (
                            denyListEntry.parametersMatchWith(function) && denyListEntry.argumentsMatchWith(
                                node
                            )
                        ) {
                            context.report(
                                issue = ISSUE,
                                location = context.getLocation(node),
                                message = denyListEntry.errorMessage
                            )
                        }
                    }
                }

                override fun visitImportStatement(node: UImportStatement) {
                    val reference = node.resolve() as? PsiField ?: return
                    visitField(reference, node)
                }

                override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
                    val reference = node.resolve() as? PsiField ?: return
                    visitField(reference, node)
                }

                private fun visitField(reference: PsiField, node: UElement) {
                    val className = reference.containingClass?.qualifiedName
                    val typeConfig = typeConfigs[className] ?: return

                    val referenceName = reference.name
                    val deniedFunctions =
                        typeConfig.referenceEntries.getOrDefault(referenceName, emptyList()) +
                                typeConfig.referenceEntries.getOrDefault(MatchAll, emptyList())

                    deniedFunctions.forEach { denyListEntry ->
                        context.report(
                            issue = ISSUE,
                            location = context.getLocation(node),
                            message = denyListEntry.errorMessage
                        )
                    }
                }
            }

        fun visitor(context: XmlContext, element: Element) {
            val denyListEntry = applicableLayoutInflaterElements.getValue(element.tagName)
            context.report(
                issue = ISSUE,
                location = context.getLocation(element, type = NAME),
                message = denyListEntry.errorMessage,
            )
        }

        private fun DenyListedEntry.parametersMatchWith(function: PsiMethod): Boolean {
            val expected = parameters
            val actual = function.parameterList.parameters.map { it.type.canonicalText }

            return when {
                expected == null -> true
                expected.isEmpty() && actual.isEmpty() -> true
                expected.size != actual.size -> false
                else -> expected == actual
            }
        }

        private fun DenyListedEntry.argumentsMatchWith(node: UCallExpression): Boolean {
            // "arguments" being null means we don't care about this check and it should just return true.
            val expected = arguments ?: return true
            val actual = node.valueArguments

            return when {
                expected.size != actual.size -> false
                else ->
                    expected.zip(actual).all { (expectedValue, actualValue) ->
                        argumentMatches(expectedValue, actualValue)
                    }
            }
        }

        private fun argumentMatches(expectedValue: String, actualValue: UExpression): Boolean {
            if (expectedValue == "*") return true
            val renderString =
                (actualValue as? ULiteralExpression)?.asRenderString()
                    ?: (actualValue as? UQualifiedReferenceExpression)
                        ?.asRenderString() // Helps to match against static method params
            // 'Class.staticMethod()'.
            if (expectedValue == renderString) return true

            return false
        }
    }

    companion object {

        internal val BLOCK_FILE_LIST =
            FileOption(
                name = "file-block-list",
                description = "A configuration xml file to point out classes, methods and fields that should be blocked",
                defaultValue = null,
                explanation = "To get help with configuration, please visit https://github.com/premex-ab/premex-lints"
            )

        private fun lastChildValue(elementsByTagName: NodeList): List<String>? {
            val list = mutableListOf<String>()
            elementsByTagName.forEach {
                list.add(it.lastChild.nodeValue)
            }
            return if (list.size > 0) {
                list
            } else {
                null
            }
        }

        private fun NodeList.text(): String? {
            return if (this.length > 0) {
                this.item(0).lastChild.nodeValue
            } else {
                null
            }
        }

        private fun loadBlocklist(context: Context): DenyListConfig {

            val entries: MutableList<DenyListedEntry> = mutableListOf()

            val fileContent: File = BLOCK_FILE_LIST.getValue(context.configuration)
                ?: File(context.project.dir, "blocklist.xml")

            if (!fileContent.exists()) {
                java.lang.RuntimeException(
                    fileContent.absolutePath +
                            " does not exists. Please create it and configure " +
                            "a blocklist before using BlockListedApi lint check"
                ).printStackTrace()
            } else {
                try {

                    val builderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder = builderFactory.newDocumentBuilder()
                    val doc = docBuilder.parse(fileContent)

                    val nList: NodeList = doc.getElementsByTagName("Blocked")
                    nList.forEach {


                        if (nList.item(0).nodeType == Node.ELEMENT_NODE) {
                            val element = it as Element

                            entries.add(
                                DenyListedEntry(
                                    className = element.getElementsByTagName("ClassName")
                                        .text()!!,
                                    functionName = element.getElementsByTagName("FunctionName")
                                        .text(),
                                    fieldName = element.getElementsByTagName("FieldName")
                                        .text(),
                                    parameters = lastChildValue(element.getElementsByTagName("Parameters")),
                                    arguments = lastChildValue(element.getElementsByTagName("Arguments")),
                                    errorMessage = element.getElementsByTagName("ErrorMessage")
                                        .text()!!,
                                )
                            )
                        }
                    }

                } catch (exception: Exception) {
                    val error = java.lang.RuntimeException(
                        "Error parsing ${fileContent.absolutePath} xml file for BlockListedApi " +
                                "lint checks please validate that the xml file has " +
                                "the correct format",
                        exception
                    )
                    error.printStackTrace()
                }
            }


            return DenyListConfig(entries)
        }

        val ISSUE =
            Issue.create(
                id = "BlockListedApi",
                briefDescription = "Block-listed API",
                explanation =
                "This lint check flags usages of APIs in external libraries that we should block and no longer use.",
                category = CORRECTNESS,
                priority = 5,
                severity = ERROR,
                implementation =
                Implementation(
                    DenyListedApiDetector::class.java,
                    EnumSet.of(Scope.JAVA_FILE, Scope.RESOURCE_FILE, Scope.TEST_SOURCES),
                    EnumSet.of(Scope.JAVA_FILE),
                    EnumSet.of(Scope.RESOURCE_FILE),
                    EnumSet.of(Scope.TEST_SOURCES),
                )
            )
                .setOptions(listOf(BLOCK_FILE_LIST))
    }
}

data class DenyListedEntry(
    val className: String,
    /** The function name to match, [MatchAll] to match all functions, or null if matching a field. */
    val functionName: String? = null,
    /** The field name to match, [MatchAll] to match all fields, or null if matching a function. */
    val fieldName: String? = null,
    /** Fully-qualified types of function parameters to match, or null to match all overloads. */
    val parameters: List<String>? = null,
    /** Argument expressions to match at the call site, or null to match all invocations. */
    val arguments: List<String>? = null,
    val errorMessage: String,
) {
    init {
        require((functionName == null) xor (fieldName == null)) {
            "One of functionName or fieldName must be set"
        }
    }

    companion object {
        const val MatchAll = "*"
    }
}
