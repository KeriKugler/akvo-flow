package org.waterforpeople.mapping.app.web;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.waterforpeople.mapping.app.gwt.server.spreadsheetmapper.SpreadsheetMappingAttributeServiceImpl;
import org.waterforpeople.mapping.app.web.dto.SpreadsheetImportRequest;

import com.gallatinsystems.framework.rest.AbstractRestApiServlet;
import com.gallatinsystems.framework.rest.RestRequest;
import com.gallatinsystems.framework.rest.RestResponse;



public class SpreadsheetImportServlet extends AbstractRestApiServlet {
	private static final Logger log = Logger
	.getLogger(SpreadsheetImportServlet.class.getName());
	private static final long serialVersionUID = 4037072154702352658L;

	@Override
	protected RestRequest convertRequest() throws Exception {
		HttpServletRequest req = getRequest();
		RestRequest restRequest = new SpreadsheetImportRequest();
		restRequest.populateFromHttpRequest(req);
		return restRequest;
	}

	@Override
	protected RestResponse handleRequest(RestRequest request) throws Exception {
		RestResponse response = new RestResponse();
		SpreadsheetImportRequest importReq = (SpreadsheetImportRequest) request;
		if (SpreadsheetImportRequest.PROCESS_FILE_ACTION
				.equalsIgnoreCase(importReq.getAction())) {
			
			SpreadsheetMappingAttributeServiceImpl mappingService = new SpreadsheetMappingAttributeServiceImpl();	
			String algorithm=importReq.getKeySpec();
			
			KeyFactory keyFactory = java.security.KeyFactory.getInstance(algorithm);
			org.apache.commons.codec.binary.Base64 b64encoder = new org.apache.commons.codec.binary.Base64();
			byte[] keyContents = b64encoder.decode(importReq.getKey());
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyContents);
			log.info("SessionToken: " + importReq.getSessionToken()+"Algo: " + algorithm + " key: " + importReq.getKey() + " keySpec: " + importReq.getKeySpec());
			PrivateKey key = keyFactory.generatePrivate(privateKeySpec);
			
			mappingService.processSurveySpreadsheetAsync(importReq.getSessionToken(),key,importReq.getIdentifier(),importReq.getStartRow(), importReq.getGroupId());
		}
		return response;
	}

	@Override
	protected void writeOkResponse(RestResponse resp) throws Exception {
		// no-op
	}

}
