package com.example.jubransh.workingtime;
import java.io.File;

/**
 * Created by jubransh on 12/17/2016.
 */

public class Settings
{
    //Private Members
    boolean _isToUseNotifications;
    boolean _showFixedSalary;
    boolean _ShowNotFixedSalary;
    boolean _useCurrentTimeAsFinishTime;
    double _payment4Hour;
    double _creditPoints;
    double _pensionFund;
    double _monthlyHourGoal;
    double _completionFund;


    File _settingsFile;
    String _settingsFilePath;

    Settings(String appSettingsPath)
    {
        //if settings directory does not exists, create one
        FileManager.createNewDir(appSettingsPath);

        _settingsFilePath = appSettingsPath + "/" + "AppConfig.cfg";
        if(FileManager.isFileExists(appSettingsPath, "AppConfig.cfg") == false)
            _settingsFile = CreateNewSettingsFile(appSettingsPath, "AppConfig.cfg");
        else
            _settingsFile = new File(_settingsFilePath);

        //String perH = readValue("PaymentForHour");
        //editValue("UserName", "Shadi");
    }

    //=============================== Getters & Setters =================================
    public boolean getUseCurrentTimeAsFinishTime()
    {
        return readValue("GetCurrentTimeAsFinishTime").toLowerCase().contains("true")?true:false;
    }
    public void setUseCurrentTimeAsFinishTime(boolean isToUseCurrentTimeAsFinishTime)
    {
        editValue("GetCurrentTimeAsFinishTime", isToUseCurrentTimeAsFinishTime?"true":"false");
    }

    public double getPay4Hour()
    {
        String val = readValue("PaymentForHour");
        return Double.parseDouble(val);
    }
    public void setPay4Hour(double p4H)
    {
        editValue("PaymentForHour", String.format("%s", p4H));
    }

    public double getMonthlyHourGoal()
    {
        String val = readValue("MonthlyHourGoal");
        return Double.parseDouble(val);
    }
    public void setMonthlyHourGoal(double mHG)
    {
        editValue("MonthlyHourGoal", String.format("%s", mHG));
    }
    public double getTravelsRefund()
    {
        String val = readValue("TravelsRefund");
        return Double.parseDouble(val);
    }
    public void setTravelsRefund(double tR)
    {
        editValue("TravelsRefund", String.format("%s", tR));
    }

    public double getCompletionFund()
    {
        String val = readValue("CompletingFund");
        return Double.parseDouble(val);
    }
    public void setCompletionFund(double cF)
    {
        editValue("CompletingFund", String.format("%s", cF));
    }

    public double getPensionFund()
    {
        String val = readValue("PensionFund");
        return Double.parseDouble(val);
    }
    public void setPensionFund(double pF)
    {
        editValue("PensionFund", String.format("%s", pF));
    }

    public String getMail()
    {
        String val = readValue("Mail");
        return val;
    }

    public void setMail(String mail)
    {
        editValue("Mail", mail);
    }
    public double getCreditPoints()
    {
        String val = readValue("CreditPoints");
        return Double.parseDouble(val);
    }
    public void setCreditPoints(double cP)
    {
        editValue("CreditPoints", String.format("%s", cP));
    }

    public boolean getUseLocation()
    {
        return readValue("UseLocation").toLowerCase().contains("true")?true:false;
    }
    public void setUseLocation(boolean useLocation)
    {
        editValue("UseLocation", useLocation?"True":"False");
    }
    public double getLocationRadius()
    {
        String val = readValue("LocationRadius");
        return Double.parseDouble(val);
    }
    public void setLocationRadius(double lR)
    {
        editValue("LocationRadius", String.format("%s", lR));
    }

    public boolean getUseNotifications()
    {
        String value = readValue("UseNotifications").toLowerCase();
        if(value == null || value.equals(""))
            return false;
        return value.contains("true")?true:false;
    }
    public void setUseNotifications(boolean useNotifications)
    {
        editValue("UseNotifications", useNotifications ?"True":"False");
    }
    //LocationRadius



