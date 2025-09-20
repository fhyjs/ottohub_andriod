package org.eu.hanana.reimu.ottohub_andriod.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtil {
    public static String replaceCommentEmoji(String raw){
        // 匹配 [xxx] 的正则
        Pattern pattern = Pattern.compile("\\[(.+?)]");
        Matcher matcher = pattern.matcher(raw);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1); // 拿到 xxx
            String replacement = "<img src=\"https://android_asset/web/emoji/" + name + ".png\" onerror=\"this.outerHTML='["+name+"]'\" />";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
