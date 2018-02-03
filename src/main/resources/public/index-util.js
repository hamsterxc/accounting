function find(items, value, fieldName = 'id') {
    return findAll(items, value, fieldName)[0];
}

function findAll(items, value, fieldName = 'id') {
    return items.filter(item => item[fieldName] === value);
}

function formatNumber(number, addString = '') {
    const value = Number(number);
    const formatted = value.toFixed(2) + addString;
    return value < 0 ? ('<span class="warn">' + formatted + '</span>') : formatted;
}

function formatDateTransaction(time) {
    const date = new Date(time);
    const d = date.getUTCDate();
    const m = date.getUTCMonth() + 1;
    const y = date.getUTCFullYear();

    return (d < 10 ? '0' : '') + d.toString()
        + '.' + (m < 10 ? '0' : '') + m.toString()
        + '.' + y.toString();
}

function formatDateSummary(time) {
    const date = new Date(time);
    const m = date.getUTCMonth();
    const y = date.getUTCFullYear();

    const months = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"];
    return months[m] + ' ' + y.toString();
}

function cutDateToMonth(date) {
    date.setUTCDate(1);
    date.setUTCHours(0, 0, 0, 0);
}
