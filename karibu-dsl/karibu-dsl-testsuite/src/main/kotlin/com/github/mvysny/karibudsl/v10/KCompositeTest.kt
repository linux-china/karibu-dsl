package com.github.mvysny.karibudsl.v10

import com.github.mvysny.dynatest.*
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._expectOne
import com.github.mvysny.kaributesting.v10._get
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import java.lang.IllegalStateException

/**
 * This is also an API test that we can create components based on [KComposite]. The components should be final,
 * so that developers prefer [composition over inheritance](https://reactjs.org/docs/composition-vs-inheritance.html).
 */
class ButtonBar : KComposite() {
    private lateinit var okButton: Button
    private val root = ui {
        // create the component UI here; maybe even attach very simple listeners here
        horizontalLayout {
            okButton = button("ok") {
                onLeftClick { okClicked() }
            }
            button("cancel") {
                onLeftClick { cancelClicked() }
            }
        }
    }

    init {
        // perform any further initialization here
    }

    // listener methods here
    private fun okClicked() {}
    private fun cancelClicked() {}
}

@VaadinDsl
fun (@VaadinDsl HasComponents).buttonBar(block: (@VaadinDsl ButtonBar).()->Unit = {}) = init(ButtonBar(), block)

class MyButton : KComposite(Button("Click me!"))

/**
 * Demoes the possibility of overriding [initContent].
 */
class MyComponent : KComposite() {
    private val content = Button("Click me!")
    override fun initContent(): Component {
        return content
    }
}

@DynaTestDsl
fun DynaNodeGroup.kcompositeTest() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("lookup") {
        UI.getCurrent().apply {
            buttonBar()
        }
        _expectOne<ButtonBar>()
        _expectOne<Button> { text = "ok" }
    }

    test("uninitialized composite fails with informative exception") {
        expectThrows(IllegalStateException::class, "The content has not yet been initialized") {
            UI.getCurrent().add(object : KComposite() {})
        }
    }

    test("provide contents in constructor") {
        UI.getCurrent().add(MyButton())
        _expectOne<MyButton>()
        _expectOne<Button> { text = "Click me!" }
    }

    test("provide contents in initContent()") {
        UI.getCurrent().add(MyComponent())
        _expectOne<Button> { text = "Click me!" }
    }
}
