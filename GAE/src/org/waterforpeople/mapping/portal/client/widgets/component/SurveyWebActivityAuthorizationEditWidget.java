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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.waterforpeople.mapping.app.gwt.client.auth.WebActivityAuthorizationDto;
import org.waterforpeople.mapping.app.gwt.client.auth.WebActivityAuthorizationService;
import org.waterforpeople.mapping.app.gwt.client.auth.WebActivityAuthorizationServiceAsync;
import org.waterforpeople.mapping.app.gwt.client.util.TextConstants;
import org.waterforpeople.mapping.portal.client.widgets.component.SurveySelectionWidget.Orientation;
import org.waterforpeople.mapping.portal.client.widgets.component.SurveySelectionWidget.TerminalType;

import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.framework.gwt.util.client.MessageDialog;
import com.gallatinsystems.framework.gwt.util.client.ViewUtil;
import com.gallatinsystems.framework.gwt.util.client.WidgetDialog;
import com.gallatinsystems.framework.gwt.wizard.client.ContextAware;
import com.gallatinsystems.user.app.gwt.client.UserDto;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * widget for editing and creating authorization objects. This implementation
 * creates objects with their activityName set to WEB_SURVEY but this can be
 * extended if/when other types are supported by the system.
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveyWebActivityAuthorizationEditWidget extends Composite
		implements ContextAware, ClickHandler {

	private static TextConstants TEXT_CONSTANTS = GWT
			.create(TextConstants.class);
	private static final String PUBLIC_PATH = "/SurveyEntry.html?token=";
	private static final String SECURE_PATH = "/SecureSurveyEntry.html?token=";

	private static final String ACTIVITY_NAME = "WebSurvey";
	private static final String DEFAULT_STYLE = "input-label-padded";
	private static final String USED_TEXT = " have been used";
	private Map<String, Object> bundle;
	private WebActivityAuthorizationServiceAsync authService;
	private WebActivityAuthorizationDto currentAuthDto;
	private SurveySelectionWidget surveySelector;
	private Panel contentPanel;
	private TextBox nameBox;
	private TextBox tokenBox;
	private RadioButton authTypeAnonButton;
	private RadioButton authTypeUserButton;
	private Button findUserButton;
	private Label userLabel;
	private DateBox expDatePicker;
	private TextBox maxUseBox;
	private Label usedLabel;

	public SurveyWebActivityAuthorizationEditWidget() {
		authService = GWT.create(WebActivityAuthorizationService.class);
		buildPanel();
		initWidget(contentPanel);
	}

	private void buildPanel() {
		contentPanel = new VerticalPanel();
		HorizontalPanel row = new HorizontalPanel();
		nameBox = new TextBox();
		expDatePicker = new DateBox();
		row.add(ViewUtil.initLabel(TEXT_CONSTANTS.name(), DEFAULT_STYLE));
		row.add(nameBox);
		row.add(ViewUtil.initLabel(TEXT_CONSTANTS.expires(), DEFAULT_STYLE));
		row.add(expDatePicker);
		contentPanel.add(row);
		surveySelector = new SurveySelectionWidget(Orientation.HORIZONTAL,
				TerminalType.SURVEY);
		contentPanel.add(surveySelector);
		row = new HorizontalPanel();
		authTypeAnonButton = new RadioButton("authType", TEXT_CONSTANTS.anonymous());
		authTypeAnonButton.addClickHandler(this);
		row.add(authTypeAnonButton);
		authTypeUserButton = new RadioButton("authType", TEXT_CONSTANTS.user());
		authTypeUserButton.addClickHandler(this);
		row.add(authTypeUserButton);
		findUserButton = new Button(TEXT_CONSTANTS.findUser());
		findUserButton.addClickHandler(this);
		row.add(findUserButton);
		userLabel = ViewUtil.initLabel("", DEFAULT_STYLE);
		row.add(userLabel);
		userLabel.setVisible(false);
		findUserButton.setVisible(false);
		ViewUtil.installFieldRow(contentPanel, TEXT_CONSTANTS.authType(), row,
				DEFAULT_STYLE);
		row = new HorizontalPanel();
		row.add(ViewUtil.initLabel(TEXT_CONSTANTS.maxUses(), DEFAULT_STYLE));
		maxUseBox = new TextBox();
		row.add(maxUseBox);
		usedLabel = ViewUtil.initLabel("", DEFAULT_STYLE);
		usedLabel.setVisible(false);
		row.add(usedLabel);
		contentPanel.add(row);
		tokenBox = new TextBox();
		tokenBox.setReadOnly(true);
		ViewUtil.installFieldRow(contentPanel, TEXT_CONSTANTS.token(),
				tokenBox, DEFAULT_STYLE);
	}

	@Override
	public void flushContext() {
		if (bundle != null) {
			bundle.remove(BundleConstants.WEB_ACTIVITY_AUTH);
		}

	}

	@Override
	public Map<String, Object> getContextBundle(boolean doPopulation) {
		if (bundle == null) {
			bundle = new HashMap<String, Object>();
		}
		return bundle;
	}

	private List<String> updateCurrentAuthDto() {
		List<String> errorList = new ArrayList<String>();
		if (currentAuthDto == null) {
			currentAuthDto = new WebActivityAuthorizationDto();
		}
		currentAuthDto.setWebActivityName(ACTIVITY_NAME);
		if (ViewUtil.isTextPopulated(nameBox)) {
			currentAuthDto.setName(nameBox.getText());
		} else {
			errorList.add(TEXT_CONSTANTS.nameMandatory());
		}
		currentAuthDto.setExpirationDate(expDatePicker.getValue());
		if (authTypeAnonButton.getValue()) {
			currentAuthDto.setAuthType(WebActivityAuthorizationDto.ANON_TYPE);
			currentAuthDto.setUserId(null);
			currentAuthDto.setUserName(null);
		} else {
			currentAuthDto.setAuthType(WebActivityAuthorizationDto.USER_TYPE);
			if (userLabel.getText() != null
					&& userLabel.getText().trim().length() > 0) {
				currentAuthDto.setUserName(userLabel.getText());
			} else {
				errorList.add(TEXT_CONSTANTS.userMandatoryForAuth());
			}
		}
		if (ViewUtil.isTextPopulated(maxUseBox)) {
			try {
				currentAuthDto.setMaxUses(Long.parseLong(maxUseBox.getText()));
			} catch (Exception e) {
				errorList.add(TEXT_CONSTANTS.maxUseNumeric());
			}
		} else {
			currentAuthDto.setMaxUses(null);
		}
		if (surveySelector.getSelectedSurveyIds().size() > 0) {
			currentAuthDto.setPayload(surveySelector.getSelectedSurveyIds()
					.get(0).toString());
		} else {
			errorList.add(TEXT_CONSTANTS.surveyMandatory());
		}
		return errorList;
	}

	@Override
	public void persistContext(String buttonText, final CompletionListener listener) {
		List<String> validationErrors = updateCurrentAuthDto();
		if (validationErrors == null || validationErrors.size() == 0) {
			authService.saveAuthorization(currentAuthDto,
					new AsyncCallback<WebActivityAuthorizationDto>() {

						@Override
						public void onSuccess(WebActivityAuthorizationDto result) {
							currentAuthDto = result;

							if (listener != null) {
								String url = formUrl(result.getToken(), result
										.getAuthType());
								ClickHandler closeClickHandler = new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										listener.operationComplete(true,
												getContextBundle(true));
									}
								};
								MessageDialog dia = new MessageDialog(
										TEXT_CONSTANTS.saveComplete(),
										TEXT_CONSTANTS.authUrl()
												+ "<br><a href='" + url + "'>"
												+ url + "<a>", false,
										closeClickHandler, closeClickHandler);
								dia.showCentered();
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							if (listener != null) {
								listener.operationComplete(false,
										getContextBundle(true));
							}
						}
					});
		} else {
			StringBuilder builder = new StringBuilder("<ul>");
			for (String err : validationErrors) {
				builder.append("<li>").append(err).append("</li>");
			}
			builder.append("</ul>");
			MessageDialog errorDialog = new MessageDialog(TEXT_CONSTANTS
					.inputError(), builder.toString());
			errorDialog.showCentered();
			listener.operationComplete(false, getContextBundle(true));
		}
	}

	@Override
	public void setContextBundle(Map<String, Object> bundle) {
		if (bundle == null) {
			this.bundle = new HashMap<String, Object>();
		} else {
			this.bundle = bundle;
			currentAuthDto = (WebActivityAuthorizationDto) bundle
					.get(BundleConstants.WEB_ACTIVITY_AUTH);
			bindData();
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == authTypeAnonButton) {
			findUserButton.setVisible(false);
			userLabel.setText("");
			userLabel.setVisible(false);
		} else if (event.getSource() == authTypeUserButton) {
			findUserButton.setVisible(true);
			userLabel.setVisible(true);
		} else if (event.getSource() == findUserButton) {
			final UserManagerWidget usrMgr = new UserManagerWidget(true);
			final WidgetDialog dia = new WidgetDialog(TEXT_CONSTANTS
					.selectUser(), usrMgr, null);
			usrMgr.setCompletionListener(new CompletionListener() {
				@Override
				public void operationComplete(boolean wasSuccessful,
						Map<String, Object> payload) {
					UserDto u = (UserDto) payload.get(BundleConstants.USER);
					if (u != null) {
						currentAuthDto.setUserId(u.getKeyId());
						currentAuthDto.setUserName(u.getUserName());
						userLabel.setText(u.getUserName() != null ? u
								.getUserName() : "");
						userLabel.setVisible(true);
					}
					dia.hide();
				}
			});
			dia.showCentered();
		}
	}

	private void bindData() {
		if (currentAuthDto == null) {
			currentAuthDto = new WebActivityAuthorizationDto();
		}
		nameBox.setValue(currentAuthDto.getName() != null ? currentAuthDto
				.getName() : "");
		usedLabel
				.setText(currentAuthDto.getUsageCount() != null ? currentAuthDto
						.getUsageCount().toString()
						+ USED_TEXT
						: "0" + USED_TEXT);
		usedLabel.setVisible(true);
		if (WebActivityAuthorizationDto.USER_TYPE
				.equalsIgnoreCase(currentAuthDto.getAuthType())) {
			authTypeUserButton.setValue(true);
			findUserButton.setVisible(true);
			userLabel
					.setText(currentAuthDto.getUserName() != null ? currentAuthDto
							.getUserName()
							: "");
			userLabel.setVisible(true);
		} else {
			authTypeAnonButton.setValue(true);
			findUserButton.setVisible(false);
			userLabel.setText("");
			userLabel.setVisible(false);
		}
		if (currentAuthDto.getMaxUses() != null) {
			maxUseBox.setText(currentAuthDto.getMaxUses().toString());
		}
		tokenBox.setText(currentAuthDto.getToken());
		if (currentAuthDto.getExpirationDate() != null) {
			expDatePicker.setValue(currentAuthDto.getExpirationDate());
		}
		if (currentAuthDto.getPayload() != null) {
			surveySelector.setSelectedSurvey(new Long(currentAuthDto
					.getPayload()));
		}
	}

	private String formUrl(String token, String authType) {
		String base = "http://" + Window.Location.getHost();
		if (WebActivityAuthorizationDto.ANON_TYPE.equals(authType)) {
			return base + PUBLIC_PATH + token;
		} else {
			return base + SECURE_PATH + token;
		}
	}
}
