/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eu.hanana.reimu.ottohub_andriod.util;

import static com.tencent.tinker.loader.shareutil.ShareConstants.TINKER_ENABLE_ALL;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.tencent.tinker.entry.ApplicationLike;
import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.patch.AbstractPatch;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.reporter.DefaultPatchReporter;
import com.tencent.tinker.lib.reporter.LoadReporter;
import com.tencent.tinker.lib.reporter.PatchReporter;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by zhangshaowen on 16/7/3.
 */
public class TinkerManager {
    private static final String TAG = "Tinker.TinkerManager";

    private static ApplicationLike                applicationLike;
    private static boolean isInstalled = false;

    public static void setTinkerApplicationLike(ApplicationLike appLike) {
        applicationLike = appLike;
    }

    public static ApplicationLike getTinkerApplicationLike() {
        return applicationLike;
    }

    public static void initFastCrashProtect(Context context) {
        CrashHandler.init(context);
    }

    public static void setUpgradeRetryEnable(boolean enable) {
        UpgradePatchRetry.getInstance(applicationLike.getApplication()).setRetryEnable(enable);
    }


    /**
     * all use default class, simply Tinker install method
     */
    public static void sampleInstallTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }
        TinkerInstaller.install(appLike);
        isInstalled = true;

    }
    public static boolean isTinkerEnabled(Tinker tinker){
        return tinker.isTinkerEnabled() && ShareTinkerInternals.isTinkerEnableWithSharedPreferences(tinker.getContext());
    }
    @Nullable
    public static Throwable enableTinker(Tinker tinker){
        try {
            Context context = tinker.getContext();

            // 获取 SharedPreferences
            SharedPreferences sp = context.getSharedPreferences(
                    ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG,
                    Context.MODE_MULTI_PROCESS
            );

            // 反射调用私有方法 getTinkerSwitchSPKey(Context)
            Method method = ShareTinkerInternals.class.getDeclaredMethod("getTinkerSwitchSPKey", Context.class);
            method.setAccessible(true);
            String keyName = (String) method.invoke(null, context);

            // 修改开关
            sp.edit().putBoolean(keyName, true).apply();

        } catch (Exception e) {
            return e;
        }

        return TinkerManager.setTinkerFlag(tinker, TINKER_ENABLE_ALL);
    }
    /**
     * @return error
     */
    @Nullable
    public static Throwable setTinkerFlag(Tinker tinker, @TypeTinkerFlag int tinkerFlags){
        try {
            Field tinkerFlags1 = Tinker.class.getDeclaredField("tinkerFlags");
            tinkerFlags1.setAccessible(true);
            tinkerFlags1.setInt(tinker,tinkerFlags);
        } catch (Exception e) {
            return  e;
        }
        return null;
    }
    /**
     * you can specify all class you want.
     * sometimes, you can only install tinker in some process you want!
     *
     * @param appLike
     */
    public static void installTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }
        //or you can just use DefaultLoadReporter
        LoadReporter loadReporter = new DefaultLoadReporter(appLike.getApplication());
        //or you can just use DefaultPatchReporter
        PatchReporter patchReporter = new DefaultPatchReporter(appLike.getApplication());
        //or you can just use DefaultPatchListener
        PatchListener patchListener = new DefaultPatchListener(appLike.getApplication());
        //you can set your own upgrade patch if you need
        AbstractPatch upgradePatchProcessor = new UpgradePatch();

        TinkerInstaller.install(appLike,
            loadReporter, patchReporter, patchListener,
                DefaultTinkerResultService.class, upgradePatchProcessor);

        isInstalled = true;
    }
}