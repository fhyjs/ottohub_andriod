package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.databinding.ActivityLoginBinding;
import org.eu.hanana.reimu.ottohub_andriod.ui.login.LoggedInUserView;
import org.eu.hanana.reimu.ottohub_andriod.ui.login.LoginFormState;
import org.eu.hanana.reimu.ottohub_andriod.ui.login.LoginResult;
import org.eu.hanana.reimu.ottohub_andriod.ui.login.LoginViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.login.LoginViewModelFactory;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;

public class LoginActivity extends BaseActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private ProgressBar loadingProgressBar;
    private EditText passwordEditText;
    private EditText usernameEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

         usernameEditText = ((EditText) binding.username);
         passwordEditText = ((EditText) binding.password);
        final Button loginButton = binding.login;
        loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful

            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            login();
            return false;
        });

        loginButton.setOnClickListener(v -> {
           login();
        });
        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra(ForgotPasswordActivity.ACTION, ForgotPasswordActivity.ACTION_RESET);
            startActivity(intent);
        });
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra(ForgotPasswordActivity.ACTION, ForgotPasswordActivity.ACTION_REGISTER);
            startActivity(intent);
        });
    }
    public void login(){
        loadingProgressBar.setVisibility(View.VISIBLE);
        Thread thread = new Thread(() -> {
            loginViewModel.login(this,usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });
        thread.setUncaughtExceptionHandler((t,e)->{
            runOnUiThread(()->AlertUtil.showError(LoginActivity.this,"ERROR: "+e));
        });
        thread.start();
    }
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        AlertDialog alertDialog = AlertUtil.showMsg(this, getString(R.string.tip), getString(R.string.ok));
        alertDialog.setOnDismissListener(dialog -> {
            finish();
        });
        alertDialog.show();
    }

    private void showLoginFailed(Throwable errorString) {
        if (isFinishing() || isDestroyed()) return; // 避免 window 泄露

        Toast.makeText(getApplicationContext(), errorString.toString(), Toast.LENGTH_SHORT).show();
        AlertDialog alertDialog = AlertUtil.showMsg(this, getString(R.string.tip), errorString.toString());
        alertDialog.setOnDismissListener(dialog -> {
            finish();
        });
        alertDialog.show();
    }
}