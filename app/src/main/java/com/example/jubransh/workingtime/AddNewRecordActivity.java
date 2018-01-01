package com.example.jubransh.workingtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;

public class AddNewRecordActivity extends Activity
{
    Settings settings;
    DataBaseManager dBM;
    Date selectedDate = new Date();
    DateTime _selectedDate;
    DateTime dT = new DateTime();
    CalendarView myCalendar;
    Button confirmAddButton, cancelAddButton;
    TextView starthour, finishHour, startMinute, finishMinute;
    String appPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time";
    String appSettingsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time_Settings";

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
        starthour = (EditText)findViewById(R.id.startHour);
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
                String sH_str = starthour.getText().toString();
                String sM_str = startMinute.getText().toString();
                String fH_str = finishHour.getText().toString();
                String fM_str = finishMinute.getText().toString();

                //validate that user filled all fields
                if(sH_str.length()>0 && sM_str.length()>0 && fH_str.length()>0 && fM_str.length()>0)
                {
                    //parsing user input to integers
                    sH = Integer.valueOf(starthour.getText().toString());
                    sM = Integer.valueOf(startMinute.getText().toString());
                    fH = Integer.valueOf(finishHour.getText().toString());
                    fM = Integer.valueOf(finishMinute.getText().toString());
                }
                else //if not all fields are filled
                {
                    showErrorToast("נא למלות כל שדות החובה לפני שמירה", true);
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
        shift.startHour = startHour;
        shift.startMinute = startMinute;
        shift.finishHour = finishHour;
        shift.finishMinute = finishMinute;
        shift.totalTime = HoursCalc.getDifferenceAsFloat(startHour, startMinute, finishHour, finishMinute);

        //shift.shiftDate = new DateTime(selectedDate.getDate(), selectedDate.getMonth(), selectedDate.getYear());//selectedDate;
        shift.shiftDate = new DateTime(_selectedDate._day, _selectedDate._month, _selectedDate._year);//selectedDate;

        shift.startTime = String.format("%02d:%02d", startHour, startMinute);
        shift.finishTime = String.format("%02d:%02d", finishHour, finishMinute);

        String rowString = DataBaseManager.convertShiftToString(shift);

        //save to file
        if(!dBM.saveShiftToDataBase(_selectedDate.getMonth(), _selectedDate.getYear(), rowString))
        //if(!dBM.saveShiftToDataBase(_selectedDate._month, _selectedDate._year, rowString))
            return false;

        return true;
    }
    @Override
    public void onBackPressed()
    {
        showBackWithoutSavingDialog();
    }
    private void backToMainActivity()
    {
        finish();
        Intent myIntent = new Intent(AddNewRecordActivity.this, MainActivity.class);
        startActivity(myIntent);
    }
    private void showBackWithoutSavingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewRecordActivity.this);
        builder.setMessage("האם אתה בטוח שברצונך לחזור למסך הראשי מבלי לשמור את המשמרת?")
                .setCancelable(false)
                .setPositiveButton("בטוח", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        backToMainActivity();
                    }
                })
                .setNegativeButton("לא", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void showErrorToast(String errorMessage, boolean isLong)
    {
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
