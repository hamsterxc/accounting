function populateMainHeader() {
    $('#main-header').html(buildMainHeader());
}

function buildMainHeader() {
    return [
            { text: '', id: 'main-header-actions' },
            { text: 'Date', id: 'main-header-date' }
        ]
        .concat(accounts.map(account => { return {
            text: account.name + ', ' + find(currencies, account.currencyId).symbol,
            id: 'main-header-account' + account.id
        } }))
        .concat([
            { text: 'Total', id: 'main-header-total' },
            { text: 'Category', id: 'main-header-category' },
            { text: 'Comment', id: 'main-header-comment' }
        ])
        .map(item => buildTh(item))
        .join('');
}

function populateMainFilter() {
    $('#main-filter').html(buildMainFilter());

    setupFilteringInput('#main-filter-show-hidden');
    $('#main-filter-cell-show-hidden').addClass('oneline');

    accounts.forEach(account => {
        setupFilteringInput('#main-filter-account' + account.id);
        $('#main-filter-cell-account' + account.id).addClass('oneline');
    });

    setupFilteringInput('#main-filter-category');
    setupFilteringInput('#main-filter-comment');
}

function setupFilteringInput(selector) {
    const element = $(selector);
    element.off('change keyup select');
    element.on('change keyup select', filter);
}

function buildMainFilter() {
    return [
            {
                text: '<div><input id="main-filter-show-hidden" type="checkbox"/></div>'
                    + '<label for="main-filter-show-hidden"><div>Show<br/>hidden</div></label>',
                id: 'main-filter-cell-show-hidden'
            },
            { text: '' }
        ]
        .concat(accounts.map(account => {
            const id = 'main-filter-account' + account.id;
            return {
                text: '<div><input id="' + id + '" type="checkbox"/></div>'
                    + '<label for="' + id + '"><div>Operation(s)<br/>present</div></label>',
                id: 'main-filter-cell-account' + account.id
            };
        }))
        .concat([
            { text: '' },
            { text: '<select id="main-filter-category">'
                + '<option id="main-filter-category-option-1" value="-1" selected/>'
                + categories.map(category => '<option id="main-filter-category-option' + category.id
                    + '" value="' + category.id + '">' + category.name + '</option>').join('')
                + '</select>' },
            { text: '<input id="main-filter-comment" type="text" placeholder="Comment substring" size="30"/>' }
        ])
        .map(item => buildTd(item))
        .join('');
}

function filter() {
    const showHidden = isChecked('#main-filter-show-hidden');
    const accountVisibility = accounts.reduce((value, account) => {
        value[account.id] = isChecked('#main-filter-account' + account.id);
        return value;
    }, {});
    const category = Number($('#main-filter-category').val() || '-1');
    const commentSubstring = ($('#main-filter-comment').val() || '').toLowerCase();

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

        toggleVisibility('#transaction' + transaction.id, transaction.visible, isFiltered);

        accounts.forEach(account => {
            toggleVisibilityItem('#transaction' + transaction.id + '-account' + account.id, account, showHidden);
        });
    });

    accounts.forEach(account => {
        toggleVisibilityItem('#main-header-account' + account.id, account, showHidden);
        toggleVisibilityItem('#main-filter-cell-account' + account.id, account, showHidden);
        toggleVisibilityItem('#main-total-before-account' + account.id, account, showHidden);
        toggleVisibilityItem('#main-total-after-account' + account.id, account, showHidden);
        toggleVisibilityItem('#transaction-edit-account' + account.id, account, showHidden);
    });

    categories.forEach(category => {
        toggleVisibility('#main-filter-category-option' + category.id, category.visible, false);
        toggleVisibilityItem('#transaction-edit-category-option' + category.id, category, showHidden);
        summaries.forEach(summary => {
            toggleVisibilityItem('#summary' + summary.from + '-category' + category.id, category, showHidden);
        });
    });
}

function toggleVisibility(selector, isVisible, isFiltered) {
    const element = $(selector);
    element.toggleClass('hidden', !isVisible);
    element.toggleClass('filtered', isFiltered);
}

function toggleVisibilityItem(selector, item, showHidden) {
    toggleVisibility(selector, item.visible, !item.visible && !showHidden);
}

