package net.antineutrino.dnd_dice;
import java.util.Locale;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Button button4, button6, button8, button10, button12, button20, buttonRoll;
    EditText editTextTotal, editTextDetails, editDisplay;
    Spinner spinnerNumDice, spinnerBonus;
    String details = "";
    int dieSides = 12;
    int numDice = 1;
    int bonus = 0;

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
                editDisplay.setText(R.string.d4);
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button6.setSelected(true);
                dieSides = 6;
                editDisplay.setText(R.string.d6);
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button8.setSelected(true);
                dieSides = 8;
                editDisplay.setText(R.string.d8);
            }
        });

        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button10.setSelected(true);
                dieSides = 10;
                editDisplay.setText(R.string.d10);
            }
        });

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button12.setSelected(true);
                dieSides = 12;
                editDisplay.setText(R.string.d12);
            }
        });

        button20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectDice();
                button20.setSelected(true);
                dieSides = 20;
                editDisplay.setText(R.string.d20);
            }
        });

        buttonRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] rolls = rollDice(dieSides, numDice);
                int total = bonus;
                editTextTotal.setText("");
                editTextDetails.setText("");
                details = getString(R.string.rolls) + ": ";
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAbout:
                // This code will start the new activity when the settings button is clicked on the bar at the top.
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
                return true;
            case R.id.menuGitHub:
                Toast.makeText(this, "Show Link", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStart() {
        super.onStart();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        bonus = sharedPref.getInt("bonus", 0);
        numDice = sharedPref.getInt("numDice", 1);
        dieSides = sharedPref.getInt("dieSides", 12);

        spinnerBonus.setSelection(bonus);
        spinnerNumDice.setSelection(numDice - 1);
        if (dieSides == 4) {
            button4.setSelected(true);
            editDisplay.setText(R.string.d4);
        } else if (dieSides == 6) {
            button6.setSelected(true);
            editDisplay.setText(R.string.d6);
        } else if (dieSides == 8) {
            button8.setSelected(true);
            editDisplay.setText(R.string.d8);
        } else if (dieSides == 10) {
            button10.setSelected(true);
            editDisplay.setText(R.string.d10);
        } else if (dieSides == 12) {
            button12.setSelected(true);
            editDisplay.setText(R.string.d12);
        } else {
            button20.setSelected(true);
            editDisplay.setText(R.string.d20);
        }
    }

    public void onStop() {
        super.onStop();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("dieSides", dieSides);
        editor.putInt("numDice", ((Spinner) findViewById(R.id.spinnerNumDice)).getSelectedItemPosition() + 1);
        editor.putInt("bonus", ((Spinner) findViewById(R.id.spinnerBonus)).getSelectedItemPosition());
        editor.apply();
    }

    /**
     * Just ensure that if one die size is selected, the rest aren't.
     */
    protected void deSelectDice() {
        ((Button) findViewById(R.id.button4)).setSelected(false);
        ((Button) findViewById(R.id.button6)).setSelected(false);
        ((Button) findViewById(R.id.button8)).setSelected(false);
        ((Button) findViewById(R.id.button10)).setSelected(false);
        ((Button) findViewById(R.id.button12)).setSelected(false);
        ((Button) findViewById(R.id.button20)).setSelected(false);
    }
}
