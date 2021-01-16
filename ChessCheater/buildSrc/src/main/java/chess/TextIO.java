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

package chess;

import java.util.ArrayList;
import java.util.List;

/** Handle conversion of positions and moves to/from text format. */
public class TextIO {
    static public final String startPosFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /** Parse a FEN string and return a chess Position object. */
    public static Position readFEN(String fen) throws ChessParseError {
        fen = fen.trim();
        Position pos = new Position();
        String[] words = fen.split(" ");
        if (words.length < 2)
            throw new ChessParseError("too few pieces");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
        }

        // Piece placement
        int row = 7;
        int col = 0;
        for (int i = 0; i < words[0].length(); i++) {
            char c = words[0].charAt(i);
            switch (c) {
                case '1': col += 1; break;
                case '2': col += 2; break;
                case '3': col += 3; break;
                case '4': col += 4; break;
                case '5': col += 5; break;
                case '6': col += 6; break;
                case '7': col += 7; break;
                case '8': col += 8; break;
                case '/': row--; col = 0; break;
                case 'P': safeSetPiece(pos, col, row, Piece.WPAWN);   col++; break;
                case 'N': safeSetPiece(pos, col, row, Piece.WKNIGHT); col++; break;
                case 'B': safeSetPiece(pos, col, row, Piece.WBISHOP); col++; break;
                case 'R': safeSetPiece(pos, col, row, Piece.WROOK);   col++; break;
                case 'Q': safeSetPiece(pos, col, row, Piece.WQUEEN);  col++; break;
                case 'K': safeSetPiece(pos, col, row, Piece.WKING);   col++; break;
                case 'p': safeSetPiece(pos, col, row, Piece.BPAWN);   col++; break;
                case 'n': safeSetPiece(pos, col, row, Piece.BKNIGHT); col++; break;
                case 'b': safeSetPiece(pos, col, row, Piece.BBISHOP); col++; break;
                case 'r': safeSetPiece(pos, col, row, Piece.BROOK);   col++; break;
                case 'q': safeSetPiece(pos, col, row, Piece.BQUEEN);  col++; break;
                case 'k': safeSetPiece(pos, col, row, Piece.BKING);   col++; break;
                default: throw new ChessParseError("invalid piece", pos);
            }
        }

        if (words[1].length() > 0) {
            boolean wtm;
            switch (words[1].charAt(0)) {
                case 'w': wtm = true; break;
                case 'b': wtm = false; break;
                default: throw new ChessParseError("invalid side", pos);
            }
            pos.setWhiteMove(wtm);
        } else {
            throw new ChessParseError("invalid side", pos);
        }

        // Castling rights
        int castleMask = 0;
        if (words.length > 2) {
            for (int i = 0; i < words[2].length(); i++) {
                char c = words[2].charAt(i);
                switch (c) {
                    case 'K':
                        castleMask |= (1 << Position.H1_CASTLE);
                        break;
                    case 'Q':
                        castleMask |= (1 << Position.A1_CASTLE);
                        break;
                    case 'k':
                        castleMask |= (1 << Position.H8_CASTLE);
                        break;
                    case 'q':
                        castleMask |= (1 << Position.A8_CASTLE);
                        break;
                    case '-':
                        break;
                    default:
                        throw new ChessParseError("invalid castling flags", pos);
                }
            }
        }
        pos.setCastleMask(castleMask);

        if (words.length > 3) {
            // En passant target square
            String epString = words[3];
            if (!epString.equals("-")) {
                if (epString.length() < 2)
                    throw new ChessParseError("invalid en passant square", pos);
                int epSq = getSquare(epString);
                if (epSq != -1) {
                    if (pos.whiteMove) {
                        if ((Position.getY(epSq) != 5) || (pos.getPiece(epSq) != Piece.EMPTY) ||
                                (pos.getPiece(epSq - 8) != Piece.BPAWN))
                            epSq = -1;
                    } else {
                        if ((Position.getY(epSq) != 2) || (pos.getPiece(epSq) != Piece.EMPTY) ||
                                (pos.getPiece(epSq + 8) != Piece.WPAWN))
                            epSq = -1;
                    }
                    pos.setEpSquare(epSq);
                }
            }
        }

        try {
            if (words.length > 4) {
                pos.halfMoveClock = Integer.parseInt(words[4]);
            }
            if (words.length > 5) {
                pos.fullMoveCounter = Integer.parseInt(words[5]);
            }
        } catch (NumberFormatException nfe) {
            // Ignore errors here, since the fields are optional
        }

