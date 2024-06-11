package com.example.demo1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.demo1.util.ToastUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private Button mBtnLogin;
    private EditText mEtUser;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mBtnLogin = findViewById(R.id.btn_login);
        mEtUser = findViewById(R.id.username);
        mEtPassword = findViewById(R.id.password);

        mBtnLogin.setOnClickListener(this::onClick);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void onClick(View v) {
        String username = mEtUser.getText().toString();
        String password = mEtPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            ToastUtil.showMs(getApplicationContext(), "未输入账号或密码");
            return;
        }
//        Intent intent = null;
//        // 在主线程上启动 MainActivity
//        intent = new Intent(LoginActivity.this, MainActivity.class);
//        startActivity(intent);

//         构建一个新线程进行POST请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://122.51.210.27:8026/api/login";

                OkHttpClient client = new OkHttpClient.Builder()
                        .build();

                // 使用 JSON 格式构建请求体
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                RequestBody requestBody = RequestBody.create(JSON, json);

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                // 创建call回调对象
                final Call call = client.newCall(request);

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 请求失败处理
                        e.printStackTrace();
                        runOnUiThread(() -> ToastUtil.showMs(getApplicationContext(), "网络请求失败"));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 请求成功处理
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            runOnUiThread(() -> {
                                if (responseData.contains("\"success\":true")) {
                                    Intent intent = null;
                                    // 在主线程上启动 MainActivity
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                    ToastUtil.showMs(getApplicationContext(), "登录成功!");
                                } else {
                                    ToastUtil.showMs(getApplicationContext(), "用户名或密码错误");
                                }
                            });
                        } else {
                            runOnUiThread(() -> ToastUtil.showMs(getApplicationContext(), "登录失败"));
                        }
                    }
                });
            }
        }).start();
    }
}
