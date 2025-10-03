package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.eu.hanana.reimu.ottohub_andriod.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentFragment extends BaseFragment {

    public Fragment fragment;

    public FragmentFragment() {
        // Required empty public constructor
    }


    public static FragmentFragment newInstance(Fragment fragment1) {
        FragmentFragment fragment = new FragmentFragment();
        fragment.fragment=fragment1;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}