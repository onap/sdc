/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.exception;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.exception.ResponseFormat.RequestErrorWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseFormatTest {

	private ResponseFormat createTestSubject() {
		return new ResponseFormat();
	}
	@Test
	public void testGetFormattedMessage() {
		// okResponseInfo not null
		ResponseFormat responseFormat1 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper = responseFormat1.new RequestErrorWrapper();
		OkResponseInfo okResponseInfo = new OkResponseInfo("1", "text", new String[2]);
		requestErrorWrapper.setOkResponseInfo(okResponseInfo);
		responseFormat1.setRequestError(requestErrorWrapper);

		assertEquals("text", responseFormat1.getFormattedMessage());

		// okResponseInfo null, serviceException not null
		ResponseFormat responseFormat2 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper2 = responseFormat2.new RequestErrorWrapper();
		ServiceException serviceException = new ServiceException("1", "text2", new String[2]);
		requestErrorWrapper2.setServiceException(serviceException);
		responseFormat2.setRequestError(requestErrorWrapper2);

		assertEquals("text2", responseFormat2.getFormattedMessage());

		// okResponseInfo null, serviceException null, policyException not null
		ResponseFormat responseFormat3 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper3 = responseFormat3.new RequestErrorWrapper();
		PolicyException policyException = new PolicyException("1", "text3", new String[2]);
		requestErrorWrapper3.setPolicyException(policyException);
		responseFormat3.setRequestError(requestErrorWrapper3);

		assertEquals("text3", responseFormat3.getFormattedMessage());
	}

	@Test
	public void testGetVariables() {
		// okResponseInfo not null
		ResponseFormat responseFormat1 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper = responseFormat1.new RequestErrorWrapper();
		OkResponseInfo okResponseInfo = new OkResponseInfo("1", "text", new String[2]);
		requestErrorWrapper.setOkResponseInfo(okResponseInfo);
		responseFormat1.setRequestError(requestErrorWrapper);

		assertEquals(0, responseFormat1.getVariables().length);

		// okResponseInfo null, serviceException not null
		ResponseFormat responseFormat2 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper2 = responseFormat2.new RequestErrorWrapper();
		ServiceException serviceException = new ServiceException("1", "text2", new String[2]);
		requestErrorWrapper2.setServiceException(serviceException);
		responseFormat2.setRequestError(requestErrorWrapper2);

		assertEquals(0, responseFormat2.getVariables().length);

		// okResponseInfo null, serviceException null, policyException not null
		ResponseFormat responseFormat3 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper3 = responseFormat3.new RequestErrorWrapper();
		PolicyException policyException = new PolicyException("1", "text3", new String[2]);
		requestErrorWrapper3.setPolicyException(policyException);
		responseFormat3.setRequestError(requestErrorWrapper3);

		assertEquals(0, responseFormat3.getVariables().length);
	}

	@Test
	public void testGetMessageId() {
		// okResponseInfo not null
		ResponseFormat responseFormat1 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper = responseFormat1.new RequestErrorWrapper();
		OkResponseInfo okResponseInfo = new OkResponseInfo("1", "text", new String[2]);
		requestErrorWrapper.setOkResponseInfo(okResponseInfo);
		responseFormat1.setRequestError(requestErrorWrapper);

		assertEquals("1", responseFormat1.getMessageId());

		// okResponseInfo null, serviceException not null
		ResponseFormat responseFormat2 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper2 = responseFormat2.new RequestErrorWrapper();
		ServiceException serviceException = new ServiceException("2", "text2", new String[2]);
		requestErrorWrapper2.setServiceException(serviceException);
		responseFormat2.setRequestError(requestErrorWrapper2);

		assertEquals("2", responseFormat2.getMessageId());

		// okResponseInfo null, serviceException null, policyException not null
		ResponseFormat responseFormat3 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper3 = responseFormat3.new RequestErrorWrapper();
		PolicyException policyException = new PolicyException("3", "text3", new String[2]);
		requestErrorWrapper3.setPolicyException(policyException);
		responseFormat3.setRequestError(requestErrorWrapper3);

		assertEquals("3", responseFormat3.getMessageId());
	}

	@Test
	public void testRequestErrorWrapper() {
		ResponseFormat responseFormat1 = new ResponseFormat();
		RequestErrorWrapper requestErrorWrapper = responseFormat1.new RequestErrorWrapper();
		ResponseFormat.RequestError requestError = responseFormat1.new RequestError();
		requestErrorWrapper.setRequestError(requestError);
		assertEquals(requestError, requestErrorWrapper.getRequestError());
	}

	@Test
	public void testRequestError() {
		ResponseFormat responseFormat1 = new ResponseFormat();
		ResponseFormat.RequestError requestError = responseFormat1.new RequestError();
		ServiceException serviceException = new ServiceException("2", "text2", new String[2]);
		requestError.setServiceException(serviceException);
		OkResponseInfo okResponseInfo = new OkResponseInfo("1", "text", new String[2]);
		requestError.setOkResponseInfo(okResponseInfo);
		PolicyException policyException = new PolicyException("1", "text3", new String[2]);
		requestError.setPolicyException(policyException);
		assertEquals(serviceException, requestError.getServiceException());
		assertEquals(okResponseInfo, requestError.getOkResponseInfo());
		assertEquals(policyException, requestError.getPolicyException());
	}

	@Test
	public void testSetStatus() throws Exception {
		ResponseFormat testSubject;
		int status = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testGetStatus() throws Exception {
		ResponseFormat testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testGetRequestError() {
		ResponseFormat testSubject;
		RequestErrorWrapper result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestError();
	}

	@Test
	public void testSetRequestError() {
		ResponseFormat testSubject;
		RequestErrorWrapper requestErrorWrapper = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestError(requestErrorWrapper);
	}

	@Test
	public void testSetPolicyException() {
		ResponseFormat testSubject;
		PolicyException policyException = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPolicyException(policyException);
	}

	@Test
	public void testSetServiceException() {
		ResponseFormat testSubject;
		ServiceException serviceException = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceException(serviceException);
	}

	@Test
	public void testSetOkResponseInfo() {
		ResponseFormat testSubject;
		OkResponseInfo okResponseInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOkResponseInfo(okResponseInfo);
	}

	@Test
	public void testToString() throws Exception {
		ResponseFormat testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
