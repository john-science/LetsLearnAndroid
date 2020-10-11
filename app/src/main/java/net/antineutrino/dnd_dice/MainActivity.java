package net.antineutrino.dnd_dice;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

// TODO: How do we remember the user's (3) selections between user log-ins?
// TODO: It looks like I have a title bar, can I have a ":" drop down there for 'about this ap'?
// TODO: Localization! Spanish. Maybe German, Norwegian, Swedish, and Icelandic? Hindi, Mandarin...

public class MainActivity extends AppCompatActivity {

    Button button4, button6, button8, button10, button12, button20, buttonRoll;
    EditText editTextTotal, editTextDetails, editDisplay;
    int dieSides, numDice, bonus = 0;
    Spinner spinnerNumDice, spinnerBonus;
    String details = "";

    /**
     * Rolls a single N-Sided die.
     *
     * @param sides number of sides of the die
     * @return random die roll (from 1 to sides inclusive)
     */
    protected static int rollOneDie(int sides) {
        return (int) (Math.random() * (sides) + 1);
    }

    /**
     * Rolls X N-Sided die.
     *
     * @param sides   number of sides of the die
     * @param numDice number of dice
     * @return rolls values of several fair dice rolls
     */
    protected static int[] rollDice(int sides, int numDice) {
        int[] rolls = new int[numDice];
        for (int i = 0; i < rolls.length; i++) {
            rolls[i] = rollOneDie(sides);
        }

        return rolls;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button4 = (Button) findViewById(R.id.button4);
        button6 = (Button) findViewById(R.id.button6);
        button8 = (Button) findViewById(R.id.button8);
        button10 = (Button) findViewById(R.id.button10);
        button12 = (Button) findViewById(R.id.button12);
        button20 = (Button) findViewById(R.id.button20);
        buttonRoll = (Button) findViewById(R.id.buttonRoll);
        spinnerNumDice = (Spinner) findViewById(R.id.spinnerNumDice);
        spinnerBonus = (Spinner) findViewById(R.id.spinnerBonus);
        editTextTotal = (EditText) findViewById(R.id.edtTotal);
        editTextDetails = (EditText) findViewById(R.id.edtDetails);
        editDisplay = (EditText) findViewById(R.id.edtDisplay);

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button4.setSelected(true);
                dieSides = 4;
                editDisplay.setText("D4");
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button6.setSelected(true);
                dieSides = 6;
                editDisplay.setText("D6");
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button8.setSelected(true);
                dieSides = 8;
                editDisplay.setText("D8");
            }
        });

        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button10.setSelected(true);
                dieSides = 10;
                editDisplay.setText("D10");
            }
        });

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button12.setSelected(true);
                dieSides = 12;
                editDisplay.setText("D12");
            }
        });

        button20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button20.setSelected(true);
                dieSides = 20;
                editDisplay.setText("D20");
            }
        });

        buttonRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] rolls = rollDice(dieSides, numDice);
                int total = bonus;
                editTextTotal.setText("");
                editTextDetails.setText("");
                details = "rolls: ";
                for (int i = 0; i < rolls.length; i++) {
                    total += rolls[i];
                    if (i == 0) {
                        details += String.format(Locale.US, "%5d", rolls[i]);
                    } else {
                        details += ", " + String.format(Locale.US, "%5d", rolls[i]);
                    }
                    editTextDetails.setText(details);
                }

                editTextTotal.setText(String.format(Locale.US, "%5d", total));
            }
        });

        spinnerNumDice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                numDice = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }  /* stub */
        });

        spinnerBonus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bonus = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }  /* stub */
        });

        // set up initial state
        button12.setSelected(true);
        dieSides = 12;
        editDisplay.setText("D12");
        spinnerNumDice.setSelection(0);
        numDice = 1;
        spinnerBonus.setSelection(0);
        bonus = 0;
    }

    /**
     * Just ensure that if one die size is selected, the rest aren't.
     */
    protected void deSelectDice() {
        button4.setSelected(false);
        button6.setSelected(false);
        button8.setSelected(false);
        button10.setSelected(false);
        button12.setSelected(false);
        button20.setSelected(false);
    }
}