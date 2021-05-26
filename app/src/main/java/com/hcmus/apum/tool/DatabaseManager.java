package com.hcmus.apum.tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.hcmus.apum.R;

import java.util.*;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String TAG = "DATABASE";

    // Data
    public static final String
            TABLE_FAVORITE = "Favorite",
            TABLE_FACES = "Faces",
            TABLE_FACES_RECT = "FacesRect";

    public DatabaseManager(Context context) {
        super(context, context.getString(R.string.app_name), null, 1);
        // Context and debugging
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table " + TABLE_FAVORITE + " (" +
                        "   string text not null," +
                        "   _timeInserted integer not null," +
                        "   primary key (string)" +
                        ")"
        );
        sqLiteDatabase.execSQL(
                "create table " + TABLE_FACES + " (" +
                        "   string text not null," +
                        "   _timeInserted integer not null," +
                        "   primary key (string)" +
                        ")"
        );
        sqLiteDatabase.execSQL(
                "create table " + TABLE_FACES_RECT + " (" +
                        "   string text not null," +
                        "   rect text not null," +
                        "   _timeInserted integer not null," +
                        "   primary key (string)," +
                        "   foreign key (string) references " + TABLE_FACES + "(string)" +
                        ")"
        );
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        for (String tableName : Arrays.asList(TABLE_FAVORITE, TABLE_FACES, TABLE_FACES_RECT)) {
            sqLiteDatabase.execSQL("drop table if exists " + tableName);
        }
        onCreate(sqLiteDatabase);
    }

    public boolean insert(HashMap<String, String> items, String table) {
        try {
            // Get data in hashmap
            if (items == null) {
                throw new Throwable("Hashmap is null");
            } else if (!items.containsKey("string")) {
                throw new Throwable("Missing key");
            } else {
                String temp = items.get("string");
                if (temp == null || temp.equals("null")) {
                    throw new Throwable("File is null");
                }
            }
            ContentValues values = new ContentValues();
            for (Map.Entry<String, String> entry : items.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
            values.put("_timeInserted", new Date().toInstant().getEpochSecond());

            // Insert or update depending on data
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            int result = db.update(table, values, "string = ?", new String[]{items.get("string")});
            if (result == 1) {
                Log.i(TAG, String.format("update '%s' in '%s'", items.get("string"), table));
            } else {
                db.insertOrThrow(table, null, values);
                Log.i(TAG, String.format("insert '%s' into '%s'", items.get("string"), table));
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.format("update in '%s' failed: Hashmap is null", table));
            return false;
        } catch (Throwable e) {
            Log.e(TAG, String.format("update in '%s' failed with message: %s", table, e.getMessage()));
            return false;
        }
    }

    public boolean delete(String stringItem, String table) {
        try {
            // Get data in hashmap
            if (stringItem.equals("null")) {
                throw new Throwable("File is null");
            }

            // Delete a row
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            int result = db.delete(table, "string = ?", new String[]{stringItem});
            if (result > 0) {
                Log.i(TAG, String.format("delete '%s' from '%s'", stringItem, table));
            } else {
                Log.w(TAG, String.format("delete '%s' from '%s' failed.", stringItem, table));
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        } catch (Throwable e) {
            Log.w(TAG, String.format("delete '%s' from '%s' failed with message: %s", stringItem, table, e.getMessage()));
            return false;
        }
    }

    public ArrayList<String> getFavorite() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            ArrayList<String> result = new ArrayList<>();
            Cursor cursor = db.query(
                    TABLE_FAVORITE,
                    new String[]{"string", "_timeInserted"},
                    null,
                    null,
                    "string",
                    null,
                    "_timeInserted desc"
            );
            if (cursor.moveToFirst()) {
                do {
                    String img = cursor.getString(cursor.getColumnIndex("string"));
                    result.add(img);
                } while (cursor.moveToNext());
            }
            cursor.close();
            Log.i(TAG, String.format("select '%d' rows from '%s'", result.size(), TABLE_FAVORITE));
            return result;
        } catch (Exception e) {
            Log.w(TAG, String.format("select rows from '%s' failed with message: %s", TABLE_FAVORITE, e.getMessage()));
            return new ArrayList<>();
        }
    }

    public HashMap<String, ArrayList<String>> getFaces() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            HashMap<String, ArrayList<String>> result = new HashMap<>();
            Cursor cursorFaces = db.query(
                    TABLE_FACES,
                    new String[]{"string", "_timeInserted"},
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursorFaces.moveToFirst()) {
                do {
                    String img = cursorFaces.getString(cursorFaces.getColumnIndex("string"));
                    ArrayList<String> rectList = new ArrayList<>();
                    Cursor cursorRect = db.query(
                            TABLE_FACES_RECT,
                            new String[]{"string", "face", "_timeInserted"},
                            null,
                            null,
                            "string",
                            String.format("string = '%s'", img),
                            null
                    );
                    if (cursorRect.moveToFirst()) {
                        do {
                            String rect = cursorRect.getString(cursorRect.getColumnIndex("rect"));
                            rectList.add(rect);
                        } while (cursorRect.moveToNext());
                    }
                    cursorRect.close();
                    result.put(img, rectList);
                } while (cursorFaces.moveToNext());
            }

            cursorFaces.close();
            Log.i(TAG, String.format("select '%d' rows from '%s'", result.size(), TABLE_FACES));
            return result;
        } catch (Exception e) {
            Log.w(TAG, String.format("select rows from '%s' failed with message: %s", TABLE_FACES, e.getMessage()));
            return new HashMap<>();
        }
    }
}