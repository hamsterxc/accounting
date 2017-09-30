var accounts;
var categories;
var currencies;

$(function() {
    refreshStatic();
    refreshDynamic();
});

function refreshStatic() {
    $.when(
        $.ajax('account'),
        $.ajax('category'),
        $.ajax('currency')
    ).then(function(accountsResponse, categoriesResponse, currenciesResponse) {
        accounts = accountsResponse[0].filter(function(account) {
            return account.visible;
        });
        categories = categoriesResponse[0].filter(function(category) {
            return category.visible;
        });
        currencies = currenciesResponse[0];

        populateMainHeader(accounts, currencies);
        populateMainAdd(accounts, categories);
    });
}

function refreshDynamic() {
    var today = new Date();
    var from = new Date(today.getUTCFullYear(), today.getUTCMonth(), 1).getTime();
    var to = new Date(today.getUTCFullYear(), today.getUTCMonth() + 1, 1).getTime();
    var summaries = [];
    $.when(
        $.ajax('transaction/runningtotal?to=' + from),
        $.ajax('transaction/runningtotal?to=' + to),
        $.ajax('transaction?from=' + from + '&to=' + to),
        $.ajax('transaction/boundary')
    ).then(function(totalBeforeResponse, totalAfterResponse, transactionsResponse, boundaryResponse) {
        var totalBefore = totalBeforeResponse[0];
        var totalAfter = totalAfterResponse[0];
        var transactions = transactionsResponse[0].transactions.filter(function(transaction) {
            return transaction.visible && (findByField(categories, 'id', transaction.categoryId) !== undefined);
        });
        var boundary = boundaryResponse[0];

        populateMainTotals(totalBefore, totalAfter, accounts);
        populateMainTransactions(transactions, accounts, categories);

        var dateLower = new Date(boundary.lower);
        var date = new Date(dateLower.getUTCFullYear(), dateLower.getUTCMonth(), 1);
        cutDateToMonth(date);
        var promises = [];
        while(date.getTime() <= boundary.upper) {
            var from = date.getTime();
            date.setUTCMonth(date.getUTCMonth() + 1);
            cutDateToMonth(date);
            promises.push($.ajax('transaction/summary?from=' + from + '&to=' + date.getTime())
                .then(function(summaryResponse) {
                    summaries.push(summaryResponse);
                }));
        }
        return $.when.apply($, promises);
    }).then(function() {
        summaries = summaries.filter(function(item) {
            return item.items.length > 0;
        });
        summaries.sort(function(a, b) {
            return a.from - b.from;
        });
        populateSummary(summaries, categories);
    });
}

function performAdd(data) {
    $.ajax({
        type: 'POST',
        url: 'transaction',
        contentType: 'application/json',
        data: JSON.stringify(data)
    }).then(function() {
        populateMainAdd(accounts, categories);
        refreshDynamic();
    });
}

function performAction(id, action) {
    $.ajax({
        type: 'POST',
        url: 'transaction/' + id + '/' + action
    }).then(function() {
        refreshDynamic();
    });
}
