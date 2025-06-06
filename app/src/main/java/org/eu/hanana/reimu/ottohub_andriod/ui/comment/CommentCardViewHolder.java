package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
        content=itemView.findViewById(R.id.tvContent);
        reply=itemView.findViewById(R.id.btn_reply);
        report=itemView.findViewById(R.id.btn_report);
    }
    public final TextView username;
    public final TextView info;
    public final ImageView avatar;
    public final TextView content;
    public final Button reply;
    public final Button report;
}
