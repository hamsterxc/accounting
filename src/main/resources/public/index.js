let accounts = [];
let categories = [];
let currencies = [];
let transactions = [];
let summaries = [];

let transactionsDateFrom = '';
let transactionsDateTo = '';

$(() => {
    const today = new Date();
    transactionsDateFrom = formatDateTransaction(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), 1));
    transactionsDateTo = formatDateTransaction(Date.UTC(today.getUTCFullYear(), today.getUTCMonth() + 1, 1));

    $
        .when(refreshStatic())
        .then(() => refreshDynamic())
        .then(() => scrollToTop());
});

function refreshStatic() {
    let deferred = new $.Deferred();

    $.when(
        $.ajax('account'),
        $.ajax('category'),
        $.ajax('currency')
    ).then((accountsResponse, categoriesResponse, currenciesResponse) => {
        transactions = [];
        summaries = [];
        accounts = accountsResponse[0];
        categories = categoriesResponse[0];
        currencies = currenciesResponse[0];

        populateTransactions();
        populateTransactionFilter();
        populateTransactionFilterDates();
        populateCurrency();
        filter();

        deferred.resolve();
    });

    return deferred.promise();
}

function refreshDynamic() {
    let deferred = new $.Deferred();

    $.when(
        $.ajax('transaction/runningtotal?' + (transactionsDateFrom === '' ? 'to=0' : 'toDate=' + transactionsDateFrom)),
        $.ajax('transaction/runningtotal?' + (transactionsDateTo === '' ? 'to=' + Date.now() : 'toDate=' + transactionsDateTo)),
        $.ajax('transaction/boundary'),
        $.ajax('transaction?fromDate=' + transactionsDateFrom + '&toDate=' + transactionsDateTo)
    ).then((totalBeforeResponse, totalAfterResponse, boundaryResponse, transactionsResponse) => {
        const totalBefore = totalBeforeResponse[0];
        const totalAfter = totalAfterResponse[0];
        const boundary = boundaryResponse[0];
        transactions = transactionsResponse[0].transactions;
        summaries = [];

        transactionsDateFrom = formatDateTransaction(transactionsResponse[0].from);
        transactionsDateTo = formatDateTransaction(transactionsResponse[0].to);

        populateTransactions();
        populateTransactionFilterDates();
        populateTransactionTotals(totalBefore, totalAfter);
        filter();

        const dateLower = new Date(boundary.lower);
        const date = new Date(dateLower.getUTCFullYear(), dateLower.getUTCMonth(), 1);
        _cutDateToMonth(date);
        const promises = [];
        while(date.getTime() <= boundary.upper) {
            const from = date.getTime();
            date.setUTCMonth(date.getUTCMonth() + 1);
            _cutDateToMonth(date);
            promises.push($.ajax('transaction/summary?from=' + from + '&to=' + date.getTime())
                .then(summaryResponse => summaries.push(summaryResponse)));
        }
        return $.when.apply($, promises);
    }).then(() => {
        summaries = summaries.filter(item => item.items.length > 0);
        summaries.sort((a, b) => a.from - b.from);

        populateSummary();
        filter();

        deferred.resolve();
    });

    return deferred.promise();
}

function _cutDateToMonth(date) {
    date.setUTCDate(1);
    date.setUTCHours(0, 0, 0, 0);
}
