package org.eu.hanana.reimu.ottohub_andriod.ui.message;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

public class SendMessageFragment extends Fragment {
    private static final String TAG = "SendMessageFragment";
    private EditText etReceiverUid;
    private MaterialButton btnSend;
    private EditText etContent;
    private TextInputLayout tilReceiverUid;
    private TextInputLayout tilContent;

    public SendMessageFragment() {
        // Required empty public constructor
    }


    public static SendMessageFragment newInstance() {
        SendMessageFragment fragment = new SendMessageFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
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
        return inflater.inflate(R.layout.fragment_send_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etReceiverUid = view.findViewById(R.id.etReceiverUid);
        btnSend = view.findViewById(R.id.btnSend);
        etContent = view.findViewById(R.id.etContent);
        tilReceiverUid = view.findViewById(R.id.tilReceiverUid);
        tilContent = view.findViewById(R.id.tilContent);

        btnSend.setOnClickListener(v -> {
            String uidText = etReceiverUid.getText() != null ? etReceiverUid.getText().toString().trim() : "";
            String contentText = etContent.getText() != null ? etContent.getText().toString().trim() : "";

            if (uidText.isEmpty()) {
                tilReceiverUid.setError(getText(R.string.noContent));
                return;
            } else {
                try {
                    Long.parseLong(uidText); // 确保是数字
                    tilReceiverUid.setError(null); // 清除错误
                    // TODO: 执行发送操作
                } catch (NumberFormatException e) {
                    tilReceiverUid.setError("请输入有效的数字");
                    return;
                }
            }

            if (contentText.isEmpty()) {
                tilContent.setError(getText(R.string.noContent));
                return;
            }
            if (contentText.length()>222) {
                tilContent.setError(getText(R.string.too_many_content));
                return;
            }
            try {
                this.send(Integer.parseInt(uidText),contentText);
            } catch (Exception e) {
                AlertUtil.showError(getContext(),e.toString()).show();
                Log.e(TAG, "error Sending message: ", e);
            }
        });
    }

    public void send(int uid, String content) {
        Thread thread = new Thread(() -> {
            EmptyResult emptyResult = ApiUtil.getAppApi().getMessageApi().send_message(uid, content);
            ApiUtil.throwApiError(emptyResult);
            getActivity().runOnUiThread(()->{
                etReceiverUid.setText("");
                etContent.setText("");
                AlertUtil.showMsg(getContext(),getString(R.string.ok),getString(R.string.sent_msg)).show();
            });
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
        thread.start();
    }
}