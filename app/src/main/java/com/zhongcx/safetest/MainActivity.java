package com.zhongcx.safetest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private TextView tv_main_screenshot;

    //手机允许/不允许截屏按钮
    private View.OnClickListener mScreenshotOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (tv_main_screenshot.getText().toString().equals("当前截屏/录屏状态：允许")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);//不允许截屏/录屏
                tv_main_screenshot.setText("当前截屏/录屏状态：拒绝");
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);//允许截屏/录屏
                tv_main_screenshot.setText("当前截屏/录屏状态：允许");
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 01 禁止【电脑模拟器】
        TextView tv_main_env = findViewById(R.id.tv_main_env);
        tv_main_env.setText("运行环境：" + (isEmulator() ? "模拟器" : "真机"));
        TextView tv_main_root = findViewById(R.id.tv_main_root);
        tv_main_root.setText("当前环境是否root：" + (checkRoot() ? "是" : "否"));

        // 02 禁止【手机截屏/录屏】
        tv_main_screenshot = findViewById(R.id.tv_main_screenshot);
        tv_main_screenshot.setText("当前截屏/录屏状态：允许");
        Button btn_main_screenshot = findViewById(R.id.btn_main_screenshot);
        btn_main_screenshot.setOnClickListener(mScreenshotOnClickListener);

        // 03 禁止
        Button btn_main_log = findViewById(R.id.btn_main_log);
        btn_main_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(MainActivity.this.getClass().getName(), "==测试");
            }
        });
    }


    /**
     * 根据bugly的实现判断root环境
     * @return true:是root环境，false:不是root环境
     */
    public boolean checkRoot() {
        boolean var0 = false;
        String[] var1 = new String[]{
                "/su",
                "/su/bin/su",
                "/sbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/data/local/su",
                "/system/xbin/su",
                "/system/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/system/bin/cufsdosck",
                "/system/xbin/cufsdosck",
                "/system/bin/cufsmgr",
                "/system/xbin/cufsmgr",
                "/system/bin/cufaevdd",
                "/system/xbin/cufaevdd",
                "/system/bin/conbb",
                "/system/xbin/conbb"};
        for (String var4 : var1) {
            if ((new File(var4)).exists()) {
                var0 = true;
                break;
            }
        }
        return Build.TAGS != null && Build.TAGS.contains("test-keys") || var0;
    }

    /**
     * 判断光传感器
     *
     * @return true是模拟器（存在光传感器），false不是模拟器（不存在光传感器）
     */
    public boolean isEmulator() {
        /*判断温度和压力传感器*/
        SensorManager sensorManager = (SensorManager) MainActivity.this.getSystemService(SENSOR_SERVICE);
        assert sensorManager != null;
        Sensor sensor8 = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //光
        return null == sensor8;
    }

}
