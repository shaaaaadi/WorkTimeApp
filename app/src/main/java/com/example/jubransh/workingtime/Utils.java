package com.example.jubransh.workingtime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by jubransh on 1/3/2018.
 */

public final class Utils
{
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
