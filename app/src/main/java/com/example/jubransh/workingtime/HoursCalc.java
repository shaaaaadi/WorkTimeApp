package com.example.jubransh.workingtime;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by jubransh on 11/16/2016.
 */

public final class HoursCalc
{
    public static double getDifferenceAsFloat(int startHour, int startMinute, int finishHour, int finishMinute)
    {
        double diff = finishHour - startHour;
        int minutesDiff = finishMinute - startMinute;
        double minutesDiffAsFloat = (Math.abs(minutesDiff))/((double)60);
        if(minutesDiff > 0) // finishminute > StartMinute
            diff += minutesDiffAsFloat;
        else                //StartMinute > FinishMinute
            diff -= minutesDiffAsFloat;

        return diff;
    }

    //This method should receive the dates in this format: DD/MM/YYYY and the Time in this format: HH:MM
    public static String getToatalTime(String startDate, String stopDate, String startTime, String stopTime)
    {
        String[] startDateArray = startDate.split("/");
        String[] stopDateArray = stopDate.split("/");
        String[] startTimeArray = startTime.split(":");
        String[] stopTimeArray = stopTime.split(":");

        //validating the correctness of the received strings skipped. assume that they are legal

        //parsing the string times/ dates to integers
        int startHour, startMinute, stopHour, stopMinute, startDay, stopDay, startMonth, stopMonth, startyear, stopYear;

        //Start Time
        startHour = Integer.parseInt(startTimeArray[0]);
        startMinute = Integer.parseInt(startTimeArray[1]);

        //Stop Time
        stopHour = Integer.parseInt(stopTimeArray[0]);
        stopMinute = Integer.parseInt(stopTimeArray[1]);

        //Start Date
        startDay = Integer.parseInt(startDateArray[0]);
        startMonth = Integer.parseInt(startDateArray[1]);
        startyear = Integer.parseInt(startDateArray[2]);

        //Stop Date
        stopDay = Integer.parseInt(stopDateArray[0]);
        stopMonth = Integer.parseInt(stopDateArray[1]);
        stopYear = Integer.parseInt(stopDateArray[2]);

        Calendar sC = Calendar.getInstance();
        Calendar fC = Calendar.getInstance();

        sC.set(startyear,startMonth,startDay,startHour,startMinute,0);
        fC.set(stopYear,stopMonth,stopDay,stopHour,stopMinute,0);

        long diff = fC.getTimeInMillis() - sC.getTimeInMillis();
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        return String.format("%02d:%02d",totalMinutes/60, totalMinutes % 60);
    }

    public static boolean isLegalShift(int startHour, int startMinute, int finishHour, int finishMinute, String[] errorString)
    {
        //check that the Hours is from 0 to 23
        if(finishHour < 0 || finishHour > 23 || startHour < 0 && startHour > 23)
        {
            errorString[0] = "פורמט שעות לא חוקי - השתמש בין 0 - 23";//"Illegal Hours Format";
            return false;
        }

        //check that the Minutes is from 0 to 59
        if(finishMinute < 0 || finishMinute > 59 || startMinute < 0 || startMinute > 59)
        {
            errorString[0] = "פורמט דקות לא חוקי - השתמש בין 00 - 59";//"Illegal Minute Format";
            return false;
        }
        //limitation: if the worker started the shift at 23:00 and finished next day at 07:00
        //validate that the start time is not after the finish time
        if(startHour > finishHour)
        {
            errorString[0] = "שעת כניסה לא יכולה להיות אחרי שעת יציאה באותו יום";//"start Hour couldn't be after the finish hour";
            return false;
        }
        if(startHour == finishHour && startMinute > finishMinute)
        {
            errorString[0] = "זמן כניסה לא יכול להיות אחרי זמן יציאה באותו יום";//"start time couldn't be after the finish time";
            return false;
        }

        return true;
    }
    public static String convertNumToHourFormat(double num)
    {
        int hours = (int)num;
        double minutes = num - hours;
        String finalString = String.format("%d:%d",(int)num, (int)(minutes*60));

        return finalString;
    }
    public static double convertHourFormatToDouble(String hourFormatStr)
    {
        double numToReturn;
        String[] items = hourFormatStr.split(":");
        if(items.length != 2)
            return -1;
        numToReturn = Double.valueOf(items[0]);
        numToReturn += Double.valueOf(items[1]) / 60.0;

        return numToReturn;
    }
    public static String getAllShiftsTotalTime(Shift[] allShifts)
    {
        double allShiftsTotalTime = 0;
        if (allShifts == null)
            return "";

        for (Shift shift : allShifts)
        {
            allShiftsTotalTime += shift.totalTime;
        }

        //convert double total time to string
        return convertNumToHourFormat(allShiftsTotalTime);
    }
    public static double getAllShiftsTotalTimeAsDouble(Shift[] allShifts)
    {
        double allShiftsTotalTime = 0;
        if (allShifts == null)
            return 0;

        for (Shift shift : allShifts)
        {
            allShiftsTotalTime += shift.totalTime;
        }

        return allShiftsTotalTime;
    }

