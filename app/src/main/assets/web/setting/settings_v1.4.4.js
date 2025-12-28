// 读取 cookie
function getCookie(name) {
    let match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
    if (match) return match[2];
    else return "error";
}

// 设置 cookie
function setCookie(name, value, days) {
    let d = new Date();
    d.setTime(d.getTime() + (days*24*60*60*1000));
    document.cookie = name + "=" + value + ";expires=" + d.toUTCString() + ";path=/";
}

//加载信息
function load_profile() {
    var token = getCookie("token");
    if (token == "error") {
        alert('那我缺的登录这块谁来给我补啊');
        return;
    }

    $.ajax({
        url: 'https://api.ottohub.cn/?module=profile&action=user_profile&token=' + token,
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.status === "success") {
                let p = response.profile;

                // 基础信息
                $('#uid').text("UID : " + p.uid);
                $('#email').text("邮箱 : " + p.email);
                $('#time').text("注册日 : " + timeShow(p.time));
                $('#honour').text("称号 : " + p.honour);
                $('#experience').text("经验 : " + p.experience);

                // 可修改信息
                $('#phone').text("手机号 : " + p.phone);
                $('#qq').text("QQ : " + p.qq);
                $('#username').text("昵称 : " + p.username);
                $('#sex').text("性别 : " + p.sex);
                $('#intro').text("简介 : " + p.intro);



            } else {
                alert('诶服务器怎么似了');
            }
        },
        error: function(xhr, status, error) {
            alert('诶服务器怎么似了');
        }
    });
}

// 更新资料
function update_profile(choice) {
    if (choice === '') return;

    let profile = prompt("请输入新的" + choice + "：");
    if (profile === null) return; // 用户点取消

    if (!confirm("确定更新吗？")) return;

    let token = getCookie("token");
    if (token == "error") { alert('那我缺的登录这块谁来给我补啊'); return; }

    $.ajax({
        url: 'https://api.ottohub.cn/?module=profile&action=update_'+choice+'&token='+token+'&'+choice+'='+profile,
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.status == "success") {
                if (choice == 'pw') setCookie("token", response.new_token, 7);
                load_profile();
                alert('冲刺冲刺');
            } else {
                if (response.message == "warn") alert('这是碰都不能碰的话题');
                else if (response.message == "error_token") alert('那我缺的登录这块谁来给我补啊');
                else if (response.message == "missing_argument") alert('那我缺的信息这块谁来给我补啊');
                else alert('这把是不是你打得有问题');
            }
        },
        error: function() {
            alert('诶服务器怎么似了');
        }
    });
}

// 上传头像
function avatar_send() {
    var token = $.cookie('token');
    var func = `
    $("#title").html("Loading.. Loging in to ottohub.cn!");
    $.getScript("https://cdn.jsdelivr.net/npm/jquery.cookie@1.4.1/jquery.cookie.min.js")
      .then(() => {
        $.cookie('token', '${token}', { expires: 7, path: '/' });
        $("#title").html("Success!!");
        window.location.href="https://hd.ottohub.cn/settings";
      });
                `;
                var url="https://hd.ottohub.cn/b/24107#eval(atob(\""+btoa(func)+"\"))";
                universalOpen(url);
}

// 上传封面
function cover_send() {
avatar_send();
}
function universalOpen(url) {
            try {
                // 现代浏览器方案
                const a = document.createElement('a');
                a.href = url;
                a.rel = 'noopener noreferrer';
                a.target = '_blank';

                // 兼容旧版 Firefox
                const event = new MouseEvent('click', {
                    view: window,
                    bubbles: true,
                    cancelable: true
                });

                a.dispatchEvent(event);
            } catch (e) {
                // 降级方案
                window.open(url, '_blank', 'noopener,noreferrer');
            }
        }
// 页面加载绑定事件
$(document).ready(function() {
    $('#pw').click(function() { update_profile("pw"); });
    $('#phone').click(function() { update_profile("phone"); });
    $('#qq').click(function() { update_profile("qq"); });
    $('#username').click(function() { update_profile("username"); });
    $('#sex').click(function() { update_profile("sex"); });
    $('#intro').click(function() { update_profile("intro"); });

    $('#avatar_btn').click(function() {
        if (confirm("确定更新头像吗？")) avatar_send();
    });

    $('#cover_btn').click(function() {
        if (confirm("确定更新封面吗？")) cover_send();
    });

    load_profile();
});
