package com.lonebytesoft.hamster.accounting.controller.view.output

data class CategoriesView(
        var categories: Collection<CategoryView> = emptyList()
)

data class CategoryView(
        var id: Long = 0,
        var name: String = "",
        var ordering: Long = 0,
        var visible: Boolean = true
)
