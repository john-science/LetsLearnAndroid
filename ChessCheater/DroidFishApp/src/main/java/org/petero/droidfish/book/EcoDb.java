/*
    DroidFish - An Android chess program.
    Copyright (C) 2016 Peter Österlund, peterosterlund2@gmail.com

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.petero.droidfish.DroidFishApp;
import org.petero.droidfish.gamelogic.ChessParseError;
import org.petero.droidfish.gamelogic.GameTree;
import org.petero.droidfish.gamelogic.Move;
import org.petero.droidfish.gamelogic.Position;
import org.petero.droidfish.gamelogic.TextIO;
import org.petero.droidfish.gamelogic.UndoInfo;

/** ECO code database. */
@SuppressLint("UseSparseArrays")
public class EcoDb {
    private static EcoDb instance;

    /** Get singleton instance. */
    public static EcoDb getInstance() {
        if (instance == null) {
            instance = new EcoDb();
        }
        return instance;
    }
    
    public static class Result {
        public final String eco; // The ECO code
        public final String opn; // The opening name, or null
        public final String var; // The variation name, or null
        public final int distToEcoTree;
        Result(String eco, String opn, String var, int d) {
            this.eco = eco;
            this.opn = opn;
            this.var = var;
            distToEcoTree = d;
        }
        /** Return string formatted as "eco: opn, var". */
        public String getName() {
            String s = eco;
            if (!opn.isEmpty()) {
                s = s + ": " + opn;
                if (!var.isEmpty())
                    s = s + ", " + var;
            }
            return s;
        }
    }

    /** Get ECO classification for a given tree node. Also returns distance in plies to "ECO tree". */
    public Result getEco(GameTree gt) {
        ArrayList<Integer> treePath = new ArrayList<>(); // Path to restore gt to original node
        ArrayList<Pair<GameTree.Node,Boolean>> toCache = new ArrayList<>();

        int nodeIdx = -1;
        int distToEcoTree = 0;

        // Find matching node furtherest from root in the ECO tree
        boolean checkForDup = true;
        while (true) {
            GameTree.Node node = gt.currentNode;
            CacheEntry e = findNode(node);
            if (e != null) {
                nodeIdx = e.nodeIdx;
                distToEcoTree = e.distToEcoTree;
                checkForDup = false;
                break;
            }
            Short idx = posHashToNodeIdx.get(gt.currentPos.zobristHash());
            boolean inEcoTree = idx != null;
            toCache.add(new Pair<>(node, inEcoTree));

            if (idx != null) {
                Node ecoNode = readNode(idx);
                if (ecoNode.ecoIdx != -1) {
                    nodeIdx = idx;
                    break;
                }
            }

            if (node == gt.rootNode)
                break;

            treePath.add(node.getChildNo());
            gt.goBack();
        }

        // Handle duplicates in ECO tree (same position reachable from more than one path)
        if (nodeIdx != -1 && checkForDup && gt.startPos.zobristHash() == startPosHash) {
            ArrayList<Short> dups = posHashToNodeIdx2.get(gt.currentPos.zobristHash());
            if (dups != null) {
                while (gt.currentNode != gt.rootNode) {
                    treePath.add(gt.currentNode.getChildNo());
                    gt.goBack();
                }

                int currEcoNode = 0;
                boolean foundDup = false;
                while (!treePath.isEmpty()) {
                    gt.goForward(treePath.get(treePath.size() - 1), false);
                    treePath.remove(treePath.size() - 1);
                    int m = gt.currentNode.move.getCompressedMove();

                    Node ecoNode = readNode(currEcoNode);
                    boolean foundChild = false;
                    int child = ecoNode.firstChild;
                    while (child != -1) {
                        ecoNode = readNode(child);
                        if (ecoNode.move == m) {
                            foundChild = true;
                            break;
                        }
                        child = ecoNode.nextSibling;
                    }
                    if (!foundChild)
                        break;
                    currEcoNode = child;
                    for (Short dup : dups) {
                        if (dup == currEcoNode) {
                            nodeIdx = currEcoNode;
                            foundDup = true;
                            break;
                        }
                    }
                    if (foundDup)
                        break;
                }
            }
        }

        for (int i = treePath.size() - 1; i >= 0; i--)
            gt.goForward(treePath.get(i), false);
        for (int i = toCache.size() - 1; i >= 0; i--) {
            Pair<GameTree.Node,Boolean> p = toCache.get(i);
            distToEcoTree++;
            if (p.second)
                distToEcoTree = 0;
            cacheNode(p.first, nodeIdx, distToEcoTree);
        }

        if (nodeIdx != -1) {
            Node n = readNode(nodeIdx);
            String eco = "", opn = "", var = "";
            if (n.ecoIdx >= 0) {
                eco = strPool[n.ecoIdx];
                if (n.opnIdx >= 0) {
                    opn = strPool[n.opnIdx];
                    if (n.varIdx >= 0)
                        var = strPool[n.varIdx];
                }
                return new Result(eco, opn, var, distToEcoTree);
            }
        }
        return new Result("", "", "", 0);
    }

