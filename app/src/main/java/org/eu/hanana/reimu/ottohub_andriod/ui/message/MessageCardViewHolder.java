package org.eu.hanana.reimu.ottohub_andriod.ui.message;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;

public class MessageCardViewHolder extends RecyclerView.ViewHolder {
    public MessageCardViewHolder(@NonNull View itemView) {
        super(itemView);
        username=itemView.findViewById(R.id.tvAuthor);
        avatar=itemView.findViewById(R.id.ivAvatar);
        content=itemView.findViewById(R.id.tvContent);
        userinfo=itemView.findViewById(R.id.tvInfo);
    }
    public final TextView userinfo;
    public final TextView username;
    public final ImageView avatar;
    public final TextView content;
}
