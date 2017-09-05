package com.jikexueyuan.audiotest;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class SecondActivity extends Activity {
    private final static int FLAG_WAV = 0;
    private int mState = -1;    //-1:没再录制，0：录制wav
    private Button btn_record_wav;
    private Button btn_stop;
    private Button btn_play;
    private TextView txt;
    private UIHandler uiHandler;
    private UIThread uiThread;
    MediaPlayer mMediaPlayer;//播放声音

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);
        setPlayer();
        findViewByIds();
        setListeners();
        init();
    }

    //mediaPlayer的create必须在oncreate里
    private void setPlayer() {
        Uri playUri = Uri.parse(AudioFileFunc.getWavFilePath());//获取wav文件路径
        mMediaPlayer = MediaPlayer.create(this, playUri);
    }

    private void findViewByIds() {
        btn_record_wav = (Button) this.findViewById(R.id.start);
        btn_stop = (Button) this.findViewById(R.id.stop);
        txt = (TextView) this.findViewById(R.id.textView1);
        btn_play = (Button) this.findViewById(R.id.play);
    }

    private void setListeners() {
        btn_record_wav.setOnClickListener(btn_record_wav_clickListener);
        btn_stop.setOnClickListener(btn_stop_clickListener);
        btn_play.setOnClickListener(btn_play_clickListener);
    }

    private void init() {
        uiHandler = new UIHandler();
    }

    private Button.OnClickListener btn_record_wav_clickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            record(FLAG_WAV);
        }
    };
    private Button.OnClickListener btn_stop_clickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            stop();
        }
    };

    private Button.OnClickListener btn_play_clickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            play();
        }
    };

    /**
     * 开始录音
     *
     * @param mFlag，0：录制wav格式，1：录音amr格式
     */
    private void record(int mFlag) {
        if (mState != -1) {
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_RECORDFAIL);
            b.putInt("msg", ErrorCode.E_STATE_RECODING);
            msg.setData(b);

            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            return;
        }
        int mResult = -1;
        AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
        mResult = mRecord_1.startRecordAndFile();
        if (mResult == ErrorCode.SUCCESS) {
            uiThread = new UIThread();
            new Thread(uiThread).start();
            mState = mFlag;
        } else {
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_RECORDFAIL);
            b.putInt("msg", mResult);
            msg.setData(b);

            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
        }
    }

    /**
     * 停止录音
     */
    private void stop() {
        if (mState != -1) {
            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
            mRecord_1.stopRecordAndFile();
            if (uiThread != null) {
                uiThread.stopThread();
            }
            if (uiHandler != null)
                uiHandler.removeCallbacks(uiThread);
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_STOP);
            b.putInt("msg", mState);
            msg.setData(b);
            uiHandler.sendMessageDelayed(msg, 1000); // 向Handler发送消息,更新UI
            mState = -1;
        }
    }

    /**
     * 播放录音
     */
    private void play() {
        if (mState != -1) {

            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_PLAYFAIL);
            b.putInt("msg", mState);
            msg.setData(b);
            uiHandler.sendMessageDelayed(msg, 1000); // 向Handler发送消息,更新UI
            mState = -1;
        } else {
            if (AudioFileFunc.getWavFilePath() != "") {
                if (mMediaPlayer != null)
                    mMediaPlayer.stop();
                try {
                    mMediaPlayer.prepare();
                } catch (IllegalStateException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
                mMediaPlayer.start();


            } else {
                Log.d("play", "找不到录音文件！！！");
            }
        }
    }

    private final static int CMD_RECORDING_TIME = 2000;
    private final static int CMD_RECORDFAIL = 2001;
    private final static int CMD_STOP = 2002;
    private final static int CMD_PLAYFAIL = 2003;

    class UIHandler extends Handler {
        public UIHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            switch (vCmd) {
                case CMD_RECORDING_TIME:
                    int vTime = b.getInt("msg");
                    SecondActivity.this.txt.setText("正在录音中，已录制：" + vTime + " s");
                    break;
                case CMD_RECORDFAIL:
                    int vErrorCode = b.getInt("msg");
                    String vMsg = ErrorCode.getErrorInfo(SecondActivity.this, vErrorCode);
                    SecondActivity.this.txt.setText("录音失败：" + vMsg);
                    break;
                case CMD_STOP:
                    int vFileType = b.getInt("msg");
                    switch (vFileType) {
                        case FLAG_WAV:
                            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                            long mSize = mRecord_1.getRecordFileSize();
                            SecondActivity.this.txt.setText("录音已停止.录音文件:" + AudioFileFunc.getWavFilePath() + "\n文件大小：" + mSize);
                            break;
                    }
                    break;
                case CMD_PLAYFAIL:
                    SecondActivity.this.txt.setText("请先录完音！");
                    break;
                default:
                    break;
            }
        }
    }

    ;

    class UIThread implements Runnable {
        int mTimeMill = 0;
        boolean vRun = true;

        public void stopThread() {
            vRun = false;
        }

        @Override
        public void run() {
            while (vRun) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mTimeMill++;
                Log.d("thread", "mThread........" + mTimeMill);
                Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                b.putInt("cmd", CMD_RECORDING_TIME);
                b.putInt("msg", mTimeMill);
                msg.setData(b);

                SecondActivity.this.uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            }

        }
    }
}
