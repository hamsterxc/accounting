let accounts = [];
let categories = [];
let currencies = [];
let transactions = [];

$(() => {
    refreshStatic();
    refreshDynamic();
});

function refreshStatic() {
    $.when(
        $.ajax('account'),
        $.ajax('category'),
        $.ajax('currency')
    ).then((accountsResponse, categoriesResponse, currenciesResponse) => {
        transactions = [];
        accounts = accountsResponse[0].filter(account => account.visible);
        categories = categoriesResponse[0].filter(category => category.visible);
        currencies = currenciesResponse[0];

        populateMainHeader();
        populateMainAdd();
        populateCurrency();
    });
}

function refreshDynamic() {
    const today = new Date();
    const from = new Date(today.getUTCFullYear(), today.getUTCMonth() - 1, 1).getTime();
    const to = new Date(today.getUTCFullYear(), today.getUTCMonth() + 1, 1).getTime();
    let summaries = [];
    $.when(
        $.ajax('transaction/runningtotal?to=' + from),
        $.ajax('transaction/runningtotal?to=' + to),
        $.ajax('transaction/boundary'),
        $.ajax('transaction?from=' + from + '&to=' + to)
    ).then((totalBeforeResponse, totalAfterResponse, boundaryResponse, transactionsResponse) => {
        const totalBefore = totalBeforeResponse[0];
        const totalAfter = totalAfterResponse[0];
        const boundary = boundaryResponse[0];
        transactions = transactionsResponse[0].transactions
            .filter(transaction => transaction.visible && (find(categories, transaction.categoryId) !== undefined));

        populateMainTotals(totalBefore, totalAfter);
        populateMainTransactions();

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
        populateSummary(summaries);
    });
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
