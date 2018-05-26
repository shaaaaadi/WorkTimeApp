package com.example.jubransh.workingtime;

import java.util.Calendar;
import java.util.Date;

/**
 * This class is a DateTime wrapper Class contains some android bugs workarounds
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public class DateTime
{
    int mDay;
    int mMonth;
    int mYear;

    int mHour;
    int mMinute;

    /** Class Constructor**/
    public DateTime(int dd, int mm, int yyyy)
    {
        mDay = dd;
        mMonth = mm;
        mYear = yyyy;
    }

    public DateTime()
    {
        Date date = new Date(System.currentTimeMillis()); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH) + 1;
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMinute = cal.get(Calendar.MINUTE);
    }

    /**
     * mYear Getter
     * @return int, mYear.
     */
    public int getYear()
    {
        return mYear;
    }

    /**
     * mMonth Getter
     * @return int, mMonth.
     */
    public int getMonth()
    {
        return mMonth;
    }

    /**
     * mDay Getter
     * @return int, mDay.
     */
    public int getDay()
    {
        return mDay;
    }

    /**
     * mDay Getter
     * @return int, mDay.
     */
    public int getCurrentHour()
    {
        return mHour;
    }

    /**
     * mDay Getter
     * @return int, mDay.
     */
    public int getCurrentMinute()
    {
        return mMinute;
    }

    /**
     * This static method parses int number to string Month
     *      1 : "January"
     *      2 : "February"
     *      3 : "March"
     *      .
     *      .
     *      .
     *      12 : "December"
     * @param month number as int
     * @return string, month name.
     */
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

    /**
     * This static method parses the string of the Month to int (from 1 - 12)
     * @param month name as string
     * @return int, month as int.
     */
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
}
