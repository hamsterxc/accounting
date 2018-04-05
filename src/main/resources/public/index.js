let accounts = [];
let categories = [];
let currencies = [];
let transactions = [];
let summaries = [];

$(() => {
    $
        .when(refreshStatic())
        .then(() => refreshDynamic());
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

        populateMainHeader();
        populateMainFilter();
        populateMainAdd();
        populateCurrency();
        filter();

        deferred.resolve();
    });

    return deferred.promise();
}

function refreshDynamic() {
    let deferred = new $.Deferred();

    const today = new Date();
    const from = new Date(today.getUTCFullYear(), today.getUTCMonth() - 1, 1).getTime();
    const to = new Date(today.getUTCFullYear(), today.getUTCMonth() + 1, 1).getTime();
    $.when(
        $.ajax('transaction/runningtotal?to=' + from),
        $.ajax('transaction/runningtotal?to=' + to),
        $.ajax('transaction/boundary'),
        $.ajax('transaction?from=' + from + '&to=' + to)
    ).then((totalBeforeResponse, totalAfterResponse, boundaryResponse, transactionsResponse) => {
        const totalBefore = totalBeforeResponse[0];
        const totalAfter = totalAfterResponse[0];
        const boundary = boundaryResponse[0];
        transactions = transactionsResponse[0].transactions;
        summaries = [];

        populateMainTotals(totalBefore, totalAfter);
        populateMainTransactions();
        filter();

        const dateLower = new Date(boundary.lower);
        const date = new Date(dateLower.getUTCFullYear(), dateLower.getUTCMonth(), 1);
        cutDateToMonth(date);
        const promises = [];
        while(date.getTime() <= boundary.upper) {
            const from = date.getTime();
            date.setUTCMonth(date.getUTCMonth() + 1);
            cutDateToMonth(date);
            promises.push($.ajax('transaction/summary?from=' + from + '&to=' + date.getTime())
                .then(summaryResponse => summaries.push(summaryResponse)));
        }
        return $.when.apply($, promises);
    }).then(() => {
        summaries = summaries.filter(item => item.items.length > 0);
        summaries.sort((a, b) => a.from - b.from);
        populateSummary();

        deferred.resolve();
    });

    return deferred.promise();
}

function performAdd(data) {
    $.ajax({
        type: 'POST',
        url: 'transaction',
        contentType: 'application/json',
        data: JSON.stringify(data)
    }).then(() => {
        refreshDynamic();
        populateMainAdd();
    });
}

function performEdit(data, transactionId) {
    $.ajax({
        type: 'PUT',
        url: 'transaction/' + transactionId,
        contentType: 'application/json',
        data: JSON.stringify(data)
    }).then(() => {
        refreshDynamic();
        populateMainAdd();
    });
}

function performAction(id, action) {
    $.ajax({
        type: 'POST',
        url: 'transaction/' + id + '/' + action
    }).then(() => {
        refreshDynamic();
    });
}