    /** Get all moves in the ECO tree from a given position. */
    public ArrayList<Move> getMoves(Position pos) {
        ArrayList<Move> moves = new ArrayList<>();
        long hash = pos.zobristHash();
        Short idx = posHashToNodeIdx.get(hash);
        if (idx != null) {
            Node node = readNode(idx);
            int child = node.firstChild;
            while (child != -1) {
                node = readNode(child);
                moves.add(Move.fromCompressed(node.move));
                child = node.nextSibling;
            }
            ArrayList<Short> lst = posHashToNodeIdx2.get(hash);
            if (lst != null) {
                for (Short idx2 : lst) {
                    node = readNode(idx2);
                    child = node.firstChild;
                    while (child != -1) {
                        node = readNode(child);
                        Move m = Move.fromCompressed(node.move);
                        if (!moves.contains(m))
                            moves.add(m);
                        child = node.nextSibling;
                    }
                }
            }
        }
        return moves;
    }


    private static class Node {
        int move;       // Move (compressed) leading to the position corresponding to this node
        int ecoIdx;     // Index in string array, or -1
        int opnIdx;     // Index in string array, or -1
        int varIdx;     // Index in string array, or -1
        int firstChild;
        int nextSibling;
    }

    private byte[] nodesBuffer;
    private String[] strPool;
    private HashMap<Long, Short> posHashToNodeIdx;
    private HashMap<Long, ArrayList<Short>> posHashToNodeIdx2; // Handles collisions
    private final long startPosHash; // Zobrist hash for standard starting position

    private static class CacheEntry {
        final int nodeIdx;
        final int distToEcoTree;
        CacheEntry(int n, int d) {
            nodeIdx = n;
            distToEcoTree = d;
        }
    }
    private WeakLRUCache<GameTree.Node, CacheEntry> gtNodeToIdx;

    /** Return cached Node index corresponding to a GameTree.Node, or -1 if not found. */
    private CacheEntry findNode(GameTree.Node node) {
        return gtNodeToIdx.get(node);
    }

    /** Store GameTree.Node to Node index in cache. */
    private void cacheNode(GameTree.Node node, int nodeIdx, int distToEcoTree) {
        gtNodeToIdx.put(node, new CacheEntry(nodeIdx, distToEcoTree));
    }

    /** Constructor. */
    private EcoDb() {
        posHashToNodeIdx = new HashMap<>();
        posHashToNodeIdx2 = new HashMap<>();
        gtNodeToIdx = new WeakLRUCache<>(50);
        try (ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
             InputStream inStream = DroidFishApp.getContext().getAssets().open("eco.dat")) {
            byte[] buf = new byte[1024];
            while (true) {
                int len = inStream.read(buf);
                if (len <= 0) break;
                bufStream.write(buf, 0, len);
            }
            bufStream.flush();
            buf = bufStream.toByteArray();
            int nNodes = 0;
            while (true) {
                Node n = readNode(nNodes, buf);
                if (n.move == 0xffff)
                    break;
                nNodes++;
            }
            nodesBuffer = new byte[nNodes * 12];
            System.arraycopy(buf, 0, nodesBuffer, 0, nNodes * 12);

            ArrayList<String> names = new ArrayList<>();
            int idx = (nNodes + 1) * 12;
            int start = idx;
            for (int i = idx; i < buf.length; i++) {
                if (buf[i] == 0) {
                    names.add(new String(buf, start, i - start, "UTF-8"));
                    start = i + 1;
                }
            }
            strPool = names.toArray(new String[0]);
        } catch (IOException ex) {
            throw new RuntimeException("Can't read ECO database");
        }
        try {
            Position pos = TextIO.readFEN(TextIO.startPosFEN);
            startPosHash = pos.zobristHash();
            if (nodesBuffer.length > 0) {
                populateCache(pos, 0);
            }
        } catch (ChessParseError e) {
            throw new RuntimeException("Internal error");
        }
    }

