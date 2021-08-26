package com.github.mvysny.karibudsl.v10

import com.vaadin.flow.component.*
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.dom.ClassList
import com.vaadin.flow.dom.DomEventListener
import com.vaadin.flow.dom.DomListenerRegistration
import com.vaadin.flow.dom.Element
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.QueryParameters
import com.vaadin.shrinkwrap.VaadinCoreShrinkWrap

/**
 * Fires given event on the component.
 */
public fun Component.fireEvent(event: ComponentEvent<*>) {
    ComponentUtil.fireEvent(this, event)
}

/**
 * Adds [com.vaadin.flow.component.button.Button.click] functionality to all [ClickNotifier]s. This function directly calls
 * all click listeners, thus it avoids the roundtrip to client and back. It even works with browserless testing.
 */
public fun <R> R.serverClick() where R : Component, R : ClickNotifier<R> {
    fireEvent(ClickEvent<R>(this, true, -1, -1, -1, -1, 1, -1, false, false, false, false))
}

/**
 * Appends a text node with given [text] to the component.
 */
@VaadinDsl
public fun (@VaadinDsl HasComponents).text(text: String, block: (@VaadinDsl Text).() -> Unit = {}): Text =
        init(Text(text), block)

/**
 * Sets the alignment of the text in the component. One of `center`, `left`, `right`, `justify`.
 */
public var Component.textAlign: String?
    get() = element.style.get("textAlign")
    set(value) { element.style.set("textAlign", value) }

/**
 * Either calls [Element.setAttribute] (if the [value] is not null), or
 * [Element.removeAttribute] (if the [value] is null).
 * @param attribute the name of the attribute.
 */
public fun Element.setOrRemoveAttribute(attribute: String, value: String?) {
    if (value == null) {
        removeAttribute(attribute)
    } else {
        setAttribute(attribute, value)
    }
}

/**
 * Sets or removes the `title` attribute on component's element.
 */
public var Component.tooltip: String?
    get() = element.getAttribute("title")
    set(value) { element.setOrRemoveAttribute("title", value) }

/**
 * Adds the right-click (context-menu) [listener] to the component. Also causes the right-click browser
 * menu not to be shown on this component (see [preventDefault]).
 */
public fun Component.addContextMenuListener(listener: DomEventListener): DomListenerRegistration =
        element.addEventListener("contextmenu", listener)
                .preventDefault()

/**
 * Makes the client-side listener call [Event.preventDefault()](https://developer.mozilla.org/en-US/docs/Web/API/Event/preventDefault)
 * on the event.
 *
 * @return this
 */
public fun DomListenerRegistration.preventDefault(): DomListenerRegistration = addEventData("event.preventDefault()")

/**
 * Removes the component from its parent. Does nothing if the component is not attached to a parent.
 */
public fun Component.removeFromParent() {
    (parent.orElse(null) as? HasComponents)?.remove(this)
}

/**
 * Toggles [className] - removes it if it was there, or adds it if it wasn't there.
 * @param className the class name to toggle, cannot contain spaces.
 */
public fun ClassList.toggle(className: String) {
    require(!className.containsWhitespace()) { "'$className' cannot contain whitespace" }
    set(className, !contains(className))
}

/**
 * Finds component's parent, parent's parent (etc) which satisfies given [predicate].
 * Returns null if there is no such parent.
 */
public fun Component.findAncestor(predicate: (Component) -> Boolean): Component? =
        findAncestorOrSelf { it != this && predicate(it) }

/**
 * Finds component, component's parent, parent's parent (etc) which satisfies given [predicate].
 * Returns null if no component on the ancestor-or-self axis satisfies.
 */
public tailrec fun Component.findAncestorOrSelf(predicate: (Component) -> Boolean): Component? {
    if (predicate(this)) {
        return this
    }
    val p: Component = parent.orElse(null) ?: return null
    return p.findAncestorOrSelf(predicate)
}

/**
 * Checks if this component is nested in [potentialAncestor].
 */
public fun Component.isNestedIn(potentialAncestor: Component): Boolean =
        findAncestor { it == potentialAncestor } != null

/**
 * Checks whether this component is currently attached to a [UI].
 *
 * Returns true for attached components even if the UI itself is closed.
 */
public fun Component.isAttached(): Boolean {
    // see https://github.com/vaadin/flow/issues/7911
    return element.node.isAttached
}

/**
 * Inserts this component as a child, right before an [existing] one.
 *
 * In case the specified component has already been added to another parent,
 * it will be removed from there and added to this one.
 */
public fun HasOrderedComponents<*>.insertBefore(newComponent: Component, existing: Component) {
    val parent: Component = requireNotNull(existing.parent.orElse(null)) { "$existing has no parent" }
    require(parent == this) { "$existing is not nested in $this" }
    addComponentAtIndex(indexOf(existing), newComponent)
}

/**
 * Return the location of the currently shown view. The function will report the current (old)
 * view in [com.vaadin.flow.router.BeforeLeaveEvent] and [com.vaadin.flow.router.BeforeEnterEvent].
 */
public val UI.currentViewLocation: Location get() = internals.activeViewLocation

/**
 * Returns the Vaadin version in use, e.g. "14.4.2".
 */
public val vaadinVersion: String? get() {
    val annotation: NpmPackage? = VaadinCoreShrinkWrap::class.java.getAnnotation(NpmPackage::class.java)
    return annotation?.version
}

/**
 * True when the component has any children.
 */
public val HasComponents.isNotEmpty: Boolean get() = (this as Component).children.findFirst().isPresent

/**
 * True when the component has no children.
 */
public val HasComponents.isEmpty: Boolean get() = !isNotEmpty

/**
 * Returns the singleton value associated with given [parameterName].
 * Returns null if there is no such parameter.
 * @throws IllegalStateException if the parameter has 2 or more values.
 */
public operator fun QueryParameters.get(parameterName: String): String? {
    val value = getValues(parameterName)
    return when {
        value.isEmpty() -> null
        value.size == 1 -> value.first()
        else -> throw IllegalStateException("Multiple values present for $parameterName: $value")
    }
}

/**
 * Returns the values associated with given [parameterName]. Returns an empty list
 * if there is no such parameter.
 */
public fun QueryParameters.getValues(parameterName: String): List<String> =
    parameters[parameterName] ?: listOf()

/**
 * Parses given query as a QueryParameters.
 * @param query the query string e.g. `foo=bar&quak=foo`; the parameters may repeat.
 */
public fun QueryParameters(query: String): QueryParameters = when {
    query.isBlank() -> QueryParameters.empty()
    else -> Location("?${query.trim('?')}").queryParameters
}
