package com.arthurassuncao.vol_ldws.util;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;

/**
 * Created by arthur on 27/03/16.
 */
public abstract class Utilities {
    public static void beep(){
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        toneG.release();
    }
}
