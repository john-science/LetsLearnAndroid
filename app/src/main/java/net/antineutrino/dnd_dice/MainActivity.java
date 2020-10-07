package net.antineutrino.dnd_dice;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Button button4, button6, button8, button10, button12, button20, buttonRoll;
    EditText editText;
    int dieSides = 0;

    protected void deClickAll() {
        button4.setPressed(false);
        button6.setPressed(false);
        button8.setPressed(false);
        button10.setPressed(false);
        button12.setPressed(false);
        button20.setPressed(false);
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
        editText = (EditText) findViewById(R.id.edt1);

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deClickAll();
                button4.setPressed(true);
                dieSides = 4;
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deClickAll();
                button6.setPressed(true);
                dieSides = 6;
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deClickAll();
                button8.setPressed(true);
                dieSides = 8;
            }
        });

        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deClickAll();
                button10.setPressed(true);
                dieSides = 10;
            }
        });

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deClickAll();
                button12.setPressed(true);
                dieSides = 12;
            }
        });

        button20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deClickAll();
                button20.setPressed(true);
                dieSides = 20;
            }
        });

        buttonRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(dieSides + " Boom!");
            }
        });
    }
}