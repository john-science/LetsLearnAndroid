package net.antineutrino.sqlitedemo;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;


public class DAL extends SQLiteOpenHelper {

    public DAL(@Nullable Context context) {
        super(context, "customer", null, 1);
    }

    // This is going to be called the first time you access a DB object.
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    // Called every time the version of your application changes.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
