package cash.atto.wallet

import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.main_nav_overview
import attowallet.composeapp.generated.resources.main_nav_receive
import attowallet.composeapp.generated.resources.main_nav_send
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import org.jetbrains.compose.resources.StringResource

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

enum class MainScreenNavDestination(
    val destinationName: StringResource
) {
    OVERVIEW(Res.string.main_nav_overview),
    SEND(Res.string.main_nav_send),
    RECEIVE(Res.string.main_nav_receive);
}