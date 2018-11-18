package coursebuddy.karthaalabs.com.Utilities

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.asoss.a3drender.app.R


/**
 * MUTHUKUMAR N
 * kumar.neelamegam17@gmail.com
 * Android Developer
 * 24-7-2018
 * Created a constants class
 */

public class LocalSharedPreference(context: Context) {

    val MY_PREFS_NAME = context.getString(R.string.app_name);
    private var context: Context? = null

    init {
        this.context = context
    }

    fun setValue(key: String, value: String) {
        val sharedPref = (context as Activity).getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        sharedPref.putString(key, value)
        sharedPref.apply()

    }

    fun clearValue(key: String) {
        val sharedPref = (context as Activity).getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        sharedPref.remove(key)
        sharedPref.apply()

    }

    fun getValue(key: String): String {
        val prefs = (context as Activity).getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(key, "")
    }

    fun setBoolean(key: String, value: Boolean) {

        val sharedPref = (context as Activity).getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()

    }


    fun clearBoolean(key: String) {

        val sharedPref = (context as Activity).getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()

    }

    fun getBoolean(key: String): Boolean {
        return (context as Activity).getPreferences(Context.MODE_PRIVATE).getBoolean(key, false)
    }

}