    public boolean getShowFixedSalary()
    {
        return readValue("ShowFixedSalary").toLowerCase().contains("true")?true:false;
    }
    public void setShowFixedSalary(boolean ShowFixedSalary)
    {
        editValue("ShowFixedSalary", ShowFixedSalary?"True":"False");
    }
    public boolean getShowNotFixedSalary()
    {
        return readValue("ShowNotFixedSalary").toLowerCase().contains("true")?true:false;
    }
    public void setShowNotFixedSalary(boolean showNotFixedSalary)
    {
        editValue("ShowNotFixedSalary", showNotFixedSalary?"True":"False");
    }
    public int[] getWorkDays()
    {
        int[] workDaysArray;
        String workDaysStr =  readValue("WorkDays");
        if(workDaysStr == null || workDaysStr == "")
            return null;
        String[] workDaysStringArray = workDaysStr.split(",");

        //convert work Array to int Array
        workDaysArray = new int[workDaysStringArray.length];
        for (int i=0; i<workDaysStringArray.length; i++)
        {
            workDaysArray[i]= Integer.parseInt(workDaysStringArray[i]);
        }
        return workDaysArray;
    }
    public void setWorkDays(int[] workDays)
    {
        String workDaysStr = "";
        for(int i=0; i<workDays.length; i++)
        {
            workDaysStr += String.format("%d%c",workDays[i], ((i != (workDays.length -1)) ? ',':'.'));
        }
        workDaysStr = workDaysStr.replace(".","");
        editValue("WorkDays", workDaysStr);
    }

    //===============================  Private Methods  ===================================
    private boolean editValue(String item, String val)
    {
        String[] settingsLines = FileManager.ReadAllLines(_settingsFile);
        int indexOfItem = getIndexOfItem(settingsLines, item);
        if(indexOfItem == -1)
            return false;

        //Build the edited Line
        String lineAfterEditing = String.format("<%s>%s</%s>", item, val, item);
        settingsLines[indexOfItem] = lineAfterEditing;

        FileManager.deleteAllData(_settingsFile);
        FileManager.writeAllLines(_settingsFile, settingsLines);
        return true;
    }
    private String readValue(String item)
    {
        String[] settingsLines = FileManager.ReadAllLines(_settingsFile);
        if(settingsLines == null)
            return null;
        int indexOfItem = getIndexOfItem(settingsLines, item);
        if(indexOfItem != -1)
            return settingsLines[indexOfItem].split(">")[1].split("<")[0].replace(" ","");
        return "";
    }
    private int getIndexOfItem(String[] lines, String item)
    {
        for (int i = 0; i < lines.length; i++)
        {
            if(lines[i].toLowerCase().startsWith("<" + item.toLowerCase()))
                return i;
        }
        return -1;
    }
    private File CreateNewSettingsFile(String location, String fileName)
    {
        File sF = null;
        try
        {
            sF = FileManager.createNewFile(location, fileName);
            FileManager.writeLine(sF, "<CreditPoints>2.5</CreditPoints>");
            FileManager.writeLine(sF, "<PensionFund>5.56</PensionFund>");
            FileManager.writeLine(sF, "<CompletingFund>5.56</CompletingFund>");
            FileManager.writeLine(sF, "<PaymentForHour>25</PaymentForHour>");
            FileManager.writeLine(sF, "<TravelsRefund>22</TravelsRefund>");
            FileManager.writeLine(sF, "<MonthlyHourGoal>186</MonthlyHourGoal>");
            FileManager.writeLine(sF, "<UseLocation>false</UseLocation>");
            FileManager.writeLine(sF, "<LocationRadius>20</LocationRadius>");
            FileManager.writeLine(sF, "<UseNotifications>false</UseNotifications>");
            FileManager.writeLine(sF, "<WorkDays>1,2,3,4,5</WorkDays>");
            FileManager.writeLine(sF, "<ShowFixedSalary>true</ShowFixedSalary>");
            FileManager.writeLine(sF, "<GetCurrentTimeAsFinishTime>true</GetCurrentTimeAsFinishTime>");
            FileManager.writeLine(sF, "<ShowNotFixedSalary>false</ShowNotFixedSalary>");
            FileManager.writeLine(sF, "<Mail>shady.jubran@gmail.com</Mail>");
        }
        catch (Exception e)
        {
            return null;
        }
        return sF;
    }

}
