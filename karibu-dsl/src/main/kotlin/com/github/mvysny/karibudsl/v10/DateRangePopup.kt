package com.github.mvysny.karibudsl.v10

import com.github.mvysny.kaributools.BrowserTimeZone
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.customfield.CustomField
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import java.io.Serializable
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/**
 * A potentially unbounded date range. If both [start] and [endInclusive] are `null`, then the interval accepts any date.
 *
 * Immutable, thread-safe.
 * @property start the minimum accepted value, inclusive. If `null` then the date range has no lower limit.
 * @property endInclusive the maximum accepted value, inclusive. If `null` then the date range has no upper limit.
 */
public data class DateInterval(override val start: LocalDate?, override val endInclusive: LocalDate?) : Serializable, ClosedInterval<LocalDate> {
    public companion object {
        public val EMPTY: DateInterval =
                DateInterval(LocalDate.of(2000, 1, 2), LocalDate.of(2000, 1, 1))
        public val UNIVERSAL: DateInterval = DateInterval(null, null)
        /**
         * Produces a degenerate date interval that only contains [LocalDate.now].
         */
        public fun now(zoneId: ZoneId = BrowserTimeZone.get): DateInterval = of(LocalDate.now(zoneId))
        public fun of(localDate: LocalDate): DateInterval = DateInterval(localDate, localDate)
    }
}

/**
 * Only shows a single button as its contents. When the button is clicked, it opens a dialog and allows the user to specify a range
 * of dates. When the user sets the values, the dialog is
 * hidden and the date range is set as the value of the popup.
 *
 * The current date range is also displayed as the caption of the button.
 */
public class DateRangePopup: CustomField<DateInterval>() {
    private val formatter: DateTimeFormatter get() =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(UI.getCurrent().locale ?: Locale.getDefault())
    private lateinit var fromField: DatePicker
    private lateinit var toField: DatePicker
    private lateinit var set: Button
    private lateinit var clear: Button
    private val dialog = Dialog()
    /**
     * The button which opens the popup [dialog].
     */
    private val content: Button = content {
        button {
            onLeftClick {
                isDialogVisible = !isDialogVisible
            }
        }
    }

    init {
        dialog.apply {
            isCloseOnEsc = true
            isCloseOnOutsideClick = true
            verticalLayout {
                fromField = datePicker(karibuDslI18n("from"))
                toField = datePicker(karibuDslI18n("to"))
                horizontalLayout {
                    set = button(karibuDslI18n("set")) {
                        onLeftClick {
                            updateValue()
                            updateCaption()
                            dialog.close()
                        }
                    }
                    clear = button(karibuDslI18n("clear")) {
                        onLeftClick {
                            fromField.value = null
                            toField.value = null
                            updateValue()
                            updateCaption()
                            dialog.close()
                        }
                    }
                }
            }
        }
        updateCaption()
    }

    public var isDialogVisible: Boolean
        get() = dialog.isOpened
        set(value) {
            dialog.isOpened = value
        }

    override fun generateModelValue(): DateInterval? {
        val from: LocalDate? = fromField.value
        val to: LocalDate? = toField.value
        return if (from == null && to == null) null else DateInterval(from, to)
    }

    override fun setPresentationValue(newPresentationValue: DateInterval?) {
        fromField.value = newPresentationValue?.start
        toField.value = newPresentationValue?.endInclusive
        updateCaption()
    }

    private fun format(date: LocalDate?): String = if (date == null) "" else formatter.format(date)

    private fun updateCaption() {
        val value: DateInterval? = value
        if (value == null) {
            content.text = karibuDslI18n("all")
        } else {
            content.text = "${format(fromField.value)} - ${format(toField.value)}"
        }
    }

    override fun setReadOnly(readOnly: Boolean) {
        set.isEnabled = !readOnly
        clear.isEnabled = !readOnly
        fromField.isEnabled = !readOnly
        toField.isEnabled = !readOnly
    }

    override fun isReadOnly(): Boolean = !fromField.isEnabled

    override fun setRequiredIndicatorVisible(requiredIndicatorVisible: Boolean) {
        fromField.isRequiredIndicatorVisible = requiredIndicatorVisible
        toField.isRequiredIndicatorVisible = requiredIndicatorVisible
    }

    override fun isRequiredIndicatorVisible(): Boolean = fromField.isRequiredIndicatorVisible
}

public fun (@VaadinDsl HasComponents).dateRangePopup(block: (@VaadinDsl DateRangePopup).() -> Unit = {}): DateRangePopup
        = init(DateRangePopup(), block)
