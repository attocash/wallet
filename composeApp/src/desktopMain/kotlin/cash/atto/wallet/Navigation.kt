package cash.atto.wallet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value

class NavigationComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    val navigation = StackNavigation<AttoDestination>()

    val childStack: Value<ChildStack<*, AttoDestination>> =
        childStack(
            source = navigation,
            serializer = AttoDestination.serializer(), // Or null to disable navigation state saving
            initialConfiguration = AttoDestination.Welcome,
            handleBackButton = true, // Pop the back stack on back button press
            childFactory = ::createChild,
        )

    private fun createChild(
        destination: AttoDestination,
        componentContext: ComponentContext
    ): AttoDestination = destination
}