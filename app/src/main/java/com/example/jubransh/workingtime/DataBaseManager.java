package com.example.jubransh.workingtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jubransh on 11/16/2016.
 */

public class DataBaseManager
{
    public String _appWorkDirPath;
    Types.DB_ERROR errorType;
    File _dbFile;

    public DataBaseManager(String appWorkingDirPath)
    {
        errorType = Types.DB_ERROR.NO_ERROR;
        _appWorkDirPath = appWorkingDirPath;
    }
    public boolean saveShiftToDataBase(int month, int year, String shiftRow)
    {
        String monthStr = DateTime.getMonthString(month);

        try
        {
            //Create Month DB file if not exists
            String currentMonthFileName =  String.format("%s_%d.txt",monthStr, year);
            _dbFile = FileManager.createNewFile(_appWorkDirPath, currentMonthFileName);
            FileManager.writeLine(_dbFile, shiftRow);
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
    public Shift[] loadMonthShifts(int month, int year)
    {
        String monthStr = DateTime.getMonthString(month);
        String yearStr = String.format("%s", year);
        String[] allShiftsAsStrings = null;
        Shift[] shiftsToReturn = null;

        File[] allDbFiles = new File(_appWorkDirPath).listFiles();
        if(allDbFiles.length == 0)
        {
            errorType = Types.DB_ERROR.DB_FILE_NOT_FOUND;
            return null;
        }
        for(File dBFile : allDbFiles)
        {
            if(dBFile.getName().contains(monthStr) && dBFile.getName().contains(yearStr))
            {
                //Load relevant Month/Year
                allShiftsAsStrings = FileManager.ReadAllLines(dBFile);

                if(allShiftsAsStrings == null)
                    return null;

                //Convert All Lines To Shift Format
                shiftsToReturn = new Shift[allShiftsAsStrings.length];
                for(int i=0; i< allShiftsAsStrings.length; i++)
                {
                    shiftsToReturn[i] = convertStringToShift(allShiftsAsStrings[i]);
                    if(shiftsToReturn[i] == null)
                    {
                        errorType = Types.DB_ERROR.CORRUPTED_FILE;
                        return null;
                    }
                }

                //quit the external for loop
                break;
            }
        }

        return sortShiftsArray(shiftsToReturn);

        //return shiftsToReturn;
    }
    public String[] loadAllFilesNamesAsNumeric()
    {
        File[] allDbFiles = new File(_appWorkDirPath).listFiles();
        List<String> filesNamesToReturn = new ArrayList<String>();
        //String[] filesNamesToReturn = new String[allDbFiles.length];
        for(int i=0; i<allDbFiles.length; i++)
        {
            //if empty file Skip it
            if(FileManager.ReadAllLines(allDbFiles[i]).length == 0
                    || FileManager.ReadAllLines(allDbFiles[i])[0].equalsIgnoreCase(""))
                continue;

            //get file name in format MM/YYYY
            String numericString = convertMonthYearToNumericString(allDbFiles[i].getName());
            if(numericString != null)
                filesNamesToReturn.add(numericString);
            //filesNamesToReturn[i] = numericString;
        }

        return sortFilesNames(filesNamesToReturn.toArray(new String[filesNamesToReturn.size()]));
        //return filesNamesToReturn;
    }
    public void deleteRowFromDB(String rowToDelete, int month, int year)
    {
        Shift[] monthShifts = loadMonthShifts(month, year);
        String[] shiftsAsDBStrings = new String[monthShifts.length - 1];

        File newFile = null;
        String monthStr = DateTime.getMonthString(month);
        String yearStr = String.format("%s", year);
        String[] allShiftsAsStrings = null;
        Shift[] shiftsToReturn = null;

        File[] allDbFiles = new File(_appWorkDirPath).listFiles();
        if(allDbFiles.length == 0)
        {
            errorType = Types.DB_ERROR.DB_FILE_NOT_FOUND;
            return;
        }
        for(File dBFile : allDbFiles)
        {
            if (dBFile.getName().contains(monthStr) && dBFile.getName().contains(yearStr))
            {
                newFile = FileManager.deleteAllData(dBFile);
                break;
            }
        }
        if(newFile == null)
            return;

        //prepare new file
        String dBRow = DataBaseManager.convertShiftAsStringToView_ToDataBaseString(rowToDelete);

        for(int i=0; i<monthShifts.length; i++)
        {
            String shift = convertShiftToString(monthShifts[i]);
            if(shift.contains(dBRow) == false)
                FileManager.writeLine(newFile, shift);
        }
    }
    //For Example: if method receive 11/2016 it should returns November_2016
    public static String convertNumericMonthYearToString(String numericMonthYear)
    {
        String[] items = numericMonthYear.split("/");
        if(items.length != 2)
        {
            return null;
        }
        String month = DateTime.getMonthString(Integer.valueOf(items[0]));
        int year = Integer.valueOf(items[1]);
        return String.format("%s/%d", month, year);
    }
    //For Example: if method receive November_2016 it should returns 11/2016
    public static String convertMonthYearToNumericString(String monthYearAsString)
    {
        try
        {
            String[] items = monthYearAsString.replace(".txt","").split("_");
            if(items.length != 2)
            {
                return null;
            }
            int month = DateTime.getMonthInt(items[0]);
            int year = Integer.valueOf(items[1]);
            return String.format("%02d/%d", month, year);
        }
        catch(Exception e)
        {
            return null;
        }

    }
    public static String convertShiftToString(Shift shiftStr)
    {
        return  String.format("%02d/%02d/%02d,%s,%s,%s",
                shiftStr.shiftDate.getDay(),
                shiftStr.shiftDate.getMonth(),
                shiftStr.shiftDate.getYear(),
                shiftStr.startTime,
                shiftStr.finishTime,
                HoursCalc.convertNumToHourFormat(shiftStr.totalTime)
                );
    }
    public static Shift convertStringToShift(String shiftStr)
    {
        String[] items = shiftStr.split(",");
        if(items.length != 4)
            return null;

        String[] dateArr = items[0].split("/");
        if(dateArr.length != 3)
            return null;

        Shift shift = new Shift();

        //parse the date of the shift and put in the Shift object
        int day = Integer.valueOf(dateArr[0]);
        int month = Integer.valueOf(dateArr[1]);
        int year = Integer.valueOf(dateArr[2]);
        shift.shiftDate = new DateTime(day, month, year);

        //put the shift strings
        shift.startTime = items[1];
        shift.finishTime = items[2];

        //parse the start time string to integers
        String[] startTimeArr = items[1].split(":");
        if(startTimeArr.length != 2)
            return null;

        shift.startHour = Integer.valueOf(startTimeArr[0]);
        shift.startMinute = Integer.valueOf(startTimeArr[1]);

        //parse the finish time string to integers
        String[] finishTimeArr = items[2].split(":");
        if(startTimeArr.length != 2)
            return null;

        shift.finishHour = Integer.valueOf(finishTimeArr[0]);
        shift.finishMinute = Integer.valueOf(finishTimeArr[1]);

        //parse the total hour and put in the Shift object
        double totalHours = HoursCalc.convertHourFormatToDouble(items[3]);
        shift.totalTime = totalHours;

        return shift;
    }
    public static String getShiftAsStringToView(Shift shift)
    {
        String totalTimeStr = HoursCalc.convertNumToHourFormat(shift.totalTime);
        return String.format("%02d/%02d/%d | %s - %s | %s - %s",
                shift.shiftDate.getDay(),
                shift.shiftDate.getMonth(),
                shift.shiftDate.getYear(),
                shift.startTime ,
                shift.finishTime ,
                "סך הכול",
                totalTimeStr );
    }
    public static String convertShiftAsStringToView_ToDataBaseString(String shiftAsStringToView)
    {
        String[] items = shiftAsStringToView.split(" ");
        if(items.length != 10)
            return null;
        String shiftDateAsString = items[0];
        String startTime = items[2];
        String finishTime = items[4];
        String toatlTimeAsString = items[9];

        return String.format("%s,%s,%s,%s", shiftDateAsString, startTime, finishTime, toatlTimeAsString);
    }
    private Shift[] sortShiftsArray(Shift[] shiftsArray)
    {
        Shift[] newArray = new Shift[shiftsArray.length];
        int[] tempArray = new int[shiftsArray.length];
        for(int i=0; i< shiftsArray.length; i++)
        {
            int cnt = 0;
            for(int j =0 ; j<shiftsArray.length; j++)
            {
                if(shiftsArray[i].shiftDate.getDay() > shiftsArray[j].shiftDate.getDay())
                    cnt++;
            }
            tempArray[i] = cnt;
        }

        //initialize the new array
        for(int i=0; i<newArray.length; i++)
        {
            newArray[i] = new Shift();
            newArray[i].shiftDate = new DateTime();
        }
        //use the temp array fo find the correct index of each shift
        for(int i=0; i<newArray.length; i++)
        {
            newArray[tempArray[i]].shiftDate._year = shiftsArray[i].shiftDate._year;
            newArray[tempArray[i]].shiftDate._month = shiftsArray[i].shiftDate._month;
            newArray[tempArray[i]].shiftDate._day = shiftsArray[i].shiftDate._day;
            newArray[tempArray[i]].startTime = shiftsArray[i].startTime;
            newArray[tempArray[i]].finishTime = shiftsArray[i].finishTime;
            newArray[tempArray[i]].totalTime = shiftsArray[i].totalTime;
            newArray[tempArray[i]].finishHour = shiftsArray[i].finishHour;
            newArray[tempArray[i]].finishMinute = shiftsArray[i].finishMinute;
            newArray[tempArray[i]].startMinute = shiftsArray[i].startMinute;
            newArray[tempArray[i]].startHour = shiftsArray[i].startHour;
        }
        return newArray;
    }
    private String[] sortFilesNames(String[] filesNames)
    {
        DateTime[] dateTimeArray = new DateTime[filesNames.length];
        //each string into the array should contains string in this format : 11/2016
        for(int i=0; i<filesNames.length; i++)
        {
            //parse string to 2 int's
            String[] ints = filesNames[i].split("/");
            if(ints.length != 2)
                return null;
            int month = Integer.valueOf(ints[0]);
            int year = Integer.valueOf(ints[0]);

            DateTime dT = new DateTime(1, month, year);
            dateTimeArray[i] = dT;
        }

        //sorting the original array using help array
        int[] tempArray = new int[dateTimeArray.length];
        for(int i=0; i< dateTimeArray.length; i++)
        {
            int cnt = 0;
            for(int j =0 ; j<dateTimeArray.length; j++)
            {
                if(dateTimeArray[i].getMonth() > dateTimeArray[j].getMonth() &&
                        dateTimeArray[i].getYear() > dateTimeArray[j].getYear())
                    cnt++;
            }
            tempArray[i] = cnt;
        }

        //use the temp array fo find the correct index of each shift
        String[] newArray = new String[dateTimeArray.length];
        for(int i=0; i<newArray.length; i++)
        {
            newArray[tempArray[i]] = filesNames[i];
        }

        return newArray;
    }
}
