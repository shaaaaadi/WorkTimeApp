package com.example.jubransh.workingtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * this activity is the main activity,
 * in this main activity you can see the current month shift or select another month to view it shifts
 * from this activity you can navigate to
 *      1. Add new shift
 *      2. open calculator
 *      3. open settings activity
 *      4. exit
 * extends standard android activity
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public class MainActivity extends Activity
{
    ImageButton openTimerActivity;
    Button  addButton,
            exitAppButton,
            openCalcButton,
            sendReportByMail,
            openSettingsButton;
    ListView daysList;
    Spinner monthsSpinner;
    TextView allShiftsTotal;
    ArrayAdapter itemsAdapter;
    String SalaryDetails;
    String allShiftsTotalTimeStr;

    SalaryParts salaryParts;
    DataBaseManager dBM;
    DateTime currentDate;
    Settings settings;
    int selectedMonth, selectedYear;
    Shift[] loadedShifts;
    boolean isToViewFixedSalary, isToViewNotFixedSalary;
    Bundle _bundle;

    /**
     * Creating all the GUI objects,
     * initializing all the GUI objects listeners,
     * loading the current month shifts from the database
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        _bundle = savedInstanceState;
        //======================================================================================
        //                             On Create Operations
        //======================================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //before loading the activity check if there is started shift
        ShiftTimer sT = null;
        try
        {
            sT = new ShiftTimer(Types.APP_SETTINGS_PATH);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        //Create Settings Instance
        settings = new Settings(Types.APP_SETTINGS_PATH);

        //pull setting to vars
        isToViewFixedSalary = settings.getShowFixedSalary();
        isToViewNotFixedSalary = settings.getShowNotFixedSalary();

        //Get Current Month/year
        currentDate = new DateTime();

        //if application directory does not exists, create one
        FileManager.createNewDir(Types.APP_PATH);

        //Create Instance Of Data Base Manager
        dBM = new DataBaseManager(Types.APP_PATH);

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
        openTimerActivity = (ImageButton) findViewById(R.id.openTimerActivity);
        exitAppButton = (Button)findViewById(R.id.exitApp);

        //Set Timer Button Color according to what it's status (started or not)
        if (sT == null || sT.isStarted() == false)
            openTimerActivity.setBackgroundResource(R.drawable.standard_button_layout);
        else
            openTimerActivity.setBackgroundResource(R.drawable.red_button_layout);

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
                openTimerActivity();
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
                String subject = String.format("%s %s/%s", getString(R.string.hour_report_for_month___), selectedMonth, selectedYear);
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

    /**
     * overriding the back button on click event
     */
    @Override
    public void onBackPressed()
    {
        showExitDialog();
    }

    /**
     * This is method is responsible to delete shift from data base
     * after verifying the delete operation be prompting the user
     * @param shiftRowAsInView
     * @return Nothing.
     */
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
     * This is method is responsible to view the analysis result
     * @param hAR - the result of the analysis
     * @return Nothing.
     */
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

    /**
     * Exit Application method - will exit the app and destroy all the sources
     * after verifying the Exit operation be prompting the user
     * @return Nothing.
     */
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
     * This is method is responsible to view the analysis result into the ListView
     * @param listView - the list view where the shifts should be displayed
     * @param month - month to load from data base
     * @param year - year to load from data base
     * @return Shift array.
     */
    private Shift[] loadMonthShiftsFromDB(ListView listView, TextView totalTimeTextView, int month, int year)
    {
        //Load From Data Base
        loadedShifts = dBM.loadMonthShifts(month, year);
        if(loadedShifts == null)
        {
            if(dBM.mErrorType == Types.DB_ERROR.CORRUPTED_FILE)
            {
                showErrorToast(getString(R.string.damaged_database_error_msg), true);
                //Toast.makeText(getApplicationContext(), "Corrupted DataBase File", Toast.LENGTH_LONG).show();
                return null;
            }
            showErrorToast((String)getString(R.string.no_Shifts_for_this_month), false);
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
        SalaryCalc sC = new SalaryCalc(Types.APP_SETTINGS_PATH, loadedShifts.length);
        salaryParts = sC.calculateSalary(totalHours);

        //update the Salary details String
        SalaryDetails = String.format
                ("%s\t\t%.2f\n%s\t\t%.2f\n%s\t\t%.2f\n%s\t\t%.2f\n%s\t\t%.2f\n%s\t\t%.2f\n%s\t\t%.2f\n%s\t\t%.2f\n\n",
                        getString(R.string.NotFixedSalary),
                        salaryParts.NotFixedSalary,
                        getString(R.string.TravelFund),
                        salaryParts.TravelFund,
                        getString(R.string.PensionFund),
                        salaryParts.PensionFund,
                        getString(R.string.CompletionFund),
                        salaryParts.CompletionFund,
                        getString(R.string.IncomeTax),
                        salaryParts.IncomeTax,
                        getString(R.string.HealthTax),
                        salaryParts.HealthTax,
                        getString(R.string.NationalInsurance),
                        salaryParts.NationalInsurance,
                        getString(R.string.FixedSalary),
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
                isToViewNotFixedSalary ? getString(R.string.NotFixedSalary): "",
                isToViewNotFixedSalary ? notFixedSalaryStr : "",
                isToViewFixedSalary ? isToViewNotFixedSalary ? "   |   שכר נטו:" : "שכר נטו:" : "",
                isToViewFixedSalary ? fixedSalaryStr : ""));

        return loadedShifts;
    }

    /**
     * this method opens the timer activity
     * @return Nothing.
     */
    private  void openTimerActivity()
    {
        finish();
        Intent myIntent = new Intent(MainActivity.this, TimerActivity.class);
        startActivity(myIntent);
    }

    /**
     * this method opens the Add new record activity
     * @return Nothing.
     */
    private void openAddNewRecordActivity()
    {
        finish();
        Intent myIntent = new Intent(MainActivity.this, AddNewRecordActivity.class);
        startActivity(myIntent);
    }

    /**
     * this method opens the calculator activity
     * @return Nothing.
     */
    private void openCalculatorActivity()
    {
        Intent myIntent = new Intent(MainActivity.this, CalculatorActivity.class);
        startActivity(myIntent);
    }

    /**
     * this method exits the application
     * @return Nothing.
     */
    private void exitApplication()
    {
        finish();
    }

    /**
     * This method selects the current month and set the spinner month to the current
     * it's called from the onCreate() method
     * @param month - the month
     * @param month - the Year
     * @return Nothing.
     */
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

    /**
     * This method loads the List Of All Available months In DataBase
     * @return Nothing.
     */
    private void updateMonthsSpinner()
    {
        String[] monthsShifts = dBM.loadAllFilesNamesAsNumeric();
        monthsSpinner = (Spinner)findViewById(R.id.monthsSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, monthsShifts);

        monthsSpinner.setAdapter(adapter);
    }

    /**
     * This method selects the current month and set the spinner month to the current
     * it's called from the onCreate() method
     * @param subject - the subject of the mail
     * @param body - the message body
     * @return Nothing.
     */
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