function populateMainAdd() {
    $('#main-add').html('<tr>' + buildMainTransactionEdit() + '</tr>');
    $('#transaction-edit-actions').addClass('controls');
    setupMainSubmit(0);
    $(buildInputSelector('date', 0)).focus();
}

function buildMainTransactionEdit(transactionId) {
    const transaction = find(transactions, transactionId);
    const isPreset = transaction !== undefined;
    transactionId = isPreset ? transaction.id : 0;

    return [
            {
                text: (isPreset
                    ? '<input type="button" name="submit" value="Save" onClick="performTransactionSave(' + transactionId + ')"'
                        + buildOperationDataAttributes(transactionId) + '/><br/>'
                        + '<input type="button" name="cancel" value="Cancel" onClick="populateMainEditCancel(' + transactionId + ')"'
                        + buildOperationDataAttributes(transactionId) + '/>'
                    : '<input type="button" name="submit" value="Add" onClick="performTransactionAdd()"'
                        + buildOperationDataAttributes(transactionId) + '/>'),
                id: 'transaction-edit-actions'
            },
            {
                text: '<input type="text" name="date" size="10" placeholder="31.12[.[20]12]"'
                + (isPreset ? ' value="' + formatDateTransaction(transaction.time) + '"' : '')
                + buildOperationDataAttributes(transactionId)
                + '/>',
                id: 'transaction-edit-date'
            }
        ]
        .concat(accounts.map(account => {
            const operations = isPreset ? findAll(transaction.operations, account.id, 'accountId') : [];
            if(operations.length === 0) {
                operations.push(buildGenericOperation(account.id));
            }

            return {
                text: '<input type="hidden" name="data-holder" '
                    + buildOperationDataAttributes(transactionId, account.id, operations.length) + '/>' +
                    operations
                        .map((operation, index) => buildOperationEditLine(transactionId, account.id, operation, operations.length <= 1, index))
                        .join('')
                        .slice(5),
                id: 'transaction-edit-account' + account.id
            };
        }))
        .concat(
            { text: '', id: 'transaction-edit-total' },
            {
                text: '<select name="categoryId"' + buildOperationDataAttributes(transactionId) + '>'
                    + categories.map(
                        category => '<option id="transaction-edit-category-option' + category.id
                            + '" value="' + category.id + '"'
                            + (isPreset && (category.id === transaction.categoryId) ? ' selected' : '')
                            + '>' + category.name + '</option>'
                    ).join('')
                    + '</select>',
                id: 'transaction-edit-category'
            },
            {
                text: '<input type="text" name="comment" placeholder="Comment" size="30"'
                    + (isPreset ? ' value="' + transaction.comment + '"' : '')
                    + buildOperationDataAttributes(transactionId)
                    + '/>',
                id: 'transaction-edit-comment'
            }
        )
        .map(item => buildTd(item))
        .join('');
}

function buildGenericOperation(accountId) {
    const account = find(accounts, accountId);
    return {accountId: account.id, currencyId: account.currencyId, amount: '', active: true};
}

function buildOperationEditLine(transactionId, accountId, operation, isAlone, index) {
    return '<br/><span class="oneline"' + buildOperationDataAttributes(transactionId, accountId, index) + '>'
        + [
            '<a class="action" onClick="addOperationEditLine(' + transactionId + ',' + accountId + ',' + index + ')"><small>+</small></a>',
            '<a class="action" onClick="removeOperationEditLine(' + transactionId + ',' + accountId + ',' + index + ')"><span class="warn"><small>X</small></span></a>'
        ]
            .concat(
                '<input name="amount" type="text" size="10" placeholder="999.99 [EUR]" value="' + operation.amount
                + (operation.amount === '' ? '' : buildOperationCurrencyString(operation, isAlone)) + '"'
                + buildOperationDataAttributes(transactionId, accountId, index) + '/>',
                '<input name="active" type="checkbox"' + (operation.active ? ' checked' : '')
                + buildOperationDataAttributes(transactionId, accountId, index) + '/>')
            .join('&#160;')
        + '</span>';
}

