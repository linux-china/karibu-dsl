package com.github.mvysny.karibudsl.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.cloneBySerialization
import com.github.mvysny.kaributesting.v8.*
import com.vaadin.shared.ui.datefield.DateTimeResolution
import com.vaadin.ui.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import kotlin.test.expect

class DateRangePopupTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke test") {
        DateRangePopup()
    }

    test("setting value and properties works even when not attached") {
        DateRangePopup().apply {
            value = DateInterval(LocalDateTime.now(), LocalDateTime.now())
            isReadOnly = true
            resolution = DateTimeResolution.DAY
            expect(false) { isPopupVisible }
        }
    }

    test("setting value and properties is applied immediately when the popup is opened") {
        UI.getCurrent().dateRangePopup {
            isPopupVisible = true
            isReadOnly = true
            resolution = DateTimeResolution.DAY
            val fields = _find<InlineDateTimeField> { count = 2..2 }
            expect(false) { fields[0].isEnabled }
            expect(false) { fields[1].isEnabled }
            expect(DateTimeResolution.DAY) { fields[0].resolution }
            expect(DateTimeResolution.DAY) { fields[1].resolution }
        }
    }

    test("setting value while popup is visible will update the text fields as well") {
        UI.getCurrent().dateRangePopup {
            isPopupVisible = true
            val now: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            value = DateInterval(now, now)
            val fields = _find<InlineDateTimeField> { count = 2..2 }
            expect(now) { fields[0]._value }
            expect(now) { fields[1]._value }
        }
    }

    test("fill in values") {
        UI.getCurrent().dateRangePopup {
            isPopupVisible = true
            val now = LocalDateTime.of(2018, 12, 22, 1, 2, 3, 459)
            val fields = _find<InlineDateTimeField> { count = 2..2 }
            fields[0]._value = now
            fields[1]._value = now
            _get<Button> { caption = "Set" } ._click()
            expect(false) { isPopupVisible }
            expect(DateInterval(LocalDateTime.of(2018, 12, 22, 1, 2, 0, 0),
                    LocalDateTime.of(2018, 12, 22, 1, 2, 59, 999))) { _value }
        }
    }

    test("serializable") {
        UI.getCurrent().dateRangePopup()
        UI.getCurrent().cloneBySerialization()
    }
})
