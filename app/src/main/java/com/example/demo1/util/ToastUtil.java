package com.example.demo1.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast mToast;
    public static void showMs(Context context, String msg){
        if (mToast == null){
            mToast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        }else {
            mToast.setText(msg);
        }
        mToast.show();
    }
}
