package com.example.jubransh.workingtime;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by jubransh on 1/17/2017.
 */


public class MyLocationService extends Service
{
    DateTime mCurrentDate;
    DataBaseManager mDBM;
    String mAppSettingsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time_Settings";
    String mAppPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Work_Time";
    Settings mSettings;
    double allowedRadius;
    int locationCheckerInterval = 5000;
    private static final int TODO = 1;
    private int ServiceCheckInterval = 10000;
    private Handler mHandler;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isUserWasInWork;
    private boolean isUserWasOutOfWork;
    Location workLocation, currentLocation;
    static boolean isFirstRun = true;
    ShiftTimer sT;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getApplicationContext(), "No Permissions", Toast.LENGTH_SHORT).show();
            return START_NOT_STICKY;
        }
        Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_SHORT).show();
        configureGPS(true);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mSettings = new Settings(mAppSettingsPath);
        allowedRadius = mSettings.getLocationRadius();

        //create new Instance of data base manager
        mDBM = new DataBaseManager(mAppPath);

        //get current date
        mCurrentDate = new DateTime();

        //Create new Shift Timer Instance
        try
        {
            sT = new ShiftTimer(mAppSettingsPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mHandler = new Handler();
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                currentLocation = new Location(location);

                if(isFirstRun)
                {
                    isUserWasInWork = true;
                    isUserWasOutOfWork = false;
                    Toast.makeText(getApplicationContext(),"First Run",Toast.LENGTH_SHORT).show();
                    workLocation = new Location(location);
                    isFirstRun = false;
                    return;
                }

                float distance = currentLocation.distanceTo(workLocation);
                if(distance < allowedRadius) // in the radius of the work
                {
                    if(isUserWasOutOfWork)
                    {
                        if(sT.isStarted())
                            return;
                        sT.start();
                        Toast.makeText(MyLocationService.this, " you Entered The Work Dis = " + "distance", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //Do Nothing
                    }
                }
                else //User is Not In The radius Of the Work Location
                {
                    if(isUserWasInWork)
                    {
                        if(sT.isStarted() == false)
                            return;
                        stopShift();
                        Toast.makeText(MyLocationService.this, "you Got Out From Work Dis = " + distance, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //Do Nothing
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s)
            {
                Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

    }

    @Override
    public void onDestroy()
    {
        //mHandler.removeCallbacks(ToastMake);
        Toast.makeText(getApplicationContext(), "Service Stopped", Toast.LENGTH_SHORT).show();
        configureGPS(false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private Runnable ToastMake = new Runnable()
    {
        @Override
        public void run()
        {
            //check is the user were out of work and now entered
            Toast.makeText(getApplicationContext(), ServiceCheckInterval/1000 + " seconds Passed", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(this, ServiceCheckInterval);
        }
    };

    private void configureGPS(boolean isToEnable)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            Toast.makeText(getApplicationContext(), "No Permissions", Toast.LENGTH_SHORT).show();
        }
        if(isToEnable)
            locationManager.requestLocationUpdates("gps", locationCheckerInterval, 0, locationListener);
        else
            locationManager.removeUpdates(locationListener);
    }
    private void stopShift()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyLocationService.this);
        builder.setMessage("האם אתה בטוח שברצונך להפסיק את המשמרת?")
                .setCancelable(false)
                .setPositiveButton("הפסק משמרת", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        String shiftRow = sT.stop();
                        Toast.makeText(getApplicationContext(), "Total Shift Time : "
                                + shiftRow, Toast.LENGTH_LONG).show();
                        mDBM.saveShiftToDataBase(mCurrentDate.getMonth(), mCurrentDate.getYear(), shiftRow);
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


}
