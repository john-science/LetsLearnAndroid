/*
    DroidFish - An Android chess program.
    Copyright (C) 2011  Peter Österlund, peterosterlund2@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.petero.droidfish.book;

import android.annotation.SuppressLint;
import android.util.Pair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.petero.droidfish.Util;
import org.petero.droidfish.book.IOpeningBook.BookPosInput;
import org.petero.droidfish.gamelogic.Move;
import org.petero.droidfish.gamelogic.MoveGen;
import org.petero.droidfish.gamelogic.Position;
import org.petero.droidfish.gamelogic.TextIO;

/** Implements an opening book. */
public final class DroidBook {
    static final class BookEntry {
        Move move;
        float weight;
        BookEntry(Move move) {
            this.move = move;
            weight = 1;
        }
        @Override
        public String toString() {
            return TextIO.moveToUCIString(move) + " (" + weight + ")";
        }
    }
    @SuppressLint("TrulyRandom")
    private Random rndGen = new SecureRandom();

    private IOpeningBook externalBook = new NullBook();
    private IOpeningBook ecoBook = new EcoBook();
    private IOpeningBook internalBook = new InternalBook();
    private IOpeningBook noBook = new NoBook();
    private BookOptions options = null;

    private static final DroidBook INSTANCE = new DroidBook();

    /** Get singleton instance. */
    public static DroidBook getInstance() {
        return INSTANCE;
    }

    private DroidBook() {
    }

    /** Set opening book options. */
    public final synchronized void setOptions(BookOptions options) {
        this.options = options;
        if (CtgBook.canHandle(options))
            externalBook = new CtgBook();
        else if (PolyglotBook.canHandle(options))
            externalBook = new PolyglotBook();
        else if (AbkBook.canHandle(options))
            externalBook = new AbkBook();
        else
            externalBook = new NullBook();
        externalBook.setOptions(options);
        ecoBook.setOptions(options);
        internalBook.setOptions(options);
        noBook.setOptions(options);
    }

    /** Return a random book move for a position, or null if out of book. */
    public final synchronized Move getBookMove(BookPosInput posInput) {
        Position pos = posInput.getCurrPos();
        if ((options != null) && (pos.fullMoveCounter > options.maxLength))
            return null;
        List<BookEntry> bookMoves = getBook().getBookEntries(posInput);
        if (bookMoves == null || bookMoves.isEmpty())
            return null;

        ArrayList<Move> legalMoves = new MoveGen().legalMoves(pos);
        double sum = 0;
        final int nMoves = bookMoves.size();
        for (int i = 0; i < nMoves; i++) {
            BookEntry be = bookMoves.get(i);
            if (!legalMoves.contains(be.move)) {
                // If an illegal move was found, it means there was a hash collision,
                // or a corrupt external book file.
                return null;
            }
            sum += scaleWeight(bookMoves.get(i).weight);
        }
        if (sum <= 0) {
            return null;
        }
        double rnd = rndGen.nextDouble() * sum;
        sum = 0;
        for (int i = 0; i < nMoves; i++) {
            sum += scaleWeight(bookMoves.get(i).weight);
            if (rnd < sum)
                return bookMoves.get(i).move;
        }
        return bookMoves.get(nMoves-1).move;
    }

    /** Return all book moves, both as a formatted string and as a list of moves. */
    public final synchronized Pair<String,ArrayList<Move>> getAllBookMoves(BookPosInput posInput,
                                                                           boolean localized) {
        Position pos = posInput.getCurrPos();
        StringBuilder ret = new StringBuilder();
        ArrayList<Move> bookMoveList = new ArrayList<>();
        ArrayList<BookEntry> bookMoves = getBook().getBookEntries(posInput);

        // Check legality
        if (bookMoves != null) {
            ArrayList<Move> legalMoves = new MoveGen().legalMoves(pos);
            for (int i = 0; i < bookMoves.size(); i++) {
                BookEntry be = bookMoves.get(i);
                if (!legalMoves.contains(be.move)) {
                    bookMoves = null;
                    break;
                }
            }
        }

        if (bookMoves != null) {
            Collections.sort(bookMoves, (arg0, arg1) -> {
                double wd = arg1.weight - arg0.weight;
                if (wd != 0)
                    return (wd > 0) ? 1 : -1;
                String str0 = TextIO.moveToUCIString(arg0.move);
                String str1 = TextIO.moveToUCIString(arg1.move);
                return str0.compareTo(str1);
            });
            double totalWeight = 0;
            for (BookEntry be : bookMoves)
                totalWeight += scaleWeight(be.weight);
            if (totalWeight <= 0) totalWeight = 1;
            boolean first = true;
            for (BookEntry be : bookMoves) {
                Move m = be.move;
                bookMoveList.add(m);
                String moveStr = TextIO.moveToString(pos, m, false, localized);
                if (first)
                    first = false;
                else
                    ret.append(' ');
                ret.append(Util.boldStart);
                ret.append(moveStr);
                ret.append(Util.boldStop);
                ret.append(':');
                int percent = (int)Math.round(scaleWeight(be.weight) * 100 / totalWeight);
                ret.append(percent);
            }
        }
        return new Pair<>(ret.toString(), bookMoveList);
    }

    private double scaleWeight(double w) {
        if (w <= 0)
            return 0;
        if (options == null)
            return w;
        return Math.pow(w, Math.exp(-options.random));
    }

    private IOpeningBook getBook() {
        if (externalBook.enabled()) {
            return externalBook;
        } else if (ecoBook.enabled()) {
            return ecoBook;
        } else if (noBook.enabled()) {
            return noBook;
        } else {
            return internalBook;
        }
    }
}
