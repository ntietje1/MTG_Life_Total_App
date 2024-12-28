package di

import androidx.navigation.NavHostController
import domain.common.Backstack

class BackHandler {

    private lateinit var nav: NavHostController

    private val backStack = Backstack()

    fun attachNavigation(nav: NavHostController) {
        this.nav = nav
    }

    fun push(block: () -> Unit) {
        backStack.push(block)
    }

    fun pop() {
        if (backStack.isEmpty.value) {
            nav.popBackStack()
        } else {
            backStack.pop().invoke()
        }
    }
}