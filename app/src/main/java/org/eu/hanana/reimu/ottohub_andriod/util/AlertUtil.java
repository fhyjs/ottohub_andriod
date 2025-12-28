package org.eu.hanana.reimu.ottohub_andriod.util;

import static android.view.View.GONE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonParseException;

import org.eu.hanana.reimu.ottohub_andriod.R;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import lombok.AllArgsConstructor;

public class AlertUtil {
    public static AlertDialog showYesNo(Context context, String title, String msg, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no) {
        if (context==null) return null;
        return new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.conform,yes)
                .setNegativeButton(R.string.cancel,no)
                .create();
    }
    public static AlertDialog showMsg(Context context, String title, String msg) {
        if (context==null) return null;
        return new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
    public static AlertDialog showLoading(Context context, String title){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title).setCancelable(false); // 禁止返回键取消

        // 自定义布局
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        ProgressBar progressBar = new ProgressBar(context);
        layout.addView(progressBar);

        var loadingTextView = new TextView(context);
        loadingTextView.setText("");
        loadingTextView.setId(android.R.id.message);
        layout.addView(loadingTextView);

        builder.setView(layout);


        var loadingDialog = builder.create();
        loadingDialog.setCanceledOnTouchOutside(false); // 禁止点击外部取消
        return loadingDialog;
    }
    public static AlertDialog showError(Context context, String message) {
        if (context==null) return null;
        Log.d("ALERT",message);
        if (true) return showErrorWithThrowable(context,new UnknownError(message));;

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
        ThemeUtil.apply(view);
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
    public static AlertDialog showAdvanceErrorDialog(Context context,@NonNull String errTitle,@NonNull String errType,@NonNull String errMessage,@NonNull String errStack){
        if (context==null) return null;
        var dialog = LayoutInflater.from(context).inflate(R.layout.advance_error_window,null);
        var dialogB = new MaterialAlertDialogBuilder(context).setView(dialog).create();
        ((TextView) Objects.requireNonNull(dialog.findViewById(R.id.tv_title))).setText(errTitle);
        ((Button) Objects.requireNonNull(dialog.findViewById(R.id.btn_type))).setText(errType);
        ((TextView) Objects.requireNonNull(dialog.findViewById(R.id.tv_message))).setText(errMessage);
        ((TextView) Objects.requireNonNull(dialog.findViewById(R.id.tv_stack))).setText(errStack);
        dialog.findViewById(R.id.ll_detail).setVisibility(GONE);
        ((Button) Objects.requireNonNull(dialog.findViewById(R.id.btn_detail))).setOnClickListener(v -> {
            UiUtil.animateView(dialog.findViewById(R.id.ll_detail),dialog.findViewById(R.id.ll_detail).getVisibility()== GONE);
        });
        try {
            dialogB.show();
        }catch (Exception e){}
        ((Button) Objects.requireNonNull(dialog.findViewById(R.id.btn_confirm))).setOnClickListener(v -> dialogB.dismiss());
        return dialogB;
    }
    public static AlertDialog showErrorWithThrowable(Context activity, Throwable e){
        e.printStackTrace();
        if (activity==null) return null;
        var type = activity.getString(R.string.unknown_error);
        var title = activity.getString(R.string.error);
        var msg = e.getLocalizedMessage();
        if (e instanceof UnknownHostException){
            type=activity.getString(R.string.network_error);
            if(!ApiUtil.isNetworkConnected(activity)){
                title=activity.getString(R.string.no_network_avable);
            }
            msg=activity.getString(R.string.pls_check_network);
        }else if (e instanceof ApiException){
            type=activity.getString(R.string.operation_error);
            msg=e.getMessage();
        }else if (e instanceof JsonParseException){
            type=activity.getString(R.string.server_error);
            msg=activity.getString(R.string.server_error_msg);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(os);

        // 打印异常堆栈
        e.printStackTrace(pw);
        pw.flush(); // ⚠️ 必须 flush()，确保内容写入 ByteArrayOutputStream

        String finalType = type;
        String finalTitle = title;
        String finalMsg = msg;
        return AlertUtil.showAdvanceErrorDialog(activity, finalTitle, finalType, finalMsg,new String(os.toByteArray(), StandardCharsets.UTF_8));
    }
    public interface InputCallback {
        void onInput(String input);
    }
    @AllArgsConstructor
    public static class ThreadAlert implements Thread.UncaughtExceptionHandler{
        protected Activity activity;
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            activity.runOnUiThread(()->AlertUtil.showErrorWithThrowable(activity,e));
        }
    }
}
