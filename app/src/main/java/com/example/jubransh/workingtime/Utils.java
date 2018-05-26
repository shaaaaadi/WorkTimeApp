package com.example.jubransh.workingtime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * This Class is Utils class for shared essential operation may activities or another class use
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public final class Utils
{
    /**
     * This is a static method is responsible to show help dialog with custom message
     * @param context context of the activity which used this method
     * @param messageBody message body to be displayed
     * @param closeStr Message Title
     * @return Nothing.
     */
    public static void showHelpDialog(Context context, String messageBody, String closeStr)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageBody)
                .setPositiveButton(closeStr, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
