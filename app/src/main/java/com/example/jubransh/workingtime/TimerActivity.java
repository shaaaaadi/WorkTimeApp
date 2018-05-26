package com.example.jubransh.workingtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TimerActivity extends Activity {
    DataBaseManager dBM;
    ShiftTimer sT;
    DateTime currentDate;
    Button startStopTimeBtn;
    TextView appTextView;
    Types.BUTTON_TYPE buttonType;

    /**
     * Creating all the GUI objects,
     * initializing all the GUI objects listeners,
     * decide what is the allowed operation (start shift timer / stop shift timer) according to
     * what written in the data base
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        //get current date
        currentDate = new DateTime();

        //create new Instance of data base manager
        dBM = new DataBaseManager(Types.APP_PATH);

        //bind view to button
        startStopTimeBtn = (Button)findViewById(R.id.startStopShift);
        appTextView = (TextView) findViewById(R.id.timerStatus);

        try
        {
            //when loading the activity check if there is started shift
            sT = new ShiftTimer(Types.APP_SETTINGS_PATH);
            if(sT.isStarted())
            {
                startStopTimeBtn.setText(R.string.stop_shift);
                appTextView.setText(R.string.after_start_message);
            }
            else
            {
                startStopTimeBtn.setText(R.string.start_shift);
                appTextView.setText(R.string.after_stop_message);
            }

        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //attach method to the event of the button click
        startStopTimeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Identify if the button is Start or Stop Button
                if(startStopTimeBtn.getText().toString().equalsIgnoreCase((String) getText(R.string.start_shift)))
                {
                    buttonType =  Types.BUTTON_TYPE.START_BUTTON;
                    startStopTimeBtn.setText(R.string.stop_shift);
                    appTextView.setText(R.string.after_start_message);

                    //Start Process
                    sT.start();

                    //finish the timer activity and back to the main one
                    backToMainActivity();
                }
                else
                {
                    //stop process (validate first that User really wants to stop Shift
                    stopShift();
                }
            }
        });
    }

    /**
     * overriding the back button on click event
     */
    @Override
    public void onBackPressed() {
        backToMainActivity();
    }

    /**
     * This is method back to the main activity and destroy the current one
     * @return Nothing.
     */
    private void backToMainActivity()
    {
        finish();
        Intent myIntent = new Intent(TimerActivity.this, MainActivity.class);
        startActivity(myIntent);
    }

    /**
     * This is method stops the timer by writing stop to the data base
     * this method saves the shift after the stop to the database
     * @return Nothing.
     */
    private void stopShift()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(TimerActivity.this);
        builder.setMessage(R.string.stop_shift_verification)
                .setCancelable(false)
                .setPositiveButton(R.string.stop_shift, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        buttonType =  Types.BUTTON_TYPE.STOP_BUTTON;
                        startStopTimeBtn.setText(R.string.start_shift);

                        String shiftRow = sT.stop();
                        Toast.makeText(getApplicationContext(), "Total Shift Time : "
                                + shiftRow, Toast.LENGTH_LONG).show();
                        dBM.saveShiftToDataBase(currentDate.getMonth(), currentDate.getYear(), shiftRow);

                        //finish the timer activity and back to the main one
                        backToMainActivity();
                    }
                })
                .setNegativeButton(R.string.NO, new DialogInterface.OnClickListener()
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
