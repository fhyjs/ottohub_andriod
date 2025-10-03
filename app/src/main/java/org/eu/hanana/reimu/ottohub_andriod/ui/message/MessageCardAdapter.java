package org.eu.hanana.reimu.ottohub_andriod.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.MessageReaderActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.message.MessageCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;

import java.util.List;

public class MessageCardAdapter extends CardAdapterBase<MessageCard, MessageCardViewHolder> {
    private static final String TAG = "MessageCardAdapter";
    private final MessageListFragment frag;

    public MessageCardAdapter(List<MessageCard> messageList, MessageListFragment messageListFragment) {
        super(messageList);
        this.frag=messageListFragment;
    }

    @Override
    public MessageCardViewHolder createViewHolder(ViewGroup parent) {
        return new MessageCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_card, parent, false));
    }

    @Override
    public void makeCardUi(final MessageCardViewHolder holder,final MessageCard object) {
        var ctx = holder.avatar.getContext();
        holder.username.setText(frag.type.equals(MessageListFragment.TYPE_SENT)?("To "+object.receiver_name):object.getMessageResult().sender_name);
        holder.userinfo.setText(object.getMessageResult().time);
        holder.content.setText(object.getContent());
        Glide.with(ctx)
                .load(frag.type.equals(MessageListFragment.TYPE_SENT)?object.getMessageResult().receiver_avatar_url:object.sender_avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(holder.avatar);
        holder.itemView.setOnClickListener(v -> {
            var intent = new Intent(ctx, MessageReaderActivity.class);
            var data = new Bundle();
            data.putInt(MessageReaderActivity.ARG_MID,object.msg_id);
            intent.putExtras(data);
            ctx.startActivity(intent);
        });
    }
}
