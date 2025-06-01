package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;

import lombok.AllArgsConstructor;

public class ProfileUtil {
    //经验展示
    public static ExpObject exp_show(int exp) {
        if (exp < 50) {return ExpObject.ZERO;}
        else if (exp < 500) {return ExpObject.UNO;}
        else if (exp < 1000) {return ExpObject.DUE;}
        else if (exp < 3000) {return ExpObject.TRE;}
        else if (exp < 8000) {return ExpObject.QUATTRO;}
        else if (exp < 15000) {return ExpObject.CINQUE;}
        else if (exp < 30000) {return ExpObject.SEI;}
        else if (exp < 80000) {return ExpObject.SETTE;}
        else {return ExpObject.OTTO;}
    }
    public static Button makeButton(Context context, String text){
        MaterialButton button = new MaterialButton(context);
        // 设置宽高为 wrap_content
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        button.setLayoutParams(params);
        button.setText(text);
        return button;
    }
    @AllArgsConstructor
    public static class ExpObject{
        public static final ExpObject ZERO = new ExpObject(50,"ZERO",0xFFff8a80);
        public static final ExpObject UNO = new ExpObject(500,"UNO",0xFFffab40);
        public static final ExpObject DUE = new ExpObject(1000,"DUE",0xFFffff8d);
        public static final ExpObject TRE = new ExpObject(3000,"TRE",0xFF00e676);
        public static final ExpObject QUATTRO = new ExpObject(8000,"QUATTRO",0xFF84ffff);
        public static final ExpObject CINQUE = new ExpObject(15000,"CINQUE",0xFF2962ff);
        public static final ExpObject SEI = new ExpObject(30000,"SEI",0xFFea80fc);
        public static final ExpObject SETTE = new ExpObject(80000,"SETTE",0xFF455a64);
        public static final ExpObject OTTO = new ExpObject(-1,"OTTO",0xFF028760);
        public final int nextExp;
        public final String level;
        public final int color;
    }
}
