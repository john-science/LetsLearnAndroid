
package org.petero.droidfish.gamelogic;


public class Move {
    /** From square, 0-63. */
    public int from;

    /** To square, 0-63. */
    public int to;

    /** Promotion piece. */
    public int promoteTo;

    /** Create a move object. */
    public Move(int from, int to, int promoteTo) {
        this.from = from;
        this.to = to;
        this.promoteTo = promoteTo;
    }

    public Move(Move m) {
        this.from = m.from;
        this.to = m.to;
        this.promoteTo = m.promoteTo;
    }

    /** Create object from compressed representation. */
    public static Move fromCompressed(int cm) {
        return new Move((cm >> 10) & 63, (cm >> 4) & 63, cm & 15);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != this.getClass()))
            return false;
        Move other = (Move)o;
        if (from != other.from)
            return false;
        if (to != other.to)
            return false;
        if (promoteTo != other.promoteTo)
            return false;
        return true;
    }
    @Override
    public int hashCode() {
        return getCompressedMove();
    }

    /** Get move as a 16-bit value. */
    public int getCompressedMove() {
        return (from * 64 + to) * 16 + promoteTo;
    }

    /** Useful for debugging. */
    public final String toString() {
        return TextIO.moveToUCIString(this);
    }
}
