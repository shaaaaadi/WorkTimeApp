package com.example.jubransh.workingtime;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
/**
 * this activity is used for the application settings,
 * via this activity user can set shifts, salary, notifications and location settings
 * extends standard android activity
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public class AppSettings extends Activity {
    int[] workDays;
    String appSettingsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time_Settings";
    Settings settings;

    SwitchCompat
            showFixedSalary,
            showNotFixedSalary,
            showCurrentTimeAsFinishTime,
            useNotifications,
            useLocation;

    EditText creditPoints,
            travelsRefund,
            completionFund,
            payment4Hour,
            monthlyGoal,
            pensionFund,
            mailAddress,
            locationRadius;
    CheckBox checkboxSunday,
            checkboxMonday,
            checkboxTuesday,
            checkboxWednesday,
            checkboxThursday,
            checkboxFriday,
            checkboxSaturday;
    TextView useLocationHelpMessage, locationRadiusTextView;
    LinearLayout locationSettingsLayout;

    private LocationManager locationManager;
    private LocationListener listener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_GPS(true);
                break;
            default:
                break;
        }
    }

    /**
     * Creating all the GUI objects,
     * initializing all the GUI objects listeners,
     * loading the current settings from the database
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        //Grant GPS Permissions
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        //Create Instance of settings Class
        settings = new Settings(appSettingsPath);

        //bind layout to an object
        locationSettingsLayout = (LinearLayout)findViewById(R.id.locationSettingsLayout);

        //bind TextView to java object
        useLocationHelpMessage = (TextView) findViewById(R.id.useLocationHelpMessage);
        locationRadiusTextView = (TextView) findViewById(R.id.locationRadiusTextView);

        //bind switches views to java objects
        showFixedSalary = (SwitchCompat) findViewById(R.id.switchShowFixedSalary);
        showNotFixedSalary = (SwitchCompat) findViewById(R.id.switchShowNotFixedSalary);
        showCurrentTimeAsFinishTime = (SwitchCompat) findViewById(R.id.switchUseCurrentTimeAsFinish);
        useLocation = (SwitchCompat) findViewById(R.id.swithUseLocation);
        useNotifications = (SwitchCompat) findViewById(R.id.switchUseNotif);


        //bind text views to java objects
        creditPoints = (EditText)findViewById(R.id.textCreditPoints);
        travelsRefund = (EditText) findViewById(R.id.travelsRefund);
        completionFund = (EditText)findViewById(R.id.textCompletingFund);
        pensionFund = (EditText)findViewById(R.id.textPensionFund);
        payment4Hour= (EditText)findViewById(R.id.textPayment4Hour);
        monthlyGoal = (EditText)findViewById(R.id.textMonthlyGoal);
        mailAddress = (EditText)findViewById(R.id.mailTo);
        locationRadius = (EditText)findViewById(R.id.textLocationRadius);

        //bind checkBoxes to java objects
        checkboxSunday = (CheckBox) findViewById(R.id.checkboxSunday);
        checkboxMonday = (CheckBox) findViewById(R.id.checkboxMonday);
        checkboxTuesday = (CheckBox) findViewById(R.id.checkboxTuesday);
        checkboxWednesday = (CheckBox) findViewById(R.id.checkboxWednesday);
        checkboxThursday = (CheckBox) findViewById(R.id.checkboxThursday);
        checkboxFriday = (CheckBox) findViewById(R.id.checkboxFriday);
        checkboxSaturday = (CheckBox) findViewById(R.id.checkboxSaturday);

        //set the switches according to the settings database
        showFixedSalary.setChecked(settings.getShowFixedSalary());
        showNotFixedSalary.setChecked(settings.getShowNotFixedSalary());
        showCurrentTimeAsFinishTime.setChecked(settings.getUseCurrentTimeAsFinishTime());
        useLocation.setChecked(settings.getUseLocation());
        useNotifications.setChecked(settings.getUseNotifications());

        //set the edit texts according to the settings database
        creditPoints.setText(String.format("%.2f", settings.getCreditPoints()));
        travelsRefund.setText(String.format("%.2f", settings.getTravelsRefund()));
        completionFund.setText(String.format("%.2f", settings.getCompletionFund()));
        pensionFund.setText(String.format("%.2f", settings.getPensionFund()));
        payment4Hour.setText(String.format("%.2f", settings.getPay4Hour()));
        monthlyGoal.setText(String.format("%.2f", settings.getMonthlyHourGoal()));
        mailAddress.setText(settings.getMail());

        //Set the location settings
        locationRadius.setText(Double.toString(settings.getLocationRadius()));
        setLocationObjectsVisibility(useLocation.isChecked());

        //set the work days checkboxes according to the settings database
        workDays = settings.getWorkDays();
        for(int i=0; i<workDays.length; i++)
        {
            switch (workDays[i])
            {
                case  1: checkboxSunday.setChecked(true); break;
                case  2: checkboxMonday.setChecked(true); break;
                case  3: checkboxTuesday.setChecked(true); break;
                case  4: checkboxWednesday.setChecked(true); break;
                case  5: checkboxThursday.setChecked(true); break;
                case  6: checkboxFriday.setChecked(true); break;
                case  7: checkboxSaturday.setChecked(true); break;
                default: break;
            }
        }

        //=========================================================================================
//                                Save changing settings to the DataBase
        //=========================================================================================
        showCurrentTimeAsFinishTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                settings.setUseCurrentTimeAsFinishTime(isChecked);
            }
        });
        showFixedSalary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                settings.setShowFixedSalary(isChecked);
            }
        });
        showNotFixedSalary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                settings.setShowNotFixedSalary(isChecked);
            }
        });
        useLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setUseLocation(isChecked);
                if (isChecked)
                {
                    configure_GPS(true);
                    Utils.showHelpDialog(AppSettings.this, getString(R.string.use_location_info), getString(R.string.close));
                }
                else
                {
                    configure_GPS(false);
                }
                setLocationObjectsVisibility(useLocation.isChecked());
                //Intent in = new Intent(getApplicationContext(), MyLocationService.class);
                //if(isChecked)
                //    startService(in);
                //else
                //   stopService(in);

            }
        });


        useNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                settings.setUseNotifications(isChecked);
            }
        });

        payment4Hour.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                String str = payment4Hour.getText().toString();
                if(str == null || str.equals(""))
                    return;
                settings.setPay4Hour(Double.parseDouble(str));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        monthlyGoal.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                String str = monthlyGoal.getText().toString();
                if(str == null || str.equals(""))
                    return;
                settings.setMonthlyHourGoal(Double.parseDouble(str));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        creditPoints.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                String str = creditPoints.getText().toString();
                if(str == null || str.equals(""))
                    return;
                settings.setCreditPoints(Double.parseDouble(str));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        pensionFund.addTextChangedListener(new TextWatcher() {

        public void afterTextChanged(Editable s)
        {
            String str = pensionFund.getText().toString();
            if(str == null || str.equals(""))
                return;
            settings.setPensionFund(Double.parseDouble(str));
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        completionFund.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                String str = completionFund.getText().toString();
                if(str == null || str.equals(""))
                    return;
                settings.setCompletionFund(Double.parseDouble(str));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        travelsRefund.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                String str = travelsRefund.getText().toString();
                if(str == null || str.equals(""))
                    return;
                settings.setTravelsRefund(Double.parseDouble(str));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        locationRadius.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                    String str = locationRadius.getText().toString();
                    if(str == null || str.equals(""))
                        return;
                    settings.setLocationRadius(Double.parseDouble(str));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {}
        });

        mailAddress.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s)
            {
                String str = mailAddress.getText().toString();
                if(str == null || str.equals(""))
                    return;
                settings.setMail(str);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        //Saving changing when checking / unchecking Work Days checkBoxes
        checkboxSunday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(1, isChecked);
            }
        });
        checkboxMonday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(2, isChecked);
            }
        });
        checkboxTuesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(3, isChecked);
            }
        });
        checkboxWednesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(4, isChecked);
            }
        });
        checkboxThursday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(5, isChecked);
            }
        });
        checkboxFriday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(6, isChecked);
            }
        });
        checkboxSaturday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                modifyWorkDays(7, isChecked);
            }
        });

    }

    /**
    * overriding the back button on click event
    */
    @Override
    public void onBackPressed()
    {
        backToMainActivity();
    }

    /**
     * This is method back to the main activity and destroy the current one
     * @return Nothing.
     */
    private void backToMainActivity()
    {
        finish();
        Intent myIntent = new Intent(AppSettings.this, MainActivity.class);
        startActivity(myIntent);
    }

    /**
     * This is method is responsible to update the work days in the data base
     * @param dayOfWeek which day of the week user wants to update
     * @param isChecked boolean which refers to if the day (param1) is a work day or day off
     * @return Nothing.
     */
    private void modifyWorkDays(int dayOfWeek, boolean isChecked)
    {
        //converting array to list
        List<Integer> workDaysList = new ArrayList<Integer>();
        for (int index = 0; index < workDays.length; index++)
        {
            workDaysList.add(workDays[index]);
        }

        //modifying the List
        if(isChecked)
            workDaysList.add(dayOfWeek);
        else
        {
            for(int i=0; i< workDaysList.size(); i++)
            {
                if(workDaysList.get(i) == dayOfWeek)
                {
                    workDaysList.remove(i);
                    break;
                }
            }
        }


        //converting back the Modified List to Array
        workDays = new int[workDaysList.size()];
        for(int i=0; i<workDays.length; i++)
        {
            workDays[i] = workDaysList.get(i);
        }

        //writing the modified array to the database
        settings.setWorkDays(workDays);
    }

    /**
     * This is method is responsible to change the GUI object on the fly according to the location Mode
     * @param isLocationEnabled setting the GUI object is according to this boolean
     * @return Nothing.
     */
    private void setLocationObjectsVisibility(boolean isLocationEnabled)
    {
        locationRadius.setEnabled(!isLocationEnabled);
        locationRadiusTextView.setVisibility(isLocationEnabled ? View.GONE : View.VISIBLE);
        locationRadius.setVisibility(isLocationEnabled ? View.GONE : View.VISIBLE);
        locationSettingsLayout.setVisibility(isLocationEnabled ? View.GONE : View.VISIBLE);
        useLocationHelpMessage.setVisibility(isLocationEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * This is method is responsible to set the GPS Mode
     * this method should grant Location permissions if needed.
     * @param isToEnable enable or disable gps service.
     * @return Nothing.
     */
    void configure_GPS(boolean isToEnable)
    {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        //if(isToEnable)
           // locationManager.requestLocationUpdates("gps", 5000, 0, listener);
        //else
           // locationManager.removeUpdates(listener);

        Intent in = new Intent(getApplicationContext(), MyLocationService.class);
        if(isToEnable)
            startService(in);
        else
           stopService(in);
    }
}
