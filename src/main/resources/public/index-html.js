function populateMainHeader(accounts, currencies) {
    $('#main-header').html(buildMainHeader(accounts, currencies));
}

function buildMainHeader(accounts, currencies) {
    return ['', 'Date']
        .concat(accounts.map(function(account) {
            return account.name + ', ' + findByField(currencies, 'id', account.currencyId).symbol;
        }))
        .concat(['Total', 'Category', 'Comment'])
        .map(function(item) {
            return '<th>' + item + '</th>';
        })
        .join('');
}

function populateMainAdd(accounts, categories) {
    $('#main-add').html('<tr>' + buildMainTransactionEdit(accounts, categories) + '</tr>');

    var form = $('#main-form');
    form.off('submit');
    form.submit(function(event) {
        event.preventDefault();

        var request = buildRequest(this);
        performAdd(request);
    });
}

function buildMainTransactionEdit(accounts, categories, transaction) {
    var isPreset = transaction !== undefined;
    return '<td class="controls">'
        + (isPreset
            ? '<input type="submit" value="Save"/><br/>'
            + '<input type="button" value="Cancel" onClick="populateMainEditCancel(' + transaction.id + ')"/>'
            : '<input type="submit" value="Add"/>')
        + '</td>'
        + [
            '<input type="text" name="date" size="10"'
            + (isPreset
                ? ' value="' + formatDateTransaction(transaction.time) + '"'
                : ' placeholder="31.12[.[20]12]"')
            + '/>'
        ]
        .concat(accounts.map(function(account) {
            var value = isPreset ? findByField(transaction.operations, 'id', account.id) : undefined;
            return '<input type="text" size="10" name="account' + account.id + '"'
                + (isPreset
                    ? ' value="' + (value === undefined ? '' : value.amount) + '"'
                    : ' placeholder="999.99"')
                + '/>';
        }))
        .concat(
            '',
            '<select name="categoryId">' +
            categories.map(function(category) {
                return '<option value="' + category.id + '"'
                    + (isPreset && (category.id == transaction.categoryId) ? ' selected' : '')
                    + '>' + category.name + '</option>';
            }).join('')
            + '</select>',
            '<input type="text" name="comment" placeholder="Comment" size="30"'
                + (isPreset ? ' value="' + transaction.comment + '"' : '')
                + '/>'
        )
        .map(function(item) {
            return '<td>' + item + '</td>';
        })
        .join('');
}

function buildRequest(form) {
    const accountPrefix = 'account';
    return $(form).serializeArray().reduce(function(request, item) {
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
    var transaction = findByField(transactions, "id", transactionId);
    $('#transaction' + transactionId).html(buildMainTransactionEdit(accounts, categories, transaction));

    var form = $('#main-form');
    form.off('submit');
    form.submit(function(event) {
        event.preventDefault();

        var request = buildRequest(this);
        performEdit(request, transactionId);
    });
}

function populateMainEditCancel(transactionId) {
    $('#transaction' + transactionId).html(buildMainTransaction(accounts, categories, transactionId));
    populateMainAdd(accounts, categories);
}

function populateMainTotals(totalBefore, totalAfter, accounts) {
    $('#main-total-before').html(buildMainTotal(totalBefore, accounts));
    $('#main-total-after').html(buildMainTotal(totalAfter, accounts));
}

function buildMainTotal(total, accounts) {
    return ['', '']
        .concat(accounts.map(function(account) {
            var value = findByField(total.items, 'id', account.id);
            return formatNumber(value === undefined ? 0 : value.amount);
        }))
        .concat(formatNumber(total.total), '', '')
        .map(function(item) {
            return '<th>' + item + '</th>';
        })
        .join('');
}

function populateMainTransactions(accounts, categories, transactions) {
    $('#main-body').html(buildMainTransactions(accounts, categories, transactions));
}

function buildMainTransactions(accounts, categories, transactions) {
    return transactions
        .map(function(transaction) {
            return '<tr id="transaction' + transaction.id + '">'
                + buildMainTransaction(accounts, categories, transaction.id)
                + '</tr>';
        })
        .join('');
}

function buildMainTransaction(accounts, categories, transactionId) {
    var transaction = findByField(transactions, "id", transactionId);
    var category = findByField(categories, 'id', transaction.categoryId);
    var categoryName = category === undefined ? '' : category.name;

    return [['<a class="action" onClick="populateMainEdit(' + transaction.id + ')">E</a>',
            '<a class="action" onClick="performAction(' + transaction.id + ',\'moveup\');">&#x2191;</a>',
            '<a class="action" onClick="performAction(' + transaction.id + ',\'movedown\');">&#x2193;</a>',
            '<a class="action" onClick="performAction(' + transaction.id + ',\'delete\');"><span class="warn">X</span></a>']
            .join('&#160;')]
            .concat(formatDateTransaction(transaction.time))
            .concat(accounts.map(function(account) {
                var value = findByField(transaction.operations, 'id', account.id);
                return value === undefined ? '' : formatNumber(value.amount);
            }))
            .concat(formatNumber(transaction.total), categoryName, transaction.comment)
            .map(function(item) {
                return '<td>' + item + '</td>';
            })
            .join('');
}

function populateCurrency(currencies) {
    $('#currency').html(buildCurrency(currencies));
}

function buildCurrency(currencies) {
    return '<table>'
        + '<tr><th colspan="2">Currency rates</th></tr>'
        + currencies.map(function(currency) {
            return '<tr><td>' + currency.name + '</td><td>' + Number(currency.value).toFixed(2) + '</td></tr>'
        }).join('')
        + '</table>';
}

function populateSummary(summaries, categories) {
    $('#summary').html(summaries
        .map(function(summary) {
            return buildSummary(summary, categories);
        })
        .join(''));
}

function buildSummary(summary, categories) {
    return '<table class="summary">'
        + ['<th colspan="2">' + formatDateSummary(summary.from) + '</th>']
            .concat(
                categories.map(function(category) {
                    var item = findByField(summary.items, 'id', category.id);
                    return [category.name, formatNumber(item === undefined ? 0 : item.amount)]
                        .map(function(item) {
                            return '<td>' + item + '</td>';
                        })
                        .join('');
                })
            )
            .map(function(item) {
                return '<tr>' + item + '</tr>';
            })
            .join('')
        + '</table>';
}
