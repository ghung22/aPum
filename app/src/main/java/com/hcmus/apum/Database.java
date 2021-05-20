package com.hcmus.apum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper {
    // Context and debugging
    private final Context context;
    private static final String TAG = "DATABASE";

    // DB Info
    private static String DB_PATH;
    private static String DB_NAME;
    private static final int DB_VERSION = 1;

    // Data
    public static final String
            TABLE_FAVORITE = "Favorite",
            TABLE_FACES = "Faces",
            TABLE_FACES_RECT = "FacesRect";

    public Database(Context context) {
        super(context, context.getString(R.string.app_name), null, DB_VERSION);
        this.context = context;
        DB_NAME = context.getString(R.string.app_name);
        DB_PATH = context.getDatabasePath(DB_NAME).getPath();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table " + TABLE_FAVORITE + " (" +
                "   string text not null," +
                "   _timeInserted integer not null," +
                "   _isDeleted boolean," +
                "   primary key (string)" +
                ")"
        );
        sqLiteDatabase.execSQL(
                "create table "+ TABLE_FACES +" (" +
                "   string text not null," +
                "   _timeInserted integer not null," +
                "   _isDeleted boolean," +
                "   primary key (string)" +
                ")"
        );
        sqLiteDatabase.execSQL(
                "create table "+ TABLE_FACES_RECT +" (" +
                "   string text not null," +
                "   rect text not null," +
                "   _timeInserted integer not null," +
                "   _isDeleted boolean," +
                "   primary key (string)," +
                "   foreign key (string) references " + TABLE_FACES + "(string)" +
                ")"
        );

        try {
            SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
            if (!isEmpty()) {
                this.getReadableDatabase();
                this.close();
                copyDatabase();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: ", e.getCause());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        for (String tableName : Arrays.asList(TABLE_FAVORITE, TABLE_FACES, TABLE_FACES_RECT))
        sqLiteDatabase.execSQL("drop table if exists " + tableName);
        onCreate(sqLiteDatabase);
    }

    private boolean isEmpty() {
        try {
            String myPath = DB_PATH + DB_NAME;
            SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            return true;
        }
        return false;
    }

    private void copyDatabase() throws IOException {
        String outPath = DB_PATH + DB_NAME;
        FileInputStream in = (FileInputStream) context.getAssets().open(DB_NAME);
        FileOutputStream out = new FileOutputStream(outPath);

        FileChannel inChannel = in.getChannel(),
                outChannel = out.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        in.close();
        out.flush();
        out.close();
    }

    public void insert(HashMap<String, String> items, String table) {
        Parcel parcel = Parcel.obtain();
        parcel.writeMap(items);
        parcel.setDataPosition(0);
        ContentValues values = ContentValues.CREATOR.createFromParcel(parcel);
        values.put("_timeInserted", new Date().toInstant().getEpochSecond());
        values.put("_isDeleted", false);

        SQLiteDatabase db = this.getWritableDatabase();
        // Insert or update depending on data
        int result = (int) db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (result != -1) {
            Log.i(TAG, String.format("insert '%s' into '%s'", items.get("string"), table));
        } else {
            db.update(table, values, "string", new String[]{items.get("string")});
            Log.i(TAG, String.format("update '%s' in '%s'", items.get("string"), table));
        }
        db.close();
    }

    public void delete(String stringItem, String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_isDeleted", true);
        int result = db.update(table, values, "string = ?", new String[]{stringItem});
        if (result != 0) {
            Log.i(TAG, String.format("delete '%s' from '%s'", stringItem, table));
        } else {
            Log.i(TAG, String.format("delete failed: the string '%s' is not existed in '%s'.", stringItem, table));
        }
        db.close();
    }
    
    public ArrayList<String> getFavorite() {
        ArrayList<String> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_FAVORITE,
                new String[]{"string", "_timeInserted", "_isDeleted"},
                null,
                null,
                "string",
                "_isDeleted = 0",
                "_timeInserted desc"
        );
        if (cursor.moveToFirst()){
            do {
                String img = "";
                img = cursor.getString(cursor.getColumnIndex("string"));
                result.add(img);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.i(TAG, String.format("select '%d' rows from '%s'", result.size(), TABLE_FAVORITE));
        return result;
    }

    public HashMap<String, ArrayList<String>> getFaces() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorFaces = db.query(
                TABLE_FACES,
                new String[]{"string", "_timeInserted", "_isDeleted"},
                null,
                null,
                "string",
                "_isDeleted = 0",
                null
        );

        if (cursorFaces.moveToFirst()) {
            do {
                String img = cursorFaces.getString(cursorFaces.getColumnIndex("string"));
                ArrayList<String> rectList = new ArrayList<>();
                Cursor cursorRect = db.query(
                        TABLE_FACES_RECT,
                        new String[]{"string", "face", "_timeInserted", "_isDeleted"},
                        null,
                        null,
                        "string",
                        String.format("_isDeleted = 0 and string = '%s'", img),
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
        db.close();
        Log.i(TAG, String.format("select '%d' rows from '%s'", result.size(), TABLE_FAVORITE));
        return result;
    }
}