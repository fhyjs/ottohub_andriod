package org.eu.hanana.reimu.ottohub_andriod.data.audit;

import org.eu.hanana.reimu.ottohub_andriod.MyAppApplicationLike;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuditTextViewModel extends TextViewModel {
    @Override
    public List<TextCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var frag = ((AuditFragment) videoListFragment);
        List<TextCard> result = new ArrayList<>();
        if (AuditFragment.TYPE_AVATAR.equals(frag.type)){
            var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getProfileApi().audit_avatar_list(12 * frag.currentPage, 12);
            ApiUtil.throwApiError(result1);
            result1.avatar_list.stream().forEach(data -> {
                var tc = new TextCard(data.username);
                tc.setExtra(data);
                result.add(tc) ;
            });
        }else if (AuditFragment.TYPE_COVER.equals(frag.type)){
            var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getProfileApi().audit_cover_list(12 * frag.currentPage, 12);
            ApiUtil.throwApiError(result1);
            result1.cover_list.stream().forEach(data -> {
                var tc = new TextCard(data.username);
                tc.setExtra(data);
                result.add(tc) ;
            });
        }else if (AuditFragment.TYPE_VIDEO.equals(frag.type)){
            var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getVideoApi().audit_video_list(12 * frag.currentPage, 12);
            ApiUtil.throwApiError(result1);
            result1.video_list.stream().forEach(data -> {
                var tc = new TextCard(data.title);
                tc.setExtra(data);
                result.add(tc) ;
            });
        }else if (AuditFragment.TYPE_BLOG.equals(frag.type)){
            var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getBlogApi().audit_blog_list(12 * frag.currentPage, 12);
            ApiUtil.throwApiError(result1);
            result1.blog_list.stream().forEach(data -> {
                var tc = new TextCard(data.title);
                tc.setExtra(data);
                result.add(tc) ;
            });
        }else if (AuditFragment.TYPE_COMMENT.equals(frag.type)){
            {
                var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getCommentApi().audit_blog_comment_list(12 * frag.currentPage, 12);
                ApiUtil.throwApiError(result1);
                result1.comment_list.stream().forEach(data -> {
                    var tc = new TextCard(data.content);
                    tc.setExtra(data);
                    result.add(tc) ;
                });
            }
            {
                var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getCommentApi().audit_video_comment_list(12 * frag.currentPage, 12);
                ApiUtil.throwApiError(result1);
                result1.comment_list.stream().forEach(data -> {
                    var tc = new TextCard(data.content);
                    tc.setExtra(data);
                    result.add(tc) ;
                });
            }
        }else if (AuditFragment.TYPE_DANMAKU.equals(frag.type)){

            var result1 = MyAppApplicationLike.getInstance().getOttohubApi().getDanmakuApi().audit_danmaku_list(12 * frag.currentPage, 12);
            ApiUtil.throwApiError(result1);
            result1.data.stream().forEach(data -> {
                var tc = new TextCard(data.text);
                tc.setExtra(data);
                result.add(tc) ;
            });

        }else{

        }
        if (result.isEmpty()){
            frag.hasMoreData=false;
        }
        return result;
    }
}
