package pers.wangdj.active.activity.demo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import pers.wangdj.active.R;
import pers.wangdj.active.activity.pub.BaseActivity;
import pers.wangdj.active.utils.Constants;
import pers.wangdj.active.utils.NotificationUtil;

/**
 * 项目名：  AndroidCommonProject
 * 包名：    pers.wangdj.active.activity
 * 文件名：  DemoActivity
 * 创建者：  wangdja
 * 创建时间：2018-01-17  10:18 上午
 * 描述：    演示样例
 */

public class DemoActivity extends BaseActivity implements View.OnClickListener {


    protected Button btnDemoButton;
    protected Button btnDemoButtonForResult;
    private String ivwNetMode = "0";
    // 语音唤醒对象
    public static VoiceWakeuper mIvw;
    private String keep_alive = "1";
    private int curThresh = 20;
    private int num = 0;
    private NotificationUtil notificationUtil;//通知 工具类
    private boolean isActive = false;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_layout);
        initView();
        initListener();
        initData();
        initBroadcast();
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(this, null);

        requestPermissions();

    }

    private void initBroadcast() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("notification_clicked_local");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }


    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initView() {
        btnDemoButton = findViewById(R.id.btn_demo_button);
        btnDemoButton.setOnClickListener(DemoActivity.this);
        btnDemoButtonForResult = findViewById(R.id.btn_demo_button_for_result);
        btnDemoButtonForResult.setOnClickListener(DemoActivity.this);


    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        btnDemoButton.setText("开启语音唤醒");
        btnDemoButtonForResult.setText("关闭语音唤醒");
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_demo_button) {

//            startActivity(actionStart("464655465555", "2641"));
            startWakeUper(true);

        } else if (view.getId() == R.id.btn_demo_button_for_result) {
//            startActivityForResult(actionStartForResult("464655465555", "2641"), Constants.requestCode350);
            shutDownWakeUper();

        }
    }

    Intent actionStart(String billno, String transtype) {
        Intent intent = new Intent(this, SecondDemoActivity.class);
        intent.putExtra("billno", billno);
        intent.putExtra("transtype", transtype);
        return intent;
    }

    Intent actionStartForResult(String billno, String transtype) {
        Intent intent = new Intent(this, SecondDemoActivity.class);
        intent.putExtra("billno", billno);
        intent.putExtra("transtype", transtype);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.requestCode350 && resultCode == Constants.resultCode340) {
            btnDemoButton.setText(data.getExtras().getString("billtype"));
        }
    }

    public void startWakeUper(boolean sendNotification) {
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
//			setRadioEnable(false);
//			resultString = "";
//			textView.setText(resultString);

            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );

            // 启动唤醒
            mIvw.startListening(mWakeuperListener);
            isActive = true;
            if(sendNotification){
                notificationUtil = new NotificationUtil(this);
                notificationUtil.sendNotification("语音唤醒","语音唤醒已开启，点击关闭");
            }
        } else {
            showToast("唤醒未初始化");
        }
    }

    private static final String TAG = "DemoActivity";

    //暂时关闭唤醒
    public void shutDownWakeUperForSomeTime() {
        mIvw.stopListening();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    do {
                        Thread.sleep(3000);
                        Log.d(TAG, "run:休眠 3s ");
                    } while (!validateMicAvailability());
                    startWakeUper(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void shutDownWakeUper(){
        mIvw.stopListening();
        isActive = false;
        notificationUtil = new NotificationUtil(this);
        notificationUtil.sendNotification("语音唤醒","语音唤醒已关闭，点击开启");
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {
        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult");
            if (!"1".equalsIgnoreCase(keep_alive)) {
            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                //关闭监听
                shutDownWakeUperForSomeTime();
                //唤醒成功
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.miui.voiceassist");
                if (intent != null) {
                    // 这里跟Activity传递参数一样的嘛，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
                    intent.putExtra("name", "Liu xiang");
                    intent.putExtra("birthday", "1983-7-13");
                    startActivity(intent);
                } else {
                    // 未安装应用
                    Toast.makeText(getApplicationContext(), "哟，赶紧下载安装这个APP吧", Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                showToast("结果解析出错");
                e.printStackTrace();
            }
        }

        @Override
        public void onError(SpeechError error) {
            showToast(error.getPlainDescription(true));
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
//        // 销毁合成对象
//        mIvw = VoiceWakeuper.getWakeuper();
//        if (mIvw != null) {
//            mIvw.destroy();
//        }
    }

    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(DemoActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.app_id) + ".jet");
        Log.d(TAG, "resPath: " + resPath);
        return resPath;
    }

    /**
     * 判断 麦克风是否被占用
     *
     * @return
     */
    private boolean validateMicAvailability() {
        Boolean available = true;
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try {
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                available = false;
            }
            recorder.startRecording();
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                recorder.stop();
                available = false;
            }
            recorder.stop();
        } catch (Exception e) {

        } finally {
            recorder.release();
            recorder = null;
        }
        Log.d("maikefeng", "麦克风可用？ : " + available);
        return available;
    }

    class LocalReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
//            showToast("收到了通知");
            if(isActive){
                //开启状态，关闭
                shutDownWakeUper();
            }else {
                startWakeUper(true);
            }
        }
    }

}
