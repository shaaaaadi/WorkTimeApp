package com.example.jubransh.workingtime;

/**
 * Created by jubransh on 12/31/2016.
 */

public class SalaryCalc
{
    int _shiftsAmount;
    Settings _appSettings;
    double _payment4Hour, _pensionFund, _travelsRefund, _creditPoints, _completionFund;

    public SalaryCalc(String _appSettingsPath, int shiftsAmount)
    {

        _shiftsAmount = shiftsAmount;
        _appSettings = new Settings(_appSettingsPath);
        _payment4Hour = _appSettings.getPay4Hour();
        _pensionFund = _appSettings.getPensionFund();
        _creditPoints = _appSettings.getCreditPoints();
        _completionFund = _appSettings.getCompletionFund();
        _travelsRefund = _appSettings.getTravelsRefund();
    }
    public SalaryParts calculateSalary(double totalHours)
    {
        SalaryParts sP = new SalaryParts();
        sP.TravelFund = _shiftsAmount * _travelsRefund;
        sP.NotFixedSalary = sP.TravelFund + _payment4Hour * totalHours;
        sP.PensionFund = (_pensionFund/100) * sP.NotFixedSalary;
        sP.CompletionFund = (_completionFund/100) * sP.CompletionFund;
        sP.NationalInsurance =  calcNationalInsurance( sP.NotFixedSalary);
        sP.HealthTax =          calcHealthTax(sP.NotFixedSalary); // Not Final
        sP.IncomeTax = calcTax(sP.NotFixedSalary);

        sP.FixedSalary =  sP.NotFixedSalary
                        - sP.PensionFund
                        - sP.CompletionFund
                        - sP.NationalInsurance
                        - sP.HealthTax
                        - sP.IncomeTax;
        return sP;
    }
    private double calcTax(double notFixedSalary)
    {
        double tax = 0;
        int level1 = 6220;
        int level2 = 8920;
        int level3 = 14320;
        int level4 = 19900;
        int level5 = 41410;
        int level6 = 53333;

        if(notFixedSalary > level6)
            tax = (notFixedSalary - level6) * 0.5 + 16940;
        else if(notFixedSalary > level5)
            tax = (notFixedSalary - level5) * 0.47 + 11335;
        else if(notFixedSalary > level4)
            tax = (notFixedSalary - level4) * 0.35 + 3810;
        else if(notFixedSalary > level3)
            tax = (notFixedSalary - level3) * 0.31 + 2080;
        else if(notFixedSalary > level2)
            tax = (notFixedSalary - level2) * 0.20 + 1000;
        else if(notFixedSalary > level1)
            tax = (notFixedSalary - level1) * 0.14 + 620;
        else
            tax = 0;

        return tax;

    }
    private double calcNationalInsurance(double notFixedSalary)
    {
        double tax = 0;
        int level1 = 5297;
        int level2 = 42435;
        double level1Tax = 0.004;
        double level2Tax = 0.07;

        if(notFixedSalary <= level1)
            tax = level1Tax * notFixedSalary;
        else if (notFixedSalary <= level2)
            tax = (notFixedSalary - level1) * level2Tax +  (level1 * level1Tax);
        else
            tax = level1 * level1Tax + (level2 - level1) * level2Tax;
        return  tax;
    }
    private double calcHealthTax(double notFixedSalary)
    {
        double tax = 0;
        int level1 = 5297;
        int level2 = 42435;
        double level1Tax = 0.031;
        double level2Tax = 0.05;

        if(notFixedSalary <= level1)
            tax = level1Tax * notFixedSalary;
        else if (notFixedSalary <= level2)
            tax = (notFixedSalary - level1) * level2Tax +  (level1 * level1Tax);
        else
            tax = level1 * level1Tax + (level2 - level1) * level2Tax;
        return  tax;
    }
}
