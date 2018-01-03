package com.example.jubransh.workingtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
    boolean _mainActivityStarted = false;
    Button  addButton,
            openTimerActivity,
            exitAppButton,
            openCalcButton,
            sendReportByMail,
            openSettingsButton;

    ListView daysList;
    Spinner monthsSpinner;
    TextView allShiftsTotal;
    ArrayAdapter itemsAdapter;
    String SalaryDetails;
    String appPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time";
    String appSettingsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time_Settings";
    String allShiftsTotalTimeStr;

    SalaryParts salaryParts;
    DataBaseManager dBM;
    DateTime currentDate;
    Settings settings;
    int selectedMonth, selectedYear;
    Shift[] loadedShifts;
    boolean isToViewFixedSalary, isToViewNotFixedSalary;
    Bundle _bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        _bundle = savedInstanceState;
        //======================================================================================
        //                             On Create Operations
        //======================================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create Settings Instance
        settings = new Settings(appSettingsPath);

        //pull setting to vars
        isToViewFixedSalary = settings.getShowFixedSalary();
        isToViewNotFixedSalary = settings.getShowNotFixedSalary();

        //Get Current Month/year
        currentDate = new DateTime();

        //if application directory does not exists, create one
        FileManager.createNewDir(appPath);

        //Create Instance Of Data Base Manager
        dBM = new DataBaseManager(appPath);

        //Load month/year shifts from DB
        daysList = (ListView)findViewById(R.id.daysList);
        allShiftsTotal = (TextView)findViewById(R.id.allShiftsTotalView);

        loadedShifts = loadMonthShiftsFromDB(daysList, allShiftsTotal, currentDate.getMonth(), currentDate.getYear());
        updateMonthsSpinner();
        //Select current month on spinner
        selectCurrentMonth(currentDate.getMonth(), currentDate.getYear());

        if( loadedShifts!= null)
        {
            //Analyzing the Shifts Status
            if(settings.getUseNotifications())
            {
                int[] workDays = settings.getWorkDays();
                HoursAnalysisResult hAR = HoursCalc.analyzeHours(
                        workDays,
                        (int)settings.getMonthlyHourGoal(),
                        HoursCalc.getAllShiftsTotalTimeAsDouble(loadedShifts),
                        currentDate.getDay(),
                        currentDate.getMonth(),
                        currentDate.getYear());
                showAnalysisDialog(hAR);
            }
        }

        addButton = (Button)findViewById(R.id.addNewRecord);
        openSettingsButton = (Button)findViewById(R.id.openSettings);
        sendReportByMail = (Button)findViewById(R.id.sendReport);
        openCalcButton = (Button)findViewById(R.id.openCalculator);
        openTimerActivity = (Button)findViewById(R.id.openTimerActivity);
        exitAppButton = (Button)findViewById(R.id.exitApp);

        //======================================================================================
    //                                   Attaching Events
        //======================================================================================
        //Attach an event when clicking on the view of the total horus/summary
        allShiftsTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicking on the total hours and salary view, details message will appear
                Utils.showHelpDialog(MainActivity.this, SalaryDetails, getString(R.string.close));
            }
        });
        if(monthsSpinner != null)
        {
            monthsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    //parsing the date string from from the comboBox to: int month, int year.
                    String s = parent.getItemAtPosition(position).toString();
                    String[] selectedDateArray = s.split("/");
                    if(selectedDateArray.length != 2)
                        return;
                    selectedMonth = Integer.valueOf(selectedDateArray[0]);
                    selectedYear = Integer.valueOf(selectedDateArray[1]);

                    //loading the selected month/year DataBase to the List view
                    loadedShifts = loadMonthShiftsFromDB(daysList, allShiftsTotal, selectedMonth, selectedYear);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    //Another interface callback
                    Toast.makeText(getApplicationContext(), "Nothing Selected", Toast.LENGTH_LONG).show();
                }
            });
        }

        // attaching event to the Add button click
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openAddNewRecordActivity();
            }
        });
        openSettingsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
                Intent intent = new Intent(getApplicationContext(), AppSettings.class);
                startActivity(intent);
            }
        });

        //attach event to click on open timer (when clicking open the Timer Activity)
        openTimerActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openTimerActivity(); //remove comment
            }
        });

        // attaching event to the open Calc button click
        openCalcButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openCalculatorActivity();
            }
        });

        // attaching event to the exit button click
        exitAppButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showExitDialog();
            }
        });

        //set on touch listener on the list - to delete shift
        daysList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub
                Log.v("long clicked","pos: " + pos);
                String data=(String)arg0.getItemAtPosition(pos);
                showDeleteDialog(data);

                return true;
            }

        });

        //attach an event to the send report button
        sendReportByMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String subject = String.format("%s%s/%s", getString(R.string.hour_report_for_month___), selectedMonth, selectedYear);
                String body = "";

                for(int i=0; i < loadedShifts.length; i++)
                {
                   String row = dBM.getShiftAsStringToView(loadedShifts[i]);
                    body += row + "\n";
                }
                body += String.format("%s: %s", getString(R.string.sum_of_all_hours_for_this_month), allShiftsTotalTimeStr);
                sendEmail(subject, body);
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        showExitDialog();
    }
    //deleting shift
    private void showDeleteDialog(final String shiftRowAsInView)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(String.format("%s: %s ?",
                getString(R.string.are_you_sure_you_wanna_delete_this_shift),
                shiftRowAsInView))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dBM.deleteRowFromDB(shiftRowAsInView, selectedMonth, selectedYear);
                        loadedShifts = loadMonthShiftsFromDB(daysList, allShiftsTotal, currentDate.getMonth(), currentDate.getYear());
                        updateMonthsSpinner();
                        selectCurrentMonth(currentDate.getMonth(), currentDate.getYear());
                        //restart the view
                        onCreate(_bundle);
                        return;
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void showAnalysisDialog(HoursAnalysisResult hAR)
    {
        String message;
        if(hAR.MissingHours <= 0.1)
        {
            if(hAR.MissingHours >= -0.1)
                message = getString(R.string.good_status_msg);
            else
                message = String.format("%s\n%s %s %s %s %s",
                        getString(R.string.all_respect_you_are_more_than_good),
                        getString(R.string.as_a_result_you_can_work_starting_from_today___),
                        HoursCalc.convertNumToHourFormat(Math.abs(hAR.NewDailyGoal)),
                        getString(R.string.instead_of),
                        HoursCalc.convertNumToHourFormat(Math.abs(hAR.ExpectedDailyGoal)),
                        getString(R.string.till_the_end_of_the_month));
        }
        else
        {
            message = String.format("%s\n%s %s %s %s %s",
                    getString(R.string.be_carefull_your_are_not_close_to_your_goal),
                    getString(R.string.if_you_wanna_achive_your_goal_you_should_work____) ,
                    HoursCalc.convertNumToHourFormat(hAR.NewDailyGoal),
                    getString(R.string.each_day),
                    getString(R.string.instead_of),
                    HoursCalc.convertNumToHourFormat(hAR.ExpectedDailyGoal));
        }
        Utils.showHelpDialog(MainActivity.this, message, getString(R.string.close));
    }
    private void showExitDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.exit_dialog_msg))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        exitApplication();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private Shift[] loadMonthShiftsFromDB(ListView listView, TextView totalTimeTextView, int month, int year)
    {
        //Load From Data Base
        loadedShifts = dBM.loadMonthShifts(month, year);
        if(loadedShifts == null)
        {
            if(dBM.errorType == Types.DB_ERROR.CORRUPTED_FILE)
            {
                showErrorToast(getString(R.string.damaged_database_error_msg), true);
                //Toast.makeText(getApplicationContext(), "Corrupted DataBase File", Toast.LENGTH_LONG).show();
                return null;
            }
            showErrorToast("לא נמצאו משמרות לחודש נוכחי", false);
            return null;
        }

        String[] shiftsArrayAsStrings = new String[loadedShifts.length];

        if(loadedShifts != null)
        {
            for(int i=0; i<loadedShifts.length; i++)
            {
                String listItem = DataBaseManager.getShiftAsStringToView(loadedShifts[i]);
                shiftsArrayAsStrings[i] = listItem.toString();
            }
        }
        itemsAdapter = new ArrayAdapter<String>(
                this, R.layout.list_black_text, R.id.list_content, shiftsArrayAsStrings);

        listView.setAdapter(itemsAdapter);

        //Get Total Hours As Double
        double totalHours = HoursCalc.getAllShiftsTotalTimeAsDouble(loadedShifts);

        //Create New Instance of Salary Calc
        SalaryCalc sC = new SalaryCalc(appSettingsPath, loadedShifts.length);
        salaryParts = sC.calculateSalary(totalHours);

        //update the Salary details String
        SalaryDetails = String.format
                ("%s%.2f\n%s%.2f\n%s%.2f\n%s%.2f\n%s%.2f\n%s%.2f\n%s%.2f\n%s%.2f\n\n",
                        "משכורת ברוטו:      ",
                        salaryParts.NotFixedSalary,
                        "תשלומי נסיעות:     ",
                        salaryParts.TravelFund,
                        "קרן פנסיה:         ",
                        salaryParts.PensionFund,
                        "קרן השתלמות:       ",
                        salaryParts.CompletionFund,
                        "מס הכנסה:          ",
                        salaryParts.IncomeTax,
                        "מס בריאות:         ",
                        salaryParts.HealthTax,
                        "ביטוח לאומי:       ",
                        salaryParts.NationalInsurance,
                        "משכורת נטו:      ",
                        salaryParts.FixedSalary
                        );

        //update the shifts Total Time label
        allShiftsTotalTimeStr = HoursCalc.getAllShiftsTotalTime(loadedShifts);
        String notFixedSalaryStr = String.format("%.2f",salaryParts.NotFixedSalary);
        String fixedSalaryStr = String.format("%.2f",salaryParts.FixedSalary);

        //view Total Salary
        totalTimeTextView.setText(String.format("%s %s %s \n %s %s%s %s",
                "סך הכול:",
                allShiftsTotalTimeStr,
                "שעות",
                isToViewNotFixedSalary ? "שכר ברוטו:" : "",
                isToViewNotFixedSalary ? notFixedSalaryStr : "",
                isToViewFixedSalary ? isToViewNotFixedSalary ? "   |   שכר נטו:" : "שכר נטו:" : "",
                isToViewFixedSalary ? fixedSalaryStr : ""));

        return loadedShifts;
    }
    private  void openTimerActivity()
    {
        finish();
        Intent myIntent = new Intent(MainActivity.this, TimerActivity.class);
        startActivity(myIntent);
    }
    private void openAddNewRecordActivity()
    {
        finish();
        Intent myIntent = new Intent(MainActivity.this, AddNewRecordActivity.class);
        startActivity(myIntent);
    }
    private void openCalculatorActivity()
    {
        Intent myIntent = new Intent(MainActivity.this, CalculatorActivity.class);
        startActivity(myIntent);
    }
    private void exitApplication()
    {
        finish();
    }
    private void selectCurrentMonth(int month, int year)
    {
        String dateFormat = String.format("%02d/%d", month, year);
        Adapter adapter = monthsSpinner.getAdapter();
        int n = adapter.getCount();
        for (int i = 0; i < n; i++)
        {
            String item = (String) adapter.getItem(i);
            if(item != null)
                if(item.toString().equals(dateFormat))
                {
                    monthsSpinner.setSelection(i);
                    return;
                }
        }

    }
    private void updateMonthsSpinner()
    {
        //Load List Of All Available months In DataBase
        String[] monthsShifts = dBM.loadAllFilesNamesAsNumeric();
        monthsSpinner = (Spinner)findViewById(R.id.monthsSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, monthsShifts);

        monthsSpinner.setAdapter(adapter);
    }
    protected void sendEmail(String subject, String body)
    {
        String[] TO = {settings.getMail()};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try
        {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
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
