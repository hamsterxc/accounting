function populateTransactionFilter() {
    $('#main-filter').html(
        '<tr>'
        + '<td class="oneline"><div><input id="filter-show-hidden" type="checkbox"/></div>'
            + '<label for="filter-show-hidden"><div>Show<br/>hidden</div></label></td>'
        + '<td><input id="filter-date-from" type="text" size="10" placeholder="Date from"/>'
            + '<br/><input id="filter-date-to" type="text" size="10" placeholder="Date to" style="margin-top:2px;"/></td>'
        + accounts.map(account =>
            '<td class="oneline" id="filter-cell-account' + account.id +
                '"><div><input id="filter-account' + account.id + '" type="checkbox"/></div>'
                + '<label for="filter-account' + account.id + '"><div>Operation(s)<br/>present</div></label></td>'
            ).join('')
        + '<td></td>'
        + '<td class="oneline"><select id="filter-category">'
            + '<option data-name="category-option" data-category-id="-1" value="-1" selected/>'
            + categories.map(category =>
                    '<option data-name="category-option" data-category-id="' + category.id + '" value="' + category.id + '">'
                    + category.name + '</option>'
                ).join('')
            + '</select></td>'
        + '<td><input id="filter-comment" type="text" placeholder="Comment substring" size="30"/></td>'
        + '</tr>'
    );

    _setupFilteringElement('#filter-show-hidden', 'change', filter);
    _setupFilteringElement('#filter-date-from', 'blur', filterDates);
    _setupFilteringElement('#filter-date-to', 'blur', filterDates);
    _setupFilteringElement('#filter-category', 'change', filter);
    _setupFilteringElement('#filter-comment', 'keyup', filter);
    accounts.forEach(account => _setupFilteringElement('#filter-account' + account.id, 'change', filter));

    setupSubmit($('#filter-date-from'), filterDates);
    setupSubmit($('#filter-date-to'), filterDates);
}

function _setupFilteringElement(selector, event, callback) {
    const element = $(selector);
    element.off(event);
    element.on(event, callback);
}

function filter() {
    const showHidden = $('#filter-show-hidden').prop('checked');
    const accountVisibility = accounts.reduce((value, account) => {
        value[account.id] = $('#filter-account' + account.id).prop('checked');
        return value;
    }, {});
    const category = Number($('#filter-category').val() || '-1');
    const commentSubstring = ($('#filter-comment').val() || '').toLowerCase();

    transactions.forEach(transaction => {
        const isFiltered = !(
            (transaction.visible || showHidden)
            && ((category === -1) || (category === transaction.categoryId))
            && (transaction.comment.toLowerCase().indexOf(commentSubstring) !== -1)
            && accounts.reduce((value, account) => {
                return value && (!accountVisibility[account.id]
                    || (findAll(transaction.operations, account.id, 'accountId').length > 0));
            }, true)
        );
        _setupVisibility('#' + transactionsTable.body[transaction.id].id, !transaction.visible, isFiltered);
        _setupVisibility('#' + transactionsTable.bodyEdit[transaction.id].id, !transaction.visible, isFiltered);

        accounts.forEach(account => {
            const cellId = 'account' + account.id;
            const isFiltered = !account.visible && !showHidden;
            _setupVisibility('#' + transactionsTable.body[transaction.id].cells[cellId], !account.visible, isFiltered);
            _setupVisibility('#' + transactionsTable.bodyEdit[transaction.id].cells[cellId], !account.visible, isFiltered);
        });
    });

    accounts.forEach(account => {
        const cellId = 'account' + account.id;
        const isFiltered = !account.visible && !showHidden;
        _setupVisibility('#' + transactionsTable.header.cells[cellId], !account.visible, isFiltered);
        _setupVisibility('#' + transactionsTable.footer.cells[cellId], !account.visible, isFiltered);
        _setupVisibility('#' + buildTotalCellId('before', account.id), !account.visible, isFiltered);
        _setupVisibility('#' + buildTotalCellId('after', account.id), !account.visible, isFiltered);
        _setupVisibility('#filter-cell-account' + account.id, !account.visible, isFiltered);
    });

    categories.forEach(category => {
        _setupVisibility('[data-name="category-option"][data-category-id="' + category.id + '"]',
            !category.visible, !category.visible && !showHidden);
        _setupVisibility('[data-name="summary-category"][data-category-id="' + category.id + '"]',
            !category.visible, !category.visible && !showHidden);
    });
}

function _setupVisibility(selector, isHidden, isFiltered) {
    const element = $(selector);
    element.toggleClass('hidden', isHidden);
    element.toggleClass('filtered', isFiltered);
}

function populateTransactionFilterDates() {
    $('#filter-date-from').val(transactionsDateFrom);
    $('#filter-date-to').val(transactionsDateTo);
}

function filterDates() {
    const from = $('#filter-date-from').val();
    const to = $('#filter-date-to').val();
    if ((transactionsDateFrom !== from) || (transactionsDateTo !== to)) {
        transactionsDateFrom = from;
        transactionsDateTo = to;
        $
            .when(refreshDynamic(false))
            .then(() => scrollToTop());
    }
}

function filterByDates(from, to) {
    $('#filter-date-from').val(formatDateTransaction(from));
    $('#filter-date-to').val(formatDateTransaction(to));
    filterDates();
}
