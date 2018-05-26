package com.example.jubransh.workingtime;

import java.io.File;

/**
 * This class contains the start stop timer methods
 * This class writes to the data
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public class ShiftTimer
{
    File mTimerFile;
    String mAppSettingsPath, mFileName, mFullFilePath, mStartTime, mStartDate, mStopTime, mStopDate;
    boolean mIsStarted;

    /**
     * This is the Constructor
     * This Method is preparing the Files for first use
     * This method initialize the flags of timer (started/ stoped) according to the data base
     * @param appSettingsPath where the data base file is saved.
     * @exception Exception On input error.
     * @see Exception
     */
    ShiftTimer(String appSettingsPath) throws Exception {
        mAppSettingsPath = appSettingsPath;
        mFileName = "TimerStatus";
        mFullFilePath = mAppSettingsPath + "/" + mFileName;

        //If File not found Create One (First run of application)
        if(FileManager.isFileExists(mAppSettingsPath, mFileName) == false)
        {
            mTimerFile = FileManager.createNewFile(mAppSettingsPath, mFileName);
            return;
        }

        //if File Found check if timer started by reading first Line and update the mIsStarted var
        mTimerFile = new File(mFullFilePath);
        String[] data = FileManager.ReadAllLines(mTimerFile);

        //if the file is empty, so the timer is stopped
        if(data.length == 0 || (data.length == 1 && data[0].equalsIgnoreCase("")))
        {
            mIsStarted = false;
            return;
        }

        //if timer File contains more than 1 line thats mean it'mSettings corrupted
        if(data.length > 1)
            throw new Exception("Corrupted Timer File");

        //check if the file format is ok - correct format should be: started,HH:MM,DD/MM/YYYY
        String[] rowArray = data[0].split(",");
        if(rowArray != null)
            if(rowArray.length == 3)
                if(rowArray[0].equalsIgnoreCase("started"))
                {
                    //Get the shift start time / shift Date
                    mStartTime = rowArray[1];
                    mStartDate = rowArray[2];

                    //init the isStarted flag
                    mIsStarted = true;
                }
                else
                {
                    throw new Exception("Corrupted Timer File: started keyword does not appear");
                }
            else
            {
                throw new Exception("Corrupted Timer File: missing column");
            }
        else
        {
            throw new Exception("Corrupted Timer File: Null Data");
        }
    }
    public void start()
    {
        // if Timer Already started, then do nothing
        if(mIsStarted)
            return;

        //Clear current file from old content and write the start time in this format : started,DD/MM/YYY,HH:MM
        FileManager.deleteAllData(mTimerFile);

        //update the mIsStarted var
        mIsStarted = true;

        //Getting Current time to write to the timer file as a line
        DateTime dT = new DateTime();
        mStartTime = String.format("%02d:%02d", dT.getCurrentHour(), dT.getCurrentMinute());
        mStartDate = String.format("%02d/%02d/%d", dT.getDay(), dT.getMonth(), dT.getYear());

        //write new Line
        FileManager.writeLine(mTimerFile, String.format("started,%s,%s",mStartTime, mStartDate));

    }
    public String stop() // returns the Total Time in this format HH:MM
    {
        // if Timer Already stopped, then do nothing
        if(mIsStarted == false)
            return "";

        mIsStarted = false;

        //read the start time from the file of the timer ans calculate the total time and update the appreciate vars
        //Get Current Time and send start time with current time to get the diff
        DateTime dT = new DateTime();
        mStopTime = String.format("%02d:%02d", dT.getCurrentHour(), dT.getCurrentMinute());
        mStopDate = String.format("%02d/%02d/%d", dT.getDay(), dT.getMonth(), dT.getYear());

        //Clear the time file content
        FileManager.deleteAllData(mTimerFile);

        //Return the string to be added to the DataBase in this format : Date,StratTime,finishTime,totalTime
        return String.format("%s,%s,%s,%s",
                mStartDate,
                mStartTime,
                mStopTime,
                HoursCalc.getToatalTime(mStartDate, mStopDate, mStartTime, mStopTime));
    }
    public boolean isStarted()
    {
        return mIsStarted;
    }
}
