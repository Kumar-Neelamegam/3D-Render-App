package com.asoss.a3drender.app.Utilities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.*;

public class BottomDialog  extends Dialog {


    public BottomDialog(@NonNull Context context) {
        this(context, 0);
    }

    private BottomDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, 0);
        // We hide the title bar for any style configuration. Otherwise, there will be a gap
        // above the bottom sheet when it is expanded.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(layoutResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null) {
            //if (Build.VERSION.SDK_INT >= 21) {
            //  window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //  window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //}
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            //window.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),
            //    com.medeasypatient.R.drawable.photo_rectangle_selection));
            //window.setLayout(1000, 600);
        }
    }

    @Override
    public void setContentView(@NonNull View view) {
        super.setContentView(view);
    }

    @Override
    public void setContentView(@NonNull View view, ViewGroup.LayoutParams params) {
        super.setContentView(view);
    }

}
