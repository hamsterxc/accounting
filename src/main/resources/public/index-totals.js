function populateTransactionTotals(totalBefore, totalAfter) {
    $('#main-total-before').html(_buildTransactionTotal(totalBefore, 'before'));
    $('#main-total-after').html(_buildTransactionTotal(totalAfter, 'after'));
}

function _buildTransactionTotal(total, id) {
    return '<tr>'
        + '<th></th>'
        + '<th></th>'
        + accounts.map(account => '<th id="' + buildTotalCellId(id, account.id) + '">'
            + formatNumber(getOrDefault(total.aggregation[account.id], item => item, 0.0)) + '</th>')
        + '<th>' + formatNumber(total.total) + '</th>'
        + '<th></th>'
        + '<th></th>'
        + '</tr>';
}

function buildTotalCellId(id, accountId) {
    return 'total-cell-' + id + '-account' + accountId;
}
