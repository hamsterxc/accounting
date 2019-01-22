function performRequest(method, url, data) {
    const request = {
        type: method,
        url: url
    };

    if(data !== undefined) {
        request['contentType'] = 'application/json';
        request['data'] = JSON.stringify(data);
    }

    return $.ajax(request);
}

function getOrDefault(item, extractor, def) {
    return item === undefined ? def : extractor(item);
}

function find(items, value, fieldName = 'id') {
    return findAll(items, value, fieldName)[0];
}

function findAll(items, value, fieldName = 'id') {
    return items.filter(item => item[fieldName] === value);
}

function sort(items, fieldName) {
    items.sort((a, b) => a[fieldName] - b[fieldName]);
    return items;
}
