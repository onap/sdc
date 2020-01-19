/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

/**
 * Nested POJOs to express required JSON format of the error
 * 
 * { "requestError": { "serviceException": { "messageId": "", "text": "",
 * "variables": [] } } }
 * 
 * 
 * @author paharoni
 *
 */

public class ResponseFormat {

	private int status;
	private RequestErrorWrapper requestErrorWrapper;

	public ResponseFormat() {
		super();
	}

	public ResponseFormat(int status) {
		super();
		this.status = status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	public RequestErrorWrapper getRequestError() {
		return requestErrorWrapper;
	}

	public void setRequestError(RequestErrorWrapper requestErrorWrapper) {
		this.requestErrorWrapper = requestErrorWrapper;
	}

	public void setPolicyException(PolicyException policyException) {
		this.requestErrorWrapper = new RequestErrorWrapper(new RequestError());
		requestErrorWrapper.setPolicyException(policyException);
	}

	public void setServiceException(ServiceException serviceException) {
		this.requestErrorWrapper = new RequestErrorWrapper(new RequestError());
		requestErrorWrapper.setServiceException(serviceException);
	}

	public void setOkResponseInfo(OkResponseInfo okResponseInfo) {
		this.requestErrorWrapper = new RequestErrorWrapper(new RequestError());
		requestErrorWrapper.setOkResponseInfo(okResponseInfo);
	}

	public String getFormattedMessage() {
		if (this.requestErrorWrapper.requestError.okResponseInfo != null) {
			return this.requestErrorWrapper.requestError.okResponseInfo.getFormattedErrorMessage();
		}
		if (this.requestErrorWrapper.requestError.serviceException != null) {
			return this.requestErrorWrapper.requestError.serviceException.getFormattedErrorMessage();
		}
		return this.requestErrorWrapper.requestError.policyException.getFormattedErrorMessage();
	}

	public String getText() {
		if (this.requestErrorWrapper.requestError.okResponseInfo != null) {
			return this.requestErrorWrapper.requestError.okResponseInfo.getText();
		}
		if (this.requestErrorWrapper.requestError.serviceException != null) {
			return this.requestErrorWrapper.requestError.serviceException.getText();
		}
		return this.requestErrorWrapper.requestError.policyException.getText();
	}

	public String[] getVariables() {
		if (this.requestErrorWrapper.requestError.okResponseInfo != null) {
			return this.requestErrorWrapper.requestError.okResponseInfo.getVariables();
		}
		if (this.requestErrorWrapper.requestError.serviceException != null) {
			return this.requestErrorWrapper.requestError.serviceException.getVariables();
		}
		return this.requestErrorWrapper.requestError.policyException.getVariables();
	}

	public String getMessageId() {
		if (this.requestErrorWrapper.requestError.okResponseInfo != null) {
			return this.requestErrorWrapper.requestError.okResponseInfo.getMessageId();
		}
		if (this.requestErrorWrapper.requestError.serviceException != null) {
			return this.requestErrorWrapper.requestError.serviceException.getMessageId();
		}
		return this.requestErrorWrapper.requestError.policyException.getMessageId();
	}

	public class RequestErrorWrapper {
		private RequestError requestError;

		public RequestErrorWrapper() {
			this.requestError = new RequestError();
		}

		public RequestErrorWrapper(RequestError requestError) {
			this.requestError = requestError;
		}

		public RequestError getRequestError() {
			return requestError;
		}

		public void setRequestError(RequestError requestError) {
			this.requestError = requestError;
		}

		public void setPolicyException(PolicyException policyException) {
			requestError.setPolicyException(policyException);
		}

		public void setServiceException(ServiceException serviceException) {
			requestError.setServiceException(serviceException);
		}

		public void setOkResponseInfo(OkResponseInfo okResponseInfo) {
			requestError.setOkResponseInfo(okResponseInfo);
		}
	}

	public class RequestError {
		@SuppressWarnings("unused")
		private PolicyException policyException;
		@SuppressWarnings("unused")
		private ServiceException serviceException;
		@SuppressWarnings("unused")
		private OkResponseInfo okResponseInfo;

		public RequestError() {
		}

		public PolicyException getPolicyException() {
			return policyException;
		}

		public ServiceException getServiceException() {
			return serviceException;
		}

		public OkResponseInfo getOkResponseInfo() {
			return okResponseInfo;
		}

		public void setPolicyException(PolicyException policyException) {
			this.policyException = policyException;
		}

		public void setServiceException(ServiceException serviceException) {
			this.serviceException = serviceException;
		}

		public void setOkResponseInfo(OkResponseInfo okResponseInfo) {
			this.okResponseInfo = okResponseInfo;
		}
	}

	@Override
	public String toString() {
		return "ResponseFormat[" + "status=" + status + ", requestErrorWrapper=" + requestErrorWrapper + ']';
	}
	
}
