function findByField(items, fieldName, value) {
    return items.filter(function(item) {
        return item[fieldName] === value;
    })[0];
}

function formatNumber(number) {
    var value = Number(number);
    var formatted = value.toFixed(2);
    return value < 0 ? ('<span class="warn">' + formatted + '</span>') : formatted;
}

function formatDateTransaction(time) {
    var date = new Date(time);
    var d = date.getUTCDate();
    var m = date.getUTCMonth() + 1;
    var y = date.getUTCFullYear();

    return (d < 10 ? '0' : '') + d.toString()
        + '.' + (m < 10 ? '0' : '') + m.toString()
        + '.' + y.toString();
}

function formatDateSummary(time) {
    var date = new Date(time);
    var m = date.getUTCMonth();
    var y = date.getUTCFullYear();

    var months = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"];
    return months[m] + ' ' + y.toString();
}

function cutDateToMonth(date) {
    date.setUTCDate(1);
    date.setUTCHours(0, 0, 0, 0);
}
