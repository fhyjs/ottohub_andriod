package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.IScrollTopChecker;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectionFragment extends BaseFragment implements IScrollTopChecker {

    private String collectionName;
    private int uid;
    private View view;
    private VideoListFragment fragment ;

    public CollectionFragment() {
        // Required empty public constructor
    }

    public static CollectionFragment newInstance(String collectionName,int uid) {
        CollectionFragment fragment = new CollectionFragment();
        fragment.collectionName = collectionName;
        fragment.uid=uid;
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("uid")) {
            uid=getArguments().getInt("uid");
        }
        if (getArguments() != null && getArguments().containsKey("collectionName")) {
            collectionName=getArguments().getString("collectionName");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_collection, container, false);
        this.view=inflate;
        ((TextView) view.findViewById(R.id.tvCollectionName)).setText(getString(R.string.collection)+" "+collectionName);
        this.fragment=VideoListFragment.newInstance(VideoListFragment.ACTION_COLLECTION);
        fragment.videosInRow=1;
        fragment.data=collectionName+"$:$"+uid;
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,(fragment) )
                .commit();
        return inflate;
    }

    @Override
    public boolean atTop() {
        return fragment.atTop();
    }
}