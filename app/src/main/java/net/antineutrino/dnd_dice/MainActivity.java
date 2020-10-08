package net.antineutrino.dnd_dice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    Button button4, button6, button8, button10, button12, button20, buttonRoll;
    EditText editText;
    int dieSides = 0;

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
        editText = (EditText) findViewById(R.id.edt1);

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectAll();
                button4.setSelected(true);
                dieSides = 4;
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectAll();
                button6.setSelected(true);
                dieSides = 6;
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectAll();
                button8.setSelected(true);
                dieSides = 8;
            }
        });

        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectAll();
                button10.setSelected(true);
                dieSides = 10;
            }
        });

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectAll();
                button12.setSelected(true);
                dieSides = 12;
            }
        });

        button20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deSelectAll();
                button20.setSelected(true);
                dieSides = 20;
            }
        });

        buttonRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(String.format(Locale.US, "%5d", rollOneDie(dieSides)));
            }
        });
    }

    /**
     * Rolls a single N-Sided die. This die is more fair than most.
     *
     * @param sides number of sides of the die
     * @return random die roll (from 1 to sides inclusive)
     */
    protected int rollOneDie(int sides) {
        return (int) (Math.random() * (sides) + 1);
    }

    /**
     * Just ensure that if one die size is selected, the rest aren't.
     */
    protected void deSelectAll() {
        button4.setSelected(false);
        button6.setSelected(false);
        button8.setSelected(false);
        button10.setSelected(false);
        button12.setSelected(false);
        button20.setSelected(false);
    }
}