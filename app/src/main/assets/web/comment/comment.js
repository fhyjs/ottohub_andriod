$(document).ready(function () {
    pageType = comment.getType();
    const intervalId = setInterval(() => {
      if(comment.init()){
        clearInterval(intervalId);
        loadData();
      }
    }, 500);

});
function loadData(){
    var rootList = null;
    if(pageType === "video"){
       $.getScript("video_v1.4.10.js", function () {
         console.log("脚本加载完成！");
         ottohub_load_comment_video();
       });
    }
}
function loadCard(username,avatarUrl,tags,time,content) {
  $.get("card.html", function (html) {
    var card = $(html);
    card.find(".username").text(username);
    card.find(".avatar").attr("src", avatarUrl);
    tags.split(",").forEach(function (tag) {
        card.find(".tags").append("<span class='tag'>" + tag + "</span>");
    });
    card.find(".timestamp").text(time);
    card.find(".content").text(content);
    $(".container").append(card);
  });
}

