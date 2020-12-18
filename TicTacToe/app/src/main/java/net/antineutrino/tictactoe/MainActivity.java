package net.antineutrino.tictactoe;
import java.util.Random;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean frozenAtWin = false;
    private Button[][] buttons = new Button[3][3];
    private int pointsPlayer = 0;
    private int pointsAI = 0;
    private int roundCount = 0;
    private String emptySymbol = "";
    private String playerSymbol = "X";
    private String aiSymbol = "O";
    private TextView tvPlayer;
    private TextView tvAI;

    // TODO: Move this out of the class
    // Implementing Fisher–Yates shuffle
    static void shuffleArray(int[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

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

        frozenAtWin = false;
        Button btnReset = findViewById(R.id.btnDifficulty);  // TODO: I don't want a "reset" button. I want a difficulty button.
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    @Override
    public void onClick(View v) {
        // After each win, we want to pause for a second on the winning board position
        if (frozenAtWin) {
            resetBoard();
            return;
        }

        // TODO: If start or end of game... if the players turn, we're good. If it's the AI's turn, we need to moveAI()

        // if this button has already been pressed, just move on
        if (!((Button) v).getText().toString().equals(emptySymbol)) {
            return;
        }

        // mark the square
        ((Button) v).setText(playerSymbol);

        // rev the round count
        roundCount++;

        // parse the board into a 3x3 int map
        int[][] board = parseBoard();

        // check if the game has been won, and handle it if so
        if (checkForWin(board)) {
            playerWins();
            return;
        } else if (roundCount == 9) {
            draw();
            return;
        }

        // time for the AI player to move
        moveAI(board);

        // check if the game has been won, and handle it if so
        if (checkForWin(board)) {
            aiWins();
        } else if (roundCount == 9) {
            draw();
        }
    }

    private void moveAI(int[][] board) {
        randomMoveAI(board);
        // TODO: 50/50 chance to hit random or pretty good AI
        // TODO: pretty good AI
    }

    private void randomMoveAI(int[][] board) {
        // randomize the order the AI will search in
        int[] rows = {0, 1, 2};
        shuffleArray(rows);
        int[] cols = {0, 1, 2};
        shuffleArray(cols);

        // The AI will hunt in random order and pick the first empty box it finds.
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[rows[r]][cols[c]] == 0) {
                    board[rows[r]][cols[c]] = 2;
                    buttons[rows[r]][cols[c]].setText(aiSymbol);
                    roundCount++;
                    return;
                }
            }
        }
    }

    private int[][] parseBoard() {
        int[][] board = new int[3][3];
        String val;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                val = buttons[r][c].getText().toString();
                if (val.equals(playerSymbol)) {
                    board[r][c] = 1;
                } else if (val.equals(aiSymbol)) {
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

    private void playerWins() {
        pointsPlayer++;
        Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
        updateScoreboard();
        frozenAtWin = true;
    }

    private void aiWins() {
        pointsAI++;
        Toast.makeText(this, "AI wins!", Toast.LENGTH_SHORT).show();
        updateScoreboard();
        frozenAtWin = true;
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

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c].setText(emptySymbol);
            }
        }

        frozenAtWin = false;
    }

    private void resetGame() {
        pointsPlayer = 0;
        pointsAI = 0;
        frozenAtWin = false;
        updateScoreboard();
        resetBoard();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("roundCount", roundCount);
        outState.putInt("pointsPlayer", pointsPlayer);
        outState.putInt("pointsAI", pointsAI);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        roundCount = savedInstanceState.getInt("roundCount");
        pointsPlayer = savedInstanceState.getInt("pointsPlayer");
        pointsAI = savedInstanceState.getInt("pointsAI");
    }
}
