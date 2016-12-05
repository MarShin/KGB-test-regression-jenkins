/**
 * TEST CASES FOR DISQUS PORTAL ON THE TEST PAGES
 */


setTimeout(function () {
    QUnit.test("Disqus Link Correct", function (assert) {
        assert.ok($('#disqus_thread iframe').first().attr('src').indexOf('&t_u=test') > -1);
    });
}, 2000);