package com.alipush;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.GcmRegister;
import com.alibaba.sdk.android.push.register.MeizuRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.alibaba.sdk.android.push.register.OppoRegister;
import com.alibaba.sdk.android.push.register.VivoRegister;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;

import com.alibaba.ha.adapter.AliHaAdapter;
import com.alibaba.ha.adapter.AliHaConfig;
import com.alibaba.ha.adapter.Plugin;

// import com.growingio.android.sdk.collection.Configuration;
// import com.growingio.android.sdk.collection.GrowingIO;

public class PushApplication extends Application {

    private static final String TAG = "Cordova Alipush PushApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // initGrowingIO(this);
            initManService(this);
            initHa(this);
            initPushService(this);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化云推送通道
     *
     * @param applicationContext
     */
    private void initPushService(final Context applicationContext) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = applicationContext.getPackageManager()
                .getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA);
        final String XiaoMiAppId = appInfo.metaData.get("XIAOMI_APPID").toString();
        final String XiaoMiAppKey = appInfo.metaData.get("XIAOMI_APPKEY").toString();
        final String OPPOAppKey = appInfo.metaData.get("OPPO_APPKEY").toString();
        final String OPPOAppSecret = appInfo.metaData.get("OPPO_SECRET").toString();
        final String VIVOAppId = appInfo.metaData.get("com.vivo.push.app_id").toString();
        final String VIVOAppKey = appInfo.metaData.get("com.vivo.push.api_key").toString();
        final String MeizuAppId = appInfo.metaData.get("MEIZU_APPID").toString();
        final String MeizuAppKey = appInfo.metaData.get("MEIZU_APPKEY").toString();
        final String NotificationChannelId = appInfo.metaData.get("NotificationChannelId").toString();

        // 创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) applicationContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id
            String id = NotificationChannelId;
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "Notification";
            // 用户可以看到的通知渠道的描述
            String description = "Push Notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[] { 100, 200, 300, 400, 500, 400, 300, 200, 400 });
            // mChannel.setSound(Uri.parse("android.resource://" + getPackageName() +
            // "/raw/qqqq"), Notification.AUDIO_ATTRIBUTES_DEFAULT);
            // 最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);

            // 设置8.0系统的通知小图标,必须要纯色的图
            // PushServiceFactory.getCloudPushService().setNotificationSmallIcon(R.drawable.notify);
        }

        PushServiceFactory.init(applicationContext);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.setLogLevel(CloudPushService.LOG_INFO);
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "init cloudchannel success");
                String deviceId = pushService.getDeviceId();
                Log.i(TAG, "deviceId " + deviceId);
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.i(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        // 华为通道
        HuaWeiRegister.register(this);
        // OPPO通道
        if (OPPOAppKey != null && OPPOAppKey.length() > 1 && OPPOAppSecret != null && OPPOAppSecret.length() > 1) {
            Log.i(TAG, "OPPO Push registered - OPPOAppKey:" + OPPOAppKey + " , OPPOAppSecret:" + OPPOAppSecret);
            OppoRegister.register(applicationContext, OPPOAppKey, OPPOAppSecret);
        }
        // 小米通道
        if (XiaoMiAppId != null && XiaoMiAppId.length() > 1 && XiaoMiAppKey != null && XiaoMiAppKey.length() > 1) {
            Log.i(TAG, "XiaoMi Push registered - XiaoMiAppId:" + XiaoMiAppId + " ,XiaoMiAppKey:" + XiaoMiAppKey);
            MiPushRegister.register(applicationContext, XiaoMiAppId, XiaoMiAppKey);
        }
        // 魅族通道
        if (MeizuAppId != null && MeizuAppId.length() > 1 && MeizuAppKey != null && MeizuAppKey.length() > 1) {
            Log.i(TAG, "OPPO Push registered - MeizuAppId:" + MeizuAppId + " , MeizuAppKey:" + MeizuAppKey);
            MeizuRegister.register(applicationContext, MeizuAppId, MeizuAppKey);
        }
        // VIVO通道
        if (VIVOAppId != null && VIVOAppId.length() > 1 && VIVOAppKey != null && VIVOAppKey.length() > 1) {
            Log.i(TAG, "VIVO Push registered - VIVOAppId:" + VIVOAppId + " , VIVOAppKey:" + VIVOAppKey);
            VivoRegister.register(applicationContext);
        }
    }

    /**
     * 初始化移动数据分析
     *
     * @param applicationContext
     */
    private void initManService(final Context applicationContext) throws PackageManager.NameNotFoundException {
        // 获取MAN服务
        MANService manService = MANServiceProvider.getService();
        // manService.getMANAnalytics().turnOnDebug();
        // manService.getMANAnalytics().turnOffCrashReporter();
        manService.getMANAnalytics().init(this, getApplicationContext());
    }

    /**
     * 初始化崩溃分析
     *
     * @param applicationContext
     */
    private void initHa(final Context applicationContext) throws PackageManager.NameNotFoundException {
        Log.e(TAG, "initHa");
        ApplicationInfo appInfo = applicationContext.getPackageManager()
                .getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA);
        final String AliAppKey = appInfo.metaData.get("com.alibaba.app.appkey").toString();
        final String AliAppSecret = appInfo.metaData.get("com.alibaba.app.appsecret").toString();
        final String versionName = applicationContext.getPackageManager()
                .getPackageInfo(applicationContext.getPackageName(), 0).versionName;
        if (AliAppKey != null && AliAppKey.length() > 1 && AliAppSecret != null && AliAppSecret.length() > 1) {
            AliHaConfig config = new AliHaConfig();
            config.appKey = AliAppKey; // appkey
            config.appSecret = AliAppSecret; // appsecret
            config.appVersion = versionName;
            config.channel = "mqc_all"; // 应用的渠道号标记，自定义
            config.userNick = null;
            config.application = this;
            config.context = getApplicationContext();
            config.isAliyunos = false; // 是否为yunos
            AliHaAdapter.getInstance().addPlugin(Plugin.crashreporter); // 崩溃分析，如不需要可注释掉
            // AliHaAdapter.getInstance().addPlugin(Plugin.apm); //性能监控，如不需要可注释掉
            // AliHaAdapter.getInstance().addPlugin(Plugin.tlog); //移动日志，如不需要可注释掉
            // AliHaAdapter.getInstance().openDebug(true); //调试日志开关
            AliHaAdapter.getInstance().start(config); // 启动
        }
    }

    /**
     * 初始化GrowingIO
     *
     * @param applicationContext
     */
    // private void initGrowingIO(final Context applicationContext) throws
    // PackageManager.NameNotFoundException {
    // GrowingIO.startWithConfiguration(this, new Configuration());
    // }
}
