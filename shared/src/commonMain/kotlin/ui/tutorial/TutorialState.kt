package ui.tutorial

data class TutorialState (
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val showWarningDialog: Boolean = false,
    val showHint: Boolean = false,
    val showSuccess: Boolean = false,
    val completed: List<Boolean> = List(SingleTutorialScreen.entries.size) { false }
)
