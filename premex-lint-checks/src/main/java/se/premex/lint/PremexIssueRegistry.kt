package se.premex.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.google.auto.service.AutoService

@Suppress("unused") // Entrypoint
@AutoService(IssueRegistry::class)
class PremexIssueRegistry : IssueRegistry() {

    override val vendor: Vendor = Vendor(vendorName = "premex", identifier = "premex-lint")

    override val api: Int = CURRENT_API
    override val minApi: Int = 12

    override val issues: List<Issue> =
        listOf(
            DenyListedApiDetector.ISSUE,
        )
}
