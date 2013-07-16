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

import java.util.List;

import com.gallatinsystems.framework.rest.RestResponse;

/**
 * response for recordData service
 * 
 * @author Mark Westra
 * 
 */
public class RecordDataResponse extends RestResponse {
	private static final long serialVersionUID = 1548249617327473969L;
	private List<RecordDataDto> recordData;
	private String cursor;

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	public List<RecordDataDto> getRecordData() {
		return recordData;
	}

	public void setRecordData(List<RecordDataDto> recordData) {
		this.recordData = recordData;
	}
}
