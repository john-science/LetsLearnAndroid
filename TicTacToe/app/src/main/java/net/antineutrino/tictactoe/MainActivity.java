package net.antineutrino.tictactoe;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int player1Points = 0;
    private int player2Points = 0;
    private int roundCount = 0;
    private TextView tvPlayer1;
    private TextView tvPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPlayer1 = findViewById(R.id.text_view_p1);
        tvPlayer2 = findViewById(R.id.text_view_p2);

        for (int r=0; r < 3; r++) {
            for (int c=0; c < 3; c++) {
                int resID = getResources().getIdentifier("btn" + r + c, "id", getPackageName());
                buttons[r][c] = findViewById(resID);
                buttons[r][c].setOnClickListener(this);
            }
        }

        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        // if this button has already been pressed, just move on
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        // mark the square
        if (player1Turn) {
            ((Button) v).setText("X");
        } else {
            ((Button) v).setText("O");
        }

        // rev the round count
        roundCount++;

        // check if the game has been won, and handle it if so
        if (checkForWin()) {
            if (player1Turn) {
                player1Wins();
            } else {
                player2Wins();
            }
        } else if (roundCount == 9) {
            draw();
        } else {
            player1Turn = !player1Turn;
        }
    }

    private void player1Wins() {

    }

    private void player2Wins() {

    }

    private void draw() {

    }

    private boolean checkForWin() {
        String[][] values = new String[3][3];

        for (int r=0; r < 3; r++) {
            for (int c=0; c < 3; c++) {
                values[r][c] = buttons[r][c].getText().toString();
            }
        }

        // check for winning columns
        for (int r=0; r<3; r++) {
            if (values[r][0].equals(values[r][1]) && values[r][0].equals(values[r][2]) && !values[r][0].equals("")) {
                return true;
            }
        }

        // check for winning rows
        for (int c=0; c<3; c++) {
            if (values[0][c].equals(values[1][c]) && values[0][c].equals(values[2][c]) && !values[0][c].equals("")) {
                return true;
            }
        }

        // check for winning diagonals
        if (values[0][0].equals(values[1][1]) && values[0][0].equals(values[2][2]) && !values[0][0].equals("")) {
            return true;
        }

        if (values[2][0].equals(values[1][1]) && values[2][0].equals(values[0][2]) && !values[2][0].equals("")) {
            return true;
        }

        return false;
    }
}