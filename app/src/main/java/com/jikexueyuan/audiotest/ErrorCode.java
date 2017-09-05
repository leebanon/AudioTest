package com.jikexueyuan.audiotest;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Administrator on 2017/9/4.
 */


public class ErrorCode {
    public final static int SUCCESS = 1000;
    public final static int E_NOSDCARD = 1001;
    public final static int E_STATE_RECODING = 1002;
    public final static int E_UNKOWN = 1003;


    public static String getErrorInfo(Context vContext, int vType) throws Resources.NotFoundException
    {
        switch(vType) {
            case SUCCESS:
                return "success";
            case E_NOSDCARD:
                return "没有SD卡，无法存储录音数据";
            case E_STATE_RECODING:
                return "正在录音中，请先停止录音";
            case E_UNKOWN:
            default:
                return "无法识别的错误";

        }
    }

}


