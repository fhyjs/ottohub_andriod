package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;

public class CommentCardViewHolder extends RecyclerView.ViewHolder {
    public CommentCardViewHolder(@NonNull View itemView) {
        super(itemView);
        username=itemView.findViewById(R.id.tvAuthor);
        info=itemView.findViewById(R.id.tvInfo);
        avatar=itemView.findViewById(R.id.ivAvatar);
        llContent=itemView.findViewById(R.id.ll_content);
        reply=itemView.findViewById(R.id.btn_reply);
        report=itemView.findViewById(R.id.btn_report);
        userinfo=itemView.findViewById(R.id.ll_userinfo);
        showReply=itemView.findViewById(R.id.btn_show);
        delete=itemView.findViewById(R.id.btn_delete);
    }
    public final Button showReply;
    public final LinearLayout userinfo;
    public final TextView username;
    public final TextView info;
    public final ImageView avatar;
    public final LinearLayout llContent;
    public final Button reply;
    public final Button report;
    public final Button delete;
}
