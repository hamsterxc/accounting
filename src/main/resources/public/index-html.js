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

    const form = $('#main-form');
    form.off('submit');
    form.submit(function(event) {
        event.preventDefault();

        const request = buildRequest(this);
        performAdd(request);
    });
}

function buildMainTransactionEdit(transactionId) {
    const transaction = find(transactions, transactionId);
    const isPreset = transaction !== undefined;
    return '<td class="controls">'
        + (isPreset
            ? '<input type="submit" value="Save"/><br/>'
            + '<input type="button" value="Cancel" onClick="populateMainEditCancel(' + transactionId + ')"/>'
            : '<input type="submit" value="Add"/>')
        + '</td>'
        + [
            '<input type="text" name="date" size="10"'
            + (isPreset
                ? ' value="' + formatDateTransaction(transaction.time) + '"'
                : ' placeholder="31.12[.[20]12]"')
            + '/>'
        ]
        .concat(accounts.map(account => {
            const value = isPreset ? find(transaction.operations, account.id) : undefined;
            return '<input type="text" size="10" name="account' + account.id + '"'
                + (isPreset
                    ? ' value="' + (value === undefined ? '' : value.amount) + '"'
                    : ' placeholder="999.99"')
                + '/>';
        }))
        .concat(
            '',
            '<select name="categoryId">' +
            categories.map(
                category => '<option value="' + category.id + '"'
                    + (isPreset && (category.id === transaction.categoryId) ? ' selected' : '')
                    + '>' + category.name + '</option>'
            ).join('')
            + '</select>',
            '<input type="text" name="comment" placeholder="Comment" size="30"'
                + (isPreset ? ' value="' + transaction.comment + '"' : '')
                + '/>'
        )
        .map(item => '<td>' + item + '</td>')
        .join('');
}

function buildRequest(form) {
    const accountPrefix = 'account';
    return $(form).serializeArray().reduce((request, item) => {
        if(item.name.startsWith(accountPrefix)) {
            if(item.value.length > 0) {
                request.operations.push({
                    id: item.name.substring(accountPrefix.length),
                    amount: item.value
                });
            }
        } else {
            request[item.name] = item.value;
        }
        return request;
    }, {
        operations: []
    });
}

function populateMainEdit(transactionId) {
    $('#main-add').html('');
    $('#transaction' + transactionId).html(buildMainTransactionEdit(transactionId));

    const form = $('#main-form');
    form.off('submit');
    form.submit(function(event) {
        event.preventDefault();

        const request = buildRequest(this);
        performEdit(request, transactionId);
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
                const value = find(transaction.operations, account.id);
                return value === undefined ? '' : formatNumber(value.amount);
            }))
            .concat(formatNumber(transaction.total), categoryName, transaction.comment)
            .map(item => '<td>' + item + '</td>')
            .join('');
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
            .join('')
        + '</table>';
}
