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

package com.gallatinsystems.survey.device.view.adapter;

import java.util.ArrayList;
import java.util.Date;

import com.gallatinsystems.survey.device.R;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gallatinsystems.survey.device.dao.SurveyDbAdapter;
import com.gallatinsystems.survey.device.domain.FileTransmission;
import com.gallatinsystems.survey.device.util.ConstantUtil;

/**
 * this adaptor can format date strings using the device's locale settings prior
 * to displaying them to the screen
 * 
 * @author Stellan Lagerström
 * 
 */
public class SubmittedSurveyReviewCursorAdaptor extends CursorAdapter {

	public static int SURVEY_ID_KEY = R.integer.surveyidkey;
	public static int RESP_ID_KEY = R.integer.respidkey;
	public static int USER_ID_KEY = R.integer.useridkey;
	
	public final int DETAIL_NONE = 0;	
	public final int DETAIL_QUEUED = 1;	
	public final int DETAIL_INPROG = 2;	
	public final int DETAIL_FAILED = 3;
	
	private SurveyDbAdapter databaseAdapter;



	public SubmittedSurveyReviewCursorAdaptor(Context context, Cursor c) {
		super(context, c);
		databaseAdapter = new SurveyDbAdapter(context);
	}

	
	
	/**
	 * Gets status of latest transmission for this respondent. 
	 * This will usually be the zip file, as it is sent after any media files
	 */
	int getTransmissionStatus(Long respondId){
		databaseAdapter.open();
		//get file transmissions, most recent first
		ArrayList<FileTransmission> transList =
				databaseAdapter.listFileTransmission(respondId, null, true);
		int sts = 0;
		if (transList != null && transList.size() > 0) {
			//idVal = transList.get(0).getId();
			String stsTxt = transList.get(0).getStatus();
			if (stsTxt != null) {
				if (ConstantUtil.QUEUED_STATUS.equals(stsTxt)) {
					sts=DETAIL_QUEUED;
				} else if (ConstantUtil.IN_PROGRESS_STATUS.equals(stsTxt)) {
					sts=DETAIL_INPROG;
				} else if (ConstantUtil.FAILED_STATUS.equals(stsTxt)) {
					sts=DETAIL_FAILED;
				}
				//COMPLETE_STATUS records should not be returned
			}
		}
		databaseAdapter.close();
		return sts;
	}
	
	/**
	 * Gets any non-sent status of any non-zip transmission for this respondent, or DETAIL_NONE if all succeeded/none can be found.
	 * We need to ignore any files that have been successful at any time
	 */
	int getWorstMediaTransmissionStatus(Long respondId) {
		databaseAdapter.open();
		//get file transmissions, most recent first, including successes
		ArrayList<FileTransmission> transList =	databaseAdapter.listFileTransmission(respondId, null, false);
		int sts = DETAIL_NONE;
		if (transList != null) {
			//first pass - mark any files that have been successful at any time as successful
			for (int i = 0; i < transList.size(); i++) {
				//need to remember all non-zip files 
				String fn = transList.get(i).getFileName();
				if (!fn.endsWith(".zip")) {
					String stsTxt = transList.get(i).getStatus();
					if (stsTxt != null) {
						if (ConstantUtil.COMPLETE_STATUS.equals(stsTxt)) {
							for (int j = 0; j < transList.size(); j++) {
								if (i != j && fn.equals(transList.get(j).getFileName() ) ) {
									transList.get(j).setStatus(ConstantUtil.COMPLETE_STATUS);
								}
							}
						}
					}
				}
			}
			//second pass - find any files that were not successful
			for (int i = 0; i < transList.size(); i++) {
				String fn = transList.get(i).getFileName();
				if (!fn.endsWith(".zip")) {
					String stsTxt = transList.get(i).getStatus();
					if (stsTxt != null) {
						if (ConstantUtil.QUEUED_STATUS.equals(stsTxt)) {
							sts = DETAIL_QUEUED;
							break;
						} else if (ConstantUtil.IN_PROGRESS_STATUS.equals(stsTxt)) {
							sts = DETAIL_INPROG;
							break;
						} else if (ConstantUtil.FAILED_STATUS.equals(stsTxt)) {
							sts = DETAIL_FAILED;
							break;
						}
					}
				}
			}
		}
		databaseAdapter.close();
		return sts;
	}
	
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//Default appearance
		int icon = R.drawable.checkmark2; //the square one
		String status = "Sent: "; //TODO: move to string resource for localization
		
		long millis = cursor.getLong(cursor.getColumnIndex(SurveyDbAdapter.DELIVERED_DATE_COL));
		if (millis != 0) {
			//sent, see how the media files did
			int detail = getWorstMediaTransmissionStatus(cursor.getLong(cursor
					.getColumnIndex(SurveyDbAdapter.PK_ID_COL)));
			if (detail != DETAIL_NONE) { //problems
				icon = R.drawable.checkmark_exclam;
				status = "Sent (not photos): "; //TODO: move to string resource for localization
			}

		} else {
			//Have not yet completed sending it
			//Find out detailed reason (queued/in-progress/failed)
			int detail = getTransmissionStatus(cursor.getLong(cursor
					.getColumnIndex(SurveyDbAdapter.PK_ID_COL)));
			switch (detail){
			case DETAIL_QUEUED:	
				status = "Submitted: "; 
				icon = R.drawable.yellowcircle;
				break;
			case DETAIL_INPROG:	
				status = "Started: ";
				icon = R.drawable.blueuparrow;
				break;
			case DETAIL_FAILED:
				status = "Not sent ";
				icon = R.drawable.redx;
				break;
			default:  //Should not happen
				status = "Submitted: ";
				icon = R.drawable.redcircle;
				break;
			
			}
			
			millis = cursor.getLong(cursor
					.getColumnIndex(SurveyDbAdapter.SUBMITTED_DATE_COL));
			// if millis is still null, then the survey hasn't been submitted yet (should not happen)
			if (millis == 0) {
				status = "Saved: ";
				icon = R.drawable.disk;
				millis = cursor.getLong(cursor
						.getColumnIndex(SurveyDbAdapter.SAVED_DATE_COL));
			}
		}

		// Format the date string
		Date date = new Date(millis);
		TextView dateView = (TextView) view.findViewById(R.id.text2);
		dateView.setText(status
				+ DateFormat.getLongDateFormat(context).format(date) + " "
				+ DateFormat.getTimeFormat(context).format(date));
		TextView headingView = (TextView) view.findViewById(R.id.text1);
		headingView.setText(cursor.getString(cursor
				.getColumnIndex(SurveyDbAdapter.DISP_NAME_COL)));
		view.setTag(SURVEY_ID_KEY, cursor.getLong(cursor
				.getColumnIndex(SurveyDbAdapter.SURVEY_FK_COL)));
		view.setTag(RESP_ID_KEY, cursor.getLong(cursor
				.getColumnIndex(SurveyDbAdapter.PK_ID_COL)));
		view.setTag(USER_ID_KEY, cursor.getLong(cursor
				.getColumnIndex(SurveyDbAdapter.USER_FK_COL)));
		ImageView stsIcon = (ImageView) view.findViewById(R.id.xmitstsicon);
		stsIcon.setImageResource(icon);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.submittedrow, null);
		bindView(view, context, cursor);
		return view;
	}

}
