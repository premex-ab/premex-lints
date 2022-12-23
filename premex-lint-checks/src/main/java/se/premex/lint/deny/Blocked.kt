package se.premex.lint.deny

data class Blocked(
    val className: String,
    val functionName: String?,
    val errorMessage: String,
    val parameter: List<String>?,
    val fieldName: String?,
    val arguments: String?,
)