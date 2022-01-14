package com.github.mvysny.karibudsl.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect
import kotlin.test.fail

@DynaTestDsl
fun DynaNodeGroup.numberRangePopupTest() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }
    lateinit var component: NumberRangePopup
    beforeEach { component = NumberRangePopup() }

    test("Initial value is null") {
        expect(null) { component.value }
    }

    test("setting the value preserves the value") {
        component.value = NumberInterval(5.0, 25.0)
        expect(NumberInterval(5.0, 25.0)) { component.value!! }
    }

    group("value change listener tests") {
        test("Setting to the same value does nothing") {
            component.addValueChangeListener {
                fail("should not be fired")
            }
            component.value = null
        }

        test("Setting the value programatically triggers value change listeners") {
            lateinit var newValue: NumberInterval<Double>
            component.addValueChangeListener {
                expect(false) { it.isFromClient }
                expect(null) { it.oldValue }
                newValue = it.value
            }
            component.value = NumberInterval(5.0, 25.0)
            expect(NumberInterval(5.0, 25.0)) { newValue }
        }

        test("value change won't trigger unregistered change listeners") {
            component.addValueChangeListener {
                fail("should not be fired")
            } .remove()
            component.value = NumberInterval(5.0, 25.0)
        }
    }

    group("popup tests") {
        beforeEach {
            // open popup
            (component.content as Button)._click()
            _get<Dialog>() // expect the dialog to pop up
        }

        test("Clear does nothing when the value is already null") {
            component.addValueChangeListener {
                fail("No listener must be fired")
            }
            _get<Button> { caption = "Clear" } ._click()
            expect(null) { component.value }
            _expectNone<Dialog>()  // the Clear button must close the dialog
        }

        test("setting the value while the dialog is opened propagates the values to date fields") {
            component.value = NumberInterval(5.0, 25.0)
            expect("5") { _get<TextField> { placeholder = "From (inclusive):" } .value }
            expect("25") { _get<TextField> { placeholder = "To (inclusive):" } .value }
        }

        test("Clear properly sets the value to null") {
            component.value = NumberInterval(25.0, 35.0)
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                expect(null) { it.value }
                wasCalled = true
            }
            _get<Button> { caption = "Clear" } ._click()
            expect(true) { wasCalled }
            expect(null) { component.value }
            _expectNone<Dialog>()  // the Clear button must close the dialog
        }

        test("Set properly sets the value to null if nothing is filled in") {
            component.value = NumberInterval(25.0, 35.0)
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                expect(null) { it.value }
                wasCalled = true
            }
            _get<TextField> { placeholder = "From (inclusive):" } .value = ""
            _get<TextField> { placeholder = "To (inclusive):" } .value = ""
            _get<Button> { caption = "Set" } ._click()
            expect(true) { wasCalled }
            expect(null) { component.value }
            _expectNone<Dialog>()  // the Set button must close the dialog
        }

        test("Set properly sets the value in") {
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                expect(NumberInterval(25.0, 35.0)) { it.value }
                wasCalled = true
            }
            _get<TextField> { placeholder = "From (inclusive):" } .value = "25"
            _get<TextField> { placeholder = "To (inclusive):" } .value = "35"
            _get<Button> { caption = "Set" } ._click()
            expect(true) { wasCalled }
            expect(NumberInterval(25.0, 35.0)) { component.value }
            _expectNone<Dialog>()  // the Set button must close the dialog
        }
    }
}
