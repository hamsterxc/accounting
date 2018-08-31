function populateCurrencies() {
    populateDataTable({
        name: 'currency',
        selectorHeader: '#manage-currencies-header',
        selectorBody: '#manage-currencies-body',
        selectorFooter: '#manage-currencies-add',
        headerActions: '',
        rows: currencies.map(currency => currency.id),
        columns: [
            {
                name: 'default',
                caption: '&#x2605;',
                supplierView: id => '<input type="radio" id="' + buildInputId('currency', 'default', id, true) + '" disabled'
                    + (find(currencies, id).default ? ' checked' : '') + '/>',
                supplierEdit: id => '<input type="radio" id="' + buildInputId('currency', 'default', id)
                    + '" onclick="onCurrencyDefaultClick(' + id + ');"' + (find(currencies, id).default ? ' checked' : '') + '/>',
                supplierAdd: () => '<input type="radio" id="' + buildInputId('currency', 'default')
                    + '" data-was-checked="false" onclick="onCurrencyDefaultClick();"/>',
            },
            {
                name: 'name',
                caption: 'Name',
                supplierView: id => find(currencies, id).name,
                supplierEdit: id => '<input type="text" id="' + buildInputId('currency', 'name', id) + '" size="15" value="' + find(currencies, id).name + '"/>',
                supplierAdd: () => '<input type="text" id="' + buildInputId('currency', 'name') + '" size="15"/>',
            },
            {
                name: 'code',
                caption: 'Code',
                supplierView: id => find(currencies, id).code,
                supplierEdit: id => '<input type="text" id="' + buildInputId('currency', 'code', id) + '" size="5" value="' + find(currencies, id).code + '"/>',
                supplierAdd: () => '<input type="text" id="' + buildInputId('currency', 'code') + '" size="5"/>',
            },
            {
                name: 'symbol',
                caption: 'Symbol',
                supplierView: id => find(currencies, id).symbol,
                supplierEdit: id => '<input type="text" id="' + buildInputId('currency', 'symbol', id) + '" size="5" value="' + find(currencies, id).symbol + '"/>',
                supplierAdd: () => '<input type="text" id="' + buildInputId('currency', 'symbol') + '" size="5"/>',
            },
            {
                name: 'value',
                caption: 'Value',
                supplierView: id => find(currencies, id).value,
                supplierEdit: id => '<input type="text" id="' + buildInputId('currency', 'value', id) + '" size="15" value="' + find(currencies, id).value + '"/>',
                supplierAdd: () => '<input type="text" id="' + buildInputId('currency', 'value') + '" size="15"/>',
            },
        ],
        actionDelete: id => 'performCurrencyAction(' + id + ',' + '\'delete\');',
        actionSave: id => 'updateCurrency(' + id + ');',
        actionAdd: () => 'addCurrency();',
        actionAfterEdit: id => 'onCurrencyEditClick();',
        actionAfterCancel: id => 'onCurrencyCancelClick();',
    });
}

function onCurrencyDefaultClick(id) {
    currencies
        .forEach(currency => {
            $('#' + buildInputId('currency', 'default', currency.id)).prop('checked', id === currency.id);
            $('#' + buildInputId('currency', 'default', currency.id, true)).prop('checked', id === currency.id);
        });

    const defaultAdd = $('#' + buildInputId('currency', 'default'));
    defaultAdd.prop('checked', id === undefined);
    if(id === undefined) {
        defaultAdd.attr('data-was-checked', 'true');
    }
}

function onCurrencyEditClick() {
    const isAddChecked = $('#' + buildInputId('currency', 'default')).prop('checked');
    currencies
        .forEach(currency => {
            const isChecked = !isAddChecked && currency.default;
            $('#' + buildInputId('currency', 'default', currency.id)).prop('checked', isChecked);
            $('#' + buildInputId('currency', 'default', currency.id, true)).prop('checked', isChecked);
        });
}

function onCurrencyCancelClick() {
    const defaultAdd = $('#' + buildInputId('currency', 'default'));
    const wasDefaultAddChecked = defaultAdd.attr('data-was-checked').toLowerCase().trim() === 'true';
    currencies
        .forEach(currency => {
            const isChecked = !wasDefaultAddChecked && currency.default;
            $('#' + buildInputId('currency', 'default', currency.id)).prop('checked', isChecked);
            $('#' + buildInputId('currency', 'default', currency.id, true)).prop('checked', isChecked);
        });
    defaultAdd.prop('checked', wasDefaultAddChecked);
}

function _collectCurrencyData(id) {
    return {
        default: $('#' + buildInputId('currency', 'default', id)).prop('checked'),
        name: $('#' + buildInputId('currency', 'name', id)).val(),
        code: $('#' + buildInputId('currency', 'code', id)).val(),
        symbol: $('#' + buildInputId('currency', 'symbol', id)).val(),
        value: parseFloat($('#' + buildInputId('currency', 'value', id)).val()),
    };
}

function addCurrency() {
    performRequest('POST', 'currency', _collectCurrencyData())
        .then(() => refreshCurrencies())
        .then(() => refreshAccounts());
}

function updateCurrency(id) {
    performRequest('PUT', 'currency/' + id, _collectCurrencyData(id))
        .then(() => refreshCurrencies())
        .then(() => refreshAccounts());
}

function performCurrencyAction(id, action) {
    performRequest('POST', 'currency/' + id + '/' + action)
        .then(() => refreshCurrencies())
        .then(() => refreshAccounts());
}
