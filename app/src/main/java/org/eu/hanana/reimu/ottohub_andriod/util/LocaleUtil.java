package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.preference.PreferenceManager;

import java.util.Locale;

public class LocaleUtil {
    public static Locale getLocale(Context context) {
        String ls = PreferenceManager.getDefaultSharedPreferences(context).getString("language", "sys");
        Locale locale;
        if (ls.equals("sys")){
            locale=Locale.getDefault();
        }else {
            locale=new Locale(ls);
        }
        return locale;
    }
    public static Context updateLocale(Context context) {
        return updateLocale(context, getLocale(context));
    }
    public static Context updateLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}