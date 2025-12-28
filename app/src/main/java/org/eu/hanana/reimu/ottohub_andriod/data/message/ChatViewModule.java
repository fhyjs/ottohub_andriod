package org.eu.hanana.reimu.ottohub_andriod.data.message;

import org.eu.hanana.reimu.lib.ottohub.api.im.MessageListResult;
import org.eu.hanana.reimu.lib.ottohub.api.im.MessageResult;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.message.ChatFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatViewModule extends TextViewModel {
    @Override
    public List<TextCard> fetchFromNetwork(ListFragmentBase cFragment) throws IOException {
        var chatFrag = ((ChatFragment) cFragment);
        MessageListResult messageListResult = ApiUtil.getAppApi().getMessageApi().friend_message(chatFrag.data.uid, chatFrag.currentPage * 12, 12, true);
        ApiUtil.throwApiError(messageListResult);
        //Collections.reverse(messageListResult.message_list);
        List<TextCard> cards = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        int date = 0;
        for (MessageResult messageResult : messageListResult.message_list) {
            // 解析时间字符串
            Date currentDate= new Date();
            try {
                currentDate = sdf.parse(messageResult.time); // 假设 msg.timeStr = "2025-12-28 12:38:31"
            }catch (Exception ignored){}
            if (currentDate!=null&&currentDate.getDate()!=date){
                date=currentDate.getDate();
                TextCard e = new TextCard(messageResult.time);
                e.extra="time";
                cards.add(e);
            }
            TextCard textCard = new TextCard(messageResult.content);
            textCard.extra=messageResult;
            cards.add(textCard);
        }
        return cards;
    }
}
