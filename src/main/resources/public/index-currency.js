function populateCurrency() {
    $('#currency').html(
        '<table>'
        + '<tr><th colspan="2">Currency rates</th></tr>'
        + currencies
            .map(currency => '<tr><td>' + currency.name + '</td><td>' + formatNumber(currency.value, 3) + '</td></tr>')
            .join('')
        + '</table>'
    );
}
