let accounts = [];
let categories = [];
let currencies = [];
let transactions = [];

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
        _buildRunningTotalsRequest(transactionsDateFrom, transactionsDateTo),
        $.ajax('transaction/boundary'),
        $.ajax('transaction?fromDate=' + transactionsDateFrom + '&toDate=' + transactionsDateTo)
    ).then((runningTotalsResponse, boundaryResponse, transactionsResponse) => {
        const runningTotals = runningTotalsResponse[0].items;
        const boundary = boundaryResponse[0];
        transactions = transactionsResponse[0].transactions;

        transactionsDateFrom = formatDateTransaction(transactionsResponse[0].from);
        transactionsDateTo = formatDateTransaction(transactionsResponse[0].to);

        populateTransactions();
        populateTransactionFilterDates();

        const isRunningTotalsSwapped = runningTotals[0].to > runningTotals[1].to;
        populateTransactionTotals(
            runningTotals[isRunningTotalsSwapped ? 1 : 0],
            runningTotals[isRunningTotalsSwapped ? 0 : 1]
        );

        filter();

        const dateLower = new Date(boundary.lower);
        const date = new Date(dateLower.getUTCFullYear(), dateLower.getUTCMonth(), 1);
        _cutDateToMonth(date);
        const boundaries = [];
        while(date.getTime() <= boundary.upper) {
            const from = date.getTime();
            date.setUTCMonth(date.getUTCMonth() + 1);
            _cutDateToMonth(date);
            boundaries.push({from: from, to: date.getTime()});
        }
        return _buildSummaryRequest(boundaries);
    }).then((summariesResponse) => {
        const summaries = summariesResponse.items.filter(item => Object.keys(item.aggregation).length > 0);
        summaries.sort((a, b) => a.from - b.from);

        populateSummary(summaries);
        filter();

        deferred.resolve();
    });

    return deferred.promise();
}

function _cutDateToMonth(date) {
    date.setUTCDate(1);
    date.setUTCHours(0, 0, 0, 0);
}

function _buildRunningTotalsRequest(lower, upper) {
    const filterBefore = {from: 0};
    if(lower === '') {
        filterBefore['to'] = 0;
    } else {
        filterBefore['toDate'] = lower;
    }

    const filterAfter = {from: 0};
    if(upper === '') {
        filterAfter['to'] = Date.now();
    } else {
        filterAfter['toDate'] = upper;
    }

    return performRequest('POST', '/transaction/aggregate', {
        filters: [filterBefore, filterAfter],
        field: 'account',
        includeHidden: true,
    });
}

function _buildSummaryRequest(boundaries) {
    return performRequest('POST', '/transaction/aggregate', {
        filters: boundaries,
        field: 'category',
        includeHidden: false,
    });
}
