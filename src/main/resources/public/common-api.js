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
