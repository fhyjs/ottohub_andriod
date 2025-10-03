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

            if (frag.currentPage>20){
                result.add(new TextCard(frag.getString(R.string.ottohub)));
                result.add(new TextCard("HANANA\uD83E\uDD70 http://hanana2.link/"));
                result.add(new TextCard("als als als als! * "+frag.currentPage));
            }else {
                result.add(new TextCard(frag.getString(R.string.under_development)));
                result.add(new TextCard("als没做接口"));
                result.add(new TextCard("There's no api for this function!"));
            }
        }else{

        }
        if (result.isEmpty()){
            frag.hasMoreData=false;
        }
        return result;
    }
}
