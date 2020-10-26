package net.antineutrino.dnd_dice;


public class RandomRolls {

    /**
     * Rolls a single N-Sided die.
     *
     * @param sides number of sides of the die
     * @return random die roll (from 1 to sides inclusive)
     */
    public static int rollOneDie(int sides) {
        return (int) (Math.random() * (sides) + 1);
    }

    /**
     * Rolls X N-Sided die.
     *
     * @param sides   number of sides of the die
     * @param numDice number of dice
     * @return rolls values of several fair dice rolls
     */
    public static int[] rollDice(int sides, int numDice) {
        int[] rolls = new int[numDice];
        for (int i = 0; i < rolls.length; i++) {
            rolls[i] = rollOneDie(sides);
        }

        return rolls;
    }
}
