package org.eu.hanana.reimu.ottohub_andriod.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;

import org.eu.hanana.reimu.ottohub_andriod.R;

import lombok.AllArgsConstructor;

public class AlertUtil {
    public static androidx.appcompat.app.AlertDialog showYesNo(Context context, String title, String msg, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no) {
        return new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok,yes)
                .setNegativeButton(R.string.no,no)
                .create();
    }
    public static androidx.appcompat.app.AlertDialog showMsg(Context context, String title, String msg) {
        return new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok,null)
                .create();
    }
    public static androidx.appcompat.app.AlertDialog showLoading(Context context, String title){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title).setCancelable(false); // 禁止返回键取消

        // 创建一个圆形进度条
        ProgressBar progressBar = new ProgressBar(context);
        builder.setView(progressBar);

        var loadingDialog = builder.create();
        loadingDialog.setCanceledOnTouchOutside(false); // 禁止点击外部取消
        return loadingDialog;
    }
    public static AlertDialog showError(Context context, String message) {

        // 加载自定义布局
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_error, null);

        // 绑定控件
        TextView tvMessage = view.findViewById(R.id.tv_message);
        Button btnOk = view.findViewById(R.id.btn_ok);
        tvMessage.setText(message);

        // 构建对话框
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(view);
        builder.setCancelable(false); // 禁止点击外部关闭

        var dialog = builder.create();

        // 设置窗口参数（可选）
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.8),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // 确认按钮点击
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        return dialog;
    }
    public static BottomSheetDialog showInput(Context context, InputCallback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input_bottom, null);

        EditText editText = view.findViewById(R.id.edit_input);
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            if (callback != null) {
                callback.onInput(editText.getText().toString());
            }
            dialog.dismiss();
        });

        dialog.setContentView(view);
        return dialog;
    }
    public interface InputCallback {
        void onInput(String input);
    }
    @AllArgsConstructor
    public static class ThreadAlert implements Thread.UncaughtExceptionHandler{
        protected Activity activity;
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            activity.runOnUiThread(()->AlertUtil.showMsg(activity,activity.getString(R.string.error),"ERROR:"+e+" at "+t.getName()).show());
        }
    }
}
