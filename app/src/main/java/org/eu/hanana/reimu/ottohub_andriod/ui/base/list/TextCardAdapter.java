package org.eu.hanana.reimu.ottohub_andriod.ui.base.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;

import java.util.List;

public class TextCardAdapter extends CardAdapterBase<TextCard, TextCardViewHolder> {
    private static final String TAG = "TextCardAdapter";
    protected final TextListFragmentBase frag;

    public TextCardAdapter(List<TextCard> messageList, TextListFragmentBase textListFragmentBase) {
        super(messageList);
        this.frag=textListFragmentBase;
    }

    @Override
    public TextCardViewHolder createViewHolder(ViewGroup parent) {
        return new TextCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_card, parent, false));
    }

    @Override
    public void makeCardUi(final TextCardViewHolder holder,final TextCard object) {
        var ctx = holder.content.getContext();
        holder.content.setText(object.text);
    }
}
