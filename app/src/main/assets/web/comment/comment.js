(function() {
    $(document).ready(function () {
        matchLink();
        $('a').attr('target', '_blank');
    });

    function matchLink(){
        var regex = /\b(ob|ov|uid)(\d+)\b/gi;
        $('body *').each(function () {
            var $el = $(this);
            if (this.tagName.match(/^(SCRIPT|STYLE|TEXTAREA|INPUT|BUTTON|A)$/i)) return;
            $el.contents().filter(function () {
                return this.nodeType === 3 && regex.test(this.nodeValue);
            }).each(function () {
                var html = this.nodeValue.replace(regex, function (match, type, num) {
                    return '<a href="https://m.ottohub.cn/' + type.toLowerCase() + '/' + num + '" target="_blank">' + match + '</a>';
                });
                $(this).replaceWith(html);
            });
        });
    }

    function replaceAll(str, search, replacement) {
        var escaped = search.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1');
        var regex = new RegExp(escaped, 'g');
        return str.replace(regex, replacement);
    }
})();
