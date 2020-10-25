package net.antineutrino.dnd_dice;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


public class RandomRollsTest {

    @Test
    public void roll_one_die_works() {
        int random_roll = 1;
        for (int i = 0; i < 100; i++) {
            random_roll = RandomRolls.rollOneDie(6);
            assertTrue(random_roll > 0);
            assertTrue(random_roll < 7);
        }
    }

    @Test
    public void roll_dice_works() {
        int[] rolls = RandomRolls.rollDice(20, 100);

        for (int roll : rolls) {
            assertTrue(roll > 0);
            assertTrue(roll < 21);
        }
    }
}