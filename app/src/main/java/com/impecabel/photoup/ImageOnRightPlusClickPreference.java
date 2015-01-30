package com.impecabel.photoup;

/**
 * Created by jrodrigues on 29-01-2015.
 */
import android.content.Context;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

/**
 * Custom preference for handling a switch with a clickable preference area as well
 */
public class ImageOnRightPlusClickPreference extends Preference {

    //
    // Public interface
    //

    /**
     * Sets listeners for the switch and the background container preference view cell
     * @param listener A valid SwitchPlusClickListener
     */
    public void setImageClickListener(ImageOnRightPlusClickListener listener){
        this.listener = listener;
    }
    private ImageOnRightPlusClickListener listener = null;

    /**
     * Interface gives callbacks in to both parts of the preference
     */
    public interface ImageOnRightPlusClickListener {
        /**
         * Called when the switch is switched
         * @param view
         */
        public void onImageClick(View view);

        /**
         * Called when the preference view is clicked
         * @param view
         */
        public void onClick(View view);
    }

    public ImageOnRightPlusClickPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageOnRightPlusClickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageOnRightPlusClickPreference(Context context) {
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
    private ImageView findImageWidget(View view){
        if (view instanceof ImageView){
            return (ImageView)view;
        }
        if (view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup)view;
            for (int i = 0; i < viewGroup.getChildCount();i++){
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup){
                    ImageView result = findImageWidget(child);
                    if (result!=null) return result;
                }
                if (child instanceof Switch){
                    return (ImageView)child;
                }
            }
        }
        return null;
    }

    //Get a handle on the 2 parts of the switch preference and assign handlers to them
    @Override
    protected void onBindView (View view){
        super.onBindView(view);

        final ImageView imageView = findImageWidget(view);
        if (imageView!=null){
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onImageClick((ImageView) v);
                }
            });
            //switchView.setChecked(getSharedPreferences().getBoolean(getKey(),false));
            imageView.setFocusable(true);
            imageView.setEnabled(true);
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
