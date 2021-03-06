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

package org.waterforpeople.mapping.app.gwt.client.standardscoring;

import java.util.ArrayList;
import java.util.Date;

import com.gallatinsystems.framework.gwt.dto.client.BaseDto;

public class StandardScoringDto extends BaseDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8196804282479668687L;
	private Boolean globalStandard = null;
	private String countryCode = null;
	private String subValue = null;
	private String pointType = null;
	private String displayName = null;
	// AccessPoint or Survey
	private String mapToObject = null;
	private String evaluateField = null;
	private ArrayList<String> positiveCriteria = null;
	private String positiveOperator = null;
	private String criteriaType = null;
	private Integer positiveScore = null;
	private String positiveMessage = null;
	private String negativeCriteria = null;
	private String negativeOperator = null;
	private Integer negativeScore = null;
	private String negativeMessage = null;
	private String neutralCriteria = null;
	private Integer neutralScore = null;
	private String neutralMessage = null;
	private Date effectiveStartDate = null;
	private Date effectiveEndDate = null;
	private Long scoreBucketId = null;
	private String scoreBucket = null;
	private Boolean negativeOverride = null;
	private Scope scoreScope = null;

	public enum Scope {
		GLOBAL, COUNTRY, SUB_COUNTRY
	};

	public Boolean getGlobalStandard() {
		return globalStandard;
	}

	public void setGlobalStandard(Boolean globalStandard) {
		this.globalStandard = globalStandard;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getSubValue() {
		return subValue;
	}

	public void setSubValue(String subValue) {
		this.subValue = subValue;
	}

	public String getPointType() {
		return pointType;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMapToObject() {
		return mapToObject;
	}

	public void setMapToObject(String mapToObject) {
		this.mapToObject = mapToObject;
	}

	public String getEvaluateField() {
		return evaluateField;
	}

	public void setEvaluateField(String evaluateField) {
		this.evaluateField = evaluateField;
	}

	public ArrayList<String> getPositiveCriteria() {
		return positiveCriteria;
	}

	public void setPositiveCriteria(ArrayList<String> positiveCriteria){
		this.positiveCriteria=positiveCriteria;
	}
	
	public void addPositiveCriteria(String positiveCriteria) {
		if(this.positiveCriteria==null){
			this.positiveCriteria = new ArrayList<String>();
		}
		this.positiveCriteria.add(positiveCriteria);
	}

	public String getPositiveOperator() {
		return positiveOperator;
	}

	public void setPositiveOperator(String positiveOperator) {
		this.positiveOperator = positiveOperator;
	}

	public String getCriteriaType() {
		return criteriaType;
	}

	public void setCriteriaType(String criteriaType) {
		this.criteriaType = criteriaType;
	}

	public Integer getPositiveScore() {
		return positiveScore;
	}

	public void setPositiveScore(Integer positiveScore) {
		this.positiveScore = positiveScore;
	}

	public String getNegativeCriteria() {
		return negativeCriteria;
	}

	public void setNegativeCriteria(String negativeCriteria) {
		this.negativeCriteria = negativeCriteria;
	}

	public String getNegativeOperator() {
		return negativeOperator;
	}

	public void setNegativeOperator(String negativeOperator) {
		this.negativeOperator = negativeOperator;
	}

	public Integer getNegativeScore() {
		return negativeScore;
	}

	public void setNegativeScore(Integer negativeScore) {
		this.negativeScore = negativeScore;
	}

	public String getNeutralCriteria() {
		return neutralCriteria;
	}

	public void setNeutralCriteria(String neutralCriteria) {
		this.neutralCriteria = neutralCriteria;
	}

	public Integer getNeutralScore() {
		return neutralScore;
	}

	public void setNeutralScore(Integer neutralScore) {
		this.neutralScore = neutralScore;
	}

	public Date getEffectiveStartDate() {
		return effectiveStartDate;
	}

	public void setEffectiveStartDate(Date effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}

	public Date getEffectiveEndDate() {
		return effectiveEndDate;
	}

	public void setEffectiveEndDate(Date effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}

	public Long getScoreBucketId() {
		return scoreBucketId;
	}

	public void setScoreBucketId(Long scoreBucketId) {
		this.scoreBucketId = scoreBucketId;
	}

	public void setNegativeOverride(Boolean negativeOverride) {
		this.negativeOverride = negativeOverride;
	}

	public Boolean getNegativeOverride() {
		return negativeOverride;
	}

	public String getPositiveMessage() {
		return positiveMessage;
	}

	public void setPositiveMessage(String positiveMessage) {
		this.positiveMessage = positiveMessage;
	}

	public String getNegativeMessage() {
		return negativeMessage;
	}

	public void setNegativeMessage(String negativeMessage) {
		this.negativeMessage = negativeMessage;
	}

	public String getNeutralMessage() {
		return neutralMessage;
	}

	public void setNeutralMessage(String neutralMessage) {
		this.neutralMessage = neutralMessage;
	}

	public void setScoreScope(Scope scoreScope) {
		this.scoreScope = scoreScope;
	}

	public Scope getScoreScope() {
		return scoreScope;
	}

	public void setScoreBucket(String scoreBucket) {
		this.scoreBucket = scoreBucket;
	}

	public String getScoreBucket() {
		return scoreBucket;
	}
}
