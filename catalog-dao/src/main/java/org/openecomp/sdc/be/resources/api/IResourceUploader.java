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

package org.openecomp.sdc.be.resources.api;

import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import fj.data.Either;

/**
 * DAO to manage image upload and retrieval.
 * 
 * @author luc boutier
 */
public interface IResourceUploader {

	/**
	 * Save an artifact in the DAO layer.
	 * 
	 * @param imageData
	 */
	ResourceUploadStatus saveArtifact(ESArtifactData artifactData, boolean isReload);

	/**
	 * Save an artifact in the DAO layer.
	 * 
	 * @param imageData
	 */
	ResourceUploadStatus updateArtifact(ESArtifactData artifactData);

	/**
	 * Get an artifact as a byte array based on the artifact id.
	 * 
	 * @param id
	 *            The id of the artifact to read.
	 * @return The artifact as a byte array.
	 */
	Either<ESArtifactData, ResourceUploadStatus> getArtifact(String id);

	/**
	 * Delete the given image.
	 * 
	 * @param id
	 *            Id of the image to delete.
	 */
	void deleteArtifact(String id);

	/**
	 * delete all artifacts
	 */
	public void deleteAllArtifacts();

}
