/*
 * @param settings {
 *   name: str,
 *   selectorHeader: str,
 *   selectorBody: str,
 *   selectorFooter: str,
 *   headerActions: str,
 *   rows: array of int,
 *   columns: array of {
 *     name: str,
 *     caption: str,
 *     supplierView: lambda int (id) -> str,
 *     supplierEdit: lambda int (id) -> str,
 *     supplierAdd: lambda () -> str,
 *   },
 *   actionUp: lambda int (id) -> str,
 *   actionDown: lambda int (id) -> str,
 *   actionDelete: lambda int (id) -> str,
 *   actionSave: lambda int (id) -> str,
 *   actionAdd: lambda () -> str,
 *   actionAfterEdit: lambda int (id) -> str,
 *   actionAfterCancel: lambda int (id) -> str,
 * }
 *
 * @return {
 *   header: row-definition,
 *   body: array of row-definition,
 *   bodyEdit: array of row-definition,
 *   footer: row-definition,
 * }
 * row-definition = {
 *   id: row-id,
 *   cells: array of row-cell-id,
 * }
 */
function populateDataTable(settings) {
    const name = settings.name || '';
    const rows = settings.rows || [];
    const columns = (settings.columns || []).map(column => ({
        name: column.name || '',
        caption: column.caption || '',
        supplierView: column.supplierView || (id => ''),
        supplierEdit: column.supplierEdit || (id => ''),
        supplierAdd: column.supplierAdd || (() => ''),
    }));

    const result = {};

    if (settings.selectorHeader !== undefined) {
        const header = $(settings.selectorHeader);
        header.empty();

        const rowObject = _createRowHeader(name, columns, settings.headerActions);
        result.header = rowObject.info;
        header.append(rowObject.element);
    }

    if (settings.selectorBody !== undefined) {
        const body = $(settings.selectorBody);
        body.empty();

        result.body = [];
        result.bodyEdit = [];

        rows.forEach(id => {
            const rowViewObject = _createRowView(name, columns, id,
                settings.actionUp, settings.actionDown, settings.actionDelete, settings.actionAfterEdit);
            const rowEditObject = _createRowEdit(name, columns, id, rowViewObject.info.id,
                settings.actionSave, settings.actionAfterCancel);

            result.body.push(rowViewObject.info);
            result.bodyEdit.push(rowEditObject.info);

            const divTemp = $('<div/>');
            divTemp.append(rowEditObject.element);
            const rowEditText = divTemp.html();

            rowViewObject.element.attr('data-row-edit', rowEditText);
            body.append(rowViewObject.element);
        });
    }

    if (settings.selectorFooter !== undefined) {
        const footer = $(settings.selectorFooter);
        footer.empty();

        const rowObject = _createRowFooter(name, columns, settings.actionAdd);
        result.footer = rowObject.info;
        footer.append(rowObject.element);
    }
    
    return result;
}

function _createRowHeader(name, columns, headerActions) {
    const rowId = name + '_header';
    const rowObject = _createRow(rowId);

    const cellActions = _createCell(
        'th',
        rowId + '-actions',
        headerActions || '',
        rowObject.element,
        rowObject.info.cells
    );
    cellActions.addClass('controls');

    columns.forEach(column => _createCell(
        'th',
        rowId + '-' + column.name,
        column.caption,
        rowObject.element,
        rowObject.info.cells
    ));

    return rowObject;
}

function _createRowEdit(name, columns, id, rowViewId, actionSave, actionAfterCancel) {
    const rowId = name + '_edit' + id;
    const rowObject = _createRow(rowId);

    const cellActions = _createCell(
        'td',
        rowId + '-actions',
        [
            actionSave === undefined ? undefined : { text: 'Save', action: actionSave(id) },
            { text: 'Cancel', action: 'stopEditing(\'' + rowViewId + '\',\'' + rowId + '\');'
                    + (actionAfterCancel === undefined ? '' : actionAfterCancel(id)) },
        ]
            .filter(action => action !== undefined)
            .map(action => '<input type="button" value="' + action.text + '" onclick="' + action.action + '"/>')
            .join('<br/>'),
        rowObject.element,
        rowObject.info.cells
    );
    cellActions.addClass('controls');

    columns.forEach(column => _createCell(
        'td',
        rowId + '-' + column.name,
        column.supplierEdit(id),
        rowObject.element,
        rowObject.info.cells
    ));

    return rowObject;
}

function _createRowView(name, columns, id, actionUp, actionDown, actionDelete, actionAfterEdit) {
    const rowId = name + id;
    const rowObject = _createRow(rowId);

    const cellActions = _createCell(
        'td',
        rowId + '-actions',
        [
            { text: 'E', action: 'startEditing(\'' + rowId + '\');' + (actionAfterEdit === undefined ? '' : actionAfterEdit(id)) },
            actionUp === undefined ? undefined : { text: '&#x2191;', action: actionUp(id) },
            actionDown === undefined ? undefined : { text: '&#x2193;', action: actionDown(id) },
            actionDelete === undefined ? undefined : { text: '<span class="warn">X</span>', action: actionDelete(id) },
        ]
            .filter(action => action !== undefined)
            .map(action => '<a class="action" onClick="' + action.action + '">' + action.text + '</a>')
            .join('&#160;'),
        rowObject.element,
        rowObject.info.cells
    );
    cellActions.addClass('controls');

    columns.forEach(column => _createCell(
        'td',
        rowId + '-' + column.name,
        column.supplierView(id),
        rowObject.element,
        rowObject.info.cells
    ));

    return rowObject;
}

function _createRowFooter(name, columns, actionAdd) {
    const rowId = name + '_footer';
    const rowObject = _createRow(rowId);

    const cellActions = _createCell(
        'td',
        rowId + '-actions',
        actionAdd === undefined ? '' : '<input type="button" name="submit" value="Add" onClick="' + actionAdd() + '"/>',
        rowObject.element,
        rowObject.info.cells
    );
    cellActions.addClass('controls');

    columns.forEach(column => _createCell(
        'td',
        rowId + '-' + column.name,
        column.supplierAdd(),
        rowObject.element,
        rowObject.info.cells
    ));

    return rowObject;
}

function _createRow(id) {
    const row = $('<tr/>');
    row.attr('id', id);
    return {
        element: row,
        info: {
            id: id,
            cells: []
        }
    };
}

function _createCell(tag, id, html, row, cells) {
    const cell = $('<' + tag + '/>');
    cell.attr('id', id);
    cell.html(html);
    row.append(cell);
    cells.push(id);
    return cell;
}

function startEditing(id) {
    const rowView = $('#' + id);
    rowView.hide();

    const rowEdit = $(rowView.attr('data-row-edit'));
    rowView.after(rowEdit);
}

function stopEditing(idView, idEdit) {
    $('#' + idEdit).remove();
    $('#' + idView).show();
}

function buildInputId(type, name, id, isView) {
    return type + (id === undefined ? '_add' : id) + '-' + name + '_input' + (isView ? '_view' : '');
}
