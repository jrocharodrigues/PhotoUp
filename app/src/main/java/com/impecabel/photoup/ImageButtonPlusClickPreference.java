package com.impecabel.photoup;

/**
 * Created by jrodrigues on 29-01-2015.
 */

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Custom preference for handling a ImageButton with a clickable preference area as well
 */
public class ImageButtonPlusClickPreference extends Preference {

    //
    // Public interface
    //

    /**
     * Sets listeners for the ImageButton and the background container preference view cell
     * @param listener A valid ImageButtonPlusClickListener
     */
    public void setImageButtonClickListener(ImageButtonPlusClickListener listener){
        this.listener = listener;
    }
    private ImageButtonPlusClickListener listener = null;

    /**
     * Interface gives callbacks in to both parts of the preference
     */
    public interface ImageButtonPlusClickListener {
        /**
         * Called when the ImageButton is clicked
         * @param view
         */
        public void onImageButtonClick(View view);

        /**
         * Called when the preference view is clicked
         * @param view
         */
        public void onClick(View view);
    }

    public ImageButtonPlusClickPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageButtonPlusClickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageButtonPlusClickPreference(Context context) {
        super(context);
    }


    //
    // Internal Functions
    //

    /**
     * Recursively go through view tree until we find an android.widget.ImageButton
     * @param view Root view to start searching
     * @return A ImageButton class or null
     */
    private ImageButton findImageButtonWidget(View view){

        if (view instanceof ImageButton){
            return (ImageButton) view;
        }
        if (view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount();i++){
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup){
                    ImageButton result = findImageButtonWidget(child);
                    if (result!=null) return result;
                }
                if (child instanceof ImageButton){
                    return (ImageButton) child;
                }
            }
        }

        return null;
    }

    //Get a handle on the 2 parts of the  preference and assign handlers to them
    @Override
    protected void onBindView (View view){
        super.onBindView(view);

        final ImageButton imageButton = findImageButtonWidget(view);
        if (imageButton!=null){
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onImageButtonClick((ImageButton) v);
                }
            });

            imageButton.setFocusable(true);
            imageButton.setEnabled(true);
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
