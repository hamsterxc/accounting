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
    $('#main-add').html(buildMainAdd(accounts, categories));

    var form = $('#main-add-form');
    form.off('submit');
    form.submit(function(event) {
        event.preventDefault();

        const accountPrefix = 'account';
        var request = $(this).serializeArray().reduce(function(request, item) {
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

        performAdd(request);
    });
}

function buildMainAdd(accounts, categories) {
    return [
        '<input type="submit" value="Add"/>',
        '<input type="text" name="date" placeholder="31.12[.[20]12]" size="10"/>']
        .concat(accounts.map(function(account) {
            return '<input type="text" placeholder="999.99" size="10" name="account' + account.id + '"/>';
        }))
        .concat(
            '',
            '<select name="categoryId">' +
            categories.map(function(category) {
                return '<option value="' + category.id + '">' + category.name + '</option>';
            }).join('')
            + '</select>',
            '<input type="text" name="comment" placeholder="Comment" size="30"/>')
        .map(function(item) {
            return '<td>' + item + '</td>';
        })
        .join('');
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

function populateMainTransactions(transactions, accounts, categories) {
    $('#main-body').html(buildMainTransactions(transactions, accounts, categories));
}

function buildMainTransactions(transactions, accounts, categories) {
    return transactions.transactions
        .map(function(transaction) {
            var category = findByField(categories, 'id', transaction.categoryId);
            var categoryName = category === undefined ? '' : category.name;

            return '<tr id="transaction' + transaction.id + '">'
                + [['<a class="action" onClick="performAction(' + transaction.id + ',\'moveup\');">&#x2191;</a>',
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
                    .join('')
                + '</tr>';
        })
        .join('');
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
