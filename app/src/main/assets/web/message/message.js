$(document).ready(function () {
  console.log("start");

  var intervalId = setInterval(function () {
    var data = blog.getData();
    if (data !== "loading") {
      clearInterval(intervalId);
      console.log("got data : " + data);
      window.blog_data = $.parseJSON(data);
      loadBlog();
    }
  }, 500);
});

function loadBlog() {
  var content = null;
  if (typeof marked === "undefined") {
  //由java后端处理
    content = blog.markdown(blog_data.content);
    content=replaceAll( content,"\n","<br/>");
  } else {
    // 兼容旧版 marked，使用函数调用而不是 marked.parse
    try{
        content = marked.parse(blog_data.content);
    }catch(e){
        //由java后端处理
        content = blog.markdown(blog_data.content);
        content=replaceAll( content,"\n","<br/>");
    }

  }

  $("#content").html(content);
  $("#title").html(blog.getTitle());
  //$("#subtitle").html(blog_data.time);
  matchLink();
  $('a').attr('target', '_blank');

}
function matchLink(){
      // 匹配 ob、ov 或 uid 开头，后面跟数字，忽略大小写
      var regex = /\b(ob|ov|uid)(\d+)\b/gi;

      $('body *').each(function () {
        var $el = $(this);

        // 跳过某些不能插入 HTML 的标签
        if (this.tagName.match(/^(SCRIPT|STYLE|TEXTAREA|INPUT|BUTTON|A)$/i)) return;

        $el.contents().filter(function () {
          return this.nodeType === 3 && regex.test(this.nodeValue);
        }).each(function () {
          var html = this.nodeValue.replace(regex, function (match, type, num) {
            // 保留原始大小写 match，用于显示；统一小写 type 用于构造链接路径
            return '<a href="https://m.ottohub.cn/' + type.toLowerCase() + '/' + num + '" target="_blank">' + match + '</a>';
          });

          $(this).replaceWith(html);
        });
      });
}
// replaceAll polyfill
function replaceAll(str, search, replacement) {
  var escaped = search.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1');
  var regex = new RegExp(escaped, 'g');
  return str.replace(regex, replacement);
}
