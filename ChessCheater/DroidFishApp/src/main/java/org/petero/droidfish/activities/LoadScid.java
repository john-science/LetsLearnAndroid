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

package org.petero.droidfish.activities;

import java.io.File;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.petero.droidfish.ColorTheme;
import org.petero.droidfish.DroidFishApp;
import org.petero.droidfish.ObjectCache;
import org.petero.droidfish.R;
import org.petero.droidfish.Util;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LoadScid extends ListActivity {
    private static final class GameInfo {
        String summary = "";
        int gameId = -1;
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append(gameId+1);
            sb.append(". ");
            sb.append(summary);
            return sb.toString();
        }
    }

    private static Vector<GameInfo> gamesInFile = new Vector<>();
    private static boolean cacheValid = false;
    private String fileName;
    private ProgressDialog progress;

    private SharedPreferences settings;
    private int defaultItem = 0;
    private String lastFileName = "";
    private long lastModTime = -1;

    private Thread workThread = null;
    private CountDownLatch progressLatch = null;
    private boolean canceled = false;

    private boolean resultSentBack = false;


    private interface OnCursorReady {
        void run(Cursor cursor);
    }
    
    private void startReadFile(final OnCursorReady r) {
        getLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String scidFileName = fileName.substring(0, fileName.indexOf("."));
                String[] proj = new String[]{"_id", "summary"};
                return new CursorLoader(getApplicationContext(),
                                        Uri.parse("content://org.scid.database.scidprovider/games"),
                                        proj, scidFileName, null, null);
            }
            @Override
            public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
                workThread = new Thread(() -> r.run(cursor));
                workThread.start();
            }
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (savedInstanceState != null) {
            defaultItem = savedInstanceState.getInt("defaultScidItem");
            lastFileName = savedInstanceState.getString("lastScidFileName");
            if (lastFileName == null) lastFileName = "";
            lastModTime = savedInstanceState.getLong("lastScidModTime");
        } else {
            defaultItem = settings.getInt("defaultScidItem", 0);
            lastFileName = settings.getString("lastScidFileName", "");
            lastModTime = settings.getLong("lastScidModTime", 0);
        }

        Intent i = getIntent();
        String action = i.getAction();
        fileName = i.getStringExtra("org.petero.droidfish.pathname");
        resultSentBack = false;
        canceled = false;
        if ("org.petero.droidfish.loadScid".equals(action)) {
            progressLatch = new CountDownLatch(1);
            showProgressDialog();
            final LoadScid lpgn = this;
            startReadFile(cursor -> {
                try {
                    progressLatch.await();
                } catch (InterruptedException e) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
                if (!readFile(cursor))
                    return;
                runOnUiThread(() -> {
                    if (canceled) {
                        setResult(RESULT_CANCELED);
                        finish();
                    } else {
                        lpgn.showList();
                    }
                });
            });
        } else if ("org.petero.droidfish.loadScidNextGame".equals(action) ||
                   "org.petero.droidfish.loadScidPrevGame".equals(action)) {
            boolean next = action.equals("org.petero.droidfish.loadScidNextGame");
            final int loadItem = defaultItem + (next ? 1 : -1);
            if (loadItem < 0) {
                DroidFishApp.toast(R.string.no_prev_game, Toast.LENGTH_SHORT);
                setResult(RESULT_CANCELED);
                finish();
            } else {
                startReadFile(cursor -> {
                    if (!readFile(cursor))
                        return;
                    runOnUiThread(() -> {
                        if (loadItem >= gamesInFile.size()) {
                            DroidFishApp.toast(R.string.no_next_game, Toast.LENGTH_SHORT);
                            setResult(RESULT_CANCELED);
                            finish();
                        } else {
                            defaultItem = loadItem;
                            sendBackResult(gamesInFile.get(loadItem));
                        }
                    });
                });
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
        outState.putInt("defaultScidItem", defaultItem);
        outState.putString("lastScidFileName", lastFileName);
        outState.putLong("lastScidModTime", lastModTime);
    }

    @Override
    protected void onPause() {
        Editor editor = settings.edit();
        editor.putInt("defaultScidItem", defaultItem);
        editor.putString("lastScidFileName", lastFileName);
        editor.putLong("lastScidModTime", lastModTime);
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (workThread != null) {
            workThread.interrupt();
            try {
                workThread.join();
            } catch (InterruptedException ignore) {
            }
            workThread = null;
        }
        super.onDestroy();
    }

    private void showList() {
        progress = null;
        removeProgressDialog();
        final ArrayAdapter<GameInfo> aa =
            new ArrayAdapter<GameInfo>(this, R.layout.select_game_list_item, gamesInFile) {
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
        ListView lv = getListView();
        Util.overrideViewAttribs(lv);
        lv.setSelectionFromTop(defaultItem, 0);
        lv.setFastScrollEnabled(true);
        lv.setOnItemClickListener((parent, view, pos, id) -> {
            defaultItem = pos;
            sendBackResult(aa.getItem(pos));
        });
    }

    public static class ProgressFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LoadScid a = (LoadScid)getActivity();
            ProgressDialog progress = new ProgressDialog(a);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setTitle(R.string.reading_scid_file);
            a.progress = progress;
            a.progressLatch.countDown();
            return progress;
        }
        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            Activity a = getActivity();
            if (a instanceof LoadScid) {
                LoadScid ls = (LoadScid)a;
                ls.canceled = true;
                Thread thr = ls.workThread;
                if (thr != null)
                    thr.interrupt();
            }
        }
    }

    private void showProgressDialog() {
        ProgressFragment f = new ProgressFragment();
        f.show(getFragmentManager(), "progress");
    }

    private void removeProgressDialog() {
        Fragment f = getFragmentManager().findFragmentByTag("progress");
        if (f instanceof DialogFragment)
            ((DialogFragment)f).dismiss();
    }

    private boolean readFile(Cursor cursor) {
        if (!fileName.equals(lastFileName))
            defaultItem = 0;
        long modTime = new File(fileName).lastModified();
        if (cacheValid && (modTime == lastModTime) && fileName.equals(lastFileName))
            return true;
        lastModTime = modTime;
        lastFileName = fileName;

        gamesInFile.clear();
        if (cursor != null) {
            try {
                int noGames = cursor.getCount();
                gamesInFile.ensureCapacity(noGames);
                int percent = -1;
                if (cursor.moveToFirst()) {
                    addGameInfo(cursor);
                    int gameNo = 1;
                    while (cursor.moveToNext()) {
                        if (Thread.currentThread().isInterrupted()) {
                            setResult(RESULT_CANCELED);
                            finish();
                            return false;
                        }
                        addGameInfo(cursor);
                        gameNo++;
                        final int newPercent = gameNo * 100 / noGames;
                        if (newPercent > percent) {
                            percent = newPercent;
                            if (progress != null) {
                                runOnUiThread(() -> progress.setProgress(newPercent));
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException ignore) {
                setResult(RESULT_CANCELED);
                finish();
                return false;
            }
        }
        cacheValid = true;
        return true;
    }

    private void addGameInfo(Cursor cursor) {
        GameInfo gi = new GameInfo();
        int idIdx = cursor.getColumnIndex("_id");
        int summaryIdx = cursor.getColumnIndex("summary");
        gi.gameId = cursor.getInt(idIdx);
        gi.summary = cursor.getString(summaryIdx);
        gamesInFile.add(gi);
    }

    private void sendBackResult(final GameInfo gi) {
        if (resultSentBack)
            return;
        resultSentBack = true;
        if (gi.gameId < 0) {
            setResult(RESULT_CANCELED);
            finish();
        }

        getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String scidFileName = fileName.substring(0, fileName.indexOf("."));
                String[] proj = new String[]{"pgn"};
                String uri = String.format(Locale.US, "content://org.scid.database.scidprovider/games/%d",
                                           gi.gameId);
                return new CursorLoader(getApplicationContext(),
                                        Uri.parse(uri),
                                        proj, scidFileName, null, null);                        
            }
            @Override
            public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
                if (cursor != null && cursor.moveToFirst()) {
                    String pgn = cursor.getString(cursor.getColumnIndex("pgn"));
                    if (pgn != null && pgn.length() > 0) {
                        String pgnToken = (new ObjectCache()).storeString(pgn);
                        setResult(RESULT_OK, (new Intent()).setAction(pgnToken));
                        finish();
                        return;
                    }
                }
                setResult(RESULT_CANCELED);
                finish();
            }
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }
}
