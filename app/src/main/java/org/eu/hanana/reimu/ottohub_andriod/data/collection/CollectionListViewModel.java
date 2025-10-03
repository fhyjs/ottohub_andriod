package org.eu.hanana.reimu.ottohub_andriod.data.collection;

import androidx.preference.PreferenceManager;

import org.eu.hanana.reimu.lib.ottohub.api.collection.CollectionListResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.collection.CollectionListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.io.IOException;
import java.util.List;

public class CollectionListViewModel extends TextViewModel {
    @Override
    public List<TextCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var frag = ((CollectionListFragment) videoListFragment);
        videoListFragment.hasMoreData=false;
        CollectionListResult userVideoCollection = ApiUtil.getAppApi().getCollectionApi().get_user_video_collection(frag.uid);
        ApiUtil.throwApiError(userVideoCollection);

        return userVideoCollection.collection_list.stream()
                .filter(s -> !s.equals(frag.getString(R.string.none))|| !PreferenceManager.getDefaultSharedPreferences(frag.requireContext()).getBoolean("rem_empty_collection",true))
                .map(s -> {
                    TextCard textCard = new TextCard(s);
                    textCard.extra=frag.uid;
                    return textCard;
                }).toList();
    }
}
