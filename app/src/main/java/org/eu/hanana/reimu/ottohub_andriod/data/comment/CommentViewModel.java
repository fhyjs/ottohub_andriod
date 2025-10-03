package org.eu.hanana.reimu.ottohub_andriod.data.comment;

import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentListResult;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.EmojiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommentViewModel extends ListViewModelBase<CommentCard> {
    @Override
    public List<CommentCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var commentFrag = ((CommentFragmentBase) videoListFragment);
        CommentListResult commentListResult;
        if (commentFrag.getType().equals(CommentFragmentBase.TYPE_VIDEO)){
            commentListResult = ApiUtil.getAppApi().getCommentApi().video_comment_list(commentFrag.getDataId(), commentFrag.getParent(), commentFrag.currentPage * 12, 12);
        }else {
            commentListResult = ApiUtil.getAppApi().getCommentApi().blog_comment_list(commentFrag.getDataId(),  commentFrag.getParent(), commentFrag.currentPage * 12, 12);
        }
        var textInfo = commentFrag.getType().equals(CommentFragmentBase.TYPE_VIDEO)?"ovc":"obc";
        var result = new ArrayList<CommentCard>();
        ApiUtil.throwApiError(commentListResult);
        commentListResult.comment_list.stream().map(commentResult -> {
            commentResult.content= EmojiUtil.replaceCommentEmoji(commentResult.content);
            return commentResult;
        }).map(comment -> new CommentCard(comment.getCid(),comment.getParentCid(),comment.username,comment.avatar_url,comment.time+" ("+textInfo+comment.getCid()+")",comment.content).withRaw(comment)).forEach(result::add);

        if (result.isEmpty()){
            commentFrag.hasMoreData=false;
        }
        return result;
    }
}
