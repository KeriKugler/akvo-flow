/*
 *  Copyright (C) 2010-2012 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo FLOW.
 *
 *  Akvo FLOW is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo FLOW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package com.gallatinsystems.survey.device.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.location.Location;
import android.util.Log;

import com.gallatinsystems.survey.device.domain.FileTransmission;
import com.gallatinsystems.survey.device.domain.PointOfInterest;
import com.gallatinsystems.survey.device.domain.Project;
import com.gallatinsystems.survey.device.domain.QuestionResponse;
import com.gallatinsystems.survey.device.domain.Survey;
import com.gallatinsystems.survey.device.domain.SurveyedLocale;
import com.gallatinsystems.survey.device.domain.SurveyedLocaleValue;
import com.gallatinsystems.survey.device.util.ConstantUtil;
import com.gallatinsystems.survey.device.util.GeoUtil;
import com.gallatinsystems.survey.device.util.PropertyUtil;

/**
 * Database class for the survey db. It can create/upgrade the database as well
 * as select/insert/update survey responses.
 * 
 * TODO: break this up into separate DAOs
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveyDbAdapter {

	public static final String QUESTION_FK_COL = "question_id";
	public static final String ANSWER_COL = "answer_value";
	public static final String ANSWER_TYPE_COL = "answer_type";
	public static final String SURVEY_RESPONDENT_ID_COL = "survey_respondent_id";
	public static final String RESP_ID_COL = "survey_response_id";
	public static final String SURVEY_FK_COL = "survey_id";
	public static final String PK_ID_COL = "_id";
	public static final String USER_FK_COL = "user_id";
	public static final String DISP_NAME_COL = "display_name";
	public static final String EMAIL_COL = "email";
	public static final String SUBMITTED_FLAG_COL = "submitted_flag";
	public static final String SUBMITTED_DATE_COL = "submitted_date";
	public static final String DELIVERED_DATE_COL = "delivered_date";
	public static final String CREATED_DATE_COL = "created_date";
	public static final String UPDATED_DATE_COL = "updated_date";
	public static final String PLOT_FK_COL = "plot_id";
	public static final String LAT_COL = "lat";
	public static final String LON_COL = "lon";
	public static final String ELEVATION_COL = "elevation";
	public static final String DESC_COL = "description";
	public static final String STATUS_COL = "status";
	public static final String VERSION_COL = "version";
	public static final String TYPE_COL = "type";
	public static final String LOCATION_COL = "location";
	public static final String FILENAME_COL = "filename";
	public static final String KEY_COL = "key";
	public static final String VALUE_COL = "value";
	public static final String DELETED_COL = "deleted_flag";
	public static final String MEDIA_SENT_COL = "media_sent_flag";
	public static final String HELP_DOWNLOADED_COL = "help_downloaded_flag";
	public static final String LANGUAGE_COL = "language";
	public static final String SAVED_DATE_COL = "saved_date";
	public static final String COUNTRY_COL = "country";
	public static final String PROP_NAME_COL = "property_names";
	public static final String PROP_VAL_COL = "property_values";
	public static final String INCLUDE_FLAG_COL = "include_flag";
	public static final String SCORED_VAL_COL = "scored_val";
	public static final String STRENGTH_COL = "strength";
	public static final String TRANS_START_COL = "trans_start_date";
	public static final String EXPORTED_FLAG_COL = "exported_flag";
	public static final String UUID_COL = "uuid";

	// surveyed locale cols. Lat, Lon, status, answer defined above
	public static final String PROJECT_COL = "project_id";
	public static final String PROJECT_NAME_COL = "project_name";

	public static final String LOCALE_UNIQUE_ID_COL = "locale_unique_id";
	public static final String LAST_SUBMITTED_COL = "last_submitted_date";
	public static final String SURVEYED_LOCALE_COL = "surveyed_locale_id";
	public static final String QUESTION_COL = "question_id";
	public static final String METRIC_NAME_COL = "metric_name";
	public static final String METRIC_ID_COL = "metric_id";
	public static final String INCLUDE_IN_LIST_COL = "include_in_list";
	private static final String TAG = "SurveyDbAdapter";
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;

	/**
	 * Database creation sql statement
	 */
	private static final String SURVEY_TABLE_CREATE = "create table survey (_id integer primary key, "
			+ "display_name text not null, project_id text, version real, type text, location text, filename text, language, help_downloaded_flag text, deleted_flag text);";

	private static final String SURVEY_RESPONDENT_CREATE = "create table survey_respondent (_id integer primary key autoincrement, "
			+ "survey_id integer not null, submitted_flag text, submitted_date text, delivered_date text, user_id integer, media_sent_flag text, status text, saved_date long, exported_flag text, uuid text);";

	private static final String SURVEY_RESPONSE_CREATE = "create table survey_response (survey_response_id integer primary key autoincrement, "
			+ " survey_respondent_id integer not null, question_id text not null, answer_value text not null, answer_type text not null, include_flag text not null, scored_val text, strength text);";

	private static final String USER_TABLE_CREATE = "create table user (_id integer primary key autoincrement, display_name text not null, email text not null, deleted_flag text);";

	private static final String PLOT_TABLE_CREATE = "create table plot (_id integer primary key autoincrement, display_name text, description text, created_date text, user_id integer, status text);";

	private static final String PLOT_POINT_TABLE_CREATE = "create table plot_point (_id integer primary key autoincrement, plot_id integer not null, lat text, lon text, elevation text, created_date text);";

	private static final String PREFERENCES_TABLE_CREATE = "create table preferences (key text primary key, value text);";

	private static final String POINT_OF_INTEREST_TABLE_CREATE = "create table point_of_interest (_id integer primary key, country text, display_name text, lat real, lon real, property_names text, property_values text, type text, updated_date integer);";

	private static final String TRANSMISSION_HISTORY_TABLE_CREATE = "create table transmission_history (_id integer primary key, survey_respondent_id integer not null, status text, filename text, trans_start_date long, delivered_date long);";

	private static final String SURVEYED_LOCALE_TABLE_CREATE = "create table surveyed_locale (_id integer primary key autoincrement, project_id text, locale_unique_id text, last_submitted_date integer, lat real, lon real, status integer)";

	private static final String SURVEYED_LOCALE_VAL_TABLE_CREATE = "create table surveyed_locale_val (_id integer primary key autoincrement, surveyed_locale_id integer not null, question_id text, answer_value text)";

	private static final String PROJECT_TABLE_CREATE = "create table project (_id integer primary key autoincrement, project_id text, project_name text)";

	private static final String LOCALE_VAL_INDEX_CREATE = "create index locale_val_index on surveyed_locale_val (answer_value COLLATE NOCASE)";

	private static final String RECORD_META_TABLE_CREATE = "create table record_meta (_id integer primary key autoincrement, project_id text, question_id text, metric_name text, metric_id text, include_in_list boolean)";

	private static final String[] DEFAULT_INSERTS = new String[] {
		
			// insert default values
			"insert into preferences values('survey.language','0')",
			"insert into preferences values('user.storelast','false')",
			"insert into preferences values('data.cellular.upload','0')",
			"insert into preferences values('plot.default.mode','manual')",
			"insert into preferences values('plot.interval','60000')",
			"insert into preferences values('user.lastuser.id','')",
			"insert into preferences values('location.sendbeacon','true')",
			"insert into preferences values('survey.precachehelp','1')",
			"insert into preferences values('backend.server','')",
			"insert into preferences values('screen.keepon','true')",
			"insert into preferences values('precache.points.countries','2')",
			"insert into preferences values('precache.points.limit','200')",
			"insert into preferences values('survey.textsize','LARGE')",
			"insert into preferences values('survey.checkforupdates','0')",
			"insert into preferences values('remoteexception.upload','0')",
			"insert into preferences values('survey.media.photo.shrink','true')",		
			"insert into preferences values('survey.media.photo.sizereminder','true')",
			"insert into preferences values('nearby.locale.radius','100000.0')" };

	private static final String DATABASE_NAME = "surveydata";
	private static final String SURVEY_TABLE = "survey";
	private static final String RESPONDENT_TABLE = "survey_respondent";
	private static final String RESPONSE_TABLE = "survey_response";
	private static final String USER_TABLE = "user";
	private static final String PLOT_TABLE = "plot";
	private static final String PLOT_POINT_TABLE = "plot_point";
	private static final String PREFERENCES_TABLE = "preferences";
	private static final String POINT_OF_INTEREST_TABLE = "point_of_interest";
	private static final String TRANSMISSION_HISTORY_TABLE = "transmission_history";
	private static final String SURVEYED_LOCALE_TABLE = "surveyed_locale";
	private static final String SURVEYED_LOCALE_VAL_TABLE = "surveyed_locale_val";
	private static final String PROJECT_TABLE = "project";
	private static final String LOCALE_VAL_INDEX = "locale_val_index";
	private static final String RECORD_META_TABLE = "record_meta";
	private static final String RESPONSE_JOIN = "survey_respondent LEFT OUTER JOIN survey_response ON (survey_respondent._id = survey_response.survey_respondent_id) LEFT OUTER JOIN user ON (user._id = survey_respondent.user_id)";
	private static final String PLOT_JOIN = "plot LEFT OUTER JOIN plot_point ON (plot._id = plot_point.plot_id) LEFT OUTER JOIN user ON (user._id = plot.user_id)";
	private static final String RESPONDENT_JOIN = "survey_respondent LEFT OUTER JOIN survey ON (survey_respondent.survey_id = survey._id)";

	private static final int DATABASE_VERSION = 80;

	private final Context context;

	/**
	 * Helper class for creating the database tables and loading reference data
	 * 
	 * It is declared with package scope for VM optimizations
	 * 
	 * @author Christopher Fagiani
	 * 
	 */
	static class DatabaseHelper extends SQLiteOpenHelper {

		private static SQLiteDatabase database;
		private static volatile Long LOCK_OBJ = 1L;
		private volatile static int instanceCount = 0;
		private Context context;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(USER_TABLE_CREATE);
			db.execSQL(SURVEY_TABLE_CREATE);
			db.execSQL(SURVEY_RESPONDENT_CREATE);
			db.execSQL(SURVEY_RESPONSE_CREATE);
			db.execSQL(PLOT_TABLE_CREATE);
			db.execSQL(PLOT_POINT_TABLE_CREATE);
			db.execSQL(PREFERENCES_TABLE_CREATE);
			db.execSQL(POINT_OF_INTEREST_TABLE_CREATE);
			db.execSQL(TRANSMISSION_HISTORY_TABLE_CREATE);
			db.execSQL(SURVEYED_LOCALE_TABLE_CREATE);
			db.execSQL(SURVEYED_LOCALE_VAL_TABLE_CREATE);
			db.execSQL(RECORD_META_TABLE_CREATE);
			db.execSQL(LOCALE_VAL_INDEX_CREATE);
			db.execSQL(PROJECT_TABLE_CREATE);

			for (int i = 0; i < DEFAULT_INSERTS.length; i++) {
				db.execSQL(DEFAULT_INSERTS[i]);
			}			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);

			if (oldVersion < 57) {
				db.execSQL("DROP TABLE IF EXISTS " + RESPONSE_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + RESPONDENT_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + PLOT_POINT_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + PLOT_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + PREFERENCES_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + POINT_OF_INTEREST_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + TRANSMISSION_HISTORY_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + SURVEYED_LOCALE_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + SURVEYED_LOCALE_VAL_TABLE);
				db.execSQL("DROP INDEX IF EXISTS " + LOCALE_VAL_INDEX);
				db.execSQL("DROP TABLE IF EXISTS " + RECORD_META_TABLE);
				onCreate(db);
			} else if (oldVersion < 75) {

				// changes made in version 57
				runSQL(TRANSMISSION_HISTORY_TABLE_CREATE, db);

				// changes made in version 58
				try {
					String value = null;
					Cursor cursor = db.query(PREFERENCES_TABLE, new String[] {
							KEY_COL, VALUE_COL }, KEY_COL + " = ?",
							new String[] { "survey.textsize" }, null, null,
							null);
					if (cursor != null) {
						if (cursor.getCount() > 0) {
							cursor.moveToFirst();
							value = cursor.getString(cursor
									.getColumnIndexOrThrow(VALUE_COL));
						}
						cursor.close();
					}
					if (value == null) {
						runSQL("insert into preferences values('survey.textsize','LARGE')",
								db);
					}
				} catch (Exception e) {
					// swallow
				}

				// changes in version 63
				runSQL("alter table survey_respondent add column exported_flag text",
						db);

				// changes in version 68
				try {
					runSQL("alter table survey_respondent add column uuid text",
							db);
					// also generate a uuid for all in-flight responses
					Cursor cursor = db.query(RESPONDENT_JOIN, new String[] {
							RESPONDENT_TABLE + "." + PK_ID_COL, DISP_NAME_COL,
							SAVED_DATE_COL, SURVEY_FK_COL, USER_FK_COL,
							SUBMITTED_DATE_COL, DELIVERED_DATE_COL, UUID_COL },
							null, null, null, null, null);
					if (cursor != null) {
						cursor.moveToFirst();
						do {
							String uuid = cursor.getString(cursor
									.getColumnIndex(UUID_COL));
							if (uuid == null || uuid.trim().length() == 0) {
								db.execSQL("update " + RESPONDENT_TABLE
										+ " set " + UUID_COL + "= '"
										+ UUID.randomUUID().toString() + "'");
							}
						} while (cursor.moveToNext());
						cursor.close();
					}
				} catch (Exception e) {
					// swallow
				}
				// changes made in version 69
				runSQL("alter table user add column deleted_flag text", db);
				runSQL("update user set deleted_flag = 'N' where deleted_flag <> 'Y'",
						db);

				runSQL("update survey set language = 'en' where language = 'english' or language is null",
						db);
				if (oldVersion < 74) {
					runSQL("insert into preferences values('survey.checkforupdates','0')",
							db);
					runSQL("insert into preferences values('remoteexception.upload','0')",
							db);
				}
			} else if (oldVersion < 80){
				db.execSQL("DROP TABLE IF EXISTS " + SURVEYED_LOCALE_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + SURVEYED_LOCALE_VAL_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + RECORD_META_TABLE);
				db.execSQL("DROP INDEX IF EXISTS " + LOCALE_VAL_INDEX);
				db.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE);
				db.execSQL(SURVEYED_LOCALE_TABLE_CREATE);
				db.execSQL(SURVEYED_LOCALE_VAL_TABLE_CREATE);
				db.execSQL(RECORD_META_TABLE_CREATE);
				db.execSQL(LOCALE_VAL_INDEX_CREATE);
				db.execSQL(PROJECT_TABLE_CREATE);
			}

			// now handle defaults
			checkDefaults(db);

			this.context = null;
		}

		// executes a sql statement and swallows errors
		private void runSQL(String ddl, SQLiteDatabase db) {
			try {
				db.execSQL(ddl);
			} catch (Exception e) {
				// no-op
			}
		}

		/**
		 * checks whether we should restore defaults and, if so, sets the
		 * properties in the table to the values from the prop file
		 * 
		 * @param db
		 */
		private void checkDefaults(SQLiteDatabase db) {
			// check for defaults and set them
			PropertyUtil props = new PropertyUtil(context.getResources());
			String defaults = props.getProperty(ConstantUtil.DEFAULT_SETTINGS);
			if (defaults != null) {
				String[] defaultPairs = defaults.split(";");
				for (int i = 0; i < defaultPairs.length; i++) {
					String[] nvp = defaultPairs[i].split("=");
					if (nvp.length == 2) {
						savePreference(db, nvp[0].trim(), nvp[1].trim());
					}
				}
			}
		}

		@Override
		public SQLiteDatabase getWritableDatabase() {
			synchronized (LOCK_OBJ) {

				if (database == null || !database.isOpen()) {
					database = super.getWritableDatabase();
					instanceCount = 0;
				}
				instanceCount++;
				return database;
			}
		}

		@Override
		public void close() {
			synchronized (LOCK_OBJ) {
				instanceCount--;
				if (instanceCount <= 0) {
					// close the database held by the helper (if any)
					super.close();
					if (database != null && database.isOpen()) {
						// we may be holding a different database than the
						// helper so
						// close that too if it's still open.
						database.close();
					}
					database = null;
				}
			}
		}

		/**
		 * returns the value of a single setting identified by the key passed in
		 */
		public String findPreference(SQLiteDatabase db, String key) {
			String value = null;
			Cursor cursor = db.query(PREFERENCES_TABLE, new String[] { KEY_COL,
					VALUE_COL }, KEY_COL + " = ?", new String[] { key }, null,
					null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					value = cursor.getString(cursor
							.getColumnIndexOrThrow(VALUE_COL));
				}
				cursor.close();
			}
			return value;
		}

		/**
		 * persists setting to the db
		 * 
		 * @param surveyId
		 */
		public void savePreference(SQLiteDatabase db, String key, String value) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(VALUE_COL, value);
			int updated = db.update(PREFERENCES_TABLE, updatedValues, KEY_COL
					+ " = ?", new String[] { key });
			if (updated <= 0) {
				updatedValues.put(KEY_COL, key);
				db.insert(PREFERENCES_TABLE, null, updatedValues);
			}
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public SurveyDbAdapter(Context ctx) {
		this.context = ctx;
	}

	/**
	 * Open or create the db
	 * 
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public SurveyDbAdapter open() throws SQLException {
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getWritableDatabase();
		return this;
	}

	/**
	 * close the db
	 */
	public void close() {
		databaseHelper.close();
	}

	/**
	 * Create a new survey using the title and body provided. If the survey is
	 * successfully created return the new id, otherwise return a -1 to indicate
	 * failure.
	 * 
	 * @param name
	 *            survey name
	 * 
	 * @return rowId or -1 if failed
	 */
	public long createSurvey(String name) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DISP_NAME_COL, name);
		initialValues.put(HELP_DOWNLOADED_COL, "N");
		return database.insert(SURVEY_TABLE, null, initialValues);
	}

	/**
	 * returns a cursor that lists all unsent (sentFlag = false) survey data
	 * 
	 * @return
	 */
	public Cursor fetchUnsentData() {
		Cursor cursor = database.query(RESPONSE_JOIN, new String[] {
				RESPONDENT_TABLE + "." + PK_ID_COL, RESP_ID_COL, ANSWER_COL,
				ANSWER_TYPE_COL, QUESTION_FK_COL, DISP_NAME_COL, EMAIL_COL,
				DELIVERED_DATE_COL, SUBMITTED_DATE_COL,
				RESPONDENT_TABLE + "." + SURVEY_FK_COL, SCORED_VAL_COL,
				STRENGTH_COL, UUID_COL }, SUBMITTED_FLAG_COL + "= 'true' AND "
				+ INCLUDE_FLAG_COL + "='true' AND" + "(" + DELIVERED_DATE_COL
				+ " is null OR " + MEDIA_SENT_COL + " <> 'true')", null, null,
				null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * returns a cursor that lists all unexported (sentFlag = false) survey data
	 * 
	 * @return
	 */
	public Cursor fetchUnexportedData() {
		Cursor cursor = database.query(RESPONSE_JOIN, new String[] {
				RESPONDENT_TABLE + "." + PK_ID_COL, RESP_ID_COL, ANSWER_COL,
				ANSWER_TYPE_COL, QUESTION_FK_COL, DISP_NAME_COL, EMAIL_COL,
				DELIVERED_DATE_COL, SUBMITTED_DATE_COL,
				RESPONDENT_TABLE + "." + SURVEY_FK_COL, SCORED_VAL_COL,
				STRENGTH_COL, UUID_COL }, SUBMITTED_FLAG_COL + "= 'true' AND "
				+ INCLUDE_FLAG_COL + "='true' AND " + EXPORTED_FLAG_COL
				+ " <> 'true' AND " + "(" + DELIVERED_DATE_COL + " is null OR "
				+ MEDIA_SENT_COL + " <> 'true')", null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * marks the data as submitted in the respondent table (submittedFlag =
	 * true) thereby making it ready for transmission
	 * 
	 * @param respondentId
	 */
	public void submitResponses(String respondentId) {
		ContentValues vals = new ContentValues();
		vals.put(SUBMITTED_FLAG_COL, "true");
		vals.put(SUBMITTED_DATE_COL, System.currentTimeMillis());
		vals.put(STATUS_COL, ConstantUtil.SUBMITTED_STATUS);
		database.update(RESPONDENT_TABLE, vals,
				PK_ID_COL + "= " + respondentId, null);
	}

	/**
	 * updates the respondent table by recording the sent date stamp
	 * 
	 * @param idList
	 */
	public void markDataAsSent(HashSet<String> idList, String mediaSentFlag) {
		if (idList != null) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(DELIVERED_DATE_COL, System.currentTimeMillis()
					+ "");
			updatedValues.put(MEDIA_SENT_COL, mediaSentFlag);
			// enhanced FOR ok here since we're dealing with an implicit
			// iterator anyway
			for (String id : idList) {
				if (database.update(RESPONDENT_TABLE, updatedValues, PK_ID_COL
						+ " = ?", new String[] { id }) < 1) {
					Log.e(TAG,
							"Could not update record for Survey_respondent_id "
									+ id);
				}
			}
		}
	}

	/**
	 * updates the respondent table by recording the sent date stamp
	 * 
	 * @param idList
	 */
	public void markDataAsExported(HashSet<String> idList) {
		if (idList != null && idList.size() > 0) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(EXPORTED_FLAG_COL, "true");
			// enhanced FOR ok here since we're dealing with an implicit
			// iterator anyway
			for (String id : idList) {
				if (database.update(RESPONDENT_TABLE, updatedValues, PK_ID_COL
						+ " = ?", new String[] { id }) < 1) {
					Log.e(TAG,
							"Could not update record for Survey_respondent_id "
									+ id);
				}
			}
		}
	}

	/**
	 * updates the status of a survey response to the string passed in
	 * 
	 * @param surveyRespondentId
	 * @param status
	 */
	public void updateSurveyStatus(String surveyRespondentId, String status) {
		if (surveyRespondentId != null) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(STATUS_COL, status);
			updatedValues.put(SAVED_DATE_COL, System.currentTimeMillis());
			if (database.update(RESPONDENT_TABLE, updatedValues, PK_ID_COL
					+ " = ?", new String[] { surveyRespondentId }) < 1) {
				Log.e(TAG, "Could not update status for Survey_respondent_id "
						+ surveyRespondentId);
			}

		}

	}

	/**
	 * returns a cursor listing all users
	 * 
	 * @return
	 */
	public Cursor listUsers() {
		Cursor cursor = database.query(USER_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, EMAIL_COL }, DELETED_COL + " <> ?",
				new String[] { ConstantUtil.IS_DELETED }, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * retrieves a user by ID
	 * 
	 * @param id
	 * @return
	 */
	public Cursor findUser(Long id) {
		Cursor cursor = database.query(USER_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, EMAIL_COL }, PK_ID_COL + "=?",
				new String[] { id.toString() }, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * if the ID is populated, this will update a user record. Otherwise, it
	 * will be inserted
	 * 
	 * @param id
	 * @param name
	 * @param email
	 * @return
	 */
	public long createOrUpdateUser(Long id, String name, String email) {
		ContentValues initialValues = new ContentValues();
		Long idVal = id;
		initialValues.put(DISP_NAME_COL, name);
		initialValues.put(EMAIL_COL, email);
		initialValues.put(DELETED_COL, ConstantUtil.NOT_DELETED);

		if (idVal == null) {
			idVal = database.insert(USER_TABLE, null, initialValues);
		} else {
			if (database.update(USER_TABLE, initialValues, PK_ID_COL + "=?",
					new String[] { idVal.toString() }) > 0) {
			}
		}
		return idVal;
	}

	/**
	 * Return a Cursor over the list of all responses for a particular survey
	 * respondent
	 * 
	 * @return Cursor over all responses
	 */
	public Cursor fetchResponsesByRespondent(String respondentID) {
		return database.query(RESPONSE_TABLE, new String[] { RESP_ID_COL,
				QUESTION_FK_COL, ANSWER_COL, ANSWER_TYPE_COL,
				SURVEY_RESPONDENT_ID_COL, INCLUDE_FLAG_COL, SCORED_VAL_COL,
				STRENGTH_COL }, SURVEY_RESPONDENT_ID_COL + "=?",
				new String[] { respondentID }, null, null, null);
	}

	/**
	 * loads a single question response
	 * 
	 * @param respondentId
	 * @param questionId
	 * @return
	 */
	public QuestionResponse findSingleResponse(Long respondentId,
			String questionId) {
		QuestionResponse resp = null;
		Cursor cursor = database.query(RESPONSE_TABLE, new String[] {
				RESP_ID_COL, QUESTION_FK_COL, ANSWER_COL, ANSWER_TYPE_COL,
				SURVEY_RESPONDENT_ID_COL, INCLUDE_FLAG_COL, SCORED_VAL_COL,
				STRENGTH_COL }, SURVEY_RESPONDENT_ID_COL + "=? and "
				+ QUESTION_FK_COL + "=?",
				new String[] { respondentId.toString(), questionId }, null,
				null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				resp = new QuestionResponse();
				resp.setQuestionId(questionId);
				resp.setRespondentId(respondentId);
				resp.setType(cursor.getString(cursor
						.getColumnIndexOrThrow(ANSWER_TYPE_COL)));
				resp.setValue(cursor.getString(cursor
						.getColumnIndexOrThrow(ANSWER_COL)));
				resp.setId(cursor.getLong(cursor
						.getColumnIndexOrThrow(RESP_ID_COL)));
				resp.setIncludeFlag(cursor.getString(cursor
						.getColumnIndexOrThrow(INCLUDE_FLAG_COL)));
				resp.setScoredValue(cursor.getString(cursor
						.getColumnIndexOrThrow(SCORED_VAL_COL)));
				resp.setStrength(cursor.getString(cursor
						.getColumnIndexOrThrow(STRENGTH_COL)));
			}
			cursor.close();
		}
		return resp;
	}

	/**
	 * inserts or updates a question response after first looking to see if it
	 * already exists in the database.
	 * 
	 * @param resp
	 * @return
	 */
	public QuestionResponse createOrUpdateSurveyResponse(QuestionResponse resp) {
		QuestionResponse responseToSave = findSingleResponse(
				resp.getRespondentId(), resp.getQuestionId());
		if (responseToSave != null) {
			responseToSave.setValue(resp.getValue());
			responseToSave.setStrength(resp.getStrength());
			responseToSave.setScoredValue(resp.getScoredValue());
			if (resp.getType() != null) {
				responseToSave.setType(resp.getType());
			}
		} else {
			responseToSave = resp;
		}
		long id = -1;
		ContentValues initialValues = new ContentValues();
		initialValues.put(ANSWER_COL, responseToSave.getValue());
		initialValues.put(ANSWER_TYPE_COL, responseToSave.getType());
		initialValues.put(QUESTION_FK_COL, responseToSave.getQuestionId());
		initialValues.put(SURVEY_RESPONDENT_ID_COL,
				responseToSave.getRespondentId());
		initialValues.put(SCORED_VAL_COL, responseToSave.getScoredValue());
		initialValues.put(INCLUDE_FLAG_COL, resp.getIncludeFlag());
		initialValues.put(STRENGTH_COL, responseToSave.getStrength());
		if (responseToSave.getId() == null) {
			id = database.insert(RESPONSE_TABLE, null, initialValues);
		} else {
			if (database.update(RESPONSE_TABLE, initialValues, RESP_ID_COL
					+ "=?", new String[] { responseToSave.getId().toString() }) > 0) {
				id = responseToSave.getId();
			}
		}
		responseToSave.setId(id);
		resp.setId(id);
		return responseToSave;
	}

	/**
	 * this method will get the max survey respondent ID that has an unsubmitted
	 * survey or, if none exists, will create a new respondent
	 * 
	 * @param surveyId
	 * @return
	 */
	public long createOrLoadSurveyRespondent(String surveyId, String userId) {
		Cursor results = database.query(RESPONDENT_TABLE, new String[] { "max("
				+ PK_ID_COL + ")" }, SUBMITTED_FLAG_COL + "='false' and "
				+ SURVEY_FK_COL + "=? and " + STATUS_COL + " =?", new String[] {
				surveyId, ConstantUtil.CURRENT_STATUS }, null, null, null);
		long id = -1;
		if (results != null && results.getCount() > 0) {
			results.moveToFirst();
			id = results.getLong(0);
		}
		if (results != null) {
			results.close();
		}
		if (id <= 0) {
			id = createSurveyRespondent(surveyId, userId);
		}
		return id;
	}

	/**
	 * creates a new unsubmitted survey respondent record
	 * 
	 * @param surveyId
	 * @return
	 */
	public long createSurveyRespondent(String surveyId, String userId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(SURVEY_FK_COL, surveyId);
		initialValues.put(SUBMITTED_FLAG_COL, "false");
		initialValues.put(EXPORTED_FLAG_COL, "false");
		initialValues.put(USER_FK_COL, userId);
		initialValues.put(STATUS_COL, ConstantUtil.CURRENT_STATUS);
		initialValues.put(UUID_COL, UUID.randomUUID().toString());
		return database.insert(RESPONDENT_TABLE, null, initialValues);
	}

	/**
	 * creates a new plot point in the database for the plot and coordinates
	 * sent in
	 * 
	 * @param plotId
	 * @param lat
	 * @param lon
	 * @return
	 */
	public long savePlotPoint(String plotId, String lat, String lon,
			double currentElevation) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(PLOT_FK_COL, plotId);
		initialValues.put(LAT_COL, lat);
		initialValues.put(LON_COL, lon);
		initialValues.put(ELEVATION_COL, currentElevation);
		initialValues.put(CREATED_DATE_COL, System.currentTimeMillis());
		return database.insert(PLOT_POINT_TABLE, null, initialValues);
	}

	/**
	 * returns a cursor listing all plots with the status passed in or all plots
	 * if status is null
	 * 
	 * @return
	 */
	public Cursor listPlots(String status) {
		Cursor cursor = database.query(PLOT_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, DESC_COL, CREATED_DATE_COL, STATUS_COL },
				status == null ? null : STATUS_COL + " = ?",
				status == null ? null : new String[] { status }, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * retrieves a plot by ID
	 * 
	 * @param id
	 * @return
	 */
	public Cursor findPlot(Long id) {
		Cursor cursor = database.query(PLOT_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, DESC_COL, CREATED_DATE_COL, STATUS_COL },
				PK_ID_COL + "=?", new String[] { id.toString() }, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * if the ID is populated, this will update a plot record. Otherwise, it
	 * will be inserted
	 * 
	 * @param id
	 * @param name
	 * @param email
	 * @return
	 */
	public long createOrUpdatePlot(Long id, String name, String desc,
			String userId) {
		ContentValues initialValues = new ContentValues();
		Long idVal = id;
		initialValues.put(DISP_NAME_COL, name);
		initialValues.put(DESC_COL, desc);
		initialValues.put(CREATED_DATE_COL, System.currentTimeMillis());
		initialValues.put(USER_FK_COL, userId);
		initialValues.put(STATUS_COL, ConstantUtil.IN_PROGRESS_STATUS);

		if (idVal == null) {
			idVal = database.insert(PLOT_TABLE, null, initialValues);
		} else {
			if (database.update(PLOT_TABLE, initialValues, PK_ID_COL + "=?",
					new String[] { idVal.toString() }) > 0) {
			}
		}
		return idVal;
	}

	/**
	 * retrieves all the points for a given plot
	 * 
	 * @param plotId
	 * @return
	 */
	public Cursor listPlotPoints(String plotId, String afterTime) {

		Cursor cursor = database
				.query(PLOT_POINT_TABLE, new String[] { PK_ID_COL, LAT_COL,
						LON_COL, CREATED_DATE_COL }, PLOT_FK_COL + " = ? and "
						+ CREATED_DATE_COL + " > ?", new String[] { plotId,
						afterTime != null ? afterTime : "0" }, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * updates the status of a plot in the db
	 * 
	 * @param plotId
	 * @param status
	 */
	public long updatePlotStatus(String plotId, String status) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(STATUS_COL, status);
		return database.update(PLOT_TABLE, initialValues, PK_ID_COL + " = ?",
				new String[] { plotId });
	}

	/**
	 * updates the status of all the plots identified by the ids sent in to the
	 * value of status
	 * 
	 * @param idList
	 * @param status
	 */
	public void updatePlotStatus(HashSet<String> idList, String status) {
		if (idList != null) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(STATUS_COL, status);
			// enhanced FOR ok here since we're dealing with an implicit
			// iterator anyway
			for (String id : idList) {
				if (updatePlotStatus(id, status) < 1) {
					Log.e(TAG, "Could not update plot status for plot " + id);
				}
			}
		}
	}

	/**
	 * lists all plot points for plots that are in the COMPLETED state
	 * 
	 * @return
	 */
	public Cursor listCompletePlotPoints() {
		Cursor cursor = database
				.query(PLOT_JOIN, new String[] {
						PLOT_TABLE + "." + PK_ID_COL + " as plot_id",
						PLOT_TABLE + "." + DISP_NAME_COL,
						PLOT_POINT_TABLE + "." + PK_ID_COL, LAT_COL, LON_COL,
						ELEVATION_COL,
						PLOT_POINT_TABLE + "." + CREATED_DATE_COL }, STATUS_COL
						+ "= ?", new String[] { ConstantUtil.COMPLETE_STATUS },
						null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * deletes the plot_point row denoted by the ID passed in
	 * 
	 * @param id
	 */
	public void deletePlotPoint(String id) {
		database.delete(PLOT_POINT_TABLE, PK_ID_COL + " = ?",
				new String[] { id });
	}

	/**
	 * returns a list of survey objects that are out of date (missing from the
	 * db or with a lower version number). If a survey is present but marked as
	 * deleted, it will not be listed as out of date (and thus won't be updated)
	 * 
	 * @param surveys
	 * @return
	 */
	public ArrayList<Survey> checkSurveyVersions(ArrayList<Survey> surveys) {
		ArrayList<Survey> outOfDateSurveys = new ArrayList<Survey>();
		for (int i = 0; i < surveys.size(); i++) {
			Cursor cursor = database.query(SURVEY_TABLE,
					new String[] { PK_ID_COL },
					PK_ID_COL + " = ? and (" + VERSION_COL + " >= ? or "
							+ DELETED_COL + " = ?)", new String[] {
							surveys.get(i).getId(),
							surveys.get(i).getVersion() + "",
							ConstantUtil.IS_DELETED }, null, null, null);

			if (cursor == null || cursor.getCount() <= 0) {
				outOfDateSurveys.add(surveys.get(i));
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		return outOfDateSurveys;
	}

	/**
	 * updates the survey table by recording the help download flag
	 * 
	 * @param idList
	 */
	public void markSurveyHelpDownloaded(String surveyId, boolean isDownloaded) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(HELP_DOWNLOADED_COL, isDownloaded ? "Y" : "N");

		if (database.update(SURVEY_TABLE, updatedValues, PK_ID_COL + " = ?",
				new String[] { surveyId }) < 1) {
			Log.e(TAG, "Could not update record for Survey " + surveyId);
		}
	}

	/**
	 * updates a survey in the db and resets the deleted flag to "N"
	 * 
	 * @param survey
	 * @return
	 */
	public void saveSurvey(Survey survey) {
		Cursor cursor = database.query(SURVEY_TABLE,
				new String[] { PK_ID_COL }, PK_ID_COL + " = ?",
				new String[] { survey.getId(), }, null, null, null);
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, survey.getId());
		updatedValues.put(VERSION_COL, survey.getVersion());
		updatedValues.put(TYPE_COL, survey.getType());
		updatedValues.put(LOCATION_COL, survey.getLocation());
		updatedValues.put(FILENAME_COL, survey.getFileName());
		updatedValues.put(DISP_NAME_COL, survey.getName());
		updatedValues.put(PROJECT_COL, survey.getProjectId());
		updatedValues.put(LANGUAGE_COL, survey.getLanguage() != null ? survey
				.getLanguage().toLowerCase() : ConstantUtil.ENGLISH_CODE);
		updatedValues.put(HELP_DOWNLOADED_COL, survey.isHelpDownloaded() ? "Y"
				: "N");
		updatedValues.put(DELETED_COL, ConstantUtil.NOT_DELETED);

		if (cursor != null && cursor.getCount() > 0) {
			// if we found an item, it's an update, otherwise, it's an insert
			database.update(SURVEY_TABLE, updatedValues, PK_ID_COL + " = ?",
					new String[] { survey.getId() });
		} else {
			database.insert(SURVEY_TABLE, null, updatedValues);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * Gets a single survey from the db using its primary key
	 */
	public Survey findSurvey(String surveyId) {
		Survey survey = null;
		Cursor cursor = database.query(SURVEY_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, LOCATION_COL, FILENAME_COL, TYPE_COL,
				LANGUAGE_COL, HELP_DOWNLOADED_COL, PROJECT_COL }, PK_ID_COL + " = ?",
				new String[] { surveyId }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				survey = new Survey();
				survey.setId(surveyId);
				survey.setName(cursor.getString(cursor
						.getColumnIndexOrThrow(DISP_NAME_COL)));
				survey.setProjectId(cursor.getString(cursor
						.getColumnIndexOrThrow(PROJECT_COL)));
				survey.setLocation(cursor.getString(cursor
						.getColumnIndexOrThrow(LOCATION_COL)));
				survey.setFileName(cursor.getString(cursor
						.getColumnIndexOrThrow(FILENAME_COL)));
				survey.setType(cursor.getString(cursor
						.getColumnIndexOrThrow(TYPE_COL)));
				survey.setHelpDownloaded(cursor.getString(cursor
						.getColumnIndexOrThrow(HELP_DOWNLOADED_COL)));
				survey.setLanguage(cursor.getString(cursor
						.getColumnIndexOrThrow(LANGUAGE_COL)));
			}
			cursor.close();
		}

		return survey;
	}

	/**
	 * deletes all the projects from the database
	 */
	public void deleteAllProjects() {
		database.delete(PROJECT_TABLE, null, null);
	}

	/**
	 * Gets a single project from the db using its primary key
	 */
	public Project findProject(String projectId) {
		Project project = null;
		Cursor cursor = database.query(PROJECT_TABLE, new String[] { PK_ID_COL,
				PROJECT_NAME_COL, PROJECT_COL}, PK_ID_COL + " = ?",
				new String[] { projectId }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				project = new Project();
				project.setProjectId(projectId);
				project.setName(cursor.getString(cursor
						.getColumnIndexOrThrow(PROJECT_NAME_COL)));
			}
			cursor.close();
		}
		return project;
	}

	/**
	 * Lists all projects in the database
	 */
	public ArrayList<Project> listProjects() {
		ArrayList<Project> projects = new ArrayList<Project>();
		Cursor cursor = database.query(PROJECT_TABLE, new String[] { PK_ID_COL,
				PROJECT_NAME_COL, PROJECT_COL}, null, null, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					Project project = new Project();
					project.setProjectId(cursor.getString(cursor
							.getColumnIndexOrThrow(PROJECT_COL)));
					project.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(PROJECT_NAME_COL)));
					projects.add(project);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return projects;
	}

	/**
	 * updates a projct in the db
	 *
	 * @param survey
	 * @return
	 */
	public void saveProject(Project project) {
		Cursor cursor = database.query(PROJECT_TABLE,
				new String[] { PK_ID_COL }, PK_ID_COL + " = ?",
				new String[] { project.getProjectId(), }, null, null, null);
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, project.getProjectId());
		updatedValues.put(PROJECT_NAME_COL, project.getName());
		updatedValues.put(PROJECT_COL, project.getProjectId());

		if (cursor != null && cursor.getCount() > 0) {
			// if we found an item, it's an update, otherwise, it's an insert
			database.update(PROJECT_TABLE, updatedValues, PK_ID_COL + " = ?",
					new String[] { project.getProjectId() });
		} else {
			database.insert(PROJECT_TABLE, null, updatedValues);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * lists all survey respondents with specified status
	 * sorted by creation order (primary key)
	 * or delivered date
	 * 
	 * @param status
	 * @return
	 */
	public Cursor listSurveyRespondent(String status, boolean byDelivered) {
		String[] whereParams = { status };
		String sortBy;
		if (byDelivered){
			sortBy = "case when " + DELIVERED_DATE_COL + " is null then 0 else 1 end, " + DELIVERED_DATE_COL + " desc"; 
		} else {
			sortBy = RESPONDENT_TABLE + "." + PK_ID_COL + " desc";
		}
		Cursor cursor = database.query(RESPONDENT_JOIN, new String[] {
				RESPONDENT_TABLE + "." + PK_ID_COL, DISP_NAME_COL,
				SAVED_DATE_COL, SURVEY_FK_COL, USER_FK_COL, SUBMITTED_DATE_COL,
				DELIVERED_DATE_COL, UUID_COL },
				"status = ?", whereParams,
				null,
				null,
				sortBy);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * count survey respondents by status
	 * 
	 * @param status
	 * @return
	 */
	public int countSurveyRespondents(String status) {
		String[] whereParams = { status };
		int i = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) as theCount FROM survey_respondent WHERE status = ?",
				whereParams);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				i = cursor.getInt(0);
			}
			cursor.close();
		}
		return i;
	}
	
	/**
	 * count unsent survey respondents by surveyId
	 * 
	 * @param surveyId
	 * @return
	 */
	// TODO check this function
	public int countUnsentSurveyRespondents(String surveyId) {
		String[] whereParams = { surveyId };
		int i = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) as theCount FROM survey_respondent WHERE survey_id = ? AND delivered_date = null",
				whereParams);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				i = cursor.getInt(0);
			}
			cursor.close();
		}
		return i;
	}
	
	
	/**
	 * Lists all non-deleted surveys from the database
	 */
	public ArrayList<Survey> listSurveys(String language) {
		ArrayList<Survey> surveys = new ArrayList<Survey>();
		String whereClause = DELETED_COL + " <> ?";
		String[] whereParams = null;
		if (language != null) {
			whereClause += " and " + LANGUAGE_COL + " = ?";
			whereParams = new String[] { ConstantUtil.IS_DELETED,
					language.toLowerCase().trim() };
		} else {
			whereParams = new String[] { ConstantUtil.IS_DELETED };
		}
		Cursor cursor = database.query(SURVEY_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, LOCATION_COL, FILENAME_COL, TYPE_COL,
				LANGUAGE_COL, HELP_DOWNLOADED_COL, VERSION_COL }, whereClause,
				whereParams, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					Survey survey = new Survey();
					survey.setId(cursor.getString(cursor
							.getColumnIndexOrThrow(PK_ID_COL)));
					survey.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(DISP_NAME_COL)));
					survey.setLocation(cursor.getString(cursor
							.getColumnIndexOrThrow(LOCATION_COL)));
					survey.setFileName(cursor.getString(cursor
							.getColumnIndexOrThrow(FILENAME_COL)));
					survey.setType(cursor.getString(cursor
							.getColumnIndexOrThrow(TYPE_COL)));
					survey.setHelpDownloaded(cursor.getString(cursor
							.getColumnIndexOrThrow(HELP_DOWNLOADED_COL)));
					survey.setLanguage(cursor.getString(cursor
							.getColumnIndexOrThrow(LANGUAGE_COL)));
					survey.setVersion(cursor.getDouble(cursor
							.getColumnIndexOrThrow(VERSION_COL)));
					surveys.add(survey);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return surveys;
	}

	/**
	 * Lists all non-deleted surveys from the database
	 */
	public ArrayList<Survey> listSurveysForProject(String projectId) {
		ArrayList<Survey> surveys = new ArrayList<Survey>();
		String whereClause = DELETED_COL + " <> ?";
		String[] whereParams = null;
		whereClause += " and " + PROJECT_COL + " = ?";
		whereParams = new String[] { ConstantUtil.IS_DELETED, projectId };
		Cursor cursor = database.query(SURVEY_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, LOCATION_COL, FILENAME_COL, TYPE_COL,
				LANGUAGE_COL, HELP_DOWNLOADED_COL, VERSION_COL, PROJECT_COL }, whereClause,
				whereParams, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					Survey survey = new Survey();
					survey.setId(cursor.getString(cursor
							.getColumnIndexOrThrow(PK_ID_COL)));
					survey.setProjectId(cursor.getString(cursor
							.getColumnIndexOrThrow(PROJECT_COL)));
					survey.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(DISP_NAME_COL)));
					survey.setLocation(cursor.getString(cursor
							.getColumnIndexOrThrow(LOCATION_COL)));
					survey.setFileName(cursor.getString(cursor
							.getColumnIndexOrThrow(FILENAME_COL)));
					survey.setType(cursor.getString(cursor
							.getColumnIndexOrThrow(TYPE_COL)));
					survey.setHelpDownloaded(cursor.getString(cursor
							.getColumnIndexOrThrow(HELP_DOWNLOADED_COL)));
					survey.setLanguage(cursor.getString(cursor
							.getColumnIndexOrThrow(LANGUAGE_COL)));
					survey.setVersion(cursor.getDouble(cursor
							.getColumnIndexOrThrow(VERSION_COL)));
					surveys.add(survey);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return surveys;
	}

	/**
	 * marks a survey record identified by the ID passed in as deleted.
	 * 
	 * @param surveyId
	 */
	public void deleteSurvey(String surveyId, boolean physicalDelete) {
		if (!physicalDelete) {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(DELETED_COL, ConstantUtil.IS_DELETED);
			database.update(SURVEY_TABLE, updatedValues, PK_ID_COL + " = ?",
					new String[] { surveyId });
		} else {
			database.delete(SURVEY_TABLE, PK_ID_COL + " = ? ",
					new String[] { surveyId });
		}
	}

	/**
	 * returns the value of a single setting identified by the key passed in
	 */
	public String findPreference(String key) {
		return databaseHelper.findPreference(database, key);
	}

	/**
	 * Lists all settings from the database
	 */
	public HashMap<String, String> listPreferences() {
		HashMap<String, String> settings = new HashMap<String, String>();
		Cursor cursor = database.query(PREFERENCES_TABLE, new String[] {
				KEY_COL, VALUE_COL }, null, null, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					settings.put(cursor.getString(cursor
							.getColumnIndexOrThrow(KEY_COL)), cursor
							.getString(cursor.getColumnIndexOrThrow(VALUE_COL)));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return settings;
	}

	/**
	 * persists setting to the db
	 * 
	 * @param surveyId
	 */
	public void savePreference(String key, String value) {
		databaseHelper.savePreference(database, key, value);
	}

	/**
	 * deletes all the surveys from the database
	 */
	public void deleteAllSurveys() {
		database.delete(SURVEY_TABLE, null, null);
	}

	/**
	 * deletes all the points of interest
	 */
	public void deleteAllPoints() {
		database.delete(POINT_OF_INTEREST_TABLE, null, null);
	}

	/**
	 * deletes all survey responses from the database
	 */
	public void deleteAllResponses() {
		database.delete(RESPONSE_TABLE, null, null);
		database.delete(RESPONDENT_TABLE, null, null);
	}

	/**
	 * deletes all survey responses from the database for a specific respondent
	 */
	public void deleteResponses(String respondentId) {
		database.delete(RESPONSE_TABLE, SURVEY_RESPONDENT_ID_COL + "=?",
				new String[] { respondentId });
	}

	/**
	 * deletes the respondent record and any responses it contains
	 * 
	 * @param respondentId
	 */
	public void deleteRespondent(String respondentId) {
		deleteResponses(respondentId);
		database.delete(RESPONDENT_TABLE, PK_ID_COL + "=?",
				new String[] { respondentId });
	}

	/**
	 * deletes a single response
	 * 
	 * @param respondentId
	 * @param questionId
	 */
	public void deleteResponse(String respondentId, String questionId) {
		database.delete(RESPONSE_TABLE, SURVEY_RESPONDENT_ID_COL + "=? AND "
				+ QUESTION_FK_COL + "=?", new String[] { respondentId,
				questionId });
	}

	/**
	 * saves or updates a PointOfInterests
	 * 
	 * @param id
	 * @param country
	 * @param jsonString
	 */
	public void saveOrUpdatePointOfInterest(PointOfInterest point) {
		Cursor cursor = database.query(POINT_OF_INTEREST_TABLE,
				new String[] { PK_ID_COL }, PK_ID_COL + "=?",
				new String[] { point.getId().toString() }, null, null, null);
		boolean isUpdate = false;
		if (cursor != null && cursor.getCount() > 0) {
			isUpdate = true;
		}
		if (cursor != null) {
			cursor.close();
		}
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, point.getId());
		updatedValues.put(COUNTRY_COL,
				point.getCountry() != null ? point.getCountry() : "unknown");
		updatedValues.put(DISP_NAME_COL,
				point.getName() != null ? point.getName() : "unknown");
		updatedValues.put(TYPE_COL, point.getType() != null ? point.getType()
				: "unknown");
		updatedValues.put(UPDATED_DATE_COL, System.currentTimeMillis());
		String temp = point.getPropertyNamesString();
		if (temp != null) {
			updatedValues.put(PROP_NAME_COL, temp);
		}
		temp = point.getPropertyValuesString();
		if (temp != null) {
			updatedValues.put(PROP_VAL_COL, temp);
		}
		if (point.getLatitude() != null) {
			updatedValues.put(LAT_COL, point.getLatitude());
		}
		if (point.getLongitude() != null) {
			updatedValues.put(LON_COL, point.getLongitude());
		}

		if (isUpdate) {
			database.update(POINT_OF_INTEREST_TABLE, updatedValues, PK_ID_COL
					+ "=?", new String[] { point.getId().toString() });
		} else {
			database.insert(POINT_OF_INTEREST_TABLE, null, updatedValues);
		}
	}

	/**
	 * lists all points of interest, optionally filtering by country code and name prefix
	 * 
	 * @param country
	 * @param prefix
	 * @return list
	 */
	public ArrayList<PointOfInterest> listPointsOfInterest(String country,
			String prefix) {
		ArrayList<PointOfInterest> points = null;
		String whereClause = null;
		String[] whereValues = null;

		if (country != null) {
			whereClause = COUNTRY_COL + "=?";
			whereValues = new String[] { country };

		}
		if (prefix != null && prefix.trim().length() > 0) {
			if (country != null) {
				whereClause = whereClause + " AND " + DISP_NAME_COL + " like ?";
				whereValues = new String[2];
				whereValues[0] = country;
				whereValues[1] = prefix + "%";
			} else {
				whereClause = DISP_NAME_COL + " like ?";
				whereValues = new String[] { prefix + "%" };
			}
		}
		Cursor cursor = database.query(POINT_OF_INTEREST_TABLE, new String[] {
				PK_ID_COL, COUNTRY_COL, DISP_NAME_COL, UPDATED_DATE_COL,
				LAT_COL, LON_COL, PROP_NAME_COL, PROP_VAL_COL, TYPE_COL },
				whereClause, whereValues, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				cursor.moveToFirst();
				points = new ArrayList<PointOfInterest>();
				do {
					PointOfInterest point = new PointOfInterest();
					point.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(DISP_NAME_COL)));
					point.setType(cursor.getString(cursor
							.getColumnIndexOrThrow(TYPE_COL)));
					point.setCountry(cursor.getString(cursor
							.getColumnIndexOrThrow(COUNTRY_COL)));
					point.setId(cursor.getLong(cursor
							.getColumnIndexOrThrow(PK_ID_COL)));
					point.setPropertyNames(cursor.getString(cursor
							.getColumnIndexOrThrow(PROP_NAME_COL)));
					point.setPropertyValues(cursor.getString(cursor
							.getColumnIndexOrThrow(PROP_VAL_COL)));
					point.setLatitude(cursor.getDouble(cursor
							.getColumnIndexOrThrow(LAT_COL)));
					point.setLongitude(cursor.getDouble(cursor
							.getColumnIndexOrThrow(LON_COL)));

					points.add(point);

				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return points;
	}

	/**
	 * lists all points of interest, filtering by distance, optionally filtering by country code
	 * 
	 * @param country - country code
	 * @param prefix
	 * @param latitude - decimal degrees
	 * @param longitude - decimal degrees
	 * @param radius - meters
	 * @return ArrayList<PointOfInterest>
	 */
	public ArrayList<PointOfInterest> listPointsOfInterest(String country, String prefix, Double longitude, Double latitude, Double radius ) {
		if (longitude == null || latitude == null)
			return listPointsOfInterest(country, prefix);
		
		ArrayList<PointOfInterest> points = null;
		String whereClause = null;
		String[] whereValues; //unfortunately, length of this array must match the number of ?'s in the whereClause
		@SuppressWarnings("unused")
		int i;
		//Maximum angular difference for a given radius. Must avoid problems at high latitudes....
		double nsDegrees = radius * 360 / 40000000;
		double ewDegrees;
		if (Math.abs(latitude) > 80.0d)
			ewDegrees = 0; //don't use, just include the whole [ant]arctic...
		else
			ewDegrees = radius * 360 / (40000000 * Math.cos(latitude*Math.PI/180.0d));
		if (Math.abs(ewDegrees) > 180.0d)
			ewDegrees = 0;

		//Longitude is a little tricky (if we are out in the Pacific...)
		double east = longitude + ewDegrees;
		double west = longitude - ewDegrees;

		whereClause = LAT_COL + "<? AND " + LAT_COL + ">?";
		if (ewDegrees == 0.0d){ //degenerate case
			whereValues = new String[2];
			i = 2;
		} else	{
			whereValues = new String[4];
			whereValues[2] = Double.toString(east); //East limit
			whereValues[3] = Double.toString(west); //West limit
			i = 4;
			if (east > 180.0d){ //wrapped
				east = east - 360.0d;
				whereClause += " AND (" + LON_COL + "<? OR " + LON_COL + ">?)"; 
			} else	
			if (west < -180.0d){
				west = west + 360.0d;
				whereClause += " AND (" + LON_COL + "<? OR " + LON_COL + ">?) "; 
					
			} else	//boring case		
				whereClause += " AND " + LON_COL + "<? AND " + LON_COL + ">? "; 
		}
		//Latitude is simple
		whereValues[0] = Double.toString(Math.min(latitude + nsDegrees,  90.0d));//North limit
		whereValues[1] = Double.toString(Math.max(latitude - nsDegrees, -90.0d));//South limit

/*	TODO: (maybe)
		if (country != null) {
			whereClause += " AND " + COUNTRY_COL + "=?";
			whereValues[i] = country;

		}
		if (prefix != null && prefix.trim().length() > 0) {
			whereClause += " AND " + DISP_NAME_COL + " like ?";
			whereValues = new String[2];
			whereValues[i] = country;
			whereValues[i+1] = prefix + "%";
		}
		*/
		Cursor cursor = database.query(POINT_OF_INTEREST_TABLE, new String[] {
				PK_ID_COL, COUNTRY_COL, DISP_NAME_COL, UPDATED_DATE_COL,
				LAT_COL, LON_COL, PROP_NAME_COL, PROP_VAL_COL, TYPE_COL },
				whereClause, whereValues, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				cursor.moveToFirst();
				points = new ArrayList<PointOfInterest>();
				do {
					PointOfInterest point = new PointOfInterest();
					point.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(DISP_NAME_COL)));
					point.setType(cursor.getString(cursor
							.getColumnIndexOrThrow(TYPE_COL)));
					point.setCountry(cursor.getString(cursor
							.getColumnIndexOrThrow(COUNTRY_COL)));
					point.setId(cursor.getLong(cursor
							.getColumnIndexOrThrow(PK_ID_COL)));
					point.setPropertyNames(cursor.getString(cursor
							.getColumnIndexOrThrow(PROP_NAME_COL)));
					point.setPropertyValues(cursor.getString(cursor
							.getColumnIndexOrThrow(PROP_VAL_COL)));
					point.setLatitude(cursor.getDouble(cursor
							.getColumnIndexOrThrow(LAT_COL)));
					point.setLongitude(cursor.getDouble(cursor
							.getColumnIndexOrThrow(LON_COL)));
					float[] distance = new float[1];
					Location.distanceBetween(latitude,longitude,
											 point.getLatitude(),point.getLongitude(),
											 distance);
					if (distance[0] < radius){//now keep only those within actual distance
						point.setDistance(Double.valueOf(distance[0]));
						points.add(point);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return points;
	}

	
	/**
	 * inserts a transmissionHistory row into the db
	 * 
	 * @param respId
	 * @param fileName
	 * @param status
	 * @return uid of created record
	 */
	public Long createTransmissionHistory(Long respId, String fileName,
			String status) {
		ContentValues initialValues = new ContentValues();
		Long idVal = null;
		initialValues.put(SURVEY_RESPONDENT_ID_COL, respId);
		initialValues.put(FILENAME_COL, fileName);

		if (status != null) {
			initialValues.put(STATUS_COL, status);
			if (ConstantUtil.IN_PROGRESS_STATUS.equals(status)) {
				initialValues.put(TRANS_START_COL, System.currentTimeMillis());
			}
		} else {
			initialValues.put(TRANS_START_COL, (Long) null);
			initialValues.put(STATUS_COL, ConstantUtil.QUEUED_STATUS);
		}
		idVal = database
				.insert(TRANSMISSION_HISTORY_TABLE, null, initialValues);
		return idVal;
	}

	
	/**
	 * updates the first matching transmission history record with the status passed in.
	 * If the status == Completed, the completion date is updated.
	 * If the status == In Progress, the start date is updated.
	 * 
	 * @param respondId
	 * @param fileName
	 * @param status
	 */
	public void updateTransmissionHistory(Long respondId, String fileName, String status) {
		ArrayList<FileTransmission> transList = listFileTransmission(respondId,	fileName, true);
		Long idVal = null;
		if (transList != null && transList.size() > 0) {
			idVal = transList.get(0).getId();
			if (idVal != null) {
				ContentValues vals = new ContentValues();
				vals.put(STATUS_COL, status);
				if (ConstantUtil.COMPLETE_STATUS.equals(status)) {
					vals.put(DELIVERED_DATE_COL, System.currentTimeMillis()
							+ "");
				} else if (ConstantUtil.IN_PROGRESS_STATUS.equals(status)) {
					vals.put(TRANS_START_COL, System.currentTimeMillis() + "");
				}
				database.update(TRANSMISSION_HISTORY_TABLE, vals, PK_ID_COL
						+ " = ?", new String[] { idVal.toString() });
			}
			else //it should have been found
				Log.e(TAG,
						"Could not update transmission history record for respondent_id "
								+ respondId
								+ " filename "
								+ fileName);

		}
	}

	
	/**
	 * lists all the file transmissions for the values passed in.
	 * 
	 * @param respondentId
	 *            - MANDATORY id of the survey respondent
	 * @param fileName
	 *            - OPTIONAL file name
	 * @param incompleteOnly
	 *            - if true, only rows without a complete status will be
	 *            returned
	 * @return
	 */
	public ArrayList<FileTransmission> listFileTransmission(Long respondentId,
			String fileName, boolean incompleteOnly) {
		ArrayList<FileTransmission> transList = null;

		String whereClause = SURVEY_RESPONDENT_ID_COL + "=?";
		if (incompleteOnly) {
			whereClause = whereClause + " AND " + STATUS_COL + " <> '"
					+ ConstantUtil.COMPLETE_STATUS + "'";
		}
		String[] whereValues = null;

		if (fileName != null && fileName.trim().length() > 0) {
			whereClause = whereClause + " AND " + FILENAME_COL + " = ?";
			whereValues = new String[2];
			whereValues[0] = respondentId.toString();
			whereValues[1] = fileName;

		} else {
			whereValues = new String[] { respondentId.toString() };
		}

		Cursor cursor = database.query(TRANSMISSION_HISTORY_TABLE,
				new String[] { PK_ID_COL, FILENAME_COL, STATUS_COL,
						TRANS_START_COL, DELIVERED_DATE_COL,
						SURVEY_RESPONDENT_ID_COL }, whereClause, whereValues,
				null, null, TRANS_START_COL + " desc");
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				cursor.moveToFirst();
				transList = new ArrayList<FileTransmission>();
				do {
					FileTransmission trans = new FileTransmission();
					trans.setId(cursor.getLong(cursor
							.getColumnIndexOrThrow(PK_ID_COL)));
					trans.setRespondentId(respondentId);
					trans.setFileName(cursor.getString(cursor
							.getColumnIndexOrThrow(FILENAME_COL)));
					Long startDateMillis = cursor.getLong(cursor
							.getColumnIndexOrThrow(TRANS_START_COL));
					if (startDateMillis != null && startDateMillis > 0) {
						trans.setStartDate(new Date(startDateMillis));
					}
					Long delivDateMillis = cursor.getLong(cursor
							.getColumnIndexOrThrow(DELIVERED_DATE_COL));
					if (delivDateMillis != null && delivDateMillis > 0) {
						trans.setEndDate(new Date(delivDateMillis));
					}
					trans.setStatus(cursor.getString(cursor
							.getColumnIndexOrThrow(STATUS_COL)));
					transList.add(trans);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return transList;
	}

	/**
	 * marks submitted data as unsent. If an ID is passed in, only that
	 * submission will be updated. If id is null, ALL data will be marked as
	 * unsent.
	 */
	public void markDataUnsent(Long id) {
		if (id == null) {
			executeSql("update survey_respondent set media_sent_flag = 'false', delivered_date = null;");
		} else {
			executeSql("update survey_respondent set media_sent_flag = 'false', delivered_date = null where _id = "
					+ id);
		}
	}

	/**
	 * executes a single insert/update/delete DML or any DDL statement without
	 * any bind arguments.
	 * 
	 * @param sql
	 */
	public void executeSql(String sql) {
		database.execSQL(sql);
	}

	/**
	 * reinserts the test survey into the database. For debugging purposes only.
	 * The survey xml must exist in the APK
	 */
	public void reinstallTestSurvey() {
		executeSql("insert into survey values(999991,'Sample Survey', 1.0,'Survey','res','testsurvey','english','N','N')");
	}

	/**
	 * permanently deletes all surveys, responses, users and transmission
	 * history from the database
	 */
	public void clearAllData() {
		executeSql("delete from survey");
		executeSql("delete from survey_respondent");
		executeSql("delete from survey_response");
		executeSql("delete from user");
		executeSql("delete from transmission_history");
		executeSql("update preferences set value = '' where key = 'user.lastuser.id'");
	}

	/**
	 * performs a soft-delete on a user
	 * 
	 * @param id
	 */
	public void deleteUser(Long id) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(DELETED_COL, "Y");
		database.update(USER_TABLE, updatedValues, PK_ID_COL + " = ?",
				new String[] { id.toString() });
	}

	/**
	 * retrieves a surveyedLocale by Id
	 * the Id is a UUID string
	 *
	 * @param id
	 * @return
	 */
	// TODO should be combined with listSurveyedLocalesByCursor
	public SurveyedLocale findSurveyedLocale(String id) {
		SurveyedLocale sl = null;
		Cursor cursor = database.query(SURVEYED_LOCALE_TABLE, new String[] { PK_ID_COL,
				LOCALE_UNIQUE_ID_COL, PROJECT_COL, LAST_SUBMITTED_COL, LAT_COL, LON_COL, STATUS_COL }, PK_ID_COL + "=?",
				new String[] { id }, null, null, null);
		if (cursor != null && cursor.moveToFirst()){
			sl = new SurveyedLocale();
			sl.setId(cursor.getLong(cursor
					.getColumnIndexOrThrow(PK_ID_COL)));
				sl.setProjectId(cursor.getString(cursor
					.getColumnIndexOrThrow(PROJECT_COL)));
				sl.setLatitude(cursor.getDouble(cursor
					.getColumnIndexOrThrow(LAT_COL)));
				sl.setLongitude(cursor.getDouble(cursor
					.getColumnIndexOrThrow(LON_COL)));
				sl.setLocaleUniqueId(cursor.getString(cursor
					.getColumnIndexOrThrow(LOCALE_UNIQUE_ID_COL)));
				sl.setLastSubmittedDate(cursor.getLong(cursor
					.getColumnIndexOrThrow(LAST_SUBMITTED_COL)));
				sl.setStatus(cursor.getInt(cursor
					.getColumnIndexOrThrow(STATUS_COL)));

				// add metric fields for display in lists
				List<String> metricNames = new ArrayList<String>();
				List<String> metricValues = new ArrayList<String>();
				List<SurveyedLocaleValue> slvList = listSurveyedLocaleValuesByLocaleId(sl.getId() + "");
				for (SurveyedLocaleValue slv : slvList){
					if (getIncludeInListByQuestionId(slv.getQuestionId())){
						metricNames.add(getMetricNameByQuestionId(slv.getQuestionId()));
						metricValues.add(slv.getAnswerValue());
					}
				}
				sl.setMetricNames(metricNames);
				sl.setMetricValues(metricValues);
		}
		return sl;
	}

	/**
	 * retrieves a surveyedLocale by uuid
	 *
	 * @param id
	 * @return
	 */
	public Cursor findSurveyedLocaleByIdentifier(String uuid) {
		Cursor cursor = database.query(SURVEYED_LOCALE_TABLE, new String[] { PK_ID_COL,
				LOCALE_UNIQUE_ID_COL, PROJECT_COL, LAST_SUBMITTED_COL, LAT_COL, LON_COL, STATUS_COL }, LOCALE_UNIQUE_ID_COL + "=?",
				new String[] { uuid }, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * returns a cursor that lists all surveyed locales
	 * used for testing
	 * @return
	 */
	public Cursor listAllSurveyedLocales() {
		Cursor cursor = database.query(SURVEYED_LOCALE_TABLE, new String[] {
				PK_ID_COL, LOCALE_UNIQUE_ID_COL, PROJECT_COL, LAST_SUBMITTED_COL, LAT_COL, LON_COL, STATUS_COL },
				null, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * returns a cursor that lists all surveyed locale values
	 * used for testing
	 *
	 * @return
	 */
	public Cursor listAllSurveyedLocaleValues() {
		Cursor cursor = database.query(SURVEYED_LOCALE_VAL_TABLE, new String[] {
				PK_ID_COL, QUESTION_COL, ANSWER_COL, SURVEYED_LOCALE_COL},
				null, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * lists all surveyal values for the locale id passed in
	 * @param slId
	 * @return
	 */
	public List<SurveyedLocaleValue> listSurveyedLocaleValuesByLocaleId(String slId) {
		Cursor cursor = database.query(SURVEYED_LOCALE_VAL_TABLE, new String[] {
				PK_ID_COL, QUESTION_COL, ANSWER_COL, SURVEYED_LOCALE_COL}, SURVEYED_LOCALE_COL + "=?",
				new String[] {slId}, null, null, null);

		List<SurveyedLocaleValue> slvList = new ArrayList<SurveyedLocaleValue>();
		if (cursor != null && cursor.moveToFirst()){
			do {
				SurveyedLocaleValue slv = new SurveyedLocaleValue();
				slv.setId(cursor.getLong(cursor
					.getColumnIndexOrThrow(PK_ID_COL)));
				slv.setQuestionId(cursor.getString(cursor
					.getColumnIndexOrThrow(QUESTION_COL)));
				slv.setAnswerValue(cursor.getString(cursor
					.getColumnIndexOrThrow(ANSWER_COL)));
				slvList.add(slv);
			} while (cursor.moveToNext());
		}
		return slvList;
	}

	/**
	 * deletes all existing surveyalValues for the surveyed locale id passed in.
	 * @param slId
	 */
	public void deleteSurveyalValuesById(Long slId){
		database.delete(SURVEYED_LOCALE_VAL_TABLE, SURVEYED_LOCALE_COL + "=?",
				new String[] { slId.toString() });
	}

	/**
	 * deletes all existing surveyalValues in the database
	 * @return
	 */
	public void deleteAllSurveyalValue(){
		database.delete(SURVEYED_LOCALE_VAL_TABLE, null, null);
	}
	
	/**
	 * deletes all existing surveyedLocales in the database
	 * @return
	 */
	public void deleteAllSurveyedLocale(){
		database.delete(SURVEYED_LOCALE_TABLE, null, null);
	}

	public int countSurveyedLocale() {
		int i = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) as theCount FROM surveyed_locale",
				null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				i = cursor.getInt(0);
			}
			cursor.close();
		}
		return i;
	}
	
	/**
	 * creates or updates a Surveyed Locale record based on the data passed in.
	 *
	 * @param id
	 * @param projectId
	 * @param lastSDateUnix
	 * @param lat
	 * @param lon
	 * @return
	 */
	public long createOrUpdateSurveyedLocale(String unique_id, String projectId, Long lastSDateUnix, Double lat, Double lon) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(LOCALE_UNIQUE_ID_COL, unique_id);
		initialValues.put(PROJECT_COL, projectId);
		initialValues.put(LAST_SUBMITTED_COL, lastSDateUnix);
		initialValues.put(LAT_COL, lat);
		initialValues.put(LON_COL, lon);
		initialValues.put(STATUS_COL, 0);

		Cursor existingSL = findSurveyedLocaleByIdentifier(unique_id);
		Long slId = null;
		if (existingSL != null && existingSL.moveToFirst()){
			slId = (long) existingSL.getInt(existingSL.getColumnIndexOrThrow(PK_ID_COL));
		}

		if (slId == null) {
			slId = database.insert(SURVEYED_LOCALE_TABLE, null, initialValues);
		} else {
			if (database.update(SURVEYED_LOCALE_TABLE, initialValues, PK_ID_COL + "=?",
					new String[] { slId.toString() }) > 0) {
			}
		}
		existingSL.close();
		return slId;
	}
	
	/**
	 * deletes all existing surveyalValues for the surveyed locale id passed in
	 * and then recreates them based on the arrays passed in.
	 * @param slId
	 * @param questionIdArray
	 * @param answerValArray
	 * @return
	 */
	public void createOrUpdateSurveyalValues(Long slId, String uniqueId, JSONArray questionIdArray, JSONArray answerValArray) {
		// first delete all surveyalValues with slId = surveyed_locale_id
		deleteSurveyalValuesById(Long.valueOf(slId));

		// recreate all surveyalValues.
		boolean failed = false;

		// put uniqueId in here as well, so we can search for it
		// we use 0 as question id, as it does not matter.
		ContentValues initialValues = new ContentValues();
		initialValues.put(SURVEYED_LOCALE_COL, slId);
		initialValues.put(QUESTION_COL,ConstantUtil.LOCALE_ID_KEY);
		initialValues.put(ANSWER_COL, uniqueId);
		database.insert(SURVEYED_LOCALE_VAL_TABLE, null, initialValues);

		// now put the the real question-answer pairs in
		for (int i = 0; i < questionIdArray.length(); i++){
			failed = false;
			initialValues = new ContentValues();
			initialValues.put(SURVEYED_LOCALE_COL, slId);
			try {
				initialValues.put(QUESTION_COL, questionIdArray.getInt(i));
				initialValues.put(ANSWER_COL, answerValArray.getString(i));
			} catch (JSONException e) {
				Log.e(TAG, "Problem with parsing questionId - answer pairs" );
				failed = true;
			}

			if (!failed) {
				database.insert(SURVEYED_LOCALE_VAL_TABLE, null, initialValues);
			}
		}
	}

	private void deleteRecordMetaByProjectId(String projectId) {
		database.delete(RECORD_META_TABLE, PROJECT_COL + "=?",
				new String[] { projectId });
	}

	public void createOrUpdateRecordMeta(String projectId,
			JSONArray questionIdsArray, JSONArray metricNamesArray, 
			JSONArray metricIdsArray, JSONArray includeInListArray) {

		// first delete all recordsMeta data by projectId
		deleteRecordMetaByProjectId(projectId);

		// recreate all record meta data
		boolean failed = false;
		for (int i = 0; i < questionIdsArray.length(); i++){
			failed = false;
			ContentValues initialValues = new ContentValues();
			initialValues.put(PROJECT_COL, projectId);
			try {
				initialValues.put(QUESTION_COL, questionIdsArray.getString(i));
				initialValues.put(METRIC_NAME_COL, metricNamesArray.getString(i));
				initialValues.put(METRIC_ID_COL, metricIdsArray.getString(i));
				initialValues.put(INCLUDE_IN_LIST_COL, includeInListArray.getBoolean(i));
			} catch (JSONException e) {
				Log.e(TAG, "Problem with parsing questionId - answer pairs" );
				failed = true;
			}

			if (!failed) {
				database.insert(RECORD_META_TABLE, null, initialValues);
			}
		}
	}

	/**
	 * Filters surveyd locales based on the parameters passed in.
	 * @param projectId
	 * @param latitude
	 * @param longitude
	 * @param filterString
	 * @param nearbyRadius
	 * @return
	 */
	public Cursor listFilteredSurveyedLocales(String projectId, Double latitude,
			Double longitude, String filterString, Double nearbyRadius) {
		List<String> whereValuesList = new ArrayList<String>();
		String queryString = "SELECT sl.* FROM " + SURVEYED_LOCALE_TABLE + " AS sl";
		String whereClause = " WHERE sl." + PROJECT_COL + " =?";
		whereValuesList.add(projectId);

		if (filterString != null && filterString.length() > 0){
			queryString += " INNER JOIN " + SURVEYED_LOCALE_VAL_TABLE + " AS slv ON sl." + PK_ID_COL + "=slv." + SURVEYED_LOCALE_COL;
			whereClause += " AND slv." + ANSWER_COL + " LIKE ?";
			whereValuesList.add(filterString+"%");
		}

		// location part
		if (latitude != null && longitude != null){
			PointF center = new PointF(latitude.floatValue(), longitude.floatValue());
			PointF p1 = GeoUtil.calculateDerivedPosition(center, nearbyRadius, 0);
			PointF p2 = GeoUtil.calculateDerivedPosition(center, nearbyRadius, 90);
			PointF p3 = GeoUtil.calculateDerivedPosition(center, nearbyRadius, 180);
			PointF p4 = GeoUtil.calculateDerivedPosition(center, nearbyRadius, 270);

			whereClause += " AND sl." + LAT_COL + " >? AND sl." + LAT_COL + " <? AND sl."
			        + LON_COL + " <? AND sl." + LON_COL + " >?";
			whereValuesList.add(String.valueOf(p3.x));
			whereValuesList.add(String.valueOf(p1.x));
			whereValuesList.add(String.valueOf(p2.y));
			whereValuesList.add(String.valueOf(p4.y));

			// this is to correct the distance for the shortening at higher latitudes
			Double fudge = Math.pow(Math.cos(Math.toRadians(latitude)),2);

			// this uses a simple planar approximation of distance. this should be good enough for our purpose.
			String orderByTempl = " ORDER BY ((%s -" + LAT_COL + ") * (%s - " + LAT_COL + ") + (%s - " + LON_COL + ") * (%s - " + LON_COL + ") * %s)";
			whereClause += String.format(orderByTempl, latitude, latitude, longitude, longitude, fudge);
		}

		String[] whereValues = (String[]) whereValuesList.toArray(new String[0]);
		Cursor cursor = database.rawQuery(queryString + whereClause, whereValues);

		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public ArrayList<SurveyedLocale> listSurveyedLocalesByCursor(Cursor slCursor, int skip, int number, Double lat, Double lon) {
		ArrayList<SurveyedLocale> slList = new ArrayList<SurveyedLocale>();
		if (slCursor != null && slCursor.moveToFirst()){
			int j = slCursor.getCount();
			if (slCursor.getCount() > skip){
				slCursor.moveToPosition(skip);
			}
			int i = 0;
			do {
				try{
					SurveyedLocale sl = new SurveyedLocale();
					sl.setId(slCursor.getLong(slCursor
						.getColumnIndexOrThrow(PK_ID_COL)));
					sl.setProjectId(slCursor.getString(slCursor
						.getColumnIndexOrThrow(PROJECT_COL)));
					sl.setLatitude(slCursor.getDouble(slCursor
						.getColumnIndexOrThrow(LAT_COL)));
					sl.setLongitude(slCursor.getDouble(slCursor
						.getColumnIndexOrThrow(LON_COL)));
					sl.setLocaleUniqueId(slCursor.getString(slCursor
						.getColumnIndexOrThrow(LOCALE_UNIQUE_ID_COL)));
					sl.setLastSubmittedDate(slCursor.getLong(slCursor
						.getColumnIndexOrThrow(LAST_SUBMITTED_COL)));
					sl.setStatus(slCursor.getInt(slCursor
						.getColumnIndexOrThrow(STATUS_COL)));

					if (lat != null && lon != null && sl.getLatitude() != null && sl.getLongitude() != null){
						float[] distance = new float[1];
						Location.distanceBetween(lat,lon, sl.getLatitude(), sl.getLongitude(), distance);
						sl.setDistance(Double.valueOf(distance[0]));
					}

					// add metric fields for display in lists
					List<String> metricNames = new ArrayList<String>();
					List<String> metricValues = new ArrayList<String>();
					List<SurveyedLocaleValue> slvList = listSurveyedLocaleValuesByLocaleId(sl.getId() + "");
					for (SurveyedLocaleValue slv : slvList){
						if (getIncludeInListByQuestionId(slv.getQuestionId())){
							metricNames.add(getMetricNameByQuestionId(slv.getQuestionId()));
							metricValues.add(slv.getAnswerValue());
						}
					}
					sl.setMetricNames(metricNames);
					sl.setMetricValues(metricValues);
					slList.add(sl);
				} catch (IllegalArgumentException e) {
					// let it pass
				}
			} while (i < number && slCursor.moveToNext());
		}
		return slList;
	}

	public String getMetricNameByQuestionId(String questionId) {
		Cursor cursor = database.query(RECORD_META_TABLE, new String[] { PK_ID_COL,
				QUESTION_COL, METRIC_NAME_COL, METRIC_ID_COL }, QUESTION_COL + "=?",
				new String[] { questionId }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			try{
				return cursor.getString(cursor.getColumnIndexOrThrow(METRIC_NAME_COL));
			} catch (IllegalArgumentException e) {
				return "";
			}
		}
		return "";
	}

	public boolean getIncludeInListByQuestionId(String questionId) {
		Cursor cursor = database.query(RECORD_META_TABLE, new String[] { PK_ID_COL,
				QUESTION_COL, INCLUDE_IN_LIST_COL }, QUESTION_COL + "=?",
				new String[] { questionId }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			try{
				int answ = cursor.getInt(cursor.getColumnIndexOrThrow(INCLUDE_IN_LIST_COL));
				return answ > 0;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
		return false;
	}
}