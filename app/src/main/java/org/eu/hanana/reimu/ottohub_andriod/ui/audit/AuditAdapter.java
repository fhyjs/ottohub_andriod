package org.eu.hanana.reimu.ottohub_andriod.ui.audit;



import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_AUDIT;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_BLOG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardViewHolder;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

public class AuditAdapter extends TextCardAdapter {
    public static final String ARG_TYPE = "type";
    public static final String ARG_RESULT = "result";
    public static final String ARG_TARGET = "target";
    private ActivityResultLauncher<Intent> launcher;
    public AuditAdapter(List<TextCard> messageList, AuditFragment textListFragmentBase) {
        super(messageList, textListFragmentBase);

        // 注册回调
        launcher = getFrag().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            performAction(data.getBooleanExtra(ARG_RESULT, false),data.getIntExtra(ARG_TARGET,0),data.getStringExtra(ARG_TYPE));
                        }
                    }
                }
        );
    }

    private void performAction(boolean pass, int id, String type) {
        Thread thread = new Thread(() -> {
            if (type.equals(TYPE_BLOG)){
                if (pass){
                    ApiUtil.getAppApi().getModerationApi().approve_blog(id);
                }else {
                    ApiUtil.getAppApi().getModerationApi().reject_blog(id);
                }
            }
            frag.refresh();
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getFrag().getActivity()));
        thread.start();
    }

    protected AuditFragment getFrag(){
        return (AuditFragment) frag;
    }
    @Override
    public void makeCardUi(TextCardViewHolder holder, TextCard object) {
        super.makeCardUi(holder, object);
        var ctx = holder.content.getContext();
        if (getFrag().type.equals(TYPE_BLOG)){
            var extra = ((BlogResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, BlogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(BlogActivity.KEY_BID,extra.bid);
                bundle.putString(BlogActivity.KEY_DATA,new Gson().toJson(extra));
                bundle.putString(BlogActivity.KEY_TYPE,TYPE_AUDIT);
                intent.putExtras(bundle);
                launcher.launch(intent);
            });
        }
    }
}
