package org.eu.hanana.reimu.ottohub_andriod.ui.collection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.data.collection.CollectionListViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextListFragmentBase;

import java.util.List;

public class CollectionListFragment extends TextListFragmentBase<CollectionListViewModel> {
    public int uid;

    @Override
    public Class<CollectionListViewModel> getViewModelClass() {
        return CollectionListViewModel.class;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }
    @Override
    public TextCardAdapter createAdapter(List<TextCard> list) {
        return new CollectionCardAdapter(list,this);
    }

    @Override
    public int getSpanCount() {
        return 1;
    }
}
