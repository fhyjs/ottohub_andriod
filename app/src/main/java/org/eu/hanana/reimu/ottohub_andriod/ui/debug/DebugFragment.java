package org.eu.hanana.reimu.ottohub_andriod.ui.debug;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.TinkerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DebugFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DebugFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_PATCH = 10010;

    public DebugFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static DebugFragment newInstance() {
        DebugFragment fragment = new DebugFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug, container, false);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_PATCH && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // 注意：Tinker 需要文件路径，所以要拷贝到可访问路径
                    String patchPath = copyUriToFile(uri);
                    if (patchPath != null) {
                        TinkerInstaller.onReceiveUpgradePatch(getActivity().getApplicationContext(), patchPath);
                        Toast.makeText(getContext(), "Patch 已加载：" + patchPath, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private String copyUriToFile(Uri uri) {
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            File outFile = new File(getContext().getFilesDir(), "temp_patch.patch");
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "加载 Patch 出错", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), "TinkerPatchTest", Toast.LENGTH_SHORT).show();
        view.findViewById(R.id.btn_crash).setOnClickListener(v -> {
            throw new RuntimeException("Crash Test");
        });
        view.findViewById(R.id.btn_load_patch).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*"); // 或者 "application/octet-stream" 只选择补丁文件
            startActivityForResult(intent, REQUEST_CODE_PICK_PATCH);
        });
        if (TinkerManager.isTinkerEnabled(Tinker.with(requireContext()))) view.findViewById(R.id.btn_enable_tinker).setEnabled(false);
        view.findViewById(R.id.btn_enable_tinker).setOnClickListener(v -> {
            Throwable throwable = TinkerManager.enableTinker(Tinker.with(requireContext()));
            if (throwable == null) {
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
            }else {
                throwable.printStackTrace();
            }
        });

        ((MaterialSwitch) view.findViewById(R.id.switch_enable_bugrep)).setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("bug_rep",true));
        ((MaterialSwitch) view.findViewById(R.id.switch_enable_bugrep)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("bug_rep",isChecked).apply();
        });

        ((MaterialSwitch) view.findViewById(R.id.switch_rem_empty_collection)).setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("rem_empty_collection",true));
        ((MaterialSwitch) view.findViewById(R.id.switch_rem_empty_collection)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("rem_empty_collection",isChecked).apply();
        });

    }
}