        // Each side must have exactly one king
        int[] nPieces = new int[Piece.nPieceTypes];
        for (int i = 0; i < Piece.nPieceTypes; i++)
            nPieces[i] = 0;
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                nPieces[pos.getPiece(Position.getSquare(x, y))]++;
        if (nPieces[Piece.WKING] != 1)
            throw new ChessParseError("white num kings", pos);
        if (nPieces[Piece.BKING] != 1)
            throw new ChessParseError("black num kings", pos);

        // White must not have too many pieces
        int maxWPawns = 8;
        maxWPawns -= Math.max(0, nPieces[Piece.WKNIGHT] - 2);
        maxWPawns -= Math.max(0, nPieces[Piece.WBISHOP] - 2);
        maxWPawns -= Math.max(0, nPieces[Piece.WROOK  ] - 2);
        maxWPawns -= Math.max(0, nPieces[Piece.WQUEEN ] - 1);
        if (nPieces[Piece.WPAWN] > maxWPawns)
            throw new ChessParseError("too many white pieces", pos);

        // Black must not have too many pieces
        int maxBPawns = 8;
        maxBPawns -= Math.max(0, nPieces[Piece.BKNIGHT] - 2);
        maxBPawns -= Math.max(0, nPieces[Piece.BBISHOP] - 2);
        maxBPawns -= Math.max(0, nPieces[Piece.BROOK  ] - 2);
        maxBPawns -= Math.max(0, nPieces[Piece.BQUEEN ] - 1);
        if (nPieces[Piece.BPAWN] > maxBPawns)
            throw new ChessParseError("too many black pieces", pos);

        // Make sure king can not be captured
        Position pos2 = new Position(pos);
        pos2.setWhiteMove(!pos.whiteMove);
        if (MoveGen.inCheck(pos2)) {
            throw new ChessParseError("king capture possible", pos);
        }

        fixupEPSquare(pos);