    /** Initialize posHashToNodeIdx. */
    private void populateCache(Position pos, int nodeIdx) {
        Node node = readNode(nodeIdx);
        long hash = pos.zobristHash();
        if (posHashToNodeIdx.get(hash) == null) {
            posHashToNodeIdx.put(hash, (short)nodeIdx);
        } else if (node.ecoIdx != -1) {
            ArrayList<Short> lst = null;
            if (posHashToNodeIdx2.get(hash) == null) {
                lst = new ArrayList<>();
                posHashToNodeIdx2.put(hash, lst);
            } else {
                lst = posHashToNodeIdx2.get(hash);
            }
            lst.add((short)nodeIdx);
        }
        int child = node.firstChild;
        UndoInfo ui = new UndoInfo();
        while (child != -1) {
            node = readNode(child);
            Move m = Move.fromCompressed(node.move);
            pos.makeMove(m, ui);
            populateCache(pos, child);
            pos.unMakeMove(m, ui);
            child = node.nextSibling;
        }
    }

    private Node readNode(int index) {
        return readNode(index, nodesBuffer);
    }

    private static Node readNode(int index, byte[] buf) {
        Node n = new Node();
        int o = index * 12;
        n.move = getU16(buf, o);
        n.ecoIdx = getS16(buf, o + 2);
        n.opnIdx = getS16(buf, o + 4);
        n.varIdx = getS16(buf, o + 6);
        n.firstChild = getS16(buf, o + 8);
        n.nextSibling = getS16(buf, o + 10);
        return n;
    }

    private static int getU16(byte[] buf, int offs) {
        int b0 = buf[offs] & 255;
        int b1 = buf[offs + 1] & 255;
        return (b0 << 8) + b1;
    }

    private static int getS16(byte[] buf, int offs) {
        int ret = getU16(buf, offs);
        if (ret >= 0x8000)
            ret -= 0x10000;
        return ret;
    }

    /** A Cache where the keys are weak references and the cache automatically
     *  shrinks when it becomes too large, using approximate LRU ordering.
     *  This cache is not designed to store null values. */
    private static class WeakLRUCache<K, V> {
        private WeakHashMap<K, V> mapNew; // Most recently used entries
        private WeakHashMap<K, V> mapOld; // Older entries
        private int maxSize;

        public WeakLRUCache(int maxSize) {
            mapNew = new WeakHashMap<>();
            mapOld = new WeakHashMap<>();
            this.maxSize = maxSize;
        }

        /** Insert a value in the map, replacing any old value with the same key. */
        public void put(K key, V val) {
            if (mapNew.containsKey(key)) {
                mapNew.put(key, val);
            } else {
                mapOld.remove(key);
                insertNew(key, val);
            }
        }

        /** Returns the value corresponding to key, or null if not found. */
        public V get(K key) {
            V val = mapNew.get(key);
            if (val != null)
                return val;
            val = mapOld.get(key);
            if (val != null) {
                mapOld.remove(key);
                insertNew(key, val);
            }
            return val;
        }

        private void insertNew(K key, V val) {
            if (mapNew.size() >= maxSize) {
                WeakHashMap<K, V> tmp = mapNew;
                mapNew = mapOld;
                mapOld = tmp;
                mapNew.clear();
            }
            mapNew.put(key, val);
        }
    }
}
