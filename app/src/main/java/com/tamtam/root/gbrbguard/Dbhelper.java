package com.tamtam.root.gbrbguard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by root on 3/3/17.
 */

public class Dbhelper extends SQLiteOpenHelper {
    //Database settings
    private static final String DATABASE_NAME = "gbr";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TOKEN = "token";
    private static final String TABLE_MESSAGES = "messages";

    //create table tags

    private static final String CREATE_TABLE_TOKEN =
            "CREATE TABLE " + TABLE_TOKEN + "(token_value TEXT, created DATETIME)";

    private static final String CREATE_TABLE_MESSAGES =
            "CREATE TABLE " + TABLE_MESSAGES +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, fname TEXT, address TEXT, phone TEXT , created DATETIME)";

    public Dbhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TOKEN);
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOKEN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    /*
    * Creating a token
    */
    public void saveToken(String token) {
        SQLiteDatabase db_read = this.getReadableDatabase();
        SQLiteDatabase db_write = this.getWritableDatabase();
        Cursor cr = db_read.rawQuery("SELECT * FROM token where token_value = '"+ token + "' limit 1",null);
        if(cr.moveToFirst()){
            if(cr.getCount() > 0){
                db_write.execSQL("DELETE FROM token");
            }
        }

        Date d = new Date();
        db_write.execSQL("INSERT INTO token values('" + token + "','" + d.toString() + "')");
        db_write.close();
        db_read.close();
    }

    public String getToken() {
        SQLiteDatabase db_read = this.getReadableDatabase();
        Cursor cr = db_read.rawQuery("SELECT * FROM token limit 1",null);
        String token ="";
        if(cr.moveToFirst()){
            token = cr.getString(0);
        }
        db_read.close();
        return token;
    }

    public void saveMessage(String fname,String address,String phone){
        SQLiteDatabase db_write = this.getWritableDatabase();
        Date d = new Date();
        String id = Long.toString(d.getTime());
        db_write.execSQL("INSERT INTO messages values("+id + ",'" + fname + "','" + address + "','" + phone + "','" +d.toString() + "')");
        db_write.close();
    }

    public JSONArray getMessages(String limit) throws JSONException{
        if(limit == null){
            limit = "100";
        }
        SQLiteDatabase db_read = this.getReadableDatabase();
        Cursor cr = db_read.rawQuery("SELECT fname,address,phone,created FROM messages order by id desc limit " + limit,null);
        JSONArray jsonArray = new JSONArray();
        if(cr.moveToFirst()) {
            do {
                JSONObject row = new JSONObject();
                row.put("fname", cr.getString(0));
                row.put("address", cr.getString(1));
                row.put("phone", cr.getString(2));
                row.put("created", cr.getString(3));
                jsonArray.put(row);
            }while (cr.moveToNext());
        }
        return jsonArray;
    }

    public void removeMessages(){
        SQLiteDatabase db_write = this.getWritableDatabase();
        db_write.execSQL("DELETE FROM messages");
        db_write.close();
    }

}
