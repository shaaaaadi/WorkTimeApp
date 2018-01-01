package com.example.jubransh.workingtime;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jubransh on 11/15/2016.
 */

public class DateTime
{
    int _dayOfWeek;
    int _day;
    int _month;
    int _year;

    int _hour;
    int _minute;

    public DateTime(int dd, int mm, int yyyy)
    {
        _day = dd;
        _month = mm;
        _year = yyyy;
    }

    public DateTime()
    {
        Date date = new Date(System.currentTimeMillis()); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        _year = cal.get(Calendar.YEAR);
        _month = cal.get(Calendar.MONTH) + 1;
        _day = cal.get(Calendar.DAY_OF_MONTH);
        _hour = cal.get(Calendar.HOUR_OF_DAY);
        _minute = cal.get(Calendar.MINUTE);
    }

    public int getYear()
    {
        return _year;
    }
    public int getMonth()
    {
        return _month;
    }
    public int getDay()
    {
        return _day;
    }

    public int getCurrentHour()
    {
        return _hour;
    }
    public int getCurrentMinute()
    {
        return _minute;
    }
    public  String getCurrentMonthAsString()
    {
        switch (_month)
        {
            case 1: return "January";
            case 2: return "February ";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October ";
            case 11: return "November";
            case 12: return "December";
            default: return "None";
        }
    }
    public static String getMonthString(int month)
    {
        switch (month)
        {
            case 1:     return "January";
            case 2:     return "February";
            case 3:     return "March";
            case 4:     return "April";
            case 5:     return "May";
            case 6:     return "June";
            case 7:     return "July";
            case 8:     return "August";
            case 9:     return "September";
            case 10:    return "October";
            case 11:    return "November";
            case 12:    return "December";
            default:    return "None";
        }
    }
    public static int getMonthInt(String month)
    {
        switch (month)
        {
            case "January":     return 1;
            case "February":    return 2;
            case "March":       return 3;
            case "April":       return 4;
            case "May":         return 5;
            case "June":        return 6;
            case "July":        return 7;
            case "August":      return 8;
            case "September":   return 9;
            case "October":     return 10;
            case "November":    return 11;
            case "December":    return 12;
            default:            return 0;
        }
    }
    public static String getNextMonthYear(String currentMonth, int currentYear)
    {
        String nextMonthString;
        int yearToreturn = currentYear;
        int nextMonth = getMonthInt(currentMonth);

        if(nextMonth == 12) //next month will be in the next year
        {
            yearToreturn ++;
            nextMonthString = getMonthString(1);
            return String.format("%s_%s", nextMonthString, yearToreturn);
        }
        nextMonth++;
        nextMonthString = getMonthString(nextMonth);
        return String.format("%s_%s", nextMonthString, yearToreturn);
    }


}
