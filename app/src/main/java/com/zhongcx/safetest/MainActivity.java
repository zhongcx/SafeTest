package com.zhongcx.safetest;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hqumath.keyboard.KeyboardTool;
import com.hqumath.keyboard.KeyboardUtil;
import com.hqumath.keyboard.MyKeyboardView;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    private TextView tv_main_screenshot;
    private KeyboardUtil keyboardUtil;
    private EditText et_main_safe_key;
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

        // 01 禁止【电脑模拟器】测试
        TextView tv_main_env = findViewById(R.id.tv_main_env);
        tv_main_env.setText("运行环境：" + (isEmulator() ? "模拟器" : "真机"));
        TextView tv_main_root = findViewById(R.id.tv_main_root);
        tv_main_root.setText("当前环境是否root：" + (checkRoot() ? "是" : "否"));

        // 02 禁止【手机截屏/录屏】测试
        tv_main_screenshot = findViewById(R.id.tv_main_screenshot);
        tv_main_screenshot.setText("当前截屏/录屏状态：允许");
        Button btn_main_screenshot = findViewById(R.id.btn_main_screenshot);
        btn_main_screenshot.setOnClickListener(mScreenshotOnClickListener);

        // 03 禁止【release版本下打印输出日志】测试
        Button btn_main_log = findViewById(R.id.btn_main_log);
        btn_main_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BuildConfig.DEBUG){
                    Log.i(MainActivity.this.getClass().getName(), "==测试");
                }
            }
        });

        // 04 禁止【认证信息使用不安全键盘】测试
        et_main_safe_key = findViewById(R.id.et_main_safe_key);
        keyboardUtil = new KeyboardUtil(this, findViewById(R.id.ll_main_content));
        keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_ABC, false, et_main_safe_key);//安全键盘

        // 05 禁止【apk被篡改后二次打包】测试
        TextView tv_main_sha = findViewById(R.id.tv_main_sha);
        tv_main_sha.setText("当前sha值："+ checkApkSha());
    }

    /**
     * 点击空白处隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            KeyboardTool.isTouchView(null, ev);
            View v = getCurrentFocus();
            if (KeyboardTool.isFocusEditText(v)//当前焦点在系统键盘EditText
                    && !KeyboardTool.isTouchView(new View[]{}, ev)) {//且没有触摸在系统键盘EditText
                KeyboardTool.hideInputForce(MainActivity.this, v);//隐藏系统键盘
                v.clearFocus();//清空焦点
            } else if (KeyboardTool.isFocusEditText(v, et_main_safe_key)//当前焦点在自定义键盘EditText
                    && !KeyboardTool.isTouchView(new View[]{et_main_safe_key}, ev)) {//且没有触摸在自定义键盘EditText
                if (keyboardUtil != null)//隐藏自定义键盘
                    keyboardUtil.hide();
                v.clearFocus();//清空焦点
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 检查当前apk的sha值
     * 建议这里从服务器中获取哈希值然后进行对比校验
     * @return 当前apk的sha值,异常时为空字符串
     */
    public String checkApkSha() {
        String apkPath = getPackageCodePath();
        MessageDigest msgDigest;
        try {
            msgDigest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = new byte[1024];
            int byteCount;
            FileInputStream fis = new FileInputStream(new File(apkPath));
            while ((byteCount = fis.read(bytes)) > 0) {
                msgDigest.update(bytes, 0, byteCount);
            }
            BigInteger bi = new BigInteger(1, msgDigest.digest());
            String sha = bi.toString(16);
            fis.close();
            return sha;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据bugly的实现判断root环境
     *
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
