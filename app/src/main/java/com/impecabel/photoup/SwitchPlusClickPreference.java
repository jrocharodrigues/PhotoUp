package com.impecabel.photoup;

/**
 * Created by jrodrigues on 29-01-2015.
 */
import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Custom preference for handling a switch with a clickable preference area as well
 */
public class SwitchPlusClickPreference extends SwitchPreference {

    //
    // Public interface
    //

    /**
     * Sets listeners for the switch and the background container preference view cell
     * @param listener A valid SwitchPlusClickListener
     */
    public void setSwitchClickListener(SwitchPlusClickListener listener){
        this.listener = listener;
    }
    private SwitchPlusClickListener listener = null;

    /**
     * Interface gives callbacks in to both parts of the preference
     */
    public interface SwitchPlusClickListener {
        /**
         * Called when the switch is switched
         * @param buttonView
         * @param isChecked
         */
        public void onCheckedChanged(Switch buttonView, boolean isChecked);

        /**
         * Called when the preference view is clicked
         * @param view
         */
        public void onClick(View view);
    }

    public SwitchPlusClickPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwitchPlusClickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPlusClickPreference(Context context) {
        super(context);
    }


    //
    // Internal Functions
    //

    /**
     * Recursively go through view tree until we find an android.widget.Switch
     * @param view Root view to start searching
     * @return A Switch class or null
     */
    private Switch findSwitchWidget(View view){
        if (view instanceof  Switch){
            return (Switch)view;
        }
        if (view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup)view;
            for (int i = 0; i < viewGroup.getChildCount();i++){
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup){
                    Switch result = findSwitchWidget(child);
                    if (result!=null) return result;
                }
                if (child instanceof Switch){
                    return (Switch)child;
                }
            }
        }
        return null;
    }

    //Get a handle on the 2 parts of the switch preference and assign handlers to them
    @Override
    protected void onBindView (View view){
        super.onBindView(view);

        final Switch switchView = findSwitchWidget(view);
        if (switchView!=null){
            switchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* if (listener != null)
                        listener.onCheckedChanged((Switch) v, ((Switch)v).isChecked());*/
                }
            });
            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null)
                        listener.onCheckedChanged((Switch) buttonView, ((Switch)buttonView).isChecked());
                }
            });
            //switchView.setChecked(getSharedPreferences().getBoolean(getKey(),false));
            switchView.setFocusable(true);
            switchView.setEnabled(true);
            //Set the thumb drawable here if you need to. Seems like this code makes it not respect thumb_drawable in the xml.
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener!=null) listener.onClick(v);
            }
        });
    }
}
