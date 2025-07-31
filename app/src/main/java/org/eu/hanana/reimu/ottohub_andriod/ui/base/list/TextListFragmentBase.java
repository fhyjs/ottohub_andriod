package org.eu.hanana.reimu.ottohub_andriod.ui.base.list;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;

import java.util.List;

public abstract class TextListFragmentBase<T extends TextViewModel> extends ListFragmentBase<TextCardAdapter,TextCardViewHolder, TextCard, T> {
    @Override
    protected void registerMenuProviders() {

    }
    @Override
    public RecyclerView findRecyclerView(View view) {
        return view.findViewById(R.id.recyclerView);
    }

    @Override
    public TextCardAdapter createAdapter(List<TextCard> list) {
        return new TextCardAdapter(list,this);
    }
}
