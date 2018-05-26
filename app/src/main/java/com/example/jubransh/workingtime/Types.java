package com.example.jubransh.workingtime;

import android.os.Environment;

/**
 * Static class contains all the enums should be used in the application activities and classed
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public final class Types
{
    public static String APP_SETTINGS_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time_Settings";
    public static String APP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time";

    public enum DB_ERROR
                        {
                            NO_ERROR,
                            CORRUPTED_FILE,
                            DB_FILE_NOT_FOUND,
                            ILLEGAL_FILE_FORMAT
                        };
    public enum BUTTON_TYPE {START_BUTTON, STOP_BUTTON};
}
