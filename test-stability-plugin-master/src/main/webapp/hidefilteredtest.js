$(document).ready(function () {
    var teststr = $("#filter-data").html();
    var tests = teststr.substring(1, teststr.length - 1).split(", ");
    if (teststr.indexOf('[') < teststr.indexOf(']') && teststr != '[]' && teststr != '' && teststr != 'null') {
        for (var i = 0; i < tests.length; i++) {
            var selector = "tr > td > a > span:contains(" + tests[i] + ")";
            $(selector).each(function (i) {
                $(this).parent().parent().parent().hide();
            });
        }
    }
});