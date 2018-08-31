let accounts = [];
let categories = [];
let currencies = [];

$(() => {
    $.when(
        refreshCategories(),
        refreshCurrencies()
    ).then(() => {
        refreshAccounts();
    });
});

function refreshAccounts() {
    let deferred = new $.Deferred();

    $.when(
        $.ajax('account')
    ).then(accountsResponse => {
        accounts = accountsResponse;
        populateAccounts();
        deferred.resolve();
    });

    return deferred.promise();
}

function refreshCategories() {
    let deferred = new $.Deferred();

    $.when(
        $.ajax('category')
    ).then(categoriesResponse => {
        categories = categoriesResponse;
        populateCategories();
        deferred.resolve();
    });

    return deferred.promise();
}

function refreshCurrencies() {
    let deferred = new $.Deferred();

    $.when(
        $.ajax('currency')
    ).then(currenciesResponse => {
        currencies = currenciesResponse;
        populateCurrencies();
        deferred.resolve();
    });

    return deferred.promise();
}
