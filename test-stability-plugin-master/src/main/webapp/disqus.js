/**
 *  RECOMMENDED CONFIGURATION VARIABLES: EDIT AND UNCOMMENT THE SECTION BELOW TO INSERT DYNAMIC VALUES FROM YOUR PLATFORM OR CMS.
 *  LEARN WHY DEFINING THESE VARIABLES IS IMPORTANT: https://disqus.com/admin/universalcode/#configuration-variables
 */

var disqus_config = function () {
    var actual_url = window.location.href;
    if (typeof debug_url !== 'undefined') {
        actual_url = debug_url;
    }
    console.log(actual_url);
    this.page.url = actual_url;
    var page_name = actual_url.split('testReport/')[1];
    this.page.identifier = page_name;
    this.page.title = 'Test Name';
};

(function () {
    var doc = document,
        s = doc.createElement('script');

    s.src = 'https://kgb427.disqus.com/embed.js';
    s.setAttribute('data-timestamp', +new Date());

    (doc.head || doc.body).appendChild(s);
})();