        return pos;
    }

    /** Remove pseudo-legal EP square if it is not legal, ie would leave king in check. */
    public static void fixupEPSquare(Position pos) {
        int epSquare = pos.getEpSquare();
        if (epSquare >= 0) {
            ArrayList<Move> moves = MoveGen.instance.legalMoves(pos);
            boolean epValid = false;
            for (Move m : moves) {
                if (m.to == epSquare) {
                    if (pos.getPiece(m.from) == (pos.whiteMove ? Piece.WPAWN : Piece.BPAWN)) {
                        epValid = true;
                        break;
                    }
                }
            }
            if (!epValid)
                pos.setEpSquare(-1);
        }
    }

    private static void safeSetPiece(Position pos, int col, int row, int p) throws ChessParseError {
        if (row < 0) throw new ChessParseError("too many rows");
        if (col > 7) throw new ChessParseError("too many columns");
        if ((p == Piece.WPAWN) || (p == Piece.BPAWN)) {
            if ((row == 0) || (row == 7))
                throw new ChessParseError("pawn on first last rank");
        }
        pos.setPiece(Position.getSquare(col, row), p);
    }

    /**
     * Convert a chess move to human readable form.
     * @param pos       The chess position.
     * @param move      The executed move.
     * @param longForm  If true, use long notation, eg Ng1-f3.
     *                  Otherwise, use short notation, eg Nf3.
     */
    public static String moveToString(Position pos, Move move, boolean longForm) {
        return moveToString(pos, move, longForm, null);
    }
    public static String moveToString(Position pos, Move move, boolean longForm,
                                      List<Move> moves) {
        if ((move == null) || move.equals(new Move(0, 0, 0)))
            return "--";
        StringBuilder ret = new StringBuilder();
        int wKingOrigPos = Position.getSquare(4, 0);
        int bKingOrigPos = Position.getSquare(4, 7);
        if (move.from == wKingOrigPos && pos.getPiece(wKingOrigPos) == Piece.WKING) {
            // Check white castle
            if (move.to == Position.getSquare(6, 0)) {
                ret.append("O-O");
            } else if (move.to == Position.getSquare(2, 0)) {
                ret.append("O-O-O");
            }
        } else if (move.from == bKingOrigPos && pos.getPiece(bKingOrigPos) == Piece.BKING) {
            // Check black castle
            if (move.to == Position.getSquare(6, 7)) {
                ret.append("O-O");
            } else if (move.to == Position.getSquare(2, 7)) {
                ret.append("O-O-O");
            }
        }
        if (ret.length() == 0) {
            int p = pos.getPiece(move.from);
            ret.append(pieceToChar(p));
            int x1 = Position.getX(move.from);
            int y1 = Position.getY(move.from);
            int x2 = Position.getX(move.to);
            int y2 = Position.getY(move.to);
            if (longForm) {
                ret.append((char)(x1 + 'a'));
                ret.append((char) (y1 + '1'));
                ret.append(isCapture(pos, move) ? 'x' : '-');
            } else {
                if (p == (pos.whiteMove ? Piece.WPAWN : Piece.BPAWN)) {
                    if (isCapture(pos, move)) {
                        ret.append((char) (x1 + 'a'));
                    }
                } else {
                    int numSameTarget = 0;
                    int numSameFile = 0;
                    int numSameRow = 0;
                    if (moves == null)
                        moves = MoveGen.instance.legalMoves(pos);
                    int mSize = moves.size();
                    for (int mi = 0; mi < mSize; mi++) {
                        Move m = moves.get(mi);
                        if ((pos.getPiece(m.from) == p) && (m.to == move.to)) {
                            numSameTarget++;
                            if (Position.getX(m.from) == x1)
                                numSameFile++;
                            if (Position.getY(m.from) == y1)
                                numSameRow++;
                        }
                    }
                    if (numSameTarget < 2) {
                        // No file/row info needed
                    } else if (numSameFile < 2) {
                        ret.append((char) (x1 + 'a'));   // Only file info needed
                    } else if (numSameRow < 2) {
                        ret.append((char) (y1 + '1'));   // Only row info needed
                    } else {
                        ret.append((char) (x1 + 'a'));   // File and row info needed
                        ret.append((char) (y1 + '1'));
                    }
                }
                if (isCapture(pos, move)) {
                    ret.append('x');
                }
            }
            ret.append((char) (x2 + 'a'));
            ret.append((char) (y2 + '1'));
            if (move.promoteTo != Piece.EMPTY)
                ret.append(pieceToChar(move.promoteTo));
        }
        UndoInfo ui = new UndoInfo();
        pos.makeMove(move, ui);
        boolean givesCheck = MoveGen.inCheck(pos);
        if (givesCheck) {
            ArrayList<Move> nextMoves = MoveGen.instance.legalMoves(pos);
            if (nextMoves.size() == 0) {
                ret.append('#');
            } else {
                ret.append('+');
            }
        }
        pos.unMakeMove(move, ui);

        return ret.toString();
    }

    private static boolean isCapture(Position pos, Move move) {
        if (pos.getPiece(move.to) == Piece.EMPTY) {
            int p = pos.getPiece(move.from);
            if ((p == (pos.whiteMove ? Piece.WPAWN : Piece.BPAWN)) && (move.to == pos.getEpSquare())) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private final static class MoveInfo {
        int piece;                  // -1 for unspecified
        int fromX, fromY, toX, toY; // -1 for unspecified
        int promPiece;              // -1 for unspecified
        MoveInfo() { piece = fromX = fromY = toX = toY = promPiece = -1; }
    }

    /**
     * Convert a chess move string to a Move object.
     * The string may specify any combination of piece/source/target/promotion
     * information as long as it matches exactly one valid move.
     */
    public static Move stringToMove(Position pos, String strMove) {
        return stringToMove(pos, strMove, null);
    }
    public static Move stringToMove(Position pos, String strMove,
                                    ArrayList<Move> moves) {
        if (strMove.equals("--"))
            return new Move(0, 0, 0);

        strMove = strMove.replaceAll("=", "");
        strMove = strMove.replaceAll("\\+", "");
        strMove = strMove.replaceAll("#", "");
        boolean wtm = pos.whiteMove;

        MoveInfo info = new MoveInfo();
        boolean capture = false;
        if (strMove.equals("O-O") || strMove.equals("0-0") || strMove.equals("o-o")) {
            info.piece = wtm ? Piece.WKING : Piece.BKING;
            info.fromX = 4;
            info.toX = 6;
            info.fromY = info.toY = wtm ? 0 : 7;
            info.promPiece = Piece.EMPTY;
        } else if (strMove.equals("O-O-O") || strMove.equals("0-0-0") || strMove.equals("o-o-o")) {
            info.piece = wtm ? Piece.WKING : Piece.BKING;
            info.fromX = 4;
            info.toX = 2;
            info.fromY = info.toY = wtm ? 0 : 7;
            info.promPiece = Piece.EMPTY;
        } else {
            boolean atToSq = false;
            for (int i = 0; i < strMove.length(); i++) {
                char c = strMove.charAt(i);
                if (i == 0) {
                    int piece = charToPiece(wtm, c);
                    if (piece >= 0) {
                        info.piece = piece;
                        continue;
                    }
                }
                int tmpX = c - 'a';
                if ((tmpX >= 0) && (tmpX < 8)) {
                    if (atToSq || (info.fromX >= 0))
                        info.toX = tmpX;
                    else
                        info.fromX = tmpX;
                }
                int tmpY = c - '1';
                if ((tmpY >= 0) && (tmpY < 8)) {
                    if (atToSq || (info.fromY >= 0))
                        info.toY = tmpY;
                    else
                        info.fromY = tmpY;
                }
                if ((c == 'x') || (c == '-')) {
                    atToSq = true;
                    if (c == 'x')
                        capture = true;
                }
                if (i == strMove.length() - 1) {
                    int promPiece = charToPiece(wtm, c);
                    if (promPiece >= 0) {
                        info.promPiece = promPiece;
                    }
                }
            }
            if ((info.fromX >= 0) && (info.toX < 0)) {
                info.toX = info.fromX;
                info.fromX = -1;
            }
            if ((info.fromY >= 0) && (info.toY < 0)) {
                info.toY = info.fromY;
                info.fromY = -1;
            }
            if (info.piece < 0) {
                boolean haveAll = (info.fromX >= 0) && (info.fromY >= 0) &&
                        (info.toX >= 0) && (info.toY >= 0);
                if (!haveAll)
                    info.piece = wtm ? Piece.WPAWN : Piece.BPAWN;
            }
            if (info.promPiece < 0)
                info.promPiece = Piece.EMPTY;
        }

        if (moves == null)
            moves = MoveGen.instance.legalMoves(pos);

        ArrayList<Move> matches = new ArrayList<>(2);
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            int p = pos.getPiece(m.from);
            boolean match = true;
            if ((info.piece >= 0) && (info.piece != p))
                match = false;
            if ((info.fromX >= 0) && (info.fromX != Position.getX(m.from)))
                match = false;
            if ((info.fromY >= 0) && (info.fromY != Position.getY(m.from)))
                match = false;
            if ((info.toX >= 0) && (info.toX != Position.getX(m.to)))
                match = false;
            if ((info.toY >= 0) && (info.toY != Position.getY(m.to)))
                match = false;
            if ((info.promPiece >= 0) && (info.promPiece != m.promoteTo))
                match = false;
            if (match) {
                matches.add(m);
            }
        }
        int nMatches = matches.size();
        if (nMatches == 0)
            return null;
        else if (nMatches == 1)
            return matches.get(0);
        if (!capture)
            return null;
        Move move = null;
        for (int i = 0; i < matches.size(); i++) {
            Move m = matches.get(i);
            int capt = pos.getPiece(m.to);
            if (capt != Piece.EMPTY) {
                if (move == null)
                    move = m;
                else
                    return null;
            }
        }
        return move;
    }

    /**
     * Convert a string, such as "e4" to a square number.
     * @return The square number, or -1 if not a legal square.
     */
    public static int getSquare(String s) {
        int x = s.charAt(0) - 'a';
        int y = s.charAt(1) - '1';
        if ((x < 0) || (x > 7) || (y < 0) || (y > 7))
            return -1;
        return Position.getSquare(x, y);
    }

    private static String pieceToChar(int p) {
        switch (p) {
            case Piece.WQUEEN:  case Piece.BQUEEN:  return "Q";
            case Piece.WROOK:   case Piece.BROOK:   return "R";
            case Piece.WBISHOP: case Piece.BBISHOP: return "B";
            case Piece.WKNIGHT: case Piece.BKNIGHT: return "N";
            case Piece.WKING:   case Piece.BKING:   return "K";
        }
        return "";
    }

    private static int charToPiece(boolean white, char c) {
        switch (c) {
            case 'Q': case 'q': return white ? Piece.WQUEEN  : Piece.BQUEEN;
            case 'R': case 'r': return white ? Piece.WROOK   : Piece.BROOK;
            case 'B':           return white ? Piece.WBISHOP : Piece.BBISHOP;
            case 'N': case 'n': return white ? Piece.WKNIGHT : Piece.BKNIGHT;
            case 'K': case 'k': return white ? Piece.WKING   : Piece.BKING;
            case 'P': case 'p': return white ? Piece.WPAWN   : Piece.BPAWN;
        }
        return -1;
    }
}
