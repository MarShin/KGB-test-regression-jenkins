/**
 * TEST CASES FOR DISQUS PORTAL ON THE TEST PAGES
 */


setTimeout(function () {
    QUnit.test("Disqus Exists", function (assert) {
        assert.ok($("#disqus_thread").html() != '');
    });
}, 2000);