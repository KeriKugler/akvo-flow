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

package org.waterforpeople.mapping.app.web.dto;

import javax.servlet.http.HttpServletRequest;

import com.gallatinsystems.framework.rest.RestError;
import com.gallatinsystems.framework.rest.RestRequest;
import com.gallatinsystems.framework.rest.exception.RestValidationException;

/**
 * data structure for rest api calls to the point of interest service
 * 
 * @author Mark Tiele Westra
 */
public class RecordDataRequest extends RestRequest {
	private static final long serialVersionUID = 2511688888372190068L;
	private static final String PROJECT_ID_PARAM = "projectId";
	private static final String IMEI_PARAM = "imei";
	private static final String PHONE_NUMBER_PARAM = "phoneNumber";
	private static final String DEVICE_ID_PARAM = "deviceId";
	private static final String CURSOR_PARAM = "cursor";
	private static final String CHECK_AVAILABLE_PARAM = "checkAvailable";

	
	private Long projectId;
	private Boolean checkAvailable;
	private String imei;
	private String phoneNumber;
	private String deviceId;
	private String cursor;

	

	
	@Override
	protected void populateErrors() {
		// TODO do a better job in error population
		if (projectId == null) {
			addError(new RestError(RestError.MISSING_PARAM_ERROR_CODE,"parameter wrong","missing projectId"));
		}
	}

	@Override
	protected void populateFields(HttpServletRequest req) throws Exception {
		projectId = Long.parseLong(req.getParameter(PROJECT_ID_PARAM));
		checkAvailable = Boolean.parseBoolean(req.getParameter(CHECK_AVAILABLE_PARAM));
		imei = req.getParameter(IMEI_PARAM);
		phoneNumber = req.getParameter(PHONE_NUMBER_PARAM);
		deviceId = req.getParameter(DEVICE_ID_PARAM);
		cursor = req.getParameter(CURSOR_PARAM);
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long surveyId) {
		this.projectId = surveyId;
	}

	public Boolean getCheckAvailable() {
		return checkAvailable;
	}

	public void setCheckAvailable(Boolean checkAvailable) {
		this.checkAvailable = checkAvailable;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
}
