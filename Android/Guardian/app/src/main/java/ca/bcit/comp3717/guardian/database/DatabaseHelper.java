package ca.bcit.comp3717.guardian.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import ca.bcit.comp3717.guardian.model.User;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "loginStorage.db";
    private static final int DB_VERSION = 1;
    private static String DatabaseHelperTAG = DatabaseHelper.class.getSimpleName();
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            createTable(db);
            insertLoginValues(db, "firstEmail", "firstPassword");
        }
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL(QueryGenerator.CreateQuery.SavedLoginTable);
    }

    private void insertLoginValues(SQLiteDatabase db, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("LoginId", 1);
        values.put("Email", email);
        values.put("Password", password);
        db.insert(QueryGenerator.SAVED_LOGIN_TABLE, null, values);
    }

    public static void insertLoginValuesIntoDatabase(SQLiteDatabase db, String table,
                                                     ContentValues values) {
        try {
            db.beginTransaction();
            db.insert(table, null, values);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            Log.e(DatabaseHelperTAG, "ERROR in insertLoginValuesIntoDatabase(): " + e.getMessage());
        }
    }

    public static void updateDbValues(SQLiteDatabase db, String table, ContentValues values,
                                      String whereClause) {
        try {
            String[] whereArgs = {"1"};
            db.beginTransaction();
            int rowsUpdated = db.update(table, values, whereClause, whereArgs);
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            Log.e(DatabaseHelperTAG, "ERROR in updateDbValues(): " + e.getMessage());
        }
    }

    public static User getLocalLoginValues(SQLiteDatabase db, String query) {
        User user = null;
        String[] whereArgs = {"1"};

        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery(query, whereArgs);

            // if cursor has results
            if (cursor.moveToFirst()) {
                user = new User();
                do {
                    String email = cursor.getString(cursor.getColumnIndex("Email"));
                    String password = cursor.getString(cursor.getColumnIndex("Password"));
                    user.setEmail(email);
                    user.setPassword(password);
                } while (cursor.moveToNext());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            cursor.close();

        } catch (Exception e) {
            Log.e(DatabaseHelperTAG, "ERROR in getLocalLoginValues(): " + e.getMessage());
        }
        return user;
    }
}
