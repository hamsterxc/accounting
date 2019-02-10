let transactionsTable = {};

function populateTransactions(populateStatic = true) {
    const table = transactionsTable;
    transactionsTable = populateDataTable({
        name: 'transaction',
        selectorHeader: populateStatic ? '#main-header' : undefined,
        selectorBody: '#main-body',
        selectorFooter: populateStatic ? '#main-add' : undefined,
        headerActions: '<a href="/manage.html">Manage</a>',
        rows: transactions.map(transaction => transaction.id),
        columns: [{
            name: 'date',
            caption: 'Date',
            supplierView: id => formatDateTransaction(find(transactions, id).time),
            supplierEdit: id => _buildTransactionDateInput(buildInputId('transaction', 'date', id),
                formatDateTransaction(find(transactions, id).time)),
            supplierAdd: () => _buildTransactionDateInput(buildInputId('transaction', 'date')),
        }]
            .concat(accounts.map(account => ({
                name: 'account' + account.id,
                caption: account.name + ', ' + find(currencies, account.currencyId).symbol,
                supplierView: id => findAll(find(transactions, id).operations, account.id, 'accountId')
                    .map(operation => '<span class="oneline' + (operation.active ? '' : ' inactive') + '">'
                        + formatNumber(operation.amount, 2, ' ' + _getOperationCurrencyCode(operation)) + '</span>')
                    .join('<br/>'),
                supplierEdit: id => '<span ' + _buildOperationElementDataAttributes('data', account.id, id)
                    + ' data-last-operation-id="0"/>',
                supplierAdd: () => '<span ' + _buildOperationElementDataAttributes('data', account.id)
                    + ' data-last-operation-id="0"/>',
            })))
            .concat([
                {
                    name: 'total',
                    caption: 'Total',
                    supplierView: id => formatNumber(find(transactions, id).total),
                    supplierEdit: id => '',
                    supplierAdd: () => '',
                },
                {
                    name: 'category',
                    caption: 'Category',
                    supplierView: id => find(categories, find(transactions, id).categoryId).name,
                    supplierEdit: id => _buildTransactionCategoryInput(buildInputId('transaction', 'category', id),
                        find(transactions, id).categoryId),
                    supplierAdd: () => _buildTransactionCategoryInput(buildInputId('transaction', 'category')),
                },
                {
                    name: 'comment',
                    caption: 'Comment',
                    supplierView: id => find(transactions, id).comment,
                    supplierEdit: id => _buildTransactionCommentInput(buildInputId('transaction', 'comment', id),
                        find(transactions, id).comment),
                    supplierAdd: () => _buildTransactionCommentInput(buildInputId('transaction', 'comment')),
                }
            ]),
        actionUp: id => 'performTransactionAction(' + id + ',' + '\'moveup\');',
        actionDown: id => 'performTransactionAction(' + id + ',' + '\'movedown\');',
        actionDelete: id => 'deleteTransaction(' + id + ');',
        actionSave: id => 'updateTransaction(' + id + ');',
        actionAdd: () => 'addTransaction();',
        actionAfterEdit: id => 'filter();fillOperations(' + id + ');setupTransactionSubmit(' + id + ');',
    });

    if (populateStatic) {
        fillOperations();
        setupTransactionSubmit();
    } else {
        transactionsTable['header'] = table['header'];
        transactionsTable['footer'] = table['footer'];
    }
}

function _buildTransactionDateInput(id, value) {
    return '<input type="text" size="10" placeholder="31.12[.[20]12]"'
        + (value === undefined ? '' : (' value="' + value + '"'))
        + ' id="' + id + '"/>';
}

function _buildTransactionCategoryInput(id, value) {
    return '<select id="' + id + '">'
        + categories.map(category => '<option value="' + category.id
            + '" data-name="category-option" data-category-id="' + category.id + '"'
            + (value !== undefined && value === category.id ? ' selected' : '') + '>' + category.name + '</option>')
            .join('')
        + '</select>';
}

function _buildTransactionCommentInput(id, value) {
    return '<input type="text" size="30" placeholder="Comment"'
        + (value === undefined ? '' : (' value="' + value + '"'))
        + ' id="' + id + '"/>';
}

function _getOperationCurrencyCode(operation) {
    const currencyId = operation.currencyId === null
        ? find(accounts, operation.accountId).currencyId
        : operation.currencyId;
    return find(currencies, currencyId).code;
}

function _obtainNextOperationId(accountId, transactionId) {
    const element = $(_buildOperationElementDataSelector('data', accountId, transactionId));
    const attrName = 'data-last-operation-id';
    const operationId = (parseInt(element.attr(attrName)) ||  0) + 1;
    element.attr(attrName, operationId);
    return operationId;
}

/*
 * @param args {
 *   accountId,
 *   transactionId,
 *   operationId,
 *   operation,
 * }
 */
