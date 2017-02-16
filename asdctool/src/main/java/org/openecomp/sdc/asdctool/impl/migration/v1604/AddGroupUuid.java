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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.slf4j.Logger;

import fj.data.Either;

public class AddGroupUuid {

	public static boolean addGroupUuids(TitanGenericDao titanGenericDao, Logger log, boolean inTrsansaction) {

		boolean result = true;

		try {

			log.debug("========================================================");
			log.debug("Before find all groups");

			Either<List<GroupData>, TitanOperationStatus> allGroups = titanGenericDao.getByCriteria(NodeTypeEnum.Group,
					null, null, GroupData.class);

			if (allGroups.isRight()) {
				TitanOperationStatus status = allGroups.right().value();
				log.debug("After finding all groups. Status is {}", status);
				if (status != TitanOperationStatus.NOT_FOUND && status != TitanOperationStatus.OK) {
					result = false;
					return result;
				} else {
					return result;
				}
			}

			List<GroupData> groups = allGroups.left().value();

			log.info("The number of groups fetched is {}", groups == null ? 0 : groups.size());

			int numberOfUpdates = 0;
			if (false == groups.isEmpty()) {
				Map<String, List<GroupData>> invariantIdToGroups = groups.stream()
						.collect(Collectors.groupingBy(p -> p.getGroupDataDefinition().getInvariantUUID()));

				// All the groups with the same invariantUUID should have the
				// same group UUID since update VF flow with CSAR was not
				// supported in the E2E environment.

				log.info("The number of different invariantUuids is {}",
						invariantIdToGroups == null ? 0 : invariantIdToGroups.size());

				for (Entry<String, List<GroupData>> entry : invariantIdToGroups.entrySet()) {

					String invariantUuid = entry.getKey();
					List<GroupData> groupsData = entry.getValue();

					StringBuilder builder = new StringBuilder();
					groupsData.forEach(p -> builder.append(p.getGroupDataDefinition().getUniqueId() + ","));

					String groupUUID = groupsData.get(0).getGroupDataDefinition().getGroupUUID();

					if (groupUUID == null) {

						groupUUID = UniqueIdBuilder.generateUUID();

						log.debug("Before updating groups {} with groupUUID {}", builder.toString(), groupUUID);

						for (GroupData groupData : groupsData) {

							numberOfUpdates++;
							groupData.getGroupDataDefinition().setGroupUUID(groupUUID);
							Either<GroupData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(groupData,
									GroupData.class);
							if (updateNode.isRight()) {
								log.error("Failed to update group " + groupData + ". Error is {}",
										updateNode.right().value().toString());
								result = false;
								return result;
							}

						}

						log.debug("After updating groups {} with groupUUID {}", builder.toString(), groupUUID);
					}

				}
			}

			log.info("The number of groups updated with groupUUID is " + numberOfUpdates);

			return result;

		} finally {
			log.info("Finish updating groupUUIDs. Status is {}.", result);
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
