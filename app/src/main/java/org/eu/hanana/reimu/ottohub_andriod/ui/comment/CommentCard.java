package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommentCard {
    public int cid;
    public String username,avatarUrl,info,content;
}
