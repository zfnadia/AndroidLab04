package com.nadiaferdoush.lab4cse491;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 14;
    public static final String DATABASE_NAME = "myapp14.db";

    public static AppDatabase instance = null;
    private final Context context;

    private static String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context);
        }
        return instance;
    }

    //private AppDatabase() { this( null); }
    private AppDatabase(Context context) {
        // PATH + DATABASE_NAME
        super(context, PATH + DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void insertJsonEmployee(JsonEmployee jsonEmployee) {
        ContentValues values = new ContentValues();
        values.put("name", jsonEmployee.getName());
        values.put("latitude", jsonEmployee.getLatitude());
        values.put("longitude", jsonEmployee.getLongitude());

        this.getWritableDatabase().insert("jsonEmployee", null, values);
    }

    public List<JsonEmployee> getEmployees() {
        List<JsonEmployee> employees = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM jsonEmployee ORDER BY name COLLATE NOCASE ASC", new String[]{});
        if (cursor.moveToFirst()) {
            do {

                String name = cursor.getString(cursor.getColumnIndex("name"));
                double latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("latitude")));
                double longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("longitude")));

                JsonEmployee jEmployee = new JsonEmployee(name, latitude, longitude);
                jEmployee.id = cursor.getInt(cursor.getColumnIndex("id"));
                employees.add(jEmployee);

            } while (cursor.moveToNext());
        }
        return employees;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.e("LL", "here");
            db.execSQL(readFromAssets(this.context, "sql/jsonEmployee.ddl"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         onUpgrade(db, oldVersion, newVersion);
    }


    public static String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }
}
