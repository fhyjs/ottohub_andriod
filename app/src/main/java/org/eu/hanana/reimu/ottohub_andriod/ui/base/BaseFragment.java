package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

public class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ThemeUtil.onViewCreated(this);
    }
}
