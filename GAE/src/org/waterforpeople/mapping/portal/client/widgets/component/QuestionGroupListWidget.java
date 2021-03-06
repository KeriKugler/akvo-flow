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

package org.waterforpeople.mapping.portal.client.widgets.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyService;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyServiceAsync;
import org.waterforpeople.mapping.app.gwt.client.util.TextConstants;

import com.gallatinsystems.framework.gwt.component.ListBasedWidget;
import com.gallatinsystems.framework.gwt.component.PageController;
import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.framework.gwt.util.client.MessageDialog;
import com.gallatinsystems.framework.gwt.wizard.client.ContextAware;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * widget for viewing QuestionGroupLists. This widget will load the edit widget
 * if the edit button beside the group is clicked.
 * 
 * TODO: handle reorder and delete
 * 
 * @author Christopher Fagiani
 * 
 */
public class QuestionGroupListWidget extends ListBasedWidget implements
		ContextAware {
	private static TextConstants TEXT_CONSTANTS = GWT
			.create(TextConstants.class);
	private SurveyServiceAsync surveyService;	
	private Map<String, Object> bundle;
	private Map<Widget, Integer> widgetRowMap;
	private Grid dataGrid;
	private QuestionGroupDto selectedQuestionGroup;
	private SurveyDto survey;

	public QuestionGroupListWidget(PageController controller) {
		super(controller);
		bundle = new HashMap<String, Object>();
		surveyService = GWT.create(SurveyService.class);		
	}

	public void loadData(SurveyDto surveyDto) {
		if (surveyDto != null) {
			surveyService.listQuestionGroupsBySurvey(surveyDto.getKeyId()
					.toString(),
					new AsyncCallback<ArrayList<QuestionGroupDto>>() {

						@Override
						public void onFailure(Throwable caught) {
							toggleLoading(false);
						}

						@Override
						public void onSuccess(ArrayList<QuestionGroupDto> result) {
							toggleLoading(false);
							survey.setQuestionGroupList(result);
							populateQuestionGroupList(result);
						}
					});
		}
	}

	private void populateQuestionGroupList(
			Collection<QuestionGroupDto> groupList) {
		toggleLoading(false);
		widgetRowMap = new HashMap<Widget, Integer>();
		if (dataGrid != null) {
			dataGrid.removeFromParent();
		}
		if (groupList != null) {
			dataGrid = new Grid(groupList.size(), 4);
			int i = 0;
			for (QuestionGroupDto q : groupList) {
				Label l = createListEntry(q.getDisplayName());
				HorizontalPanel bp = new HorizontalPanel();

				Image moveUp = new Image("/images/greenuparrow.png");
				if (i > 0) {
					createClickableWidget(ClickMode.MOVE_UP, moveUp);
				}
				Image moveDown = new Image("/images/greendownarrow.png");
				if (i < groupList.size() - 1) {
					createClickableWidget(ClickMode.MOVE_DOWN, moveDown);
				}
				Button deleteButton = createButton(ClickMode.DELETE,
						TEXT_CONSTANTS.delete());
				Button editButton = createButton(ClickMode.EDIT, TEXT_CONSTANTS
						.edit());

				bp.add(moveUp);
				bp.add(moveDown);

				widgetRowMap.put(l, i);
				dataGrid.setWidget(i, 0, l);
				dataGrid.setWidget(i, 1, bp);
				dataGrid.setWidget(i, 2, editButton);
				dataGrid.setWidget(i, 3, deleteButton);

				widgetRowMap.put(editButton, i);
				widgetRowMap.put(deleteButton, i);
				widgetRowMap.put(moveUp, i);
				widgetRowMap.put(moveDown, i);

				i++;
			}
		}
		addWidget(dataGrid);
	}

	@Override
	public void setContextBundle(Map<String, Object> bundle) {
		this.bundle = bundle;
		flushContext();
		survey = (SurveyDto) bundle.get(BundleConstants.SURVEY_KEY);
		loadData(survey);
	}

	@Override
	protected void handleItemClick(Object source, ClickMode mode) {
		selectedQuestionGroup = survey.getQuestionGroupList().get(
				widgetRowMap.get((Widget) source));

		bundle.put(BundleConstants.QUESTION_GROUP_KEY, selectedQuestionGroup);
		if (ClickMode.OPEN == mode) {
			openPage(QuestionListWidget.class, bundle);
		} else if (ClickMode.EDIT == mode) {
			openPage(QuestionGroupEditWidget.class, bundle);
		} else if (ClickMode.DELETE == mode) {
			deleteQuestionGroup(selectedQuestionGroup);
		} else if (ClickMode.MOVE_DOWN == mode) {
			moveQuestionGroup(1, selectedQuestionGroup);
		} else if (ClickMode.MOVE_UP == mode) {
			moveQuestionGroup(-1, selectedQuestionGroup);
		}
	}

	private void moveQuestionGroup(int increment, QuestionGroupDto questionGroup) {
		setWorking(true);
		final MessageDialog savingDialog = new MessageDialog(TEXT_CONSTANTS
				.saving(), TEXT_CONSTANTS.pleaseWait(), true);
		savingDialog.showCentered();		

		Integer prevIdx = null;
		Integer nextIdx = null;
		Integer idx = null;		
		for (int i =0; i < survey.getQuestionGroupList().size(); i++) {
			if (idx != null) {
				nextIdx = i;
				break;
			}
			if (survey.getQuestionGroupList().get(i)!= null && survey.getQuestionGroupList().get(i).getKeyId().equals(questionGroup.getKeyId())) {
				idx = i;
			} else {
				prevIdx =i;
			}
		}
		
		List<QuestionGroupDto> groupsToUpdate = new ArrayList<QuestionGroupDto>();
		QuestionGroupDto gToMove = survey.getQuestionGroupList().get(idx);
		QuestionGroupDto targetGroup = null;
		if (increment > 0) {
			targetGroup = survey.getQuestionGroupList().get(nextIdx);
		} else {
			targetGroup = survey.getQuestionGroupList().get(prevIdx);
		}
				
		
		if (idx + increment >= 0
				&& idx + increment < survey.getQuestionGroupList().size()) {
			targetGroup = survey.getQuestionGroupList().get(idx + increment);
		}

		gToMove.setOrder(idx+1 + increment);
		targetGroup.setOrder(idx+1);		
		
		survey.getQuestionGroupList().set(targetGroup.getOrder()-1, targetGroup);
		survey.getQuestionGroupList().set(gToMove.getOrder()-1, gToMove);

		groupsToUpdate.add(gToMove);
		groupsToUpdate.add(targetGroup);
		Label widgetToMove = null;
		Label targetWidget = null;
		for (Entry<Widget, Integer> entry : widgetRowMap.entrySet()) {
			if (entry.getKey() instanceof Label) {
				if (((Label) entry.getKey()).getText().equals(
						gToMove.getDisplayName())) {
					widgetToMove = (Label) entry.getKey();
				} else if (((Label) entry.getKey()).getText().equals(
						targetGroup.getDisplayName())) {
					targetWidget = (Label) entry.getKey();
				}
			}

			if (widgetToMove != null && targetWidget != null) {
				break;
			}
		}
		widgetToMove.setText(targetGroup.getDisplayName());
		targetWidget.setText(gToMove.getDisplayName());

		surveyService.updateQuestionGroupOrder(groupsToUpdate,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						setWorking(false);
						savingDialog.hide();
						MessageDialog errDia = new MessageDialog(TEXT_CONSTANTS
								.error(), TEXT_CONSTANTS.errorTracePrefix()
								+ " " + caught.getLocalizedMessage());
						errDia.showCentered();
					}

					@Override
					public void onSuccess(Void result) {
						setWorking(false);
						savingDialog.hide();
					}
				});

	}

	/**
	 * iterates over all the question groups in the selected survey to find the
	 * one with the ID that matches the question group passed in. If found,
	 * returns its list index. otherwise returns null;
	 * 
	 * @param questionGroup
	 * @return
	 */
	private Integer findIndexForGroup(QuestionGroupDto questionGroup) {
		Integer index = null;
		for (int i = 0; i < survey.getQuestionGroupList().size(); i++) {
			if (survey.getQuestionGroupList().get(i).getKeyId().equals(
					questionGroup.getKeyId())) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void deleteQuestionGroup(QuestionGroupDto group) {
		setWorking(true);

		Integer key = findIndexForGroup(group);
		if (key != null) {
			survey.getQuestionGroupList().remove(key.intValue());
			final MessageDialog dia = new MessageDialog(TEXT_CONSTANTS
					.deleting(), TEXT_CONSTANTS.pleaseWait(), true);
			surveyService.deleteQuestionGroup(group, group.getSurveyId(),
					new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							dia.hide(true);
							setWorking(false);
							MessageDialog errDia = new MessageDialog(TEXT_CONSTANTS
									.error(), TEXT_CONSTANTS.errorTracePrefix()
									+ " " + caught.getLocalizedMessage());
							errDia.showCentered();
						}

						@Override
						public void onSuccess(String result) {
							dia.hide(true);
							setWorking(false);
							loadData(survey);
						}
					});
		}
	}

	@Override
	public Map<String, Object> getContextBundle(boolean doPopulation) {
		return bundle;
	}

	@Override
	public void persistContext(String buttonText,CompletionListener listener) {
		if (listener != null) {
			listener.operationComplete(true, getContextBundle(true));
		}

	}

	@Override
	public void flushContext() {
		if (bundle != null) {
			bundle.remove(BundleConstants.QUESTION_GROUP_KEY);
		}
	}
}