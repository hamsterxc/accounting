function populateMainHeader() {
    $('#main-header').html(buildMainHeader());
}

function buildMainHeader() {
    return ['', 'Date']
        .concat(accounts.map(account => {
            return account.name + ', ' + find(currencies, account.currencyId).symbol;
        }))
        .concat(['Total', 'Category', 'Comment'])
        .map(item => '<th>' + item + '</th>')
        .join('');
}

function populateMainAdd() {
    $('#main-add').html('<tr>' + buildMainTransactionEdit() + '</tr>');
    setupMainSubmit(0);
    $(buildInputSelector('date', 0)).focus();
}

function buildMainTransactionEdit(transactionId) {
    const transaction = find(transactions, transactionId);
    const isPreset = transaction !== undefined;
    transactionId = isPreset ? transaction.id : 0;
    return '<td class="controls">'
        + (isPreset
            ? '<input type="button" name="submit" value="Save" onClick="performTransactionSave(' + transactionId + ')"'
                + buildOperationDataAttributes(transactionId) + '/><br/>'
                + '<input type="button" name="cancel" value="Cancel" onClick="populateMainEditCancel(' + transactionId + ')"'
                + buildOperationDataAttributes(transactionId) + '/>'
            : '<input type="button" name="submit" value="Add" onClick="performTransactionAdd()"'
                + buildOperationDataAttributes(transactionId) + '/>')
        + '</td>'
        + [
            '<input type="text" name="date" size="10" placeholder="31.12[.[20]12]"'
            + (isPreset ? ' value="' + formatDateTransaction(transaction.time) + '"' : '')
            + buildOperationDataAttributes(transactionId)
            + '/>'
        ]
        .concat(accounts.map(account => {
            const operations = isPreset ? findAll(transaction.operations, account.id, 'accountId') : [];
            if(operations.length === 0) {
                operations.push(buildGenericOperation(account.id));
            }

            return '<input type="hidden" name="data-holder" ' + buildOperationDataAttributes(transactionId, account.id, operations.length) + '/>' +
                operations
                    .map((operation, index) => buildOperationEditLine(transactionId, account.id, operation, operations.length <= 1, index))
                    .join('')
                    .slice(5);
        }))
        .concat(
            '',
            '<select name="categoryId"' + buildOperationDataAttributes(transactionId) + '>'
            + categories.map(
                category => '<option value="' + category.id + '"'
                    + (isPreset && (category.id === transaction.categoryId) ? ' selected' : '')
                    + '>' + category.name + '</option>'
            ).join('')
            + '</select>',
            '<input type="text" name="comment" placeholder="Comment" size="30"'
                + (isPreset ? ' value="' + transaction.comment + '"' : '')
                + buildOperationDataAttributes(transactionId)
                + '/>'
        )
        .map(item => '<td>' + item + '</td>')
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
    populateMainAdd();
}

function populateMainTotals(totalBefore, totalAfter) {
    $('#main-total-before').html(buildMainTotal(totalBefore));
    $('#main-total-after').html(buildMainTotal(totalAfter));
}

function buildMainTotal(total) {
    return ['', '']
        .concat(accounts.map(account => {
            const value = find(total.items, account.id);
            return formatNumber(value === undefined ? 0 : value.amount);
        }))
        .concat(formatNumber(total.total), '', '')
        .map(item => '<th>' + item + '</th>')
        .join('');
}

function populateMainTransactions() {
    $('#main-body').html(buildMainTransactions());
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

    return [['<a class="action" onClick="populateMainEdit(' + transactionId + ')">E</a>',
            '<a class="action" onClick="performAction(' + transactionId + ',\'moveup\');">&#x2191;</a>',
            '<a class="action" onClick="performAction(' + transactionId + ',\'movedown\');">&#x2193;</a>',
            '<a class="action" onClick="performAction(' + transactionId + ',\'delete\');"><span class="warn">X</span></a>']
            .join('&#160;')]
            .concat(formatDateTransaction(transaction.time))
            .concat(accounts.map(account => {
                const operations = findAll(transaction.operations, account.id, 'accountId');
                return operations.length === 0 ? '' : operations
                    .map(operation => {
                        const operationCurrencyString = buildOperationCurrencyString(operation, operations.length <= 1);
                        const operationString = formatNumber(operation.amount, operationCurrencyString);
                        return operation.active ? operationString : '<s>' + operationString + '</s>';
                    })
                    .map(operationString => '<span class="oneline">' + operationString + '</span>')
                    .join('<br/>');
            }))
            .concat(formatNumber(transaction.total), categoryName, transaction.comment)
            .map(item => '<td>' + item + '</td>')
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

function populateSummary(summaries) {
    $('#summary').html(summaries
        .map(summary => buildSummary(summary))
        .join(''));
}

function buildSummary(summary) {
    return '<table class="summary">'
        + ['<th colspan="2">' + formatDateSummary(summary.from) + '</th>']
            .concat(
                categories.map(category => {
                    const item = find(summary.items, category.id);
                    return [category.name, formatNumber(item === undefined ? 0 : item.amount)]
                        .map(item => '<td>' + item + '</td>')
                        .join('');
                })
            )
            .map(item => '<tr>' + item + '</tr>')
            .concat(['<th colspan="2">' + formatNumber(summary.total) + '</th>'])
            .join('')
        + '</table>';
}
