package com.example.newcalculator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private boolean lastNumeric;
    private boolean stateError;
    private boolean lastDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);

        setNumericOnClickListener();
        setOperatorOnClickListener();
    }

    private void setNumericOnClickListener() {
        int[] numericButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numericButtons) {
            findViewById(id).setOnClickListener(view -> {
                if (stateError) {
                    // If current state is Error, replace the error message
                    tvDisplay.setText(((Button) view).getText());
                    stateError = false;
                } else {
                    // If not, append the text
                    tvDisplay.append(((Button) view).getText());
                }
                lastNumeric = true;
            });
        }

        // Decimal point
        findViewById(R.id.btnDecimal).setOnClickListener(view -> {
            if (lastNumeric && !lastDot && !stateError) {
                tvDisplay.append(".");
                lastNumeric = false;
                lastDot = true;
            }
        });
    }

    private void setOperatorOnClickListener() {
        int[] operatorButtons = {
                R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide
        };

        for (int id : operatorButtons) {
            findViewById(id).setOnClickListener(view -> {
                if (lastNumeric && !stateError) {
                    tvDisplay.append(((Button) view).getText());
                    lastNumeric = false;
                    lastDot = false;
                }
            });
        }

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(view -> {
            tvDisplay.setText("");
            lastNumeric = false;
            stateError = false;
            lastDot = false;
        });

        // Delete button
        findViewById(R.id.btnDelete).setOnClickListener(view -> {
            int length = tvDisplay.getText().length();
            if (length > 0) {
                tvDisplay.setText(tvDisplay.getText().subSequence(0, length - 1));
            }
        });

        // Equal button
        findViewById(R.id.btnEquals).setOnClickListener(view -> onEqual());
    }

    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = tvDisplay.getText().toString();
            try {
                double result = evaluate(txt);
                tvDisplay.setText(Double.toString(result));
                lastDot = true; // Result contains a dot
            } catch (Exception ex) {
                tvDisplay.setText("Error");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    // Simple expression evaluator
    private double evaluate(final String expression) {
        return new Object() {
            int pos = -1;
            int ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
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
                if (pos < expression.length()) throw new RuntimeException("Unexpected character: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression '+' term | expression '-' term
            // term = factor | term '*' factor | term '/' factor
            // factor = '+' factor | '-' factor | number | '(' expression ')'

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('−') || eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('×') || eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('÷') || eat('/')) x /= parseFactor(); // division
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
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected character: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}
