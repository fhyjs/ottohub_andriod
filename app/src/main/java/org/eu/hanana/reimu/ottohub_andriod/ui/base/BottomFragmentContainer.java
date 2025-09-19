package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

import lombok.Setter;

public class BottomFragmentContainer extends BottomSheetDialogFragment {

    private Fragment contentFragment;
    private String title;
    @Setter
    private int backgroundColor = Color.WHITE;

    public BottomFragmentContainer(Fragment fragment, String title) {
        this.contentFragment = fragment;
        this.title = title;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 背景色
        view.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        // 设置标题
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(title);

        // 加载子 Fragment
        if (contentFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, contentFragment)
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            var shapeDrawable = AppCompatResources.getDrawable(getContext(),R.drawable.bottom_sheet_background);
            shapeDrawable.setTint((backgroundColor));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                shapeDrawable.setTintBlendMode(BlendMode.COLOR);
            }
            bottomSheet.setBackground(shapeDrawable);


            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

            behavior.setFitToContents(false);
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            behavior.setHalfExpandedRatio(0.5f);
            behavior.setExpandedOffset(0);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            behavior.setDraggable(true);

        }
    }
}
