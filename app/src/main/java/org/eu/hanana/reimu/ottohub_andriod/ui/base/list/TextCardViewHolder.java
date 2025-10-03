package org.eu.hanana.reimu.ottohub_andriod.ui.base.list;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;

public class TextCardViewHolder extends RecyclerView.ViewHolder {
    public TextCardViewHolder(@NonNull View itemView) {
        super(itemView);
        content=itemView.findViewById(R.id.tvContent);
    }
    public final TextView content;
}
