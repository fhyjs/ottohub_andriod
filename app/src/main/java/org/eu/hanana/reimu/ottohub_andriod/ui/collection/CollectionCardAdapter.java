package org.eu.hanana.reimu.ottohub_andriod.ui.collection;

import android.os.Bundle;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.FragActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardViewHolder;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.message.SendMessageFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.CollectionFragment;

import java.util.List;

public class CollectionCardAdapter extends TextCardAdapter {
    private final CollectionListFragment fragment;

    public CollectionCardAdapter(List<TextCard> messageList, CollectionListFragment textListFragmentBase) {
        super(messageList, textListFragmentBase);
        this.fragment=textListFragmentBase;
    }

    @Override
    public void makeCardUi(TextCardViewHolder holder, TextCard object) {
        super.makeCardUi(holder, object);
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("uid", (Integer) object.extra);
            bundle.putString("collectionName",object.text);
            fragment.requireContext().startActivity(FragActivity.create(fragment.requireContext(), CollectionFragment.class,bundle,fragment.requireContext().getString(R.string.collection)+" "+object.text));
        });
    }
}
