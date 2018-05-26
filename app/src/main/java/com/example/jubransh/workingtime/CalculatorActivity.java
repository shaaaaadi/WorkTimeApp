package com.example.jubransh.workingtime;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
/**
 * This activity is calculator activity supporting basic math operations
 * extends standard android activity
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */
public class CalculatorActivity extends Activity implements View.OnClickListener
{
    TextView mViewScreen;
    boolean mIsToStartFromScratch;

    /**
     * Creating all the GUI objects,
     * initializing all the GUI objects listeners,
     * filling the start time / end time automatically if needed (related to what configured in the settings)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        mIsToStartFromScratch = true;
        mViewScreen = (TextView) findViewById(R.id.viewScreen);
    }

    /**
     * overriding the GUI onClick events, and decide what to do when clicking on each calc button
     */
    @Override
    public void onClick(View v)
    {
        Button b = (Button)v;
        String currentButtonText = b.getText().toString();
        String currentTextInViewScreen = mViewScreen.getText().toString();

        if(currentButtonText.equals("AC") && currentTextInViewScreen.length() != 0)
        {
            mViewScreen.setText("");
            return;
        }
        else if(currentButtonText.equals("AC"))
            return;

        if(currentButtonText.equals("C") && currentTextInViewScreen.length() != 0)
            mViewScreen.setText(currentTextInViewScreen.substring(0,currentTextInViewScreen.length()-1));
        else if (currentButtonText.equals("C"))
            return;

        else if (currentButtonText.equals("="))
        {
            try
            {
                double res = eval(mViewScreen.getText().toString());
                mViewScreen.setText(Double.toString(res));
            }
            catch (Exception ex)
            {
                mViewScreen.setText(ex.getMessage());
            }
            mIsToStartFromScratch = true;
        }
        else
        {
            if(mIsToStartFromScratch)
                mViewScreen.setText("");
            mIsToStartFromScratch = false;
            mViewScreen.setText(mViewScreen.getText().toString() + currentButtonText);
        }

    }


   /* private boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }*/

    /**
     * This is method calculate math expression
     * this method parses the string and extract it to operations
     * @param str math expression as string
     * @return double, the result of the calculation.
     */
    private double eval(final String str)
    {
        try
        {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < str.length()) ? str.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return x;
                }

                // Grammar:
                // expression = term | expression `+` term | expression `-` term
                // term = factor | term `*` factor | term `/` factor
                // factor = `+` factor | `-` factor | `(` expression `)`
                //        | number | functionName factor | factor `^` factor

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if      (eat('+')) x += parseTerm(); // addition
                        else if (eat('-')) x -= parseTerm(); // subtraction
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if      (eat('*')) x *= parseFactor(); // multiplication
                        else if (eat('/')) x /= parseFactor(); // division
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor(); // unary plus
                    if (eat('-')) return -parseFactor(); // unary minus

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) { // parentheses
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(str.substring(startPos, this.pos));
                    } else if (ch >= 'a' && ch <= 'z') { // functions
                        while (ch >= 'a' && ch <= 'z') nextChar();
                        String func = str.substring(startPos, this.pos);
                        x = parseFactor();
                        if (func.equals("sqrt")) x = Math.sqrt(x);
                        else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                        else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                        else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                        else throw new RuntimeException("Unknown function: " + func);
                    } else {
                        throw new RuntimeException("Unexpected: " + (char)ch);
                    }

                    if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                    return x;
                }
            }.parse();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Syntax Error !");
        }
    }
}