function buildOperationDataAttributes(transactionId, accountId, index) {
    return ' data-transaction="' + transactionId + '"'
        + (accountId === undefined ? '' : ' data-account="' + accountId + '"')
        + (index === undefined ? '' : ' data-index="' + index + '"');
}

function addOperationEditLine(transactionId, accountId, index) {
    const xpath = '[data-transaction="' + transactionId + '"][data-account="' + accountId + '"]';

    const dataHolder = $('input' + xpath + '[name="data-holder"]');
    const indexNew = dataHolder.attr('data-index');
    dataHolder.attr('data-index', Number(indexNew) + 1);

    let line = buildOperationEditLine(transactionId, accountId, buildGenericOperation(accountId), false, indexNew);
    if(index === undefined) {
        line = line.slice(5);
    }

    const lastLine = index === undefined ? dataHolder : $('span' + xpath + '[data-index="' + index + '"]');
    lastLine.after(line);
}

function removeOperationEditLine(transactionId, accountId, index) {
    const line = $('span[data-transaction="' + transactionId + '"][data-account="' + accountId + '"][data-index="' + index + '"]');

    const prev = line.prev();
    const next = line.next();
    line.remove();
    if(prev.prop("tagName").toLowerCase() === 'br') {
        prev.remove();
    } else if(next.length === 0) {
        addOperationEditLine(transactionId, accountId);
    } else {
        next.remove();
    }
}

function performTransactionAdd() {
    const request = buildRequest(0);
    performAdd(request);
}

function performTransactionSave(transactionId) {
    const request = buildRequest(transactionId);
    performEdit(request, transactionId);
}

function buildRequest(transactionId) {
    const request = {
        date: $(buildInputSelector('date', transactionId)).val(),
        categoryId: $(buildInputSelector('categoryId', transactionId)).val(),
        comment: $(buildInputSelector('comment', transactionId)).val(),
        operations: []
    };

    $(buildInputSelector('amount', transactionId)).each(function() {
        const element = $(this);
        request.operations.push({
            accountId: element.attr('data-account'),
            amount: element.val(),
            active: $(buildInputSelector('active', transactionId) + '[data-account="' + element.attr('data-account')
                + '"][data-index="' + element.attr('data-index') + '"]').is(':checked')
        });
    });

    return request;
}

function buildInputSelector(name, transactionId) {
    return '[name="' + name + '"][data-transaction="' + transactionId + '"]';
}

function populateMainEdit(transactionId) {
    $('#main-add').html('');
    $('#transaction' + transactionId).html(buildMainTransactionEdit(transactionId));
    filter();
    $('#transaction-edit-actions').addClass('controls');
    setupMainSubmit(transactionId);
}

function setupMainSubmit(transactionId) {
    $('input[data-transaction="' + transactionId + '"]').each(function() {
        $(this).keyup(function(event) {
            if(event.keyCode === 13) {
                $(buildInputSelector('submit', transactionId)).click();
            }
        });
    });
}

function populateMainEditCancel(transactionId) {
    $('#transaction' + transactionId).html(buildMainTransaction(transactionId));
    $('#transaction' + transactionId + '-actions').addClass('controls');
    populateMainAdd();
    filter();
}

function populateMainTotals(totalBefore, totalAfter) {
    $('#main-total-before').html(buildMainTotal(totalBefore, 'before'));
    $('#main-total-after').html(buildMainTotal(totalAfter, 'after'));
}

function buildMainTotal(total, id) {
    const idPrefix = 'main-total-' + id + '-';
    return [
            { text: '', id: idPrefix + 'actions' },
            { text: '', id: idPrefix + 'date' }
        ]
        .concat(accounts.map(account => {
            const value = find(total.items, account.id);
            return {
                text: formatNumber(value === undefined ? 0 : value.amount),
                id: idPrefix + 'account' + account.id
            };
        }))
        .concat([
            { text: formatNumber(total.total), id: idPrefix + 'total' },
            { text: '', id: idPrefix + 'category' },
            { text: '', id: idPrefix + 'comment' }
        ])
        .map(item => buildTh(item))
        .join('');
}

