var url = comment.getVUrl(); // 获取当前 URL
var pathParts = url.split('/'); // 分割路径
var vid = pathParts[4];
var parent_vcid = 0;
var if_comment_sender_open = 0;
let dp;
let danmaku;
var danmaku_list;
function ottohub_load_comment_video(){
    $('#comment_btn').click(function() {
        if (parent_vcid == 0) {$('#comment_title').text('评论OV'+vid);}
        else {$('#comment_title').text('回复评论OVC'+parent_vcid);}
        if (if_comment_sender_open == 0) {$('#comment_sender').show();if_comment_sender_open = 1;}
        else {$('#comment_sender').hide();if_comment_sender_open = 0;parent_vcid = 0;}
    });
    $('#empty_btn').click(function() {$('#comment_sender').hide();if_comment_sender_open = 0;parent_vcid = 0;});
    load_comment(0);
}
//评论视频
function comment_video() {
    var token = getCookie("token");
    if (token == "error") {alert('那我缺的登录这块谁来给我补啊');}
    var content = $('#comment_content').val();
    content = content.replace(/\r?\n/g, "\\n");
    $.ajax({
        url: 'https://api.ottohub.cn/?module=comment&action=comment_video&vid='+vid+'&parent_vcid='+parent_vcid+'&content='+content+'&token='+token,  // 请求的 URL
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.status == "success") {
                $('.comment_container[name="'+parent_vcid+'"]').html('');
                $('.comment_container[name="'+parent_vcid+'"]').attr('offset', 0);
                load_comment(parent_vcid);
                $('#comment_content').val('');$('#comment_sender').hide();parent_vcid = 0;
                if (response.if_get_experience == 1) {alert('经验加三');}
                else {alert('今日经验已达上限');}
            } else {
                if (response.message == 'error_vid') {alert('评论棍母');}
                else if (response.message == 'error_parent_vcid') {alert('评论棍母');}
                else if (response.message == 'error_parent') {alert('盖楼笑传之层层包');}
                else if (response.message == 'warn') {alert('这是碰都不能碰的话题');}
                else if (response.message == 'error_token') {alert('那我缺的登录这块谁来给我补啊');}
                else {alert('诶服务器怎么似了');}
            }
        },
        error: function(xhr, status, error) {
            //alert('诶服务器怎么似了');
        }
    });
}
//加载评论
function load_comment(parent_comment) {
    var temp_offset = Number($('.comment_container[name="'+parent_comment+'"]').attr('offset'));
    $('.comment_container[name="'+parent_comment+'"]').attr('offset', temp_offset+12);
    var api = 'https://api.ottohub.cn/?module=comment&action=video_comment_list&num=12&vid='+vid+'&parent_vcid='+parent_comment+'&offset='+temp_offset;
    var token = getCookie("token");
    if (token != "error") {api = 'https://api.ottohub.cn/?module=comment&action=video_comment_list&num=12&vid='+vid+'&parent_vcid='+parent_comment+'&offset='+temp_offset+'&token='+token;}
    $.ajax({
        url: api,  // 请求的 URL
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.status == "success") {
                var comment_html = '';
                var comment_list = response.comment_list;
                if (comment_list.length === 0) {$('.bottom[name="'+parent_comment+'"]').remove();return;}
                comment_list.forEach(function(comment) {
                    comment_html = comment_html + comment_card(comment.vcid, comment.parent_vcid, comment.uid, comment.content, comment.time, comment.child_comment_num, comment.if_my_comment, comment.username, comment.honour, comment.avatar_url);
                });
                $('.bottom[name="'+parent_comment+'"]').remove();
                $('.comment_container[name="'+parent_comment+'"]').append(comment_html+'<div class="bottom" name="'+parent_comment+'">走位中...</div>');
                //自动刷新
                let observer = new IntersectionObserver(function (entries) {entries.forEach(function(entry) {if (entry.isIntersecting) {let elementName = entry.target.getAttribute("name");load_comment(elementName);}});}, { threshold: 0.1 });
                document.querySelectorAll('.bottom').forEach(function (element) {observer.observe(element);});
                $('.delete_btn').click(function() {delete_comment(this.getAttribute('name'));});
                $('.report_btn').click(function() {report_comment(this.getAttribute('name'));});
                $('.comment_card').on('click', function(event) {event.stopPropagation();});
                $('.comment_btn').click(function() {
                    $('#comment_content').val('');
                    var name = this.getAttribute('name');
                    var parent_cid = this.getAttribute('parent_cid');
                    var username = this.getAttribute('username');
                    if (parent_cid == 0) {parent_vcid = name;}
                    else {parent_vcid = parent_cid;$('#comment_content').val('@'+username+' '+$('#comment_content').val());}
                    $('#comment_title').text('回复评论OVC'+parent_vcid);
                    $('#comment_sender').show();
                    if_comment_sender_open = 1;
                });
                $('.child_container').click(function() {
                    let parent_cid = this.getAttribute('name');
                    $(this).html('');
                    $(this).addClass('child_container_selected');
                    $(this).removeClass('child_container');
                    load_comment(parent_cid);
                });
            } else {
                alert('诶服务器怎么似了');
            }
        },
        error: function(xhr, status, error) {
            //alert('诶服务器怎么似了');
        }
    });
}
//举报评论
function report_comment(cid) {
    if (!confirm('确定举报')) {return;}
    var token = getCookie("token");
    if (token == "error") {alert('那我缺的登录这块谁来给我补啊');}
    $.ajax({
        url: 'https://api.ottohub.cn/?module=comment&action=report_video_comment&vcid='+cid+'&token='+token,  // 请求的 URL
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.status == "success") {
                $('.comment_card[name="'+cid+'"]').remove();
                alert('啊哥我错啦');
            } else {
                if (response.message == 'error_vcid') {alert('举报棍母');}
                else if (response.message == 'error_token') {alert('那我缺的登录这块谁来给我补啊');}
                else if (response.message == 'no_permission') {alert('等级达到DUE后可以举报');}
                else {alert('诶服务器怎么似了');}
            }
        },
        error: function(xhr, status, error) {
            //alert('诶服务器怎么似了');
        }
    });
}
//删除评论
function delete_comment(cid) {
    if (!confirm('确定删除')) {return;}
    var token = getCookie("token");
    if (token == "error") {alert('那我缺的登录这块谁来给我补啊');}
    $.ajax({
        url: 'https://api.ottohub.cn/?module=comment&action=delete_video_comment&vcid='+cid+'&token='+token,  // 请求的 URL
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.status == "success") {
                $('.comment_card[name="'+cid+'"]').remove();
                alert('只能阴间再见了');
            } else {
                if (response.message == 'error_vcid') {alert('删除棍母');}
                else if (response.message == 'error_token') {alert('那我缺的登录这块谁来给我补啊');}
                else if (response.message == 'no_permission') {alert('删的道理');}
                else {alert('诶服务器怎么似了');}
            }
        },
        error: function(xhr, status, error) {
            //alert('诶服务器怎么似了');
        }
    });
}
//制作评论卡片
function comment_card(cid, parent_cid, uid, content, time, child_comment_num, if_my_comment, username, honour, avatar_url) {
    var delete_btn_html = '';var honour_html = '';var child_html = '';var honours = honour.split(',');
    if (if_my_comment == 1) {delete_btn_html = '<div class="delete_btn waves-effect light-blue-text text-lighten-3" name="'+cid+'">删除</div>';}
    if (child_comment_num > 0) {child_html = '<div class="child_container comment_container grey lighten-3 black-text" name="'+cid+'" offset="0">查看'+child_comment_num+'条回复</div>'}
    else {child_html = '<div class="child_container_selected comment_container grey lighten-3 black-text" name="'+cid+'" offset="0"></div>'}
    honours.forEach(function(part) {
        if (part != '吉吉国民') {honour_html = honour_html + '<span class="badge waves-effect waves-light light-blue lighten-3 white-text z-depth-1">'+part+'</span>';}
    });
    return  `
<div class="comment_card" name="${cid}">
<div class="header">
<a href="/u/${uid}"><div class="comment_avatar"><img class="black circle" src="${avatar_url}"></div></a>
<div class="comment_title black-text">${username}${honour_html}</div>
<div class="comment_subtitle black-text">${timeShow(time)} OVC${cid}</div>
</div>
<div class="comment_content black-text">${content}</div>
<div class="footer">
${delete_btn_html}
<div class="report_btn waves-effect light-blue-text text-lighten-3" name="${cid}">举报</div>
<div class="comment_btn waves-effect light-blue-text text-lighten-3" name="${cid}" parent_cid="${parent_cid}" username="${username}">回复</div>
</div>
${child_html}
</div>
`;
}
//制作视频卡片
function video_card(vid, cover_url, title, like_count, favorite_count, view_count, time) {
    var subtitle = timeShow(time);
    var content = like_count+"点赞 • "+favorite_count+"收藏 • "+view_count+"播放";
    return  `
<div class="video_card waves-effect white" onclick="window.location.href='/v/${vid}'">
    <div class="video_cover"><img class="black" src="${cover_url}" alt="cover" referrerpolicy="no-referrer"></div>
    <div class="video_title black-text">${title.replace(/</g, "&lt;")}</div>
    <div class="video_subtitle grey-text">${subtitle.replace(/</g, "&lt;")}</div>
    <div class="video_content black-text">${content.replace(/</g, "&lt;")}</div>
</div>
`;
}