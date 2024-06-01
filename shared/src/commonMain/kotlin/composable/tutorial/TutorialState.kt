package composable.tutorial

data class TutorialState(
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val showWarningDialog: Boolean = false
)
