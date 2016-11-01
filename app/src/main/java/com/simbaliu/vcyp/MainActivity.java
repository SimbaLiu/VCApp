package com.simbaliu.vcyp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.simbaliu.vcyp.bean.TalkBackVo;
import com.simbaliu.vcyp.view.FloatView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.et_tts)
    EditText etTts;
    @BindView(R.id.btn_tts)
    Button btnTts;
    @BindView(R.id.btn_talk)
    Button btnTalk;
    @BindView(R.id.tv_talk_back)
    TextView tvTalkBack;
    @BindView(R.id.btn_show_float_view)
    Button btnShowFloatView;

    //语音合成器
    private SpeechSynthesizer mSynthesizer;

    private static Gson gson = new Gson();
    private List<TalkBackVo> talkBackVoList;
    private FloatView floatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        talkBackVoList = new ArrayList<TalkBackVo>();

        //语音初始化，在使用应用使用时需要初始化一次就好，如果没有这句会出现10111初始化失败
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=580f0e15");
        //处理语音合成关键类
        mSynthesizer = SpeechSynthesizer.createSynthesizer(this, mInitListener);
    }

    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.e("tag", "initListener init code = " + code);
        }
    };

    @OnClick({R.id.btn_tts, R.id.btn_talk, R.id.btn_show_float_view})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_tts:
                //设置发音人
                mSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
                //设置音调
                mSynthesizer.setParameter(SpeechConstant.PITCH, "50");
                //设置音量
                mSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
                String str = etTts.getText().toString();
                int code;
                if (TextUtils.isEmpty(str)) {
                    code = mSynthesizer.startSpeaking("你想要我说什么？", mTtsListener);
                } else {
                    code = mSynthesizer.startSpeaking(str, mTtsListener);
                }
                Log.e("tag", "mSynthesizer start code = " + code);
                break;
            case R.id.btn_talk:
                //语音识别dialog
                RecognizerDialog mDialog = new RecognizerDialog(this, mInitListener);
                //设置accent、language等参数
                mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

//                mDialog.setParameter("asr_sch","1");
//                mDialog.setParameter("nlp_version","2.0");

                mDialog.setListener(mRecognizerDialogListerer);
                mDialog.show();
                break;
            case R.id.btn_show_float_view:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        showFloatView();
                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(intent);
                    }
                } else {
                    showFloatView();
                }
                break;
        }
    }

    //语音合成器listener
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            if (speechError != null) {
                Log.e("tag", "speechError.getErrorCode() = " + speechError.getErrorCode());
            } else {
                Log.e("tag", "success???");
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    //语音识别器dialog的listener
    private RecognizerDialogListener mRecognizerDialogListerer = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            String jsonStr = new String(recognizerResult.getResultString());
            Type type = new TypeToken<TalkBackVo>() {
            }.getType();
            TalkBackVo talkBackVo = gson.fromJson(jsonStr, type);
            talkBackVoList.add(talkBackVo);
            Log.e("tag", recognizerResult.getResultString());
            String finalStr = "";
            //当是最后一句话时，才执行拼接字段操作
            if (isLast) {
                for (int i = 0; i < talkBackVoList.size(); i++) {
                    for (int j = 0; j < talkBackVoList.get(i).getWs().size(); j++) {
                        for (int k = 0; k < talkBackVoList.get(i).getWs().get(j).getCw().size(); k++) {
                            finalStr = finalStr + talkBackVoList.get(i).getWs().get(j).getCw().get(k).getW();
                        }
                    }
                }
                tvTalkBack.setText(finalStr);
            }
            if (finalStr.contains("点击")) {
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 1000;
                float x = 553;
                float y = 867;
                int metaState = 0;
                MotionEvent downEvent = MotionEvent.obtain(
                        downTime, downTime, MotionEvent.ACTION_DOWN, x, y, metaState);
                MotionEvent upEvent = MotionEvent.obtain(
                        eventTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState);
                Log.e("tag", "motionEvent.obtain()");
                getWindow().getDecorView().dispatchTouchEvent(downEvent);
                getWindow().getDecorView().dispatchTouchEvent(upEvent);
                Log.e("tag", "view");
                downEvent.recycle();
                upEvent.recycle();
                Log.e("tag", "motionEvent.recycle()");
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            if (speechError != null) {
                Log.e("tag", "speechError.getErrorCode() = " + speechError.getErrorCode());
            } else {
                Log.e("tag", "success???");
            }
        }
    };

    /**
     * 显示悬浮窗
     */
    public void showFloatView() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = new FloatView(getApplicationContext());
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = 150;
        params.height = 150;
        params.x = 0;
        params.y = 0;
        windowManager.addView(floatView, params);
        floatView.setOnClickListener(new FloatViewOnClickListener());
    }

    public class FloatViewOnClickListener implements View.OnClickListener {
        public void onClick(View view) {
            /**
             * 语音识别器
             * 第二个参数在线识别传null；本地识别时传InitListener
             */
            floatView.isSpeeking(true);
            SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(getApplicationContext(), null);
            mIat.setParameter(SpeechConstant.DOMAIN, "iat");
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
            mIat.startListening(mRecoListener);
        }
    }

    private RecognizerListener mRecoListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            String jsonStr = new String(recognizerResult.getResultString());
            Type type = new TypeToken<TalkBackVo>() {
            }.getType();
            TalkBackVo talkBackVo = gson.fromJson(jsonStr, type);
            talkBackVoList.add(talkBackVo);
            Log.e("tag", recognizerResult.getResultString());
            String finalStr = "";
            //当是最后一句话时，才执行拼接字段操作
            if (isLast) {
                for (int i = 0; i < talkBackVoList.size(); i++) {
                    for (int j = 0; j < talkBackVoList.get(i).getWs().size(); j++) {
                        for (int k = 0; k < talkBackVoList.get(i).getWs().get(j).getCw().size(); k++) {
                            finalStr = finalStr + talkBackVoList.get(i).getWs().get(j).getCw().get(k).getW();
                        }
                    }
                }
                tvTalkBack.setText(finalStr);
            }
            floatView.isSpeeking(false);
            if (finalStr.contains("点击")) {
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 1000;
                float x = 420;
                float y = 1370;
//                float x = 553;
//                float y = 867;
                int metaState = 0;
                MotionEvent downEvent = MotionEvent.obtain(
                        downTime, downTime, MotionEvent.ACTION_DOWN, x, y, metaState);
                MotionEvent upEvent = MotionEvent.obtain(
                        eventTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState);
                Log.e("tag", "motionEvent.obtain()");
                getWindow().getDecorView().dispatchTouchEvent(downEvent);
                getWindow().getDecorView().dispatchTouchEvent(upEvent);
                Log.e("tag", "view");
                downEvent.recycle();
                upEvent.recycle();
                Log.e("tag", "motionEvent.recycle()");
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            if (speechError != null) {
                Log.e("tag", "speechError.getErrorCode() = " + speechError.getErrorCode());
            } else {
                Log.e("tag", "success???");
            }
            floatView.isSpeeking(false);
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

}
