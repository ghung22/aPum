package com.hcmus.apum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DatabaseFavorites extends SQLiteOpenHelper {
    private static final String DB_PATH = "/data/data/com.hcmus.apum/databases/";
    private static final String DB_NAME = "dbFavorite";
    private SQLiteDatabase dbFavorite = null;
    private final Context context;

    private static final String TAG = "DatbaseFavorites";
    private static final String TABLE_NAME = "favorite_images";
    private static final String COL1 = "ID";
    private static final String COL2 = "Name";

    public DatabaseFavorites(Context context){
        super(context, TABLE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "create table " + TABLE_NAME + " (ID integer primary key autoincrement," + COL2 + " TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void createDataBase() throws IOException {
//        SQLiteDatabase db = null;
        dbFavorite = null;
        try {
            dbFavorite = SQLiteDatabase.openOrCreateDatabase(DB_PATH+DB_NAME, null);
        } catch (SQLiteException ex) {

            Log.e(TAG, ex.getMessage());
        }

        boolean dbExist = checkDataBase();
        if (dbExist) { } else {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
        close();
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null;
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        dbFavorite = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if (dbFavorite != null)
            dbFavorite.close();
        super.close();
    }

    public boolean checkDataExists(String item){
        String sql_check = "Select * from " + TABLE_NAME + " where " + COL2 + "=" + item;
        Cursor cursor = dbFavorite.rawQuery(sql_check, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean addData (String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);
        Log.d(TAG, "addData: " + item + " to " +TABLE_NAME );
        long result = db.insert(TABLE_NAME, null, contentValues);
        close();
        return result != -1;
    }

    public boolean removeData (String item){
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(TAG, "removeData: " + item + " from " +TABLE_NAME );
        long result = db.delete(TABLE_NAME, COL2+"=?",new String[]{item});
        close();
        return result != -1;
    }

    public ArrayList<String> getAllFavorite(){
        String[] columns = {
                COL2
        };
//        String sortOrder = COL1 + " ASC";
        ArrayList<String> fav = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(TABLE_NAME,columns,null,null,null,null,sortOrder);
        Cursor cursor = db.query(TABLE_NAME,columns,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                String img = "";
                img = cursor.getString(cursor.getColumnIndex(COL2));
                fav.add(img);

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        close();
        return fav;
    }
}