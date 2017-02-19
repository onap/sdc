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

package org.openecomp.sdc.asdctool.impl.migration.v1604;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public class VfcNamingAlignment {

	private static Logger log = LoggerFactory.getLogger(VfcNamingAlignment.class.getName());

	@Autowired
	protected TitanGenericDao titanGenericDao;
	@Autowired
	protected ResourceOperation resourceOperation;

	public boolean alignVfcNames1604(String appConfigDir) {
		log.debug("Started alignVfcNames1604 procedure..");
		log.debug("Getting all resources with resourceType = VFC/CP/VL");
		boolean result = false;
		try {
			Map<String, Object> notProperties = new HashMap<>();
			notProperties.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());
			Either<List<ResourceMetadataData>, TitanOperationStatus> allVfcResources = titanGenericDao
					.getByCriteria(NodeTypeEnum.Resource, null, notProperties, ResourceMetadataData.class);
			if (allVfcResources.isRight()) {
				log.error("Couldn't get VFC resources from DB, error: {}", allVfcResources.right().value());
				result = false;
				return result;
			}
			List<ResourceMetadataData> vfcList = allVfcResources.left().value();
			if (vfcList == null) {
				log.error("Couldn't get VFC/CP/VL resources from DB, no resources found");
				result = false;
				return result;
			}
			log.debug("Found {} VFC/CP/VL resources", vfcList.size());
			for (ResourceMetadataData vfc : vfcList) {
				log.debug("Checking resource {}", vfc.getMetadataDataDefinition().getName());
				boolean wasChanged = false;

				Either<Boolean, StorageOperationStatus> vfcEither = fixToscaNameEmpty(vfc);
				if (vfcEither.isRight()) {
					log.error("DB error during checkIsToscaNameEmpty - exiting...");
					result = false;
					return result;
				}
				wasChanged = wasChanged | vfcEither.left().value();

				vfcEither = fixVfcToscaNameHasVf(vfc);
				if (vfcEither.isRight()) {
					log.error("DB error during checkIsVfcToscaNameHasVf - exiting...");
					result = false;
					return result;
				}
				wasChanged = wasChanged | vfcEither.left().value();

				if (wasChanged) {
					Either<ResourceMetadataData, TitanOperationStatus> updateVfc = updateVfc(vfc);
					if (updateVfc.isRight()) {
						log.error("DB error during while updating resource {}, error: {} - exiting...",
								vfc.getMetadataDataDefinition().getName(), updateVfc.right().value());
						result = false;
						return result;
					}
					log.debug("Resource {} was successfully updated", vfc.getMetadataDataDefinition().getName());
				}

			}
			result = true;
		} finally {
			if (!result) {
				titanGenericDao.rollback();
				log.debug("**********************************************");
				log.debug("alignVfcNames1604 procedure FAILED!!");
				log.debug("**********************************************");
			} else {
				titanGenericDao.commit();
				log.debug("**********************************************");
				log.debug("alignVfcNames1604 procedure ended successfully!");
				log.debug("**********************************************");
			}
		}

		return result;
	}

	private Either<ResourceMetadataData, TitanOperationStatus> updateVfc(ResourceMetadataData vfc) {
		return titanGenericDao.updateNode(vfc, ResourceMetadataData.class);
	}

	private Either<Boolean, StorageOperationStatus> fixToscaNameEmpty(ResourceMetadataData vfc) {
		String toscaResourceName = ((ResourceMetadataDataDefinition) vfc.getMetadataDataDefinition())
				.getToscaResourceName();
		if (toscaResourceName == null || toscaResourceName.trim().equals(Constants.EMPTY_STRING)) {
			log.debug("Tosca resource name is empty - setting new tosca name...");
			Either<Boolean, StorageOperationStatus> generateAndSetToscaResourceName = generateAndSetToscaResourceName(
					vfc, null);
			if (generateAndSetToscaResourceName.isRight()) {
				return Either.right(generateAndSetToscaResourceName.right().value());
			}
			return Either.left(true);
		}
		return Either.left(false);
	}

	private Either<Boolean, StorageOperationStatus> fixVfcToscaNameHasVf(ResourceMetadataData vfc) {
		String toscaResourceName = ((ResourceMetadataDataDefinition) vfc.getMetadataDataDefinition())
				.getToscaResourceName();
		if (toscaResourceName.contains(".vf.")) {
			log.debug("Tosca resource name {} is VF-style - setting new tosca name...", toscaResourceName);
			Either<Boolean, StorageOperationStatus> generateAndSetToscaResourceName = generateAndSetToscaResourceName(
					vfc, null);
			if (generateAndSetToscaResourceName.isRight()) {
				return Either.right(generateAndSetToscaResourceName.right().value());
			}
			return Either.left(true);
		}
		return Either.left(false);
	}


	private Either<Boolean, StorageOperationStatus> generateAndSetToscaResourceName(ResourceMetadataData vfc,
			String toscaResourceName) {
		if (toscaResourceName == null) {
			toscaResourceName = CommonBeUtils.generateToscaResourceName(
					((ResourceMetadataDataDefinition) vfc.getMetadataDataDefinition()).getResourceType().name(),
					vfc.getMetadataDataDefinition().getSystemName());
		}
		Either<Boolean, StorageOperationStatus> validateToscaResourceNameExists = resourceOperation
				.validateToscaResourceNameExists(toscaResourceName);
		if (validateToscaResourceNameExists.isRight()) {
			StorageOperationStatus storageOperationStatus = validateToscaResourceNameExists.right().value();
			log.error("Couldn't validate toscaResourceName uniqueness - error: {}", storageOperationStatus);
			return Either.right(storageOperationStatus);
		}
		if (validateToscaResourceNameExists.left().value()) {
			log.debug("Setting tosca resource name to be {}", toscaResourceName);
			((ResourceMetadataDataDefinition) vfc.getMetadataDataDefinition()).setToscaResourceName(toscaResourceName);
			return Either.left(true);
		} else {
			// As agreed with Renana - cannot be fixed automatically
			log.warn("toscaResourceName {} is not unique! Cannot set it. Continuing...");
			return Either.left(false);
		}
	}
}
