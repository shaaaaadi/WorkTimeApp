package com.example.jubransh.workingtime;

import java.io.File;

/**
 * Created by jubransh on 12/29/2016.
 */

public class ShiftTimer
{
    File _timerFile;
    String _appSettingsPath, _fileName, _fullFilePath, _startTime, _startDate, _stopTime, _stopDate;
    int _startHour, _stopHour, _startMionute, _stopMinute, _totalHours, _totalMinutes;
    boolean _isStarted;

    ShiftTimer(String appSettingsPath) throws Exception {
        _appSettingsPath = appSettingsPath;
        _fileName = "TimerStatus";
        _fullFilePath = _appSettingsPath + "/" + _fileName;

        //If File not found Create One (First run of application)
        if(FileManager.isFileExists(_appSettingsPath, _fileName) == false)
        {
            _timerFile = FileManager.createNewFile(_appSettingsPath, _fileName);
            return;
        }

        //if File Found check if timer started by reading first Line and update the _isStarted var
        _timerFile = new File(_fullFilePath);
        String[] data = FileManager.ReadAllLines(_timerFile);

        //if the file is empty, so the timer is stopped
        if(data.length == 0 || (data.length == 1 && data[0].equalsIgnoreCase("")))
        {
            _isStarted = false;
            return;
        }

        //if timer File contains more than 1 line thats mean it's corrupted
        if(data.length > 1)
            throw new Exception("Corrupted Timer File");

        //check if the file format is ok - correct format should be: started,HH:MM,DD/MM/YYYY
        String[] rowArray = data[0].split(",");
        if(rowArray != null)
            if(rowArray.length == 3)
                if(rowArray[0].equalsIgnoreCase("started"))
                {
                    //Get the shift start time / shift Date
                    _startTime = rowArray[1];
                    _startDate = rowArray[2];

                    //init the isStarted flag
                    _isStarted = true;
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
        if(_isStarted)
            return;

        //Clear current file from old content and write the start time in this format : started,DD/MM/YYY,HH:MM
        FileManager.deleteAllData(_timerFile);

        //update the _isStarted var
        _isStarted = true;

        //Getting Current time to write to the timer file as a line
        DateTime dT = new DateTime();
        _startTime = String.format("%02d:%02d", dT.getCurrentHour(), dT.getCurrentMinute());
        _startDate = String.format("%02d/%02d/%d", dT.getDay(), dT.getMonth(), dT.getYear());

        //write new Line
        FileManager.writeLine(_timerFile, String.format("started,%s,%s",_startTime, _startDate));

    }
    public String stop() // returns the Total Time in this format HH:MM
    {
        // if Timer Already stopped, then do nothing
        if(_isStarted == false)
            return "";

        _isStarted = false;

        //read the start time from the file of the timer ans calculate the total time and update the appreciate vars
        //Get Current Time and send start time with current time to get the diff
        DateTime dT = new DateTime();
        _stopTime = String.format("%02d:%02d", dT.getCurrentHour(), dT.getCurrentMinute());
        _stopDate = String.format("%02d/%02d/%d", dT.getDay(), dT.getMonth(), dT.getYear());

        //Clear the time file content
        FileManager.deleteAllData(_timerFile);

        //Return the string to be added to the DataBase in this format : Date,StratTime,finishTime,totalTime
        return String.format("%s,%s,%s,%s",
                _startDate,
                _startTime,
                _stopTime,
                HoursCalc.getToatalTime(_startDate, _stopDate, _startTime, _stopTime ));
    }
    public boolean isStarted()
    {
        return _isStarted;
    }
}
