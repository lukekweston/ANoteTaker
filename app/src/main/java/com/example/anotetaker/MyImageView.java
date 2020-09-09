package com.example.anotetaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


//https://stackoverflow.com/questions/13622081/imageview-onimagechangedlistener-android
@SuppressLint("AppCompatCustomView")
public class MyImageView extends ImageView {

    private OnImageChangeListiner onImageChangeListiner;


    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }


    public void setImageChangeListener(
            OnImageChangeListiner onImageChangeListiner) {
        this.onImageChangeListiner = onImageChangeListiner;
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        if (onImageChangeListiner != null)
            onImageChangeListiner.imageChangedinView(this);
    }


    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        if (onImageChangeListiner != null)
            onImageChangeListiner.imageChangedinView(this);
    }




    public static interface OnImageChangeListiner {
        public void imageChangedinView(ImageView mImageView);
    }
}