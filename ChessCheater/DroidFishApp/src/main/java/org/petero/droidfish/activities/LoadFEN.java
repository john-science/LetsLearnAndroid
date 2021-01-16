/*
    DroidFish - An Android chess program.
    Copyright (C) 2013  Peter Österlund, peterosterlund2@gmail.com

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

package org.petero.droidfish.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import org.petero.droidfish.ColorTheme;
import org.petero.droidfish.DroidFishApp;
import org.petero.droidfish.R;
import org.petero.droidfish.Util;
import org.petero.droidfish.activities.util.FENFile;
import org.petero.droidfish.activities.util.FENFile.FenInfo;
import org.petero.droidfish.activities.util.FENFile.FenInfoResult;
import org.petero.droidfish.databinding.LoadFenBinding;
import org.petero.droidfish.gamelogic.ChessParseError;
import org.petero.droidfish.gamelogic.Position;
import org.petero.droidfish.gamelogic.TextIO;

import java.io.File;
import java.util.ArrayList;

public class LoadFEN extends ListActivity {
    private static ArrayList<FenInfo> fensInFile = new ArrayList<>();
    private static boolean cacheValid = false;
    private FENFile fenFile;
    private FenInfo selectedFi = null;
    private ArrayAdapter<FenInfo> aa = null;

    private SharedPreferences settings;
    private int defaultItem = 0;
    private String lastFileName = "";
    private long lastModTime = -1;

    private Thread workThread = null;

    LoadFenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        Util.setFullScreenMode(this, settings);

        if (savedInstanceState != null) {
            defaultItem = savedInstanceState.getInt("defaultItem");
            lastFileName = savedInstanceState.getString("lastFenFileName");
            if (lastFileName == null) lastFileName = "";
            lastModTime = savedInstanceState.getLong("lastFenModTime");
        } else {
            defaultItem = settings.getInt("defaultItem", 0);
            lastFileName = settings.getString("lastFenFileName", "");
            lastModTime = settings.getLong("lastFenModTime", 0);
        }

        Intent i = getIntent();
        String action = i.getAction();
        String fileName = i.getStringExtra("org.petero.droidfish.pathname");
        if ("org.petero.droidfish.loadFen".equals(action)) {
            fenFile = new FENFile(fileName);
            final LoadFEN lfen = this;
            workThread = new Thread(() -> {
                if (!readFile())
                    return;
                runOnUiThread(lfen::showList);
            });
            workThread.start();
        } else if ("org.petero.droidfish.loadNextFen".equals(action) ||
                   "org.petero.droidfish.loadPrevFen".equals(action)) {
            fenFile = new FENFile(fileName);
            boolean next = action.equals("org.petero.droidfish.loadNextFen");
            final int loadItem = defaultItem + (next ? 1 : -1);
            if (loadItem < 0) {
                DroidFishApp.toast(R.string.no_prev_fen, Toast.LENGTH_SHORT);
                setResult(RESULT_CANCELED);
                finish();
            } else {
                workThread = new Thread(() -> {
                    if (!readFile())
                        return;
                    runOnUiThread(() -> {
                        if (loadItem >= fensInFile.size()) {
                            DroidFishApp.toast(R.string.no_next_fen, Toast.LENGTH_SHORT);
                            setResult(RESULT_CANCELED);
                            finish();
                        } else {
                            defaultItem = loadItem;
                            sendBackResult(fensInFile.get(loadItem), true);
                        }
                    });
                });
                workThread.start();
            }
        } else { // Unsupported action
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(DroidFishApp.setLanguage(newBase, false));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("defaultItem", defaultItem);
        outState.putString("lastFenFileName", lastFileName);
        outState.putLong("lastFenModTime", lastModTime);
    }

    @Override
    protected void onPause() {
        Editor editor = settings.edit();
        editor.putInt("defaultItem", defaultItem);
        editor.putString("lastFenFileName", lastFileName);
        editor.putLong("lastFenModTime", lastModTime);
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (workThread != null) {
            workThread.interrupt();
            try {
                workThread.join();
            } catch (InterruptedException e) {
            }
            workThread = null;
        }
        super.onDestroy();
    }

    private void showList() {
        setContentView(R.layout.load_fen);
        binding = DataBindingUtil.setContentView(this, R.layout.load_fen);
        binding.loadfenOk.setEnabled(false);
        binding.loadfenOk.setOnClickListener(v -> {
            if (selectedFi != null)
                sendBackResult(selectedFi, false);
        });
        binding.loadfenCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        Util.overrideViewAttribs(findViewById(android.R.id.content));
        aa = new ArrayAdapter<FenInfo>(this, R.layout.select_game_list_item, fensInFile) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    int fg = ColorTheme.instance().getColor(ColorTheme.FONT_FOREGROUND);
                    ((TextView) view).setTextColor(fg);
                }
                return view;
            }
        };
        setListAdapter(aa);
        final ListView lv = getListView();
        lv.setSelectionFromTop(defaultItem, 0);
        lv.setFastScrollEnabled(true);
        lv.setOnItemClickListener((parent, view, pos, id) -> {
            selectedFi = aa.getItem(pos);
            if (selectedFi == null)
                return;
            defaultItem = pos;
            Position chessPos;
            try {
                chessPos = TextIO.readFEN(selectedFi.fen);
            } catch (ChessParseError e2) {
                chessPos = e2.pos;
            }
            if (chessPos != null) {
                binding.loadfenChessboard.setPosition(chessPos);
                binding.loadfenOk.setEnabled(true);
            }
        });
        lv.setOnItemLongClickListener((parent, view, pos, id) -> {
            selectedFi = aa.getItem(pos);
            if (selectedFi == null)
                return false;
            defaultItem = pos;
            Position chessPos;
            try {
                chessPos = TextIO.readFEN(selectedFi.fen);
            } catch (ChessParseError e2) {
                chessPos = e2.pos;
            }
            if (chessPos != null)
                sendBackResult(selectedFi, false);
            return true;
        });
        lv.requestFocus();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (binding != null && binding.loadfenChessboard != null) {
            Position pos = binding.loadfenChessboard.pos;
            showList();
            binding.loadfenChessboard.setPosition(pos);
            binding.loadfenOk.setEnabled(selectedFi != null);
        }
    }

    private boolean readFile() {
        String fileName = fenFile.getName();
        if (!fileName.equals(lastFileName))
            defaultItem = 0;
        long modTime = new File(fileName).lastModified();
        if (cacheValid && (modTime == lastModTime) && fileName.equals(lastFileName))
            return true;
        fenFile = new FENFile(fileName);
        Pair<FenInfoResult, ArrayList<FenInfo>> p = fenFile.getFenInfo();
        if (p.first != FenInfoResult.OK) {
            fensInFile = new ArrayList<>();
            if (p.first == FenInfoResult.OUT_OF_MEMORY) {
                runOnUiThread(() -> DroidFishApp.toast(R.string.file_too_large, Toast.LENGTH_SHORT));
            }
            setResult(RESULT_CANCELED);
            finish();
            return false;
        }
        fensInFile = p.second;
        cacheValid = true;
        lastModTime = modTime;
        lastFileName = fileName;
        return true;
    }

    private void sendBackResult(FenInfo fi, boolean toast) {
        String fen = fi.fen;
        if (fen != null) {
            if (toast)
                DroidFishApp.toast(String.valueOf(fi.gameNo) + ": " + fen, Toast.LENGTH_SHORT);
            setResult(RESULT_OK, (new Intent()).setAction(fen));
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
