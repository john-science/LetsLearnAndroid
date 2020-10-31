package net.antineutrino.sqlitedemo;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;


public class DAL extends SQLiteOpenHelper {

    public static final String TABLE = "CUSTOMER_TABLE";
    public static final String COL_NAME = "CUSTOMER_NAME";
    public static final String COL_AGE = "CUSTOMER_AGE";
    public static final String COL_ACTIVE = "ACTIVE_CUSTOMER";
    public static final String COL_ID = "ID";

    public DAL(@Nullable Context context) {
        super(context, "customer", null, 1);
    }

    // This is going to be called the first time you access a DB object.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + TABLE + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " + COL_AGE + " INT, " + COL_ACTIVE + " BOOL)";

        db.execSQL(createTableStatement);
    }

    // Called every time the version of your application changes.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(CustomerModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_NAME, model.getName());
        cv.put(COL_AGE, model.getAge());
        cv.put(COL_ACTIVE, model.isActive());

        long insert = db.insert(TABLE, null, cv);
        return insert >= 0;
    }
}
