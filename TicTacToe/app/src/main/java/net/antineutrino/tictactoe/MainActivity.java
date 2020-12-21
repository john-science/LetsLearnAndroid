package net.antineutrino.tictactoe;
import java.util.Random;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static int maxDifficulty = 2;
    private boolean frozenAtWin = false;
    private boolean lastWinWasPlayer = false;
    private Button[][] buttons = new Button[3][3];
    private int difficulty = 0;
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
        displayDifficulty();

        Button btnDiff = findViewById(R.id.btnDifficulty);
        btnDiff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementDifficulty();
            }
        });
    }

    protected void incrementDifficulty() {
        difficulty += 1;
        if (difficulty > maxDifficulty) {
            difficulty = 0;
        }

        displayDifficulty();
    }

    protected void displayDifficulty() {
        Resources res = getResources();

        if (difficulty == 2) {
            ((Button) findViewById(R.id.btnDifficulty)).setText(res.getString(R.string.hard));
        } else if (difficulty == 1) {
            ((Button) findViewById(R.id.btnDifficulty)).setText(res.getString(R.string.medium));
        } else {
            ((Button) findViewById(R.id.btnDifficulty)).setText(res.getString(R.string.easy));
        }
    }

    @Override
    public void onClick(View v) {
        // After each win, we want to pause for a second on the winning board position
        if (frozenAtWin) {
            resetBoard();
            return;
        }

        // if this button has already been pressed, just move on
        if (!((Button) v).getText().toString().equals(emptySymbol)) {
            return;
        }

        // parse the board into a 3x3 int map
        int[][] board = parseBoard();

        // The first player in the game will be the one that lost the last game
        boolean skip = false;
        if (roundCount == 0 && lastWinWasPlayer) {
            skip = true;
        }

        if (!skip) {
            // mark the square
            ((Button) v).setText(playerSymbol);

            board = parseBoard();

            // rev the round count
            roundCount++;

            // check if the game has been won, and handle it if so
            if (checkForWin(board)) {
                playerWins();
                return;
            } else if (roundCount == 9) {
                draw();
                return;
            }
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
        if (difficulty == 2) {
            // decent AI
            betterMoveAI(board);
        } else if (difficulty == 1) {
            // 50/50 chance to hit smart or stupid AI
            Random rnd = new Random();
            if (rnd.nextInt(2) == 0) {
                randomMoveAI(board);
            } else {
                betterMoveAI(board);
            }
        } else {
            // totally random AI
            randomMoveAI(board);
        }
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

    private void firstCornerMoveAI(int[][] board) {
        // randomize the order of the corners
        int[] rows = {0, 2};
        shuffleArray(rows);
        int[] cols = {0, 2};
        shuffleArray(cols);

        // pick the first random corner, and have the AI go there (NOTE: This assumes the corners are all open)
        board[rows[0]][cols[0]] = 2;
        buttons[rows[0]][cols[0]].setText(aiSymbol);
        roundCount++;
    }

    private void betterMoveAI(int[][] board) {
        if (roundCount == 0) {
            // If it's the first move, grab a corner
            firstCornerMoveAI(board);
        } else if (roundCount == 1) {
            if (board[1][1] == 0) {
                // If it's the second move, grab the center if you can
                board[1][1] = 2;
                buttons[1][1].setText(aiSymbol);
                roundCount++;
            } else {
                // If the center is taken, grab a random corner
                firstCornerMoveAI(board);
            }
        } else {
            // TODO: check if we can win this turn, if so do it
            // TODO: check if the player can win this turn, if so block it (if they can win two ways, we only block one)
            // If neither are true, make a random move
            randomMoveAI(board);
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
        lastWinWasPlayer = true;
    }

    private void aiWins() {
        pointsAI++;
        Toast.makeText(this, "AI wins!", Toast.LENGTH_SHORT).show();
        updateScoreboard();
        frozenAtWin = true;
        lastWinWasPlayer = false;
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

        outState.putInt("difficulty", difficulty);
        outState.putInt("pointsAI", pointsAI);
        outState.putInt("pointsPlayer", pointsPlayer);
        outState.putInt("roundCount", roundCount);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        difficulty = savedInstanceState.getInt("difficulty");
        pointsAI = savedInstanceState.getInt("pointsAI");
        pointsPlayer = savedInstanceState.getInt("pointsPlayer");
        roundCount = savedInstanceState.getInt("roundCount");
    }
}
