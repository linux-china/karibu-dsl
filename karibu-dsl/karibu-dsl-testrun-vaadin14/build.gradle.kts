dependencies {
    testImplementation(project(":karibu-dsl:karibu-dsl-testsuite"))

    // Vaadin
    testImplementation("com.vaadin:vaadin-core:${properties["vaadin14_version"]}")
}
