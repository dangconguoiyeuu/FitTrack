package com.fitness.fittrack.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class VoiceCounter implements TextToSpeech.OnInitListener {
    private static final String[] DIGITS = {
            "kh\u00f4ng", "m\u1ed9t", "hai", "ba", "b\u1ed1n",
            "n\u0103m", "s\u00e1u", "b\u1ea3y", "t\u00e1m", "ch\u00edn"
    };

    private final TextToSpeech tts;
    private boolean ready = false;
    private long lastSpeakTime = 0L;

    public VoiceCounter(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) return;
        int result = tts.setLanguage(new Locale("vi", "VN"));
        ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
        tts.setSpeechRate(1.05f);
    }

    public boolean speakCount(String type, int count) {
        if ("running".equals(type)) return true;
        if (!ready || count <= 0) return false;

        long now = SystemClock.elapsedRealtime();
        if (now - lastSpeakTime < 450) return false;
        lastSpeakTime = now;

        String phrase = toVietnameseNumber(count);
        Bundle params = new Bundle();
        tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, params, "fittrack_count_" + count);
        return true;
    }

    public void shutdown() {
        tts.stop();
        tts.shutdown();
    }

    private static String toVietnameseNumber(int number) {
        if (number < 0) return "";
        if (number < 10) return DIGITS[number];
        if (number < 100) return belowHundred(number);
        if (number < 1000) return belowThousand(number);
        if (number < 10000) {
            int thousands = number / 1000;
            int rest = number % 1000;
            if (rest == 0) return DIGITS[thousands] + " ngh\u00ecn";
            return DIGITS[thousands] + " ngh\u00ecn " + belowThousand(rest);
        }
        return String.valueOf(number);
    }

    private static String belowThousand(int number) {
        int hundreds = number / 100;
        int rest = number % 100;
        String prefix = DIGITS[hundreds] + " tr\u0103m";
        if (rest == 0) return prefix;
        if (rest < 10) return prefix + " linh " + DIGITS[rest];
        return prefix + " " + belowHundred(rest);
    }

    private static String belowHundred(int number) {
        if (number < 10) return DIGITS[number];
        if (number < 20) {
            if (number == 10) return "m\u01b0\u1eddi";
            if (number == 15) return "m\u01b0\u1eddi l\u0103m";
            return "m\u01b0\u1eddi " + DIGITS[number % 10];
        }

        int tens = number / 10;
        int unit = number % 10;
        String result = DIGITS[tens] + " m\u01b0\u01a1i";
        if (unit == 0) return result;
        if (unit == 1) return result + " m\u1ed1t";
        if (unit == 5) return result + " l\u0103m";
        return result + " " + DIGITS[unit];
    }
}
