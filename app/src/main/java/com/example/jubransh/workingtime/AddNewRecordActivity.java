package com.example.jubransh.workingtime;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;

/**
 * this activity is used to add new shift to the Database
 * extends standard android activity
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public class AddNewRecordActivity extends Activity
{
    Settings settings;
    DataBaseManager dBM;
    Date selectedDate = new Date();
    DateTime _selectedDate;
    DateTime dT = new DateTime();
    CalendarView myCalendar;
    Button confirmAddButton, cancelAddButton;
    TextView startHour, finishHour, startMinute, finishMinute;
    String appPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time";
    String appSettingsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time_Settings";

    /**
     * Creating all the GUI objects,
     * initializing all the GUI objects listeners,
     * filling the start time / end time automatically if needed (related to what configured in the settings)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_record);

        settings = new Settings(appSettingsPath);

        //Get Current Date;
        _selectedDate = new DateTime(dT.getDay(), dT.getMonth(), dT.getYear());
        selectedDate.setDate(dT.getDay());
        selectedDate.setMonth(dT.getMonth());
        selectedDate.setYear(dT.getYear());

        //Connecting Button from layout to java objects
        confirmAddButton = (Button)findViewById(R.id.confirmAdd);
        cancelAddButton = (Button)findViewById(R.id.cancelAdd);

        //Create Instance Of Data Base Manager
        dBM = new DataBaseManager(appPath);

        //Creating Calendar View
        myCalendar = (CalendarView)findViewById(R.id.appCalendar);
        myCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
            {
                _selectedDate = new DateTime(dayOfMonth, month+1, year);
                selectedDate.setYear(year);
                selectedDate.setMonth(month + 1); // the month + 1 is a workaround for some bug on CalendarView Class
                selectedDate.setDate(dayOfMonth);

                //int m = selectedDate.getMonth();
                Toast.makeText(getApplicationContext(), dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_LONG).show();
            }
        });

        //Create TextBoxes Instances
        startHour = (EditText)findViewById(R.id.startHour);
        startMinute = (EditText)findViewById(R.id.startMinute);
        finishHour = (EditText)findViewById(R.id.finishHour);
        finishMinute = (EditText)findViewById(R.id.finishMinute);

        //put current Time into start time fields
        if(settings.getUseCurrentTimeAsFinishTime())
        {
            finishHour.setText(String.format("%02d", dT.getCurrentHour()));
            finishMinute.setText(String.format("%02d", dT.getCurrentMinute()));
        }

        //confirming adding new day
        confirmAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int sH, sM, fH, fM;

                //Get all strings from text boxes
                String sH_str = startHour.getText().toString();
                String sM_str = startMinute.getText().toString();
                String fH_str = finishHour.getText().toString();
                String fM_str = finishMinute.getText().toString();

                //validate that user filled all fields
                if(sH_str.length()>0 && sM_str.length()>0 && fH_str.length()>0 && fM_str.length()>0)
                {
                    //parsing user input to integers
                    sH = Integer.valueOf(startHour.getText().toString());
                    sM = Integer.valueOf(startMinute.getText().toString());
                    fH = Integer.valueOf(finishHour.getText().toString());
                    fM = Integer.valueOf(finishMinute.getText().toString());
                }
                else //if not all fields are filled
                {
                    showErrorToast(getString(R.string.missing_fields), true);
                    return;
                }

                //Save the day and back to the main activity
                if(SaveToDataBase(sH, sM, fH, fM))
                    backToMainActivity();
            }
        });

        cancelAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Back To The Main Activity without saving anything
                showBackWithoutSavingDialog();
            }
        });
    }
    /**
     * This is method saves the user input to the data base by using the dataBaseManager
     * this method performs the above after validating the user input correctness
     * @param startHour Shift start time user inserted - hour part
     * @param startMinute Shift start time user inserted - Minutes part
     * @param finishHour Shift end time user inserted - hour part
     * @param finishMinute Shift end time user inserted - Minutes part
     * @return boolean, true if success to save and false else.
     */
    private boolean SaveToDataBase(int startHour, int startMinute, int finishHour, int finishMinute)
    {
        //Check legality of hours user inserted
        String[] errorMessage = new String[1];
        if(! HoursCalc.isLegalShift(startHour, startMinute, finishHour, finishMinute, errorMessage))
        {
            showErrorToast(errorMessage[0], true);
            return false;
        }

        //Building new shift
        Shift shift = new Shift();

        // fill the shift object with the data taken from the GUI
        shift.StartHour = startHour;
        shift.startMinute = startMinute;
        shift.FinishHour = finishHour;
        shift.finishMinute = finishMinute;
        shift.totalTime = HoursCalc.getDifferenceAsFloat(startHour, startMinute, finishHour, finishMinute);
        shift.ShiftDate = new DateTime(_selectedDate.mDay, _selectedDate.mMonth, _selectedDate.mYear);//selectedDate;
        shift.StartTime = String.format("%02d:%02d", startHour, startMinute);
        shift.finishTime = String.format("%02d:%02d", finishHour, finishMinute);

        //convert the Shift Object to data base string
        String rowString = DataBaseManager.convertShiftToString(shift);

        //save to file
        if(!dBM.saveShiftToDataBase(_selectedDate.getMonth(), _selectedDate.getYear(), rowString))
            return false;

        return true;
    }

    @Override
    public void onBackPressed()
    {
        //ask the user before exiting the current screen if he is sure
        showBackWithoutSavingDialog();
    }

    /**
     * This is method back to the main activity without saving changes
     * this will be performed only after prompting the user
     * @return Nothing.
     */
    private void showBackWithoutSavingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewRecordActivity.this);
        builder.setMessage(getString(R.string.discard_shift_verify))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        backToMainActivity();
                    }
                })
                .setNegativeButton(getString(R.string.NO), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This is method back to the main activity and destroy the current one
     * @return Nothing.
     */
    private void backToMainActivity()
    {
        //dispose current activity
        finish();

        //prepare and start the main activity screen
        Intent myIntent = new Intent(AddNewRecordActivity.this, MainActivity.class);
        startActivity(myIntent);
    }

    /**
     * This is method is displaying Error message to inform the user
     * @param errorMessage the error message should appears on the screen
     * @param isLong the duration flag of the message, t
     *               he message will appear for long time if this flag = true
     *               else the message will be displayed for short time
     * @return Nothing.
     */
    private void showErrorToast(String errorMessage, boolean isLong)
    {
        //Show Red Warning / Error
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView tV = (TextView) layout.findViewById(R.id.errorMessage);
        tV.setText(errorMessage);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
