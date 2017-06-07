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

package org.openecomp.sdc.be.resources.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ICatalogDAO;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.exception.ResourceDAOException;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("resource-upload")
public class ResourceUploader implements IResourceUploader {

	private static final String DEFAULT_ARTIFACT_INDEX_NAME = "resources";

	@Resource
	private ICatalogDAO resourceDAO;
	private static Logger log = LoggerFactory.getLogger(ResourceUploader.class.getName());

	@PostConstruct
	public void init() {
		ConfigurationManager configMgr = ConfigurationManager.getConfigurationManager();
		String artifactsIndex = null;
		artifactsIndex = configMgr.getConfiguration().getArtifactsIndex();
		if (artifactsIndex == null || artifactsIndex.isEmpty()) {
			artifactsIndex = DEFAULT_ARTIFACT_INDEX_NAME;
		}
		resourceDAO.addToIndicesMap(ESArtifactData.class.getSimpleName().toLowerCase(), artifactsIndex);
	}

	public ResourceUploader() {
		super();
	}

	public ResourceUploader(ICatalogDAO resourcetDAO) {
		super();
		this.resourceDAO = resourcetDAO;
	}

	public ICatalogDAO getResourceDAO() {
		return resourceDAO;
	}

	public void setResourceDAO(ICatalogDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	@Override
	public ResourceUploadStatus saveArtifact(ESArtifactData artifactData, boolean isReload) {
		ResourceUploadStatus status = ResourceUploadStatus.OK;
		if (resourceDAO == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInitializationError,
					"Save Artifact - internal object not initialized");
			BeEcompErrorManager.getInstance()
					.logBeInitializationError("Save Artifact - internal object not initialized");
			log.debug("update artifact failed - resourceDAO is null");
			return ResourceUploadStatus.ERROR;
		}

		Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = getArtifact(artifactData.getId());
		if (getArtifactStatus.isLeft()) {
			status = ResourceUploadStatus.ALREADY_EXIST;
			log.debug("ResourceUploadStatus:saveArtifact artifact with id {} already exist.", artifactData.getId());
			if (isReload) {
				status = updateArtifact(artifactData, getArtifactStatus.left().value());
			}
		} else {
			try {

				resourceDAO.writeArtifact(artifactData);
				status = ResourceUploadStatus.OK;

			} catch (ResourceDAOException e) {
				status = ResourceUploadStatus.ERROR;
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
						"Save Artifact to database");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Save Artifact to database");
				log.debug("ResourceUploadStatus:saveArtifact failed with exception ", e);
			}

		}

		return status;
	}

	@Override
	public ResourceUploadStatus updateArtifact(ESArtifactData artifactUpdateData) {
		ResourceUploadStatus status = ResourceUploadStatus.OK;
		if (resourceDAO == null)
			return ResourceUploadStatus.ERROR;

		Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = getArtifact(artifactUpdateData.getId());
		if (getArtifactStatus.isRight()) {
			status = getArtifactStatus.right().value();
			log.debug("ResourceUploadStatus:updateArtifactt artifact with id {}", artifactUpdateData.getId()
					+ " not exist.");
		}
		if (getArtifactStatus.isLeft()) {
			status = updateArtifact(artifactUpdateData, getArtifactStatus.left().value());
		}

		return status;
	}

	/*
	 * @Override public ResourceUploadStatus
	 * updateServiceArtifact(ServiceArtifactData artifactUpdateData) {
	 * ResourceUploadStatus status = ResourceUploadStatus.OK; if(resourceDAO ==
	 * null) return ResourceUploadStatus.ERROR; Either<ServiceArtifactData,
	 * ResourceUploadStatus> getServiceArtifactStatus =
	 * getServiceArtifact(artifactUpdateData.getId());
	 * 
	 * if(getServiceArtifactStatus.isRight()){
	 * log.debug("ResourceUploadStatus:updateArtifactt artifact with id " +
	 * artifactUpdateData.getId() + " not exist."); status =
	 * getServiceArtifactStatus.right().value(); }
	 * if(getServiceArtifactStatus.isLeft()){ status =
	 * updateServiceArtifact(artifactUpdateData,
	 * getServiceArtifactStatus.left().value()); }
	 * 
	 * return status; }
	 * 
	 */

	@Override
	public Either<ESArtifactData, ResourceUploadStatus> getArtifact(String id) {
		if (resourceDAO == null)
			return Either.right(ResourceUploadStatus.ERROR);

		return resourceDAO.getArtifact(id);
	}

	/*
	 * @Override public Either<ServiceArtifactData, ResourceUploadStatus>
	 * getServiceArtifact(String id) { if(resourceDAO == null) return
	 * Either.right(ResourceUploadStatus.ERROR);
	 * 
	 * return resourceDAO.getServiceArtifact(id); }
	 */
	@Override
	public void deleteArtifact(String id) {
		if (resourceDAO != null) {
			resourceDAO.deleteArtifact(id);
		}

	}

	private ResourceUploadStatus updateArtifact(ESArtifactData artifactUpdateData, ESArtifactData existData) {
		ResourceUploadStatus status;

		updateData(artifactUpdateData, existData);

		try {
			resourceDAO.writeArtifact(artifactUpdateData);
			status = ResourceUploadStatus.OK;

		} catch (ResourceDAOException e) {
			status = ResourceUploadStatus.ERROR;
			log.debug("ResourceUploadStatus:updateArtifact failed with exception ", e);
		}
		return status;
	}

	private void updateData(ESArtifactData artifactUpdateData, ESArtifactData existData) {

		if (artifactUpdateData.getData() == null) {
			artifactUpdateData.setData(existData.getData());
		}

	}

	@Override
	public void deleteAllArtifacts() {
		resourceDAO.deleteAllArtifacts();
	}

}
