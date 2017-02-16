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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.slf4j.Logger;

import fj.data.Either;

public class AllowMultipleHeats {

	public static boolean removeAndUpdateHeatPlaceHolders(TitanGenericDao titanGenericDao, Logger log,
			boolean inTrsansaction) {

		boolean result = true;

		try {

			List<ArtifactData> artifactsToDelete = new ArrayList<>();
			List<ArtifactData> artifactsToUpdate = new ArrayList<>();

			String[] phLabels = { "heat", "heatvol", "heatnet" };

			for (String artifactLabel : phLabels) {
				Map<String, Object> properties = new HashMap<>();

				properties.put(GraphPropertiesDictionary.ARTIFACT_LABEL.getProperty(), artifactLabel);

				Either<List<ArtifactData>, TitanOperationStatus> allHeatArtifacts = titanGenericDao
						.getByCriteria(NodeTypeEnum.ArtifactRef, properties, null, ArtifactData.class);

				if (allHeatArtifacts.isRight()) {
					TitanOperationStatus status = allHeatArtifacts.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						continue;
					} else {
						result = false;
						return result;
					}

				}

				List<ArtifactData> list = allHeatArtifacts.left().value();
				log.debug("Found {} artifacts with label {}", (list == null ? 0 : list.size()), artifactLabel);

				if (list != null && false == list.isEmpty()) {

					for (ArtifactData artifactData : list) {
						String esId = artifactData.getArtifactDataDefinition().getEsId();
						if (esId == null || true == esId.isEmpty()) {
							artifactsToDelete.add(artifactData);
						} else {
							artifactsToUpdate.add(artifactData);
						}
					}
				}
			}

			if (false == artifactsToDelete.isEmpty()) {
				for (ArtifactData artifactData : artifactsToDelete) {
					// System.out.println("Going to delete artifact " +
					// artifactData);
					log.debug("Going to delete artifact {}", artifactData);
					Either<ArtifactData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(artifactData,
							ArtifactData.class);
					if (deleteNode.isRight()) {
						log.error("Failed to delete artifact node {}", deleteNode.left().value());
						result = false;
						return result;
					} else {
						log.debug("Delete artifact node {}", deleteNode.left().value());
					}
				}
			}

			log.debug("Number of deleted artifacts is {}", artifactsToDelete.size());

			int counter = 0;
			if (false == artifactsToUpdate.isEmpty()) {
				for (ArtifactData artifactData : artifactsToUpdate) {
					// System.out.println("Going to update artifact " +
					// artifactData);

					if (artifactData.getArtifactDataDefinition().getMandatory() != null
							&& true == artifactData.getArtifactDataDefinition().getMandatory()) {
						log.debug("Going to update artifact {}", artifactData);
						counter++;
						artifactData.getArtifactDataDefinition().setMandatory(false);
						Either<ArtifactData, TitanOperationStatus> updatedNode = titanGenericDao
								.updateNode(artifactData, ArtifactData.class);
						if (updatedNode.isRight()) {
							log.error("Failed to update artifact node {}", updatedNode.left().value());
							result = false;
							return result;
						} else {
							log.debug("Update artifact node {}", updatedNode.left().value());
						}
					}
				}
			}

			log.debug("Number of updated artifacts is {}", counter);

			return result;

		} finally {
			if (inTrsansaction == false) {
				if (result == false) {
					titanGenericDao.rollback();
				} else {
					titanGenericDao.commit();
				}
			}
		}
	}

}
