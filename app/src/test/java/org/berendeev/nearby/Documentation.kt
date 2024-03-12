package org.berendeev.nearby

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.berendeev.nearby.ui.SearchBarCoordinatesUnavailablePreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h914dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class Documentation {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun generateSearchBarDocumentation() = with(composeTestRule) {
        // api
        componentDocumentation {
            name = "SearchBar"
            description = "Search field with navigation icon"
            sample = {
                SearchBarCoordinatesUnavailablePreview()
            }
        }
    }
}


// implementation
fun ComposeContentTestRule.componentDocumentation(block: Component.() -> Unit) {
    val component = Component()
    component.block()
    setContent {
        val sample = component.sample
        checkNotNull(sample)
        sample()
    }
    val samplePath = "img/${component.name}-sample.png"
    onRoot().captureRoboImage("documentation/$samplePath")
    val docPath = "documentation/${component.name}-doc.md"
    File(docPath)
        .writeText(
            template(requireNotNull(component.name), requireNotNull(component.description), samplePath)
        )
}

class Component {
    var name: String? = null
    var description: String? = null
    var sample: @Composable (() -> Unit)? = null
}

fun template(name: String, description: String, samplePath: String): String {
    return """
        # $name
        ## Overview
        $description
        <div class="row">
            <div class="col-md-6 mobile-visuals">
                <img src="$samplePath" alt="Screenshot of the $name" />
            </div>
        </div>
        <br>
    """.trimIndent()
}
