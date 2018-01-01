package com.example.jubransh.workingtime;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CalculatorActivity extends Activity implements View.OnClickListener
{
    TextView viewScreen;
    boolean isToStratFromScratch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        isToStratFromScratch = true;
        viewScreen = (TextView) findViewById(R.id.viewScreen);
    }

    @Override
    public void onClick(View v)
    {
        Button b = (Button)v;
        String currentButtonText = b.getText().toString();
        String currentTextInViewScreen = viewScreen.getText().toString();

        if(currentButtonText.equals("AC") && currentTextInViewScreen.length() != 0)
        {
            viewScreen.setText("");
            return;
        }
        else if(currentButtonText.equals("AC"))
            return;

        if(currentButtonText.equals("C") && currentTextInViewScreen.length() != 0)
            viewScreen.setText(currentTextInViewScreen.substring(0,currentTextInViewScreen.length()-1));
        else if (currentButtonText.equals("C"))
            return;

        else if (currentButtonText.equals("="))
        {
            try
            {
                double res = eval(viewScreen.getText().toString());
                viewScreen.setText(Double.toString(res));
            }
            catch (Exception ex)
            {
                viewScreen.setText(ex.getMessage());
            }
            isToStratFromScratch = true;
        }
        else
        {
            if(isToStratFromScratch)
                viewScreen.setText("");
            isToStratFromScratch = false;
            viewScreen.setText(viewScreen.getText().toString() + currentButtonText);
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

    private  double eval(final String str)
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
