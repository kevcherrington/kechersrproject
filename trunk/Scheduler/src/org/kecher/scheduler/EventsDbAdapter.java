package org.kecher.scheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventsDbAdapter {


    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_RUN_TIME_HOUR = "run_time_hour";
    public static final String KEY_RUN_TIME_MINUTE = "run_time_minute";
    public static final String KEY_SUN = "sun";
    public static final String KEY_MON = "mon";
    public static final String KEY_TUES = "tues";
    public static final String KEY_WED = "wed";
    public static final String KEY_THUR = "thur";
    public static final String KEY_FRI = "fri";
    public static final String KEY_SAT = "sat";
    public static final String KEY_MODE = "mode";
    public static final String KEY_VOL = "vol";
    public static final String KEY_VIBRATE = "vibrate";

    private static final String TAG = "EventsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "CREATE TABLE events (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "title TEXT NOT NULL, run_time_hour INTEGER NOT NULL, "
        + "run_time_minute INTEGER NOT NULL, sun INTEGER NOT NULL, "
        + "mon INTEGER NOT NULL, tues INTEGER NOT NULL, "
        + "wed INTEGER NOT NULL, thur INTEGER NOT NULL, "
        + "fri INTEGER NOT NULL, sat INTEGER NOT NULL, "
        + "mode TEXT NOT NULL, vol INTEGER NOT NULL, "
        + "vibrate INTEGER NOT NULL);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "events";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS events");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public EventsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the events database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public EventsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new event using the title provided. If the event is
     * successfully created return the new rowId for that event, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the event
     * @return rowId or -1 if failed
     */
    public long createEvent(String title, int runTimeHour, int runTimeMin, int sun, int mon, int tues,
    		int wed, int thur, int fri, int sat, String mode, int vol, int vibe) { 
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_RUN_TIME_HOUR, runTimeHour);
        initialValues.put(KEY_RUN_TIME_MINUTE, runTimeMin);
        initialValues.put(KEY_SUN, sun);
        initialValues.put(KEY_MON, mon);
        initialValues.put(KEY_TUES, tues);
        initialValues.put(KEY_WED, wed);
        initialValues.put(KEY_THUR, thur);
        initialValues.put(KEY_FRI, fri);
        initialValues.put(KEY_SAT, sat);
        initialValues.put(KEY_MODE, mode);
        initialValues.put(KEY_VOL, vol);
        initialValues.put(KEY_VIBRATE, vibe);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the event with the given rowId
     * 
     * @param rowId id of event to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEvent(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all events in the database
     * 
     * @return Cursor over all events
     */
    public Cursor fetchAllEvents() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
        		KEY_RUN_TIME_HOUR, KEY_RUN_TIME_MINUTE, KEY_SUN, KEY_MON,
        		KEY_TUES, KEY_WED, KEY_THUR, KEY_FRI, KEY_SAT, KEY_MODE,
        		KEY_VOL, KEY_VIBRATE},
        		null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the event that matches the given rowId
     * 
     * @param rowId id of event to retrieve
     * @return Cursor positioned to matching event, if found
     * @throws SQLException if event could not be found/retrieved
     */
    public Cursor fetchEvent(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_RUN_TIME_HOUR, KEY_RUN_TIME_MINUTE, KEY_SUN, KEY_MON,
            		KEY_TUES, KEY_WED, KEY_THUR, KEY_FRI, KEY_SAT, KEY_MODE, KEY_VOL, KEY_VIBRATE},
            		KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the event using the details provided. The event to be updated is
     * specified using the rowId, and it is altered to use the title
     * value passed in
     * 
     * @param rowId id of event to update
     * @param title value to set event title to
     * @return true if the event was successfully updated, false otherwise
     */
    public boolean updateEvent(long rowId, String title, int runTimeHour, int runTimeMin,
    		int sun, int mon, int tues, int wed, int thur, int fri, int sat, String mode, int vol, int vibe) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_RUN_TIME_HOUR, runTimeHour);
        args.put(KEY_RUN_TIME_MINUTE, runTimeMin);
        args.put(KEY_SUN, sun);
        args.put(KEY_MON, mon);
        args.put(KEY_TUES, tues);
        args.put(KEY_WED, wed);
        args.put(KEY_THUR, thur);
        args.put(KEY_FRI, fri);
        args.put(KEY_SAT, sat);
        args.put(KEY_MODE, mode);
        args.put(KEY_VOL, vol);
        args.put(KEY_VIBRATE, vibe);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
