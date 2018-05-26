package com.example.jubransh.workingtime;

import android.support.v4.view.animation.FastOutLinearInInterpolator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * This class is a a Data Base Manager
 *the data base of the application is located into files into the android device
 * This class is responsible to save, edit, delete file, raws or data from those files
 * This file uses the FileManager Class to perform the operations above
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */


public class DataBaseManager
{
    public String mAppWorkDirPath;
    Types.DB_ERROR mErrorType;
    File mDbFile;

    public DataBaseManager(String appWorkingDirPath)
    {
        mErrorType = Types.DB_ERROR.NO_ERROR;
        mAppWorkDirPath = appWorkingDirPath;
    }
    /**
     * This method saves new raw to the database using the FileManager (a Final Class)
     * @param month the month number
     * @param year the year number
     * @param shiftRow the string should be saved into the database file of the file: month/year.txt
     * @return boolean. True if success, else false.
     */
    public boolean saveShiftToDataBase(int month, int year, String shiftRow)
    {
        String monthStr = DateTime.getMonthString(month);

        try
        {
            //Create Month DB file if not exists
            String currentMonthFileName =  String.format("%s_%d.txt",monthStr, year);
            mDbFile = FileManager.createNewFile(mAppWorkDirPath, currentMonthFileName);
            FileManager.writeLine(mDbFile, shiftRow);
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    /**
     * This method loads all the shifts of all months/years from database and check if shift conflict with on of them
     * @param shift the shift we want to check if it valid number of the wanted month
     * @return boolean. True if the shift is valid and false otherwise.
     */
    public boolean isValidShift(Shift shift)
    {
        File[] allDbFiles = new File(mAppWorkDirPath).listFiles();
        if(allDbFiles.length == 0)
            return true;

        for(File dBFile : allDbFiles)
        {
            //Load relevant Month/Year
            String[] allShiftsAsStrings = FileManager.ReadAllLines(dBFile);

            if(allShiftsAsStrings == null)
                return true;

            //Convert All Lines To Shift Format
            Shift[] dbShifts = new Shift[allShiftsAsStrings.length];
            for(int i=0; i< allShiftsAsStrings.length; i++)
            {
                dbShifts[i] = convertStringToShift(allShiftsAsStrings[i]);
                int shiftStartTime = shift.StartHour*100 + shift.StartMinute;
                int shiftFinishTime = shift.FinishHour*100 + shift.FinishMinute;

                int dbShiftStartTime = dbShifts[i].StartHour*100 + dbShifts[i].StartMinute;
                int dbShiftFinishTime = dbShifts[i].FinishHour*100 + dbShifts[i].FinishMinute;

                if(shift.ShiftDate.isTheSameDay(dbShifts[i].ShiftDate)) {
                    if (shiftStartTime < dbShiftFinishTime) {
                        if (shiftStartTime > dbShiftStartTime) {
                            return false;
                        }
                        else if (shiftFinishTime > dbShiftFinishTime){
                            return false;
                        }
                    }

                    if (shiftStartTime < dbShiftStartTime){
                        if(shiftFinishTime > dbShiftStartTime){
                            return false;
                        }
                    }
                    else {
                        if(shiftStartTime == dbShiftStartTime)
                            return false;
                        else {
                            if(shiftStartTime < dbShiftFinishTime)
                                return false;
                        }
                    }
                }

            }
        }

        return true;
    }
    /**
     * This method loads all the shifts of specific month/year from database
     * @param month the month number of the wanted month
     * @param year the year number of the wanted year
     * @return Shift[]. Array of all the shifts of the wanted month/year as Shift objects.
     */
    public Shift[] loadMonthShifts(int month, int year)
    {
        String monthStr = DateTime.getMonthString(month);
        String yearStr = String.format("%s", year);
        String[] allShiftsAsStrings = null;
        Shift[] shiftsToReturn = null;

        File[] allDbFiles = new File(mAppWorkDirPath).listFiles();
        if(allDbFiles.length == 0)
        {
            mErrorType = Types.DB_ERROR.DB_FILE_NOT_FOUND;
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
                        mErrorType = Types.DB_ERROR.CORRUPTED_FILE;
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

    /**
     * This method loads all the non-empty database files in string format
     * for example, if database has 2 month files
     *      - December_2017.txt
     *      - Januray_2018.txt
     * the method should return array of 2 strings => [12/2017, 01/2018]
     * @return String[]. Array of month/year as numeric (see the explaining above)
     */
    public String[] loadAllFilesNamesAsNumeric()
    {
        File[] allDbFiles = new File(mAppWorkDirPath).listFiles();
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

    /**
     * This method deletes specific shift from the database
     * @param month the month number of the wanted month
     * @param year the year number of the wanted year
     * @param rowToDelete the wanted shift to be deleted (as string)
     * @return nothing.
     */
    public void deleteRowFromDB(String rowToDelete, int month, int year)
    {
        Shift[] monthShifts = loadMonthShifts(month, year);
        String[] shiftsAsDBStrings = new String[monthShifts.length - 1];

        File newFile = null;
        String monthStr = DateTime.getMonthString(month);
        String yearStr = String.format("%s", year);
        String[] allShiftsAsStrings = null;
        Shift[] shiftsToReturn = null;

        File[] allDbFiles = new File(mAppWorkDirPath).listFiles();
        if(allDbFiles.length == 0)
        {
            mErrorType = Types.DB_ERROR.DB_FILE_NOT_FOUND;
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

    /**
     * This method converts the month/year to int string to be saved to the data base
     * For Example: if method receive November_2016 it should returns 11/2016
     * @param monthYearAsString the name of the database file w/o extension
     * @return String (see explaining above).
     */
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

    /**
     * This method converts Shift object to formatted string
     * this method should be used to parse shift to a database format
     * @param shiftStr as Shift object
     * @return String formatted string of the shift object
     */
    public static String convertShiftToString(Shift shiftStr)
    {
        return  String.format("%02d/%02d/%02d,%s,%s,%s",
                shiftStr.ShiftDate.getDay(),
                shiftStr.ShiftDate.getMonth(),
                shiftStr.ShiftDate.getYear(),
                shiftStr.StartTime,
                shiftStr.finishTime,
                HoursCalc.convertNumToHourFormat(shiftStr.totalTime)
                );
    }

    /**
     * This method converts formatted shift (as string) to a shift object
     * This Method should be used when Database manger loads the shift from database
     * @param shiftStr as string
     * @return Shift Shift Object
     */
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
        shift.ShiftDate = new DateTime(day, month, year);

        //put the shift strings
        shift.StartTime = items[1];
        shift.finishTime = items[2];

        //parse the start time string to integers
        String[] startTimeArr = items[1].split(":");
        if(startTimeArr.length != 2)
            return null;

        shift.StartHour = Integer.valueOf(startTimeArr[0]);
        shift.StartMinute = Integer.valueOf(startTimeArr[1]);

        //parse the finish time string to integers
        String[] finishTimeArr = items[2].split(":");
        if(startTimeArr.length != 2)
            return null;

        shift.FinishHour = Integer.valueOf(finishTimeArr[0]);
        shift.FinishMinute = Integer.valueOf(finishTimeArr[1]);

        //parse the total hour and put in the Shift object
        double totalHours = HoursCalc.convertHourFormatToDouble(items[3]);
        shift.totalTime = totalHours;

        return shift;
    }

    /**
     * This method converts the shift object to formatted string to be displayed
     * @param shift as Shift object
     * @return String shift to view as string
     */
    public static String getShiftAsStringToView(Shift shift)
    {
        String totalTimeStr = HoursCalc.convertNumToHourFormat(shift.totalTime);
        return String.format("%02d/%02d/%d | %s - %s | %s - %s",
                shift.ShiftDate.getDay(),
                shift.ShiftDate.getMonth(),
                shift.ShiftDate.getYear(),
                shift.StartTime,
                shift.finishTime ,
                "סך הכול",
                totalTimeStr );
    }

    /**
     * This method converts the shift as string to view to formatted string in database format
     * @param shiftAsStringToView as String
     * @return String shift as string in database raw format to be saved in the database later
     */
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

    /**
     * This method is responsible to sort the Shift array
     * This method uses the Bubble sorting
     * @param shiftsArray as Shift[]
     * @return Shift[] sorted shifts
     */
    private Shift[] sortShiftsArray_old(Shift[] shiftsArray)
    {
        Shift[] newArray = new Shift[shiftsArray.length];
        int[] tempArray = new int[shiftsArray.length];
        for(int i=0; i< shiftsArray.length; i++)
        {
            int cnt = 0;
            for(int j =0 ; j<shiftsArray.length; j++)
            {
                if(shiftsArray[i].ShiftDate.getDay() > shiftsArray[j].ShiftDate.getDay())
                    cnt++;
            }
            tempArray[i] = cnt;
        }

        //initialize the new array
        for(int i=0; i<newArray.length; i++)
        {
            newArray[i] = new Shift();
            newArray[i].ShiftDate = new DateTime();
        }
        //use the temp array fo find the correct index of each shift
        for(int i=0; i<newArray.length; i++)
        {
            newArray[tempArray[i]].ShiftDate.mYear = shiftsArray[i].ShiftDate.mYear;
            newArray[tempArray[i]].ShiftDate.mMonth = shiftsArray[i].ShiftDate.mMonth;
            newArray[tempArray[i]].ShiftDate.mDay = shiftsArray[i].ShiftDate.mDay;
            newArray[tempArray[i]].StartTime = shiftsArray[i].StartTime;
            newArray[tempArray[i]].finishTime = shiftsArray[i].finishTime;
            newArray[tempArray[i]].totalTime = shiftsArray[i].totalTime;
            newArray[tempArray[i]].FinishHour = shiftsArray[i].FinishHour;
            newArray[tempArray[i]].FinishMinute = shiftsArray[i].FinishMinute;
            newArray[tempArray[i]].StartMinute = shiftsArray[i].StartMinute;
            newArray[tempArray[i]].StartHour = shiftsArray[i].StartHour;
        }
        return newArray;
    }

    private Shift[] sortShiftsArray(Shift[] shiftsArray)
    {
        //Bubble sorting
        int n = shiftsArray.length;
        Shift temp = new Shift();
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (shiftsArray[j - 1].ShiftDate.getDay() > shiftsArray[j].ShiftDate.getDay()) {
                    //swap elements
                    temp = shiftsArray[j - 1];
                    shiftsArray[j - 1] = shiftsArray[j];
                    shiftsArray[j] = temp;
                }

            }
        }
        return shiftsArray;
    }
    /**
     * This method is responsible to sort the files names (the database files names)
     * this method is sorting the files according to the date (name) of the file
     * This method uses the Bubble sorting
     * @param filesNames as String[]
     * @return String[] sorted shifts
     */
    private String[] sortFilesNames(String[] filesNames)
    {
        DateTime[] dateTimeArray = new DateTime[filesNames.length];
        //each string into the array should contains string in this format : 11/2016
        for(int i=0; i<filesNames.length; i++)
        {
            //parse string to 2 int'mSettings
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
