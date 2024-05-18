package com.example.demo1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.demo1.util.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    // 声明控件
    private Button mBtnLogin;
    private EditText mEtUser;
    private EditText mEtPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // 找到控件
        mBtnLogin = findViewById(R.id.btn_login);
        mEtUser = findViewById(R.id.username);
        mEtPassword = findViewById(R.id.password);

        // 跳转
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
        // 弹出内容设置
        String ok = "登录成功!";
        String fail = "密码或者账号有误";
        String not_input = "未输入账号或密码";
        Intent intent = null;
        // 假设正确的账号和密码分别是cyh, 123456
        if (username.equals("cyh") && password.equals("123456")) {
            //正确则进行跳转
            intent = new Intent(LoginActivity.this, SlideActivity.class);
            startActivity(intent);
            ToastUtil.showMs(getApplicationContext(), ok);
        } else {
            if (username.isEmpty() || password.isEmpty()) {
                ToastUtil.showMs(getApplicationContext(), not_input);
            }else {
                // 不正确
                ToastUtil.showMs(getApplicationContext(), fail);
            }
        }
    }
}