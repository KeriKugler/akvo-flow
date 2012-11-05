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

package com.gallatinsystems.survey.device.domain;

import java.util.ArrayList;
import java.util.HashMap;

import com.gallatinsystems.survey.device.util.ConstantUtil;

/**
 * data structure for individual survey questions. Questions have a type which
 * can be any one of:
 * <ul>
 * <li>option - radio-button like selection</li>
 * <li>free - free text</li>
 * <li>video - video capture</li>
 * <li>photo - photo capture</li>
 * <li>geo - geographic detection (GPS)</li>
 * </ul>
 * 
 * @author Christopher Fagiani
 * 
 */
public class Question {
	private String id;
	private String text;
	private int order;
	private ValidationRule validationRule;
	private String renderType;
	private ArrayList<QuestionHelp> questionHelp;
	private boolean mandatory;
	private String type;
	private ArrayList<Option> options;
	private boolean allowOther;
	private boolean allowMultiple;
	private boolean locked;
	private HashMap<String, AltText> altTextMap = new HashMap<String, AltText>();
	private ArrayList<Dependency> dependencies;
	private ArrayList<ScoringRule> scoringRules;
	private boolean useStrength;
	private int strengthMin;
	private int strengthMax;

	public void setUseStrength(boolean val) {
		useStrength = val;
	}

	public boolean useStrength() {
		return useStrength;
	}

	public int getStrengthMin() {
		return strengthMin;
	}

	public void setStrengthMin(int strengthMin) {
		this.strengthMin = strengthMin;
	}

	public int getStrengthMax() {
		return strengthMax;
	}

	public void setStrengthMax(int strengthMax) {
		this.strengthMax = strengthMax;
	}

	public ArrayList<QuestionHelp> getQuestionHelp() {
		return questionHelp;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public HashMap<String, AltText> getAltTextMap() {
		return altTextMap;
	}

	public AltText getAltText(String lang) {
		return altTextMap.get(lang);
	}

	public void addAltText(AltText altText) {
		altTextMap.put(altText.getLanguage(), altText);
	}

	public boolean isAllowMultiple() {
		return allowMultiple;
	}

	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}

	public String getRenderType() {
		return renderType;
	}

	public void setRenderType(String renderType) {
		this.renderType = renderType;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public ArrayList<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(ArrayList<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<Option> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<Option> options) {
		this.options = options;
	}

	public boolean isAllowOther() {
		return allowOther;
	}

	public void setAllowOther(boolean allowOther) {
		this.allowOther = allowOther;
	}

	public void addDependency(Dependency dep) {
		if (dependencies == null) {
			dependencies = new ArrayList<Dependency>();
		}
		dependencies.add(dep);
	}

	public ArrayList<QuestionHelp> getHelpByType(String type) {
		ArrayList<QuestionHelp> help = new ArrayList<QuestionHelp>();
		if (questionHelp != null && type != null) {
			for (int i = 0; i < questionHelp.size(); i++) {
				if (type.equalsIgnoreCase(questionHelp.get(i).getType())) {
					help.add(questionHelp.get(i));
				}
			}
		}
		return help;
	}

	public void addQuestionHelp(QuestionHelp help) {
		if (questionHelp == null) {
			questionHelp = new ArrayList<QuestionHelp>();
		}
		questionHelp.add(help);
	}

	public ValidationRule getValidationRule() {
		return validationRule;
	}

	public void setValidationRule(ValidationRule validationRule) {
		this.validationRule = validationRule;
	}

	/**
	 * counts the number of non-empty help tip types
	 * 
	 * @return
	 */
	public int getHelpTypeCount() {
		int count = 0;
		if (getHelpByType(ConstantUtil.IMAGE_HELP_TYPE).size() > 0) {
			count++;
		}
		if (getHelpByType(ConstantUtil.TIP_HELP_TYPE).size() > 0) {
			count++;
		}
		if (getHelpByType(ConstantUtil.VIDEO_HELP_TYPE).size() > 0) {
			count++;
		}
		if (getHelpByType(ConstantUtil.ACTIVITY_HELP_TYPE).size() > 0) {
			count++;
		}
		return count;
	}

	public void addScoringRule(ScoringRule rule) {
		if (scoringRules == null) {
			scoringRules = new ArrayList<ScoringRule>();
		}
		scoringRules.add(rule);
	}

	/**
	 * scores a response according to the question's scoring rules. If there are
	 * no rules or none of the rules match, this method will return null
	 * otherwise it will return the scored value
	 * 
	 * @param response
	 * @return
	 */
	public String getResponseScore(String response) {
		String result = null;
		if (scoringRules != null) {
			int i = 0;
			while (i < scoringRules.size() && result == null) {
				result = scoringRules.get(i++).scoreResponse(response);
			}
		}
		return result;
	}

	public String toString() {
		return text;
	}
}
