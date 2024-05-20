package com.example.demo1;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SlideActivity extends AppCompatActivity {
    private TextView mqttStatusTextView;
    private TextView current;
    private TextView power;
    private TextView temperature;
    private EchartView lineChart;
    private Button btnOpen;
    private ScheduledExecutorService scheduler;
    private MqttClient client;
    private Handler handler;
    private String host = "tcp://122.51.210.27:1883";     // TCP协议
    private String userName = "cyh991103";
    private String passWord = "cyh991103";
    private String mqtt_id = "power";
    private String mqtt_sub_topic = "power/sys";
    private String mqtt_pub_topic = "power/sys";

    // 声明控件
    private TextView mHead;
    private SlideMenu slideMenu;
    private FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_slide);

        // 找到控件
        mHead = findViewById(R.id.top_title);
        slideMenu = findViewById(R.id.slideMenu);
        contentFrame = findViewById(R.id.content_frame);

        // menu部分控件
        TextView menuHome = findViewById(R.id.menu_home);
        TextView menuInfo = findViewById(R.id.menu_info);
        TextView menuMessage = findViewById(R.id.menu_message);
        TextView menuSetting = findViewById(R.id.menu_setting);
        TextView menuAbout = findViewById(R.id.menu_about);
        TextView menuExit = findViewById(R.id.menu_exit);


        // 创建MenuManager实例
        MenuManager menuManager = new MenuManager();

        // 添加菜单项到MenuManager
        menuManager.addMenuItem(menuHome);
        menuManager.addMenuItem(menuInfo);
        menuManager.addMenuItem(menuSetting);
        menuManager.addMenuItem(menuAbout);
        menuManager.addMenuItem(menuExit);
        menuManager.addMenuItem(menuMessage);

        // 侧滑部分
        mHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideMenu.switchMenu();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 侧边部分对应页面跳转
        menuHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchContent(R.layout.layout_main, SlideActivity.this::initHomePage);
            }
        });

        menuInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchContent(R.layout.layout_info, SlideActivity.this::initInfoPage);
            }
        });
        menuMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchContent(R.layout.layout_message, SlideActivity.this::initMessagePage);
            }
        });

        menuSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchContent(R.layout.layout_setting, SlideActivity.this::initSettingPage);
            }
        });

        menuAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchContent(R.layout.layout_about, SlideActivity.this::initAboutPage);
            }
        });

        menuExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 默认加载主界面
        switchContent(R.layout.layout_main, this::initHomePage);
    }



    // 动态切换布局的方法
    private void switchContent(int layoutId, Runnable initializer) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View newView = inflater.inflate(layoutId, null);
        contentFrame.removeAllViews();
        contentFrame.addView(newView);

        // 调用相应的初始化方法
        initializer.run();
    }

    // 初始化主页面
    private void initHomePage() {
        lineChart = findViewById(R.id.lineChart);
        btnOpen = findViewById(R.id.btn_open);
        mqttStatusTextView = findViewById(R.id.m_mqtt1);
        current = findViewById(R.id.current);
        power = findViewById(R.id.power);
        temperature = findViewById(R.id.temperature);

        // MQTT连接
        Mqtt_init();
        startReconnect();

        handler = new Handler(Looper.myLooper()) {
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 3: // MQTT 收到消息回传
                        Log.d("MQTT", "处理消息: " + msg.obj.toString());
                        String receivedMessage = msg.obj.toString();
                        parseJsonObj(receivedMessage); // 解析接收到的 JSON 数据并更新 UI
                        System.out.println(receivedMessage); // 显示MQTT数据
                        break;
                    case 30: // 连接失败
                        Toast.makeText(SlideActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        mqttStatusTextView.setText("未连接");
                        break;
                    case 31: // 连接成功
                        mqttStatusTextView.setText("已连接");
                        Log.d("MQTT", "已连接");

                        try {
                            client.subscribe(mqtt_sub_topic, 1);
                            Log.d("MQTT", "订阅主题成功: " + mqtt_sub_topic);
                        } catch (MqttException e) {
                            Log.e("MQTT", "订阅主题失败: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        // 加载图表数据
        loadChartData();

        // 设置按钮点击事件监听器
        btnOpen.setOnClickListener(v -> {
            if (btnOpen.getText().equals(getString(R.string.Open))) {
                showConfirmationDialog("开闸操作", "你确定要开闸吗？", this::openGate);
            } else {
                showConfirmationDialog("合闸操作", "你确定要合闸吗？请等待工作人员手工合闸", this::closeGate);
            }
        });
    }

    // 初始化个人信息页面
    private void initInfoPage() {
        EditText nameEditText = findViewById(R.id.info_name);
        EditText emailEditText = findViewById(R.id.info_email);
        Button saveButton = findViewById(R.id.info_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                // 保存个人信息的逻辑
            }
        });
    }
    private void initMessagePage() {
    }
    // 初始化设置页面
    private void initSettingPage() {
        // 在这里初始化设置页面的控件和逻辑
    }

    // 初始化关于页面
    private void initAboutPage() {
        // 在这里初始化关于页面的控件和逻辑
    }

    // MQTT初始化
    private void Mqtt_init() {
        try {
            // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id, new MemoryPersistence());
            // MQTT的连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d("MQTT", "连接丢失: " + cause.getMessage());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d("MQTT", "消息传递完成：" + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    String messageContent = new String(message.getPayload());
                    Log.d("MQTT", "消息已到达：Topic: " + topicName + " 内容: " + messageContent);
                    Message msg = new Message();
                    msg.what = 3; // 收到消息标志位
                    msg.obj = messageContent;
                    handler.sendMessage(msg); // 通过 handler 发送消息
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MQTT连接
    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!(client.isConnected())) {
                        MqttConnectOptions options = null;
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    // MQTT重连函数
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    // 订阅函数
    private void publishMessagePlus(String topic, String message2) {
        if (client == null || !client.isConnected()) {
            Log.d("MQTT", "MQTT客户端未连接");
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic, message);
            Log.d("MQTT", "消息发布成功: " + message2);
        } catch (MqttException e) {
            Log.e("MQTT", "消息发布失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadChartData() {
        lineChart.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 在h5页面加载完毕后加载数据
                refreshLineChart();
            }
        });
    }

    private void openGate() {
        // 开闸的实际操作代码
        publishMessagePlus(mqtt_pub_topic, "开闸");

        // 切换按钮的文本和背景颜色
        btnOpen.setText(R.string.Close);
        btnOpen.setBackgroundResource(R.drawable.but_2);
    }

    private void closeGate() {
        // 关闸的实际操作代码
        publishMessagePlus(mqtt_pub_topic, "人工确认合闸指令");

        // 切换按钮的文本和背景颜色
        btnOpen.setText(R.string.Open);
        btnOpen.setBackgroundResource(R.drawable.but_1);
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确认", (dialog, which) -> onConfirm.run())
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void refreshLineChart() {
        Object[] x = new Object[]{
                "周一", "周二", "周三", "周四", "周五", "周六", "周日"
        };

        Object[] y = generateRandomData();
        Object[] y2 = generateRandomData();
        Object[] y3 = generateRandomData();
        Object[] y4 = generateRandomData();
        Object[] y5 = generateRandomData();
        Object[] y6 = generateRandomData();
        Object[] y7 = new Object[7];

        // 计算y7
        for (int i = 0; i < 7; i++) {
            y7[i] = (int) y[i] + (int) y2[i] + (int) y3[i] + (int) y4[i] + (int) y5[i] + (int) y6[i];
        }

        lineChart.refreshEchartsWithOption(EchartOptionUtil.getLineChartOptions(x, y, y2, y3, y4, y5, y6, y7));
    }

    private Object[] generateRandomData() {
        Object[] data = new Object[7];
        for (int i = 0; i < 7; i++) {
            data[i] = (int) (Math.random() * 4 + 1);
        }
        return data;
    }

    // Json数据解析
    private void parseJsonObj(String jsonObj) {
        // 解析json
        try {
            JSONObject jsonObject = new JSONObject(jsonObj);
            String DL = jsonObject.getString("DL");
            String GL = jsonObject.getString("GL");
            String WD = jsonObject.getString("WD");

            //设置文本内容
            current.setText(DL + "v");
            power.setText(GL + "w");
            temperature.setText(WD + "℃");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
