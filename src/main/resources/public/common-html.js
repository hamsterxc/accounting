function scrollToTop() {
    $("html, body").animate({scrollTop: 0}, 'fast')
}

function buildInputId(type, name, id, isView) {
    return type + (id === undefined ? '_add' : id) + '-' + name + '_input' + (isView ? '_view' : '');
}

function setupSubmit(input, action) {
    input.off('keyup');
    input.on('keyup', function(event) {
        if(event.keyCode === 13) {
            action();
        }
    });
}
