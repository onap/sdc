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

package org.openecomp.sdc.be.dao.api;

import java.util.List;

import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.exception.ResourceDAOException;

import fj.data.Either;

public interface ICatalogDAO {

	public static final String TOSCA_ELEMENT_INDEX = "toscaelement";
	public static final String RESOURCES_INDEX = "resources";
	public final static String REF_NAME_FIELD = "refName";
	public final static String REF_VERSION_FIELD = "refVersion";
	public final static String ARTIFACT_NAME_FIELD = "artifactName";

	void addToIndicesMap(String typeName, String indexName);

	/**
	 * Save an artifact in the DAO layer.
	 * 
	 * @param imageData
	 */
	void writeArtifact(ESArtifactData artifactData) throws ResourceDAOException;

	/**
	 * Get an artifact as a byte array based on the artifact id.
	 * 
	 * @param id
	 *            The id of the artifact to read.
	 * @param id2
	 * @return The artifact as a byte array.
	 */
	Either<ESArtifactData, ResourceUploadStatus> getArtifact(String id);

	Either<List<ESArtifactData>, ResourceUploadStatus> getArtifacts(String[] ids);

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
	void deleteAllArtifacts();

}
