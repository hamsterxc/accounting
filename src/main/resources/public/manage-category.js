function populateCategories() {
    populateDataTable({
        name: 'category',
        selectorHeader: '#manage-categories-header',
        selectorBody: '#manage-categories-body',
        selectorFooter: '#manage-categories-add',
        headerActions: '',
        rows: categories.map(category => category.id),
        columns: [
            {
                name: 'visible',
                caption: '&#x1f441;',
                supplierView: id => '<input type="checkbox" disabled' + (find(categories, id).visible ? ' checked' : '') + '/>',
                supplierEdit: id => '<input type="checkbox" id="' + buildInputId('category', 'visible', id) + '"' + (find(categories, id).visible ? ' checked' : '') + '/>',
                supplierAdd: () => '<input type="checkbox" id="' + buildInputId('category', 'visible') + '"/>',
            },
            {
                name: 'name',
                caption: 'Name',
                supplierView: id => find(categories, id).name,
                supplierEdit: id => '<input type="text" id="' + buildInputId('category', 'name', id) + '" size="10" value="' + find(categories, id).name + '"/>',
                supplierAdd: () => '<input type="text" id="' + buildInputId('category', 'name') + '" size="10"/>',
            },
        ],
        actionUp: id => 'performCategoryAction(' + id + ',' + '\'moveup\');',
        actionDown: id => 'performCategoryAction(' + id + ',' + '\'movedown\');',
        actionDelete: id => 'deleteCategory(' + id + ');',
        actionSave: id => 'updateCategory(' + id + ');',
        actionAdd: () => 'addCategory();',
        actionAfterEdit: id => 'setupCategorySubmit(' + id + ');',
    });

    setupCategorySubmit();
}

function setupCategorySubmit(categoryId) {
    const submit = _obtainCategorySubmit(categoryId);
    setupSubmit($('#' + buildInputId('category', 'name', categoryId)), submit);
}

function _obtainCategorySubmit(categoryId) {
    return () => {
        if(isNaN(categoryId)) {
            addCategory();
        } else {
            updateCategory(categoryId);
        }
    };
}

function _collectCategoryData(id) {
    return {
        visible: $('#' + buildInputId('category', 'visible', id)).prop('checked'),
        name: $('#' + buildInputId('category', 'name', id)).val(),
    };
}

function addCategory() {
    performRequest('POST', 'category', _collectCategoryData())
        .then(() => refreshCategories());
}

function updateCategory(id) {
    performRequest('PUT', 'category/' + id, _collectCategoryData(id))
        .then(() => refreshCategories());
}

function performCategoryAction(id, action) {
    performRequest('POST', 'category/' + id + '/' + action)
        .then(() => refreshCategories());
}

function deleteCategory(id) {
    performRequest('DELETE', 'category/' + id)
        .then(() => refreshCategories());
}