function populateMainTransactions() {
    $('#main-body').html(buildMainTransactions());
    transactions.forEach(transaction => {
        $('#transaction' + transaction.id + '-actions').addClass('controls');
    });
}

function buildMainTransactions() {
    return transactions
        .map(transaction => '<tr id="transaction' + transaction.id + '">' + buildMainTransaction(transaction.id) + '</tr>')
        .join('');
}

function buildMainTransaction(transactionId) {
    const transaction = find(transactions, transactionId);
    const category = find(categories, transaction.categoryId);
    const categoryName = category === undefined ? '' : category.name;
    const idPrefix = 'transaction' + transactionId + '-';

    return [
            {
                text: ['<a class="action" onClick="populateMainEdit(' + transactionId + ')">E</a>',
                    '<a class="action" onClick="performAction(' + transactionId + ',\'moveup\');">&#x2191;</a>',
                    '<a class="action" onClick="performAction(' + transactionId + ',\'movedown\');">&#x2193;</a>',
                    '<a class="action" onClick="performAction(' + transactionId + ',\'delete\');"><span class="warn">X</span></a>']
                    .join('&#160;'),
                id: idPrefix + 'actions'
            },
            {
                text: formatDateTransaction(transaction.time),
                id: idPrefix + 'date'
            }
        ]
        .concat(accounts.map(account => {
            const operations = findAll(transaction.operations, account.id, 'accountId');
            return {
                text: operations.length === 0 ? '' : operations
                        .map(operation => {
                            const operationCurrencyString = buildOperationCurrencyString(operation, operations.length <= 1);
                            const operationString = formatNumber(operation.amount, operationCurrencyString);
                            return operation.active ? operationString : '<s>' + operationString + '</s>';
                        })
                        .map(operationString => '<span class="oneline">' + operationString + '</span>')
                        .join('<br/>'),
                id: idPrefix + 'account' + account.id
            };
        }))
        .concat([
            { text: formatNumber(transaction.total), id: idPrefix + 'total' },
            { text: categoryName, id: idPrefix + 'category' },
            { text: transaction.comment, id: idPrefix + 'comment' }
        ])
        .map(item => buildTd(item))
        .join('');
}

function buildOperationCurrencyString(operation, isAlone) {
    const account = find(accounts, operation.accountId);
    const accountCurrency = find(currencies, account.currencyId);
    const operationCurrency = operation.currencyId == null ? accountCurrency : find(currencies, operation.currencyId);
    return isAlone && (operationCurrency.id === accountCurrency.id) ? '' : ' ' + operationCurrency.code;
}

function populateCurrency() {
    $('#currency').html(buildCurrency());
}

function buildCurrency() {
    return '<table>'
        + '<tr><th colspan="2">Currency rates</th></tr>'
        + currencies
            .map(currency => '<tr><td>' + currency.name + '</td><td>' + formatNumber(currency.value) + '</td></tr>')
            .join('')
        + '</table>';
}

function populateSummary() {
    $('#summary').html(summaries
        .map(summary => buildSummary(summary))
        .join(''));
}

function buildSummary(summary) {
    return '<table class="summary">'
        + [{ text: '<th colspan="2">' + formatDateSummary(summary.from) + '</th>' }]
            .concat(
                categories.map(category => {
                    const item = find(summary.items, category.id);
                    return {
                        text: [
                                { text: category.name },
                                { text: formatNumber(item === undefined ? 0 : item.amount) }
                            ]
                            .map(item => buildTd(item))
                            .join(''),
                        id: 'summary' + summary.from + '-category' + category.id
                    };
                })
            )
            .concat([{ text: '<th colspan="2">' + formatNumber(summary.total) + '</th>' }])
            .map(item => buildTr(item))
            .join('')
        + '</table>';
}

function isChecked(selector) {
    return $(selector).is(':checked');
}

function buildTag(tag, text, id) {
    const idString = ((id || '') === '') ? '' : (' id="' + id + '"');
    return '<' + tag + idString + '>' + text + '</' + tag + '>';
}

function buildTr(data) {
    return buildTag('tr', data.text, data.id);
}

function buildTh(data) {
    return buildTag('th', data.text, data.id);
}

function buildTd(data) {
    return buildTag('td', data.text, data.id);
}
