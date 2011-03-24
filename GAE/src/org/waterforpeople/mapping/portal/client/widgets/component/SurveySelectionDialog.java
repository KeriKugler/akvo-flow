package org.waterforpeople.mapping.portal.client.widgets.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.waterforpeople.mapping.portal.client.widgets.component.SurveySelectionWidget.Orientation;
import org.waterforpeople.mapping.portal.client.widgets.component.SurveySelectionWidget.TerminalType;

import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.framework.gwt.util.client.WidgetDialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog box that allows selection of a survey
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveySelectionDialog extends WidgetDialog implements ClickHandler {

	public static final String SURVEY_KEY = "survey";
	private SurveySelectionWidget selector;
	private Button okButton;
	private Button cancelButton;	
	private Label messageLabel;

	public SurveySelectionDialog(CompletionListener listener) {
		super("Select Survey", null, true, listener);
		Panel panel = new VerticalPanel();
		messageLabel = new Label();
		Panel buttonPanel = new HorizontalPanel();
		selector = new SurveySelectionWidget(Orientation.HORIZONTAL,
				TerminalType.SURVEY);
		panel.add(selector);
		panel.add(messageLabel);
		messageLabel.setVisible(false);
		okButton = new Button("Ok");
		okButton.addClickHandler(this);
		cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel);
		setContentWidget(panel);
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == cancelButton) {
			hide(true);
		} else if (event.getSource() == okButton) {
			if (selector.getSelectedSurveyIds() == null
					|| selector.getSelectedSurveyIds().size() == 0) {
				messageLabel
						.setText("Please select a survey before you click 'Ok'");
				messageLabel.setVisible(true);
			} else {
				hide(true);
				Map<String, Object> payload = new HashMap<String, Object>();
				List<Long> ids = selector.getSelectedSurveyIds();
				payload.put(SURVEY_KEY, ids.get(0));
				notifyListener(true, payload);
			}
		}
	}
}