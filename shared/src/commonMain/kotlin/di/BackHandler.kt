package di

import androidx.navigation.NavHostController

class BackHandler() {

    private lateinit var nav: NavHostController

    val backStack = mutableListOf<Pair<String, () -> Unit>>(
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
        backStack.add(Pair("unlabeled", block))
    }

    fun push(label: String, block: () -> Unit) {
//        println("backhandler.push()")
        backStack.add(Pair(label, block))
//        backStack.add(block)
    }


    fun pop() {
//        println("backhandler.pop()")
        if (backStack.isNotEmpty()) {
//            println("backstack removeLast()")
            backStack.removeAt(backStack.size - 1).second.invoke()
        } else {
//            println("nav.navigateUp()")
            nav.popBackStack()
        }
    }

    fun popUntil(label: String) {
//        println("backhandler.popUntil()")
        while (backStack.isNotEmpty() && backStack.last().first != label) {
            backStack.removeAt(backStack.size - 1).second.invoke()
        }
    }
}