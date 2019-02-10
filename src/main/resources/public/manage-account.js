function populateAccounts() {
    populateDataTable({
        name: 'account',
        selectorHeader: '#manage-accounts-header',
        selectorBody: '#manage-accounts-body',
        selectorFooter: '#manage-accounts-add',
        headerActions: '',
        rows: accounts.map(account => account.id),
        columns: [
            {
                name: 'visible',
                caption: '&#x1f441;',
                supplierView: id => '<input type="checkbox" disabled' + (find(accounts, id).visible ? ' checked' : '') + '/>',
                supplierEdit: id => '<input type="checkbox" id="' + buildInputId('account', 'visible', id) + '"' + (find(accounts, id).visible ? ' checked' : '') + '/>',
                supplierAdd: () => '<input type="checkbox" id="' + buildInputId('account', 'visible') + '"/>',
            },
            {
                name: 'name',
                caption: 'Name',
                supplierView: id => find(accounts, id).name,
                supplierEdit: id => '<input type="text" id="' + buildInputId('account', 'name', id) + '" size="10" value="' + find(accounts, id).name + '"/>',
                supplierAdd: () => '<input type="text" id="' + buildInputId('account', 'name') + '" size="10"/>',
            },
            {
                name: 'currency',
                caption: 'Currency',
                supplierView: id => find(currencies, find(accounts, id).currencyId).code,
                supplierEdit: id => {
                    const account = find(accounts, id);
                    return '<select id="' + buildInputId('account', 'currency', id) + '">'
                        + currencies.map(currency =>
                            '<option value="' + currency.id + '"'
                            + (currency.id === account.currencyId ? ' selected' : '')
                            + '>' + currency.code + '</option>'
                        ).join('')
                        + '</select>';
                },
                supplierAdd: () => '<select id="' + buildInputId('account', 'currency') + '">'
                    + currencies.map(currency => '<option value="' + currency.id + '">' + currency.code + '</option>').join('')
                    + '</select>',
            },
        ],
        actionUp: id => 'performAccountAction(' + id + ',' + '\'moveup\');',
        actionDown: id => 'performAccountAction(' + id + ',' + '\'movedown\');',
        actionDelete: id => 'deleteAccount(' + id + ');',
        actionSave: id => 'updateAccount(' + id + ');',
        actionAdd: () => 'addAccount();',
        actionAfterEdit: id => 'setupAccountSubmit(' + id + ');',
    });

    setupAccountSubmit();
}

function setupAccountSubmit(accountId) {
    const submit = _obtainAccountSubmit(accountId);
    setupSubmit($('#' + buildInputId('account', 'name', accountId)), submit);
}

function _obtainAccountSubmit(accountId) {
    return () => {
        if(isNaN(accountId)) {
            addAccount();
        } else {
            updateAccount(accountId);
        }
    };
}

function _collectAccountData(id) {
    return {
        visible: $('#' + buildInputId('account', 'visible', id)).prop('checked'),
        name: $('#' + buildInputId('account', 'name', id)).val(),
        currencyId: parseInt($('#' + buildInputId('account', 'currency', id)).val()),
    };
}

function addAccount() {
    performRequest('POST', 'account', _collectAccountData())
        .then(() => refreshAccounts());
}

function updateAccount(id) {
    performRequest('PUT', 'account/' + id, _collectAccountData(id))
        .then(() => refreshAccounts());
}

function performAccountAction(id, action) {
    performRequest('POST', 'account/' + id + '/' + action)
        .then(() => refreshAccounts());
}

function deleteAccount(id) {
    performRequest('DELETE', 'account/' + id)
        .then(() => refreshAccounts());
}
