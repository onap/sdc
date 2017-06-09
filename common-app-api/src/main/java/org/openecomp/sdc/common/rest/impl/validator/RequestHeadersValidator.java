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

package org.openecomp.sdc.common.rest.impl.validator;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHeadersValidator {
	private static Logger log = LoggerFactory.getLogger(RequestHeadersValidator.class.getName());

	public static void validateContentType(HttpServletRequest request, MediaType expectedContentType,
			Map<String, String> headersMap) throws RestRequestValidationException {

		log.debug("validateContentType - expected: {}", expectedContentType);
		if (request == null || expectedContentType == null) {
			throw new RestRequestValidationException("request or media-type are null");
		}
		String contentType = request.getHeader(Constants.CONTENT_TYPE_HEADER);
		if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON)) {
			throw new RestRequestValidationException(
					"Content-Type of requset is different then " + expectedContentType);
		} else {
			headersMap.put(Constants.CONTENT_TYPE_HEADER, contentType);
		}
	}

	public static void validateIdentificationHeaders(HttpServletRequest request, List<String> identificationList,
			Map<String, String> headersMap) throws RestRequestValidationException {

		log.debug("validateIdentificationHeaders");
		for (String requiredHeader : identificationList) {
			String headerVal = request.getHeader(requiredHeader);
			if (headerVal != null && !headerVal.isEmpty()) {
				headersMap.put(requiredHeader, headerVal);
				log.debug("found header - {} : {}", requiredHeader, headerVal);
			} else {
				log.error("missing identification header: {}", requiredHeader);
				throw new RestRequestValidationException("missing identification header: " + requiredHeader);
			}
		}

	}

	public static void validateMd5(byte[] encodedData, HttpServletRequest request, Map<String, String> headersMap)
			throws RestRequestValidationException {

		// validate parameters
		if (encodedData == null || request == null) {
			throw new RestRequestValidationException("encoded data or request are not valid");
		}

		// calculate MD5 on the data
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(encodedData);
		byte[] encodedMd5 = Base64.encodeBase64(md5.getBytes());

		// read the Content-MD5 header
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		if ((origMd5 == null) || origMd5.isEmpty()) {
			throw new RestRequestValidationException("missing Content-MD5 header ");
		}

		// verify MD5 value
		if (!origMd5.equals(new String(encodedMd5))) {
			throw new RestRequestValidationException("uploaded file failed MD5 validation");
		}
		headersMap.put(Constants.MD5_HEADER, origMd5);
	}

}
