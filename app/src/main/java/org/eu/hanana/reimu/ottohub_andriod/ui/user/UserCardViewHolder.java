package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;

public class UserCardViewHolder extends RecyclerView.ViewHolder {
    public final TextView username;
    public final TextView info;
    public final ImageView avatar;
    public UserCardViewHolder(@NonNull View itemView) {
        super(itemView);
        username=itemView.findViewById(R.id.tvAuthor);
        info=itemView.findViewById(R.id.tvInfo);
        avatar=itemView.findViewById(R.id.ivAvatar);
    }
}
