function populateSummary(summaries) {
    $('#summary').html(summaries.map(summary =>
        '<table class="inlined">'
        + '<tr><th colspan="2"><a class="action" onclick="filterByDates('
            + summary.from + ',' + summary.to + ')">' + formatDateSummary(summary.from) + '</a></th></tr>'
        + categories.map(category =>
                '<tr data-name="summary-category" data-category-id="' + category.id + '"><td>' + category.name
                + '</td><td>' + formatNumber(getOrDefault(summary.aggregation[category.id], item => item, 0.0))
                + '</td></tr>'
            ).join('')
        + '<tr><th colspan="2">' + formatNumber(summary.total) + '</th></tr>'
        + '</table>'
    ).join(''));
}
