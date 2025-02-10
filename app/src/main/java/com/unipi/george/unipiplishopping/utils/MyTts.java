package com.unipi.george.unipiplishopping.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class MyTts {
    private static TextToSpeech tts;
    private static boolean isSpeaking = false;

    public static void speakOrPause(Context context, String text) {
        if (tts == null) {
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                    startSpeaking(text);
                } else {
                    Log.e("MyTts", "Initialization failed");
                }
            });
        } else {
            if (isSpeaking) {
                tts.stop();
                isSpeaking = false;
            } else {
                startSpeaking(text);
            }
        }
    }

    private static void startSpeaking(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        isSpeaking = true;
    }

/*    public static void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        isSpeaking = false;
    }*/
}