    public static HoursAnalysisResult analyzeHours(int[] workDays, int hoursGoal, double currentTotalHours,
                           int currDay, int currMonth, int currYear)
    {
        int dayOfWeek = getDayOfWeek(currDay, currMonth, currYear);
        HoursAnalysisResult hAR = new HoursAnalysisResult();

        //identify status
        hAR.WorkDaysInMonth = getWorkDaysInMonth(workDays,dayOfWeek, currDay, currMonth, currYear);
        hAR.ExpectedDailyGoal = (double)hoursGoal / hAR.WorkDaysInMonth;
        hAR.CurrentTotalDays = getCurrentTotal(workDays, dayOfWeek, currDay, currMonth, currYear);
        hAR.ExpectedCurrentHours = hAR.ExpectedDailyGoal * hAR.CurrentTotalDays;
        hAR.RestMonthDays = hAR.WorkDaysInMonth - hAR.CurrentTotalDays;
        hAR.RestMonthHours = hoursGoal - currentTotalHours;
        hAR.NewDailyGoal = hAR.RestMonthHours  / hAR.RestMonthDays;
        hAR.MissingHours = hAR.ExpectedCurrentHours - currentTotalHours;

        return hAR;
    }
    private static int getCurrentTotal(int[] workDays, /*sunday=1 - */int dayOfWeek, int currDay, int currMonth, int currentYear)
    {
        int expectedCurrentTotal = 0;
        try
        {
            int firstDay = getFirstDayOfMonth(dayOfWeek, currDay); //Sunday =1 - Monday=2 .........
            int day = firstDay;
            for (int i = 1; i <= currDay; i++)
            {
                for (int j = 0; j < workDays.length; j++)
                {
                    if (workDays[j] == day)
                    {
                        expectedCurrentTotal++;
                        break;
                    }
                }
                day = ++day % 7;
            }
        }
        catch(Exception e)
        {

        }
        return expectedCurrentTotal;
    }
    private static int getWorkDaysInMonth(int[] workDays, int dayOfWeek, int currDay, int currMonth, int currentYear)
    {
        int count = 0;
        //su  mo  Tu  We  Th  Fr  Sa
        //---------------------------
        //----------  01  02  03  04
        //05  06  07  08  09  10  11
        //12  13  14  15  16  17  18
        //19  20  21  22  23  24  25
        //26  27  28  --------------

        //get First Day In Month
        int firstDay = getFirstDayOfMonth(dayOfWeek, currDay);
        int availableDaysInMonth = getMonthDays(currMonth, currentYear);

        //count work days in month
        int day = firstDay;
        for (int i = 1; i <= availableDaysInMonth; i++)
        {
            for (int j = 0; j < workDays.length; j++)
            {
                if (workDays[j] == day)
                {
                    count++;
                    break;
                }
            }
            day = ++day % 7;
        }
        return count;
    }
    private static int getMonthDays(int month, int year)
    {
        switch (month)
        {
            case 2:
                if (year % 4 == 0)
                    return 29;
                else
                    return 28;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12: return 31;
            case 4:
            case 6:
            case 9:
            case 11: return 30;
            default: return 0;
        }
    }
    //this method should return, which day (sunday/mondy/....) is the first day in the month
    private static int getFirstDayOfMonth(int dayOfWeek, int currentDay)
    {
        int daysToBack = currentDay - 1;
        int fD = dayOfWeek - (daysToBack % 7);
        return fD <= 0 ? 7- Math.abs(fD) : fD;
    }
    private static int getDayOfWeek(int day, int month, int year)
    {
        //First date is 1/1/1900 Monday
        double daysInYear = 365.25;
        int firstDay = 1;
        int firstYear = 1900;

        //calculate Diff in days:
        int diffDays = 0;

        //Adding Days
        diffDays += day - firstDay;

        //Adding Months
        for(int i=1; i < month; i++)
        {
            diffDays += getMonthDays(i, year);
        }

        //Adding Years
        diffDays += (int)(daysInYear * (year - firstYear));

        //totalYears = diffDays / (int)daysInYear;
        int restOfDays = diffDays % (int)daysInYear;
        return (restOfDays % 7 == 0) ? 7 : restOfDays % 7;
    }
}
