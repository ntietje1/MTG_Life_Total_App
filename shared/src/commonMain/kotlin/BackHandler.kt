import androidx.navigation.NavHostController

class BackHandler() {

    private lateinit var nav: NavHostController

    val backStack = mutableListOf<() -> Unit>(
//        { println("backstack 3") },
//        { println("backstack 2") },
//        { println("backstack 1") }
    )

    fun attachNavigation(nav: NavHostController) {
//        println("backhandler.attachNavigation()")
        this.nav = nav
//        nav.addOnDestinationChangedListener() {
//        }
    }

    fun push(block: () -> Unit) {
//        println("backhandler.push()")
        backStack.add(block)
    }

    fun pop() {
//        println("backhandler.pop()")
        if (backStack.isNotEmpty()) {
//            println("backstack removeLast()")
            backStack.removeLast().invoke()
        } else {
//            println("nav.navigateUp()")
            nav.popBackStack()
        }
    }
}