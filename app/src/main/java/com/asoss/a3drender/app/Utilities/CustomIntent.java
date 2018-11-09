package com.asoss.a3drender.app.Utilities;

import android.app.Activity;
import android.content.Context;
import com.asoss.a3drender.app.R;

public class CustomIntent {
    public static String[] types = {
            "",
            "",

    };

    public CustomIntent() {
    }

    public static void customType(Context context, int animtype) {
        Activity act = (Activity) context;
        switch (animtype) {
            case 1://left-to-right
                act.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case 2://"right-to-left":
                act.overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                break;
            case 3://"bottom-to-up":
                act.overridePendingTransition(R.anim.bottom_to_up, R.anim.up_to_bottom);
                break;
            case 4://"up-to-bottom":
                act.overridePendingTransition(R.anim.up_to_bottom2, R.anim.bottom_to_up2);
                break;
            case 5://"fadein-to-fadeout":
                act.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case 6://"rotateout-to-rotatein":
                act.overridePendingTransition(R.anim.rotatein_out, R.anim.rotateout_in);
                break;
            default:
                break;

        }
    }
}
