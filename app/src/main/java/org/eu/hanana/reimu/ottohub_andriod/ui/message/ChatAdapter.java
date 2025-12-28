package org.eu.hanana.reimu.ottohub_andriod.ui.message;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

import org.eu.hanana.reimu.lib.ottohub.api.im.MessageResult;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardViewHolder;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

import java.util.List;

public class ChatAdapter extends TextCardAdapter {
    public ChatAdapter(List<TextCard> messageList, ChatFragment textListFragmentBase) {
        super(messageList, textListFragmentBase);
    }

    @Override
    public void makeCardUi(TextCardViewHolder holder, TextCard object) {
        super.makeCardUi(holder, object);

        var mcv= ((MaterialCardView) ((ViewGroup) holder.itemView).getChildAt(0));
        var llv= ((LinearLayout)(mcv.getChildAt(0)));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mcv.getLayoutParams();
        layoutParams.width=FrameLayout.LayoutParams.WRAP_CONTENT;
        llv.setBackgroundColor(0);
        if (object.extra instanceof MessageResult ext) {
            if (ApiUtil.getAppApi().getLoginResult().uid.equals(String.valueOf(ext.sender))) {
                //self msg
                layoutParams.gravity = Gravity.END;
                llv.setGravity(Gravity.END);
                // 添加半透明背景色（例如蓝色 50% 透明）
                int semiTransparentBlue = ThemeUtil.getTheme(frag.requireActivity()).getColorPrimary(); // 前两位 80 = 50% 透明
                int alpha = 128; // 0~255，128 = 50% 透明

                // 修改透明度
                semiTransparentBlue = (semiTransparentBlue & 0x00FFFFFF) | (alpha << 24);
                llv.setBackgroundColor(semiTransparentBlue);

            } else {
                // 对方发的 → 左对齐
                layoutParams.gravity = Gravity.START;
                llv.setGravity(Gravity.START);
            }
        }if (object.extra instanceof String ext) {
            layoutParams.gravity = Gravity.CENTER;
            llv.setGravity(Gravity.CENTER);
        }
        mcv.setLayoutParams(layoutParams);
    }
}
