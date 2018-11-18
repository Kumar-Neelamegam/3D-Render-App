package com.asoss.a3drender.app.CoreModules;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.Utilities.CustomDialog;
import com.asoss.a3drender.app.Utilities.CustomIntent;

public class Constants {


    public Constants constants;

    public static String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/3d-render";
    public static String Preferred_Language_Key = "Preferred_Language_Key";
    public static String Preferred_Language_Code = "Preferred_Language_Code";
    public static String Preferred_Language_Status = "Preferred_Language_Status";

    public Constants getInstance() {
        if (constants == null) {
            constants = new Constants();
            return constants;
        }

        return constants;
    }


    public static void globalStartIntent(Context context, Class classes, Bundle bundle, int id) {
        ((Activity) context).finish();
        Intent intent = new Intent(context, classes);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        CustomIntent.customType(context, id);
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


    public static void ExitSweetDialog(final Context ctx, final Class<?> className) {

        new CustomDialog(ctx)
                .setLayoutColor(R.color.md_green_500)
                .setImage(R.drawable.ic_exit_to_app_black_24dp)
                .setTitle(ctx.getResources().getString(R.string.message_title))
                .setDescription(ctx.getResources().getString(R.string.are_you_sure_want_to_exit))
                .setPossitiveButtonTitle("YES")
                .setNegativeButtonTitle("NO")
                .setOnPossitiveListener(((Activity) ctx)::finishAffinity);


    }

}//END
