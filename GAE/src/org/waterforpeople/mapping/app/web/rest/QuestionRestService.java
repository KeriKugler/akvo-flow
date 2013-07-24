/*
 *  Copyright (C) 2012 Stichting Akvo (Akvo Foundation)
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
package org.waterforpeople.mapping.app.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.waterforpeople.mapping.app.gwt.client.survey.MetricDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto;
import org.waterforpeople.mapping.app.util.DtoMarshaller;
import org.waterforpeople.mapping.app.web.rest.dto.QuestionPayload;
import org.waterforpeople.mapping.app.web.rest.dto.RestStatusDto;
import org.waterforpeople.mapping.dao.QuestionAnswerStoreDao;

import com.gallatinsystems.common.Constants;
import com.gallatinsystems.framework.exceptions.IllegalDeletionException;
import com.gallatinsystems.metric.dao.MetricDao;
import com.gallatinsystems.metric.dao.SurveyMetricMappingDao;
import com.gallatinsystems.metric.domain.Metric;
import com.gallatinsystems.metric.domain.SurveyMetricMapping;
import com.gallatinsystems.survey.dao.QuestionDao;
import com.gallatinsystems.survey.dao.QuestionOptionDao;
import com.gallatinsystems.survey.domain.Question;

@Controller
@RequestMapping("/questions")
public class QuestionRestService {

	@Inject
	private QuestionDao questionDao;

	@Inject
	private QuestionOptionDao questionOptionDao;

	@Inject
	private SurveyMetricMappingDao surveyMetricMappingDao;

	// TODO put in dependencies
	// list all questions
	@RequestMapping(method = RequestMethod.GET, value = "/all")
	@ResponseBody
	public Map<String, List<QuestionDto>> listQuestions() {
		final Map<String, List<QuestionDto>> response = new HashMap<String, List<QuestionDto>>();
		List<QuestionDto> results = new ArrayList<QuestionDto>();
		List<Question> questions = questionDao.list(Constants.ALL_RESULTS);
		if (questions != null) {
			for (Question s : questions) {
				QuestionDto dto = new QuestionDto();
				DtoMarshaller.copyToDto(s, dto);
				dto.setOptionList(questionOptionDao
						.listOptionInStringByQuestion(dto.getKeyId()));
				results.add(dto);
			}
		}
		response.put("questions", results);
		return response;
	}

	// list questions by questionGroup or by survey. If includeNumber or
	// includeOption are true, only NUMBER and OPTION type questions are
	// returned
	// TODO put in dependencies
	@RequestMapping(method = RequestMethod.GET, value = "")
	@ResponseBody
	public Map<String, Object> listQuestions(
			@RequestParam(value = "questionGroupId", defaultValue = "") Long questionGroupId,
			@RequestParam(value = "surveyId", defaultValue = "") Long surveyId,
			@RequestParam(value = "includeNumber", defaultValue = "") String includeNumber,
			@RequestParam(value = "includeOption", defaultValue = "") String includeOption,
			@RequestParam(value = "preflight", defaultValue = "") String preflight,
			@RequestParam(value = "questionId", defaultValue = "") Long questionId) {
		final Map<String, Object> response = new HashMap<String, Object>();
		List<QuestionDto> results = new ArrayList<QuestionDto>();
		List<Question> questions = new ArrayList<Question>();
		RestStatusDto statusDto = new RestStatusDto();
		statusDto.setStatus("");
		statusDto.setMessage("");
		
		// if this is a pre-flight delete check, handle that
		if (preflight != null && preflight.equals("delete")
				&& questionId != null) {
			QuestionAnswerStoreDao qasDao = new QuestionAnswerStoreDao();
			statusDto.setStatus("preflight-delete-question");
			statusDto.setMessage("cannot_delete");
			
			if (qasDao.listByQuestion(questionId).size() == 0) {
				statusDto.setMessage("can_delete");
				statusDto.setKeyId(questionId);
			} 
		} else {

			// load questions in a question group, or all questions in the
			// survey
			if (questionGroupId != null) {
				questions = questionDao
						.listQuestionsInOrderForGroup(questionGroupId);
			} else if (surveyId != null) {
				questions = questionDao.listQuestionsInOrder(surveyId);
			}

			if (questions.size() > 0) {
				for (Question question : questions) {

					Boolean includeElement = false;
					// include if we are requesting questions in a group
					if (questionGroupId != null)
						includeElement = true;

					// include if we are requesting questions in a survey, with
					// non of the other parameters set
					if (surveyId != null
							&& ("".equals(includeNumber) && ""
									.equals(includeOption)))
						includeElement = true;

					// include if we request options, and the present element is
					// an option
					if (surveyId != null && "true".equals(includeOption)
							&& question.getType() == Question.Type.OPTION)
						includeElement = true;

					// include if we request numbers, and the present element is
					// a number
					if (surveyId != null && "true".equals(includeNumber)
							&& question.getType() == Question.Type.NUMBER)
						includeElement = true;

					if (includeElement) {
						QuestionDto dto = new QuestionDto();
						DtoMarshaller.copyToDto(question, dto);
						if (question.getType() == Question.Type.OPTION) {
							dto.setOptionList(questionOptionDao
									.listOptionInStringByQuestion(dto
											.getKeyId()));
						}
						results.add(dto);
					}
				}
			}
		}
		response.put("questions", results);
		response.put("meta",statusDto);
		return response;
	}

	// find a single question by the questionId
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody
	public Map<String, QuestionDto> findQuestion(@PathVariable("id") Long id) {
		final Map<String, QuestionDto> response = new HashMap<String, QuestionDto>();
		Question s = questionDao.getByKey(id);
		QuestionDto dto = null;
		if (s != null) {
			dto = new QuestionDto();
			DtoMarshaller.copyToDto(s, dto);
			dto.setOptionList(questionOptionDao
					.listOptionInStringByQuestion(dto.getKeyId()));
		}
		response.put("question", dto);
		return response;

	}

	// delete question by id
	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	@ResponseBody
	public Map<String, RestStatusDto> deleteQuestionById(
			@PathVariable("id") Long id) {
		final Map<String, RestStatusDto> response = new HashMap<String, RestStatusDto>();
		Question q = questionDao.getByKey(id);
		RestStatusDto statusDto = null;
		statusDto = new RestStatusDto();
		statusDto.setStatus("failed");
		statusDto.setMessage("_cannot_delete");

		// check if question exists in the datastore
		if (q != null) {
			// delete question
			try {
				// first try delete, to see if it is allowed
				questionDao.delete(q);

				// if successful, we can delete the options and metric mappings
				// as well
				questionOptionDao.deleteOptionsForQuestion(id);
				surveyMetricMappingDao.deleteMetricMapping(id);

				statusDto.setStatus("ok");
				statusDto.setMessage("");
			} catch (IllegalDeletionException e) {
				statusDto.setStatus("failed");
				statusDto.setMessage(e.getMessage());
			}
		}
		response.put("meta", statusDto);
		return response;
	}

	// update existing question
	// TODO put in metrics
	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	@ResponseBody
	public Map<String, Object> saveExistingQuestion(
			@RequestBody QuestionPayload payLoad) {
		final QuestionDto questionDto = payLoad.getQuestion();
		final Map<String, Object> response = new HashMap<String, Object>();
		QuestionDto dto = null;

		RestStatusDto statusDto = new RestStatusDto();
		statusDto.setStatus("failed");
		statusDto.setMessage("Cannot change question");

		// if the POST data contains a valid questionDto, continue. Otherwise,
		// server will respond with 400 Bad Request
		if (questionDto != null) {
			Long keyId = questionDto.getKeyId();
			Question q;

			// if the questionDto has a key, try to get the question.
			if (keyId != null) {
				q = questionDao.getByKey(keyId);
				// if we find the question, update it's properties
				if (q != null) {

					Integer origOrder = q.getOrder();
					// copy the properties, except the createdDateTime property,
					// because it is set in the Dao.
					BeanUtils.copyProperties(questionDto, q, new String[] {
							"createdDateTime", "type", "optionList" });
					if (questionDto.getType() != null)
						q.setType(Question.Type.valueOf(questionDto.getType()
								.toString()));

					questionOptionDao.saveOptionInStringByQuestion(keyId,
							questionDto.getOptionList());

					// FIXME do we still need this? is it used?
					if (questionDto.getMetricId() != null) {
						// delete existing mappings
						surveyMetricMappingDao.deleteMetricMapping(keyId);

						// create a new mapping
						SurveyMetricMapping newMapping = new SurveyMetricMapping();
						newMapping.setMetricId(questionDto.getMetricId());
						newMapping.setQuestionGroupId(questionDto
								.getQuestionGroupId());
						newMapping.setSurveyId(questionDto.getSurveyId());
						newMapping.setSurveyQuestionId(keyId);
						surveyMetricMappingDao.save(newMapping);
					}

					List<Long> mList = new ArrayList<Long>();
					if (questionDto.getNewMetricName() != null && questionDto.getNewMetricName().length() > 0){
						// we need to create a new metric
						MetricDao mDao = new MetricDao();
						Metric metr = new Metric();
						metr.setName(questionDto.getNewMetricName());
						// FIXME for now, the project Id is the same as the surveyId
						metr.setProjectId(questionDto.getSurveyId());
						metr = mDao.save(metr);

						MetricDto mDto = new MetricDto();
						DtoMarshaller.copyToDto(metr, mDto);
						response.put("metrics", mDto);

						mList.add(metr.getKey().getId());
						q.setMetricId(metr.getKey().getId());
					}

					q = questionDao.save(q);
					dto = new QuestionDto();
					DtoMarshaller.copyToDto(q, dto);
					dto.setOptionList(questionOptionDao
							.listOptionInStringByQuestion(dto.getKeyId()));

					if (mList != null && mList.size() > 0){
						dto.setMetrics(mList);
					}
					statusDto.setStatus("ok");
					statusDto.setMessage("");
				}
			}
		}
		response.put("meta", statusDto);
		response.put("question", dto);
		return response;
	}

	// create new question
	@RequestMapping(method = RequestMethod.POST, value = "")
	@ResponseBody
	public Map<String, Object> saveNewQuestion(
			@RequestBody QuestionPayload payLoad) {
		final QuestionDto questionDto = payLoad.getQuestion();
		final Map<String, Object> response = new HashMap<String, Object>();
		QuestionDto dto = null;

		RestStatusDto statusDto = new RestStatusDto();
		statusDto.setStatus("failed");
		statusDto.setMessage("Cannot create question");

		// if the POST data contains a valid questionDto, continue. Otherwise,
		// server will respond with 400 Bad Request
		if (questionDto != null) {
			Question q = new Question();

			// copy the properties, except the createdDateTime property, because
			// it is set in the Dao.
			BeanUtils.copyProperties(questionDto, q, new String[] {
					"createdDateTime", "type", "optionList", "newMetricName"});
			if (questionDto.getType() != null)
				q.setType(Question.Type.valueOf(questionDto.getType()
						.toString()));
			q = questionDao.save(q);

			dto = new QuestionDto();
			DtoMarshaller.copyToDto(q, dto);
			statusDto.setStatus("ok");
			statusDto.setMessage("");
		}
		response.put("meta", statusDto);
		response.put("question", dto);
		return response;
	}
}
