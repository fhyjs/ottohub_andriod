$(document).ready(function(){
    console.log("start");
    const intervalId = setInterval(() => {
      var data = blog.getData();
      if(data!=="loading"){
        clearInterval(intervalId);
        console.log("got data : "+ data);
        window.blog_data = $.parseJSON(data);
        loadBlog();
      }
    }, 500);
});
function loadBlog(){
    var content = null;
    if(typeof marked === "undefined"){
        content = blog.markdown(blog_data.content);
    }else{
        content = marked.parse(blog_data.content);
    }
    $("#content").html($.parseHTML(content));
    $("#title").html(blog_data.title);
    $("#subtitle").html(blog_data.time);
}
function replaceAll(str, search, replacement) {
  // 将 search 转为转义后的正则
  const escaped = search.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1');
  const regex = new RegExp(escaped, 'g');
  return str.replace(regex, replacement);
}