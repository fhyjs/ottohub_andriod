package org.eu.hanana.reimu.ottohub_andriod.data.login;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import org.eu.hanana.reimu.lib.ottohub.api.auth.LoginResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.login.model.LoggedInUser;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.SharedPreferencesKeys;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            LoginResult login = ApiUtil.login(username, password);
            if (!login.isSuccess()){
                var msg = login.getMessage();
                if (msg.contains("error_password")){
                    msg=MyApp.getInstance().getString(R.string.error_password);
                }
                throw new IllegalStateException(msg);
            }
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            Integer.parseInt(login.uid),
                            login.uid);
            //throw new IOException();
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(e);
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}