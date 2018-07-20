package com.example.android.rektext;

/**
 * Created by Arjun Vidyarthi on 19-Jul-18.
 */

public interface Classifier {
    String name();

    Classification recognize(final float[] pixels);
}
