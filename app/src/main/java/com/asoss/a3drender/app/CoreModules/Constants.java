package com.asoss.a3drender.app.CoreModules;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.asoss.a3drender.app.Utilities.CustomIntent;

public class Constants {


    public Constants constants;

    public Constants getInstance() {
        if (constants == null) {
            constants = new Constants();
            return constants;
        }

        return constants;
    }




    public static void globalStartIntent(Context context, Class classes, Bundle bundle) {
        ((Activity) context).finish();
        Intent intent = new Intent(context, classes);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        CustomIntent.customType(context, 4);
        context.startActivity(intent);

    }

    public static void globalStartIntent2(Context context, Class classes, Bundle bundle) {

        Intent intent = new Intent(context, classes);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        CustomIntent.customType(context, 4);
        context.startActivity(intent);

    }



}//END