function _buildOperationElement(args) {
    const operationId = args.operationId || _obtainNextOperationId(args.accountId, args.transactionId);
    const addRemoveArguments = '{accountId:' + args.accountId
        + ',transactionId:' + (isNaN(args.transactionId) ? '\'_add\'' : args.transactionId)
        + ',operationId:' + operationId + '}';
    return '<div class="oneline" ' + _buildOperationElementDataAttributes('line', args.accountId, args.transactionId, operationId) + '>'
        + '<a class="action" onclick="addOperationElement(' + addRemoveArguments + ')"><span class="small">+</span></a>'
        + '&nbsp;'
        + '<a class="action" onclick="removeOperationElement(' + addRemoveArguments + ')"><span class="small warn">X</span></a>'
        + '&nbsp;'
        + '<input type="text" size="10" placeholder="999.99 [EUR]" value="'
            + (args.operation === undefined ? '' : (args.operation.amount + ' ' + _getOperationCurrencyCode(args.operation))) + '" '
            + _buildOperationElementDataAttributes('amount', args.accountId, args.transactionId, operationId) + '/>'
        + '&nbsp;'
        + '<input type="checkbox"' + (args.operation === undefined || args.operation.active ? ' checked' : '')
            + ' ' + _buildOperationElementDataAttributes('active', args.accountId, args.transactionId, operationId) + '/>'
        + '</div>';
}

function _buildOperationElementDataAttributes(name, accountId, transactionId, operationId) {
    return 'data-name="' + name
        + '" data-account-id="' + accountId
        + '" data-transaction-id="' + (transactionId === undefined ? '_add' : transactionId)
        + '"' + (operationId === undefined ? '' : ' data-operation-id="' + operationId + '"');
}

function _buildOperationElementDataSelector(name, accountId, transactionId, operationId) {
    return '[data-name="' + name
        + '"][data-account-id="' + accountId
        + '"][data-transaction-id="' + (transactionId === undefined ? '_add' : transactionId)
        + '"]' + (operationId === undefined ? '' : '[data-operation-id="' + operationId + '"]');
}

/*
 * @param args {
 *   accountId,
 *   transactionId,
 *   operationId,
 *   operation,
 * }
 */
function addOperationElement(args) {
    let element;
    if(args.operationId === undefined) {
        element = $(_buildOperationElementDataSelector('data', args.accountId, args.transactionId));
    } else {
        element = $(_buildOperationElementDataSelector('line', args.accountId, args.transactionId, args.operationId));
    }

    const line = $(_buildOperationElement({
        accountId: args.accountId,
        transactionId: args.transactionId,
        operation: args.operation
    }));

    element.after(line);

    const input = line.find(_buildOperationElementDataSelector('amount', args.accountId, args.transactionId));
    setupSubmit(input, _obtainTransactionSubmit(args.transactionId));

    return line;
}

function _obtainTransactionSubmit(transactionId) {
    return () => {
        if(isNaN(transactionId)) {
            addTransaction();
        } else {
            updateTransaction(transactionId);
        }
    };
}

function setupTransactionSubmit(transactionId) {
    const submit = _obtainTransactionSubmit(transactionId);
    setupSubmit($('#' + buildInputId('transaction', 'date', transactionId)), submit);
    setupSubmit($('#' + buildInputId('transaction', 'comment', transactionId)), submit);
}

/*
 * @param args {
 *   accountId,
 *   transactionId,
 *   operationId,
 * }
 */
function removeOperationElement(args) {
    if($(_buildOperationElementDataSelector('line', args.accountId, args.transactionId)).length <= 1) {
        addOperationElement({
            accountId: args.accountId,
            transactionId: args.transactionId,
            operationId: args.operationId
        });
    }
    $(_buildOperationElementDataSelector('line', args.accountId, args.transactionId, args.operationId)).remove();
}

function fillOperations(transactionId) {
    accounts.forEach(account => {
        const operations = transactionId !== undefined
            ? findAll(find(transactions, transactionId).operations, account.id, 'accountId')
            : [];
        if(operations.length === 0) {
            operations.push(undefined);
        }

        let operationId = undefined;
        operations.forEach(operation => {
            const line = addOperationElement({
                accountId: account.id,
                transactionId: transactionId,
                operationId: operationId,
                operation: operation,
            });
            operationId = line.attr('data-operation-id');
        });
    });
}

function _collectTransactionData(id) {
    const data = {
        date: $('#' + buildInputId('transaction', 'date', id)).val(),
        categoryId: $('#' + buildInputId('transaction', 'category', id)).val(),
        comment: $('#' + buildInputId('transaction', 'comment', id)).val(),
        operations: []
    };

    accounts.forEach(account => {
        $(_buildOperationElementDataSelector('amount', account.id, id)).each(function() {
            const element = $(this);
            const amount = element.val();
            if(amount.length > 0) {
                data.operations.push({
                    accountId: account.id,
                    amount: amount,
                    active: $(_buildOperationElementDataSelector('active', account.id, id, element.attr('data-operation-id')))
                        .prop('checked'),
                });
            }
        });
    });

    return data;
}

function addTransaction() {
    performRequest('POST', 'transaction', _collectTransactionData())
        .then(() => refreshDynamic(true))
        .then(() => $('#' + buildInputId('transaction', 'date')).focus());
}

function updateTransaction(id) {
    performRequest('PUT', 'transaction/' + id, _collectTransactionData(id))
        .then(() => refreshDynamic(false));
}

function performTransactionAction(id, action) {
    performRequest('POST', 'transaction/' + id + '/' + action)
        .then(() => refreshDynamic(false));
}

function deleteTransaction(id) {
    performRequest('DELETE', 'transaction/' + id)
        .then(() => refreshDynamic(false));
}
