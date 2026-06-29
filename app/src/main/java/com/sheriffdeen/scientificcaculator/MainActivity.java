package com.sheriffdeen.scientificcaculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvInput, tvResult;
    private GridLayout scientificPanel, basicPanel;
    private Button btnMode;
    private boolean isScientificVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvInput = findViewById(R.id.tvInput);
        tvResult = findViewById(R.id.tvResult);
        scientificPanel = findViewById(R.id.scientificPanel);
        basicPanel = findViewById(R.id.basicPanel);
        btnMode = findViewById(R.id.btnMode);

        btnMode.setOnClickListener(v -> {
            isScientificVisible = !isScientificVisible;
            scientificPanel.setVisibility(isScientificVisible ? View.VISIBLE : View.GONE);
            btnMode.setText(isScientificVisible ? "Basic" : "Scientific");
        });

        setButtonClickListeners(basicPanel);
        setButtonClickListeners(scientificPanel);
    }

    private void setButtonClickListeners(GridLayout panel) {
        int childCount = panel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = panel.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                button.setOnClickListener(this::onButtonClick);
            }
        }
    }

    private void onButtonClick(View view) {
        Button button = (Button) view;
        String text = button.getText().toString();

        switch (text) {
            case "C":
                tvInput.setText("");
                tvResult.setText("");
                break;
            case "DEL":
                String currentText = tvInput.getText().toString();
                if (!currentText.isEmpty()) {
                    tvInput.setText(currentText.substring(0, currentText.length() - 1));
                }
                break;
            case "=":
                String expression = tvInput.getText().toString();
                try {
                    double result = evaluate(expression);
                    // Check if it's a whole number to display nicely
                    if (result == (long) result) {
                        tvResult.setText(String.valueOf((long) result));
                    } else {
                        tvResult.setText(String.valueOf(result));
                    }
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
                break;
            case "sin":
            case "cos":
            case "tan":
            case "log":
            case "ln":
            case "abs":
                tvInput.append(text + "(");
                break;
            case "√":
                tvInput.append("sqrt(");
                break;
            case "π":
                tvInput.append(String.valueOf(Math.PI));
                break;
            case "e":
                tvInput.append(String.valueOf(Math.E));
                break;
            case "inv":
                tvInput.append("1/");
                break;
            default:
                tvInput.append(text);
                break;
        }
    }

    // A robust mathematical expression evaluator
    public static double evaluate(final String str) {
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
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing closing parenthesis");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing closing parenthesis after function " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else if (func.equals("log")) x = Math.log10(x);
                    else if (func.equals("ln")) x = Math.log(x);
                    else if (func.equals("abs")) x = Math.abs(x);
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                if (eat('!')) x = factorial((int)x); // factorial

                return x;
            }

            double factorial(int n) {
                if (n < 0) return 0;
                double res = 1;
                for (int i = 2; i <= n; i++) res *= i;
                return res;
            }
        }.parse();
    }
}
