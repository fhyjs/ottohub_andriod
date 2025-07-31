package org.eu.hanana.reimu.ottohub_andriod.ui.audit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.audit.AuditTextViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextListFragmentBase;

public class AuditFragment extends TextListFragmentBase<AuditTextViewModel> {
    public final static String TYPE = "type";
    public final static String TYPE_AVATAR = "a";
    public final static String TYPE_COVER = "c";
    public final static String TYPE_VIDEO = "v";
    public final static String TYPE_BLOG = "b";
    public final static String TYPE_COMMENT = "comment";
    public String type;
    public static AuditFragment newInstance(String type) {
        var frag = new AuditFragment();
        var data = new Bundle();
        data.putString(TYPE,type);
        frag.setArguments(data);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(TYPE);
        }
    }

    @Override
    public Class<AuditTextViewModel> getViewModelClass() {
        return AuditTextViewModel.class;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }
    @Override
    public int getSpanCount() {
        return 1;
    }

}
