package com.impecabel.photoup;

import android.content.Context;
import android.preference.Preference;
import android.preference.TwoStatePreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * A {@link Preference} that provides a two-state toggleable option.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 */
public class TogglePreference extends TwoStatePreference {
    public TogglePreference(Context context) {
        this(context, null);
    }

    public TogglePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TogglePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
