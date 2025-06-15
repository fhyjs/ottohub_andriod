package org.eu.hanana.reimu.ottohub_andriod.data.message;

import static org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil.throwApiError;

import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentListResult;
import org.eu.hanana.reimu.lib.ottohub.api.im.MessageListResult;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.message.MessageListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageViewModel extends ListViewModelBase<MessageCard> {
    @Override
    public List<MessageCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var frag = ((MessageListFragment) videoListFragment);
        MessageListResult messageListResult=null;
        if (frag.getType().equals(MessageListFragment.TYPE_UNREAD)){
            messageListResult=ApiUtil.getAppApi().getMessageApi().unread_message_list(frag.currentPage*12,12);
        }else if (frag.getType().equals(MessageListFragment.TYPE_READ)){
            messageListResult=ApiUtil.getAppApi().getMessageApi().read_message_list(frag.currentPage*12,12);
        }else if (frag.getType().equals(MessageListFragment.TYPE_SENT)){
            messageListResult=ApiUtil.getAppApi().getMessageApi().sent_message_list(frag.currentPage*12,12);
        }

        var result = new ArrayList<MessageCard>();
        throwApiError(messageListResult);
        messageListResult.message_list.stream().forEach(messageResult -> {
            if (messageResult.sender_avatar_url!=null&&messageResult.sender_avatar_url.startsWith("/")) messageResult.sender_avatar_url="https://m.ottohub.cn"+messageResult.sender_avatar_url;
        });
        messageListResult.message_list.stream().map(comment -> new MessageCard(comment.msg_id,comment.sender,comment.content,comment.receiver_name,comment.sender_avatar_url).withRaw(comment)).forEach(result::add);

        if (result.isEmpty()){
            frag.hasMoreData=false;
        }
        return result;
    }
}
