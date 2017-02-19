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

package org.openecomp.sdc.be.resources.exception;

import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.exception.TechnicalException;

public class ResourceDAOException extends TechnicalException {

	private static final long serialVersionUID = 171917520842336653L;

	private ResourceUploadStatus status;

	public ResourceDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceDAOException(String message) {
		super(message);
	}

	public ResourceDAOException(ResourceUploadStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public ResourceDAOException(ResourceUploadStatus status, String message) {
		super(message);
		this.status = status;
	}

	public ResourceUploadStatus getStatus() {
		return status;
	}

	public void setStatus(ResourceUploadStatus status) {
		this.status = status;
	}

}
