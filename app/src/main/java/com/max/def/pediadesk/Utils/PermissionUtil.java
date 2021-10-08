package com.max.def.pediadesk.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.max.def.pediadesk.R;

public class PermissionUtil
{
    private Context context;
    private SharedPreferences sharedPreferences;

    public static final String PERMISSION_INTERNET = "INTERNET";

    public static final int READ_INTERNET = 1;

    public static final int REQUEST_INTERNET = 2;

    public PermissionUtil(Context context)
    {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.permission_preference),Context.MODE_PRIVATE);
    }

    public void updatePermissionPreference(String permission)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (permission)
        {
            case PERMISSION_INTERNET:
                editor.putBoolean(context.getString(R.string.permission_INTERNET),true);
                editor.apply();
                break;
        }
    }

    public boolean checkPermissionPreference(String permission)
    {
        boolean isShown = false;

        switch (permission)
        {
            case PERMISSION_INTERNET:
                isShown = sharedPreferences.getBoolean(context.getString(R.string.permission_INTERNET),false);
                break;
        }
        return !isShown;
    }
}