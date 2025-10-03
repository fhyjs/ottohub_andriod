package org.eu.hanana.reimu.ottohub_andriod;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.entry.DefaultApplicationLike;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.ottohub_andriod.util.TinkerManager;

import lombok.Getter;

@DefaultLifeCycle(application = "org.eu.hanana.reimu.ottohub_andriod.MyApp",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
// MyApp.java
public class MyAppApplicationLike extends DefaultApplicationLike  {
    @Getter
    private static MyAppApplicationLike instance;
    @Getter
    private OttohubApi ottohubApi;

    public MyAppApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //RePlugin.App.onCreate();
        instance = this;
        ottohubApi=new OttohubApi();
        TinkerManager.initFastCrashProtect(getApplication());
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        TinkerManager.setTinkerApplicationLike(this);

        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true);

        //optional set logIml, or you can use default debug log
        //TinkerInstaller.setLogIml(new MyLogImp());

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(this);
        Tinker tinker = Tinker.with(getApplication());
        Log.d("AppLike", "onBaseContextAttached: tinker loaded");
    }
}