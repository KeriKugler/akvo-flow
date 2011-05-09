package com.gallatinsystems.standards.domain;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;

import com.gallatinsystems.framework.domain.BaseDomain;

@PersistenceCapable
public class StandardScoring extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2361133441074173260L;

	private Boolean globalStandard = null;
	private String countryCode = null;
	private String subValue = null;
	private String pointType = null;
	private String displayName = null;
	// AccessPoint or Survey
	private String mapToObject = null;
	private String evaluateField = null;
	private String positiveCriteria = null;
	private String positiveOperand =null;
	private String positiveCriteriaType = null;
	private Integer positiveScore = null;
	private String negativeCriteria = null;
	private String negativeOperand = null;
	private String negativeCriteriaType=null;
	private Integer negativeScore = null;
	private String neutralCriteria = null;
	private Integer neutralScore = null;
	private Date effectiveStartDate = null;
	private Date effectiveEndDate = null;

	public Boolean getGlobalStandard() {
		return globalStandard;
	}

	public String getPositiveOperand() {
		return positiveOperand;
	}

	public void setPositiveOperand(String positiveOperand) {
		this.positiveOperand = positiveOperand;
	}

	public String getPositiveCriteriaType() {
		return positiveCriteriaType;
	}

	public void setPositiveCriteriaType(String positiveCriteriaType) {
		this.positiveCriteriaType = positiveCriteriaType;
	}

	public String getNegativeOperand() {
		return negativeOperand;
	}

	public void setNegativeOperand(String negativeOperand) {
		this.negativeOperand = negativeOperand;
	}

	public String getNegativeCriteriaType() {
		return negativeCriteriaType;
	}

	public void setNegativeCriteriaType(String negativeCriteriaType) {
		this.negativeCriteriaType = negativeCriteriaType;
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

	public String getPositiveCriteria() {
		return positiveCriteria;
	}

	public void setPositiveCriteria(String positiveCriteria) {
		this.positiveCriteria = positiveCriteria;
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
}