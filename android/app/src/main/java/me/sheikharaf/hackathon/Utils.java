package me.sheikharaf.hackathon;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class Utils {
    public static View blink(View view, int duration, int offset) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(duration);
        anim.setStartOffset(offset);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
        return view;
    }
}
