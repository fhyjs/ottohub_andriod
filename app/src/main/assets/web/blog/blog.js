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
    content = blog.markdown(blog_data.content);
  } else {
    // 兼容旧版 marked，使用函数调用而不是 marked.parse
    content = marked.parse(blog_data.content);
  }

  $("#content").html($.parseHTML(content));
  $("#title").html(blog_data.title);
  $("#subtitle").html(blog_data.time);

  $("#content").html(replaceAll( $("#content").html(),"\n","<br/>"));
  $('a').attr('target', '_blank');
}

// replaceAll polyfill
function replaceAll(str, search, replacement) {
  var escaped = search.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1');
  var regex = new RegExp(escaped, 'g');
  return str.replace(regex, replacement);
}
