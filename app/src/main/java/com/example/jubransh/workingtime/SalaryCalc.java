package com.example.jubransh.workingtime;

/**
 * This class is salary calculator class
 * this class uses all salary components from settings database to calculate the final salary
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */

public class SalaryCalc
{
    int mShiftsAmount;
    Settings mAppSettings;
    double mPayment4Hour, mPensionFund, mTravelsRefund, mCreditPoints, mCompletionFund;

    public SalaryCalc(String _appSettingsPath, int shiftsAmount)
    {
        mShiftsAmount = shiftsAmount;
        mAppSettings = new Settings(_appSettingsPath);
        mPayment4Hour = mAppSettings.getPay4Hour();
        mPensionFund = mAppSettings.getPensionFund();
        mCreditPoints = mAppSettings.getCreditPoints();
        mCompletionFund = mAppSettings.getCompletionFund();
        mTravelsRefund = mAppSettings.getTravelsRefund();
    }


    public SalaryParts calculateSalary(double totalHours)
    {
        SalaryParts sP = new SalaryParts();
        sP.TravelFund = mShiftsAmount * mTravelsRefund;
        sP.NotFixedSalary = sP.TravelFund + mPayment4Hour * totalHours;
        sP.PensionFund = (mPensionFund /100) * sP.NotFixedSalary;
        sP.CompletionFund = (mCompletionFund /100) * sP.CompletionFund;
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

    /**
     * calculating the value of the Tax from the not fixed salary
     * @param notFixedSalary
     * @return double, the tax amount.
     */
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

    /**
     * calculating the value of the National Insurance Tax from the not fixed salary
     * @param notFixedSalary
     * @return double, the tax amount.
     */
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

    /**
     * calculating the value of the Health Insurance Tax from the not fixed salary
     * @param notFixedSalary
     * @return double, the tax amount.
     */
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
