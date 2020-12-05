package net.antineutrino.tictactoe;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];
    private boolean playerTurn = true;
    private int pointsPlayer = 0;
    private int pointsAI = 0;
    private int roundCount = 0;
    private String emptySymbol = "";
    private String player1Symbol = "X";
    private String player2Symbol = "O";
    private TextView tvPlayer;
    private TextView tvAI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPlayer = findViewById(R.id.text_view_player);
        tvAI = findViewById(R.id.text_view_ai);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int resID = getResources().getIdentifier("btn" + r + c, "id", getPackageName());
                buttons[r][c] = findViewById(resID);
                buttons[r][c].setOnClickListener(this);
            }
        }

        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    @Override
    public void onClick(View v) {
        // if this button has already been pressed, just move on
        if (!((Button) v).getText().toString().equals(emptySymbol)) {
            return;
        }

        // mark the square
        if (playerTurn) {
            ((Button) v).setText(player1Symbol);
        } else {
            ((Button) v).setText(player2Symbol);
        }

        // rev the round count
        roundCount++;

        // parse the board into a 3x3 int map
        int[][] board = parseBoard();

        // check if the game has been won, and handle it if so
        if (checkForWin(board)) {
            if (playerTurn) {
                player1Wins();
            } else {
                player2Wins();
            }
        } else if (roundCount == 9) {
            draw();
        } else {
            playerTurn = !playerTurn;
        }
    }

    private int[][] parseBoard() {
        int[][] board = new int[3][3];
        String val;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                val = buttons[r][c].getText().toString();
                if (val.equals(player1Symbol)) {
                    board[r][c] = 1;
                } else if (val.equals(player2Symbol)) {
                    board[r][c] = 2;
                }
            }
        }

        return board;
    }

    private boolean checkForWin(int[][] board) {
        // check for winning columns
        for (int r = 0; r < 3; r++) {
            if (board[r][0] != 0 && board[r][0] == board[r][1] && board[r][0] == board[r][2]) {
                return true;
            }
        }

        // check for winning rows
        for (int c = 0; c < 3; c++) {
            if (board[0][c] != 0 && board[0][c] == board[1][c] && board[0][c] == board[2][c]) {
                return true;
            }
        }

        // check for winning diagonals
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            return true;
        }

        if (board[2][0] != 0 && board[2][0] == board[1][1] && board[2][0] == board[0][2]) {
            return true;
        }

        return false;
    }

    private void player1Wins() {
        pointsPlayer++;
        Toast.makeText(this, "Player 1 wins!", Toast.LENGTH_SHORT).show();
        updateScoreboard();
        resetBoard();
    }

    private void player2Wins() {
        pointsAI++;
        Toast.makeText(this, "Player 2 wins!", Toast.LENGTH_SHORT).show();
        updateScoreboard();
        resetBoard();
    }

    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
        resetBoard();
    }

    private void updateScoreboard() {
        tvPlayer.setText("Player: " + pointsPlayer);
        tvAI.setText("AI: " + pointsAI);
    }

    // wipe the board clean, and start fresh
    private void resetBoard() {
        roundCount = 0;
        playerTurn = true;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c].setText("");
            }
        }
    }

    private void resetGame() {
        pointsPlayer = 0;
        pointsAI = 0;
        updateScoreboard();
        resetBoard();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("roundCount", roundCount);
        outState.putInt("pointsPlayer", pointsPlayer);
        outState.putInt("pointsAI", pointsAI);
        outState.putBoolean("player1Turn", playerTurn);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        roundCount = savedInstanceState.getInt("roundCount");
        pointsPlayer = savedInstanceState.getInt("pointsPlayer");
        pointsAI = savedInstanceState.getInt("pointsAI");
        playerTurn = savedInstanceState.getBoolean("player1Turn");
    }
}