package com.vaadin.starter.beveragebuddy.backend

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Simple backend service to store and retrieve [Category] instances.
 */
object CategoryService {

    private val categories = ConcurrentHashMap<Long, Category>()
    private val nextId = AtomicLong(0)

    init {
        StaticData.BEVERAGES.values.distinct().forEach { name -> saveCategory(Category(name = name)) }
    }

    /**
     * Fetches the categories whose name matches the given filter text.
     *
     * The matching is case-insensitive. When passed an empty filter text,
     * the method returns all categories. The returned list is ordered
     * by name.
     *
     * @param filter    the filter text
     * @return          the list of matching categories
     */
    fun findCategories(filter: String): List<Category> {
        val normalizedFilter: String = filter.trim()
        // Make a copy of each matching item to keep entities and DTOs separated
        return categories.values
                .filter { categoryMatchesFilter(it, normalizedFilter) }
                .map { it.copy() }
                .sortedBy { it.name }
    }

    fun categoryMatchesFilter(category: Category, filter: String): Boolean {
        val normalizedFilter: String = filter.trim()
        return category.name.contains(normalizedFilter, ignoreCase = true)
    }

    /**
     * Searches for the exact category whose name matches the given [filter] text.
     *
     * The matching is substring-based and case-insensitive.
     * @return the category or null.
     * @throws IllegalStateException    if the result is ambiguous
     */
    fun findCategoryByName(filter: String): Category? {
        val categoriesMatching = findCategories(filter)
        check(categoriesMatching.size <= 1) { "Category $filter is ambiguous" }
        return categoriesMatching.firstOrNull()
    }

    /**
     * Fetches the exact category whose name matches the given [filter] text.
     *
     * Behaves like [findCategoryByName], except that returns
     * a [Category] instead of an [Optional]. If the category
     * can't be identified, an exception is thrown.
     *
     * @return      the category, if found
     * @throws IllegalStateException    if not exactly one category matches the given name
     */
    fun findCategoryOrThrow(filter: String): Category = findCategoryByName(filter) ?: throw IllegalStateException("Category matching $filter does not exist")

    /**
     * Searches for the exact category with the given id.
     * @param id    the category id
     * @return      the category
     */
    fun findCategoryById(id: Long): Category? = categories[id]
    fun getById(id: Long): Category = categories[id] ?: throw IllegalStateException("Category with id $id does not exist")

    /**
     * Deletes the given category from the category store.
     * @param category  the category to delete
     * @return  true if the operation was successful, otherwise false
     */
    fun deleteCategory(category: Category): Boolean {
        checkNotNull(category.id)
        return categories.remove(category.id!!) != null
    }

    /**
     * Persists the given category into the category store.
     *
     * If the category is already persistent, the saved category will get updated
     * with the name of the given category object.
     * If the category is new (i.e. its id is null), it will get a new unique id
     * before being saved.
     *
     * @param dto   the category to save
     */
    fun saveCategory(dto: Category) {
        if (dto.id == null) {
            // create
            // Make a copy to keep entities and DTOs separated
            val entity = dto.copy()
            entity.id = nextId.incrementAndGet()
            dto.id = entity.id
            categories[entity.id!!] = entity
            return
        }
        var entity: Category? = categories[dto.id!!]

        if (entity == null) {
            // Make a copy to keep entities and DTOs separated
            entity = dto.copy()
            categories[entity.id!!] = entity
        } else {
            entity.name = dto.name
        }
    }

    fun findAll() = findCategories("")

    fun deleteAll() {
        categories.clear()
    }
}
