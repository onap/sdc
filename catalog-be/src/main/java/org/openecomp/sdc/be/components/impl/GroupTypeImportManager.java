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

package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.CommonImportManager.ElementTypeEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("groupTypeImportManager")
public class GroupTypeImportManager {

	public static void main(String[] args) {

		List<PropertyDefinition> properties = new ArrayList<>();
		PropertyDefinition propertyDefintion = new PropertyDefinition();
		propertyDefintion.setName("aaa");
		properties.add(propertyDefintion);

		List<String> allParentsProps = new ArrayList<>();
		allParentsProps.add("aaa");
		allParentsProps.add("bbb");

		Set<String> alreadyExistPropsCollection = properties.stream().filter(p -> allParentsProps.contains(p.getName())).map(p -> p.getName()).collect(Collectors.toSet());
		System.out.println(alreadyExistPropsCollection);

	}

	private static Logger log = LoggerFactory.getLogger(GroupTypeImportManager.class.getName());
	@Resource
	private IGroupTypeOperation groupTypeOperation;
	@Resource
	private ComponentsUtils componentsUtils;
	@Resource
	private ToscaOperationFacade toscaOperationFacade;

	@Resource
	private CommonImportManager commonImportManager;

	public Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> createGroupTypes(String groupTypesYml) {
		return commonImportManager.createElementTypes(groupTypesYml, elementTypeYml -> createGroupTypesFromYml(elementTypeYml), groupTypesList -> createGroupTypesByDao(groupTypesList), ElementTypeEnum.GroupType);
	}

	private Either<List<GroupTypeDefinition>, ActionStatus> createGroupTypesFromYml(String groupTypesYml) {

		return commonImportManager.createElementTypesFromYml(groupTypesYml, (groupTypeName, groupTypeJsonData) -> createGroupType(groupTypeName, groupTypeJsonData));
	}

	private Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> createGroupTypesByDao(List<GroupTypeDefinition> groupTypesToCreate) {
		return commonImportManager.createElementTypesByDao(groupTypesToCreate, groupType -> validateGroupType(groupType), groupType -> new ImmutablePair<>(ElementTypeEnum.GroupType, groupType.getType()),
				groupTypeName -> groupTypeOperation.getLatestGroupTypeByType(groupTypeName), groupType -> groupTypeOperation.addGroupType(groupType), null);
	}

	private Either<ActionStatus, ResponseFormat> validateGroupType(GroupTypeDefinition groupType) {
		Either<ActionStatus, ResponseFormat> result = Either.left(ActionStatus.OK);
		if (groupType.getMembers() != null) {
			if (groupType.getMembers().isEmpty()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GROUP_MEMBER_EMPTY, groupType.getType());
				result = Either.right(responseFormat);
			} else {
				for (String member : groupType.getMembers()) {
					// Verify that such Resource exist
					Either<org.openecomp.sdc.be.model.Resource, StorageOperationStatus> eitherMemberExist = toscaOperationFacade.getLatestByToscaResourceName(member);
					if (eitherMemberExist.isRight()) {
						StorageOperationStatus operationStatus = eitherMemberExist.right().value();
						log.debug("Error when fetching parent resource {}, error: {}", member, operationStatus);
						ActionStatus convertFromStorageResponse = componentsUtils.convertFromStorageResponse(operationStatus);
						BeEcompErrorManager.getInstance().logBeComponentMissingError("Import GroupType", "resource", member);
						result = Either.right(componentsUtils.getResponseFormat(convertFromStorageResponse, member));
						break;
					}
				}

			}
		}
		return result;
	}

	private GroupTypeDefinition createGroupType(String groupTypeName, Map<String, Object> toscaJson) {

		GroupTypeDefinition groupType = new GroupTypeDefinition();

		if (toscaJson != null) {
			// Description
			final Consumer<String> descriptionSetter = description -> groupType.setDescription(description);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DESCRIPTION.getElementName(), descriptionSetter);
			// Derived From
			final Consumer<String> derivedFromSetter = derivedFrom -> groupType.setDerivedFrom(derivedFrom);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DERIVED_FROM.getElementName(), derivedFromSetter);
			// Properties
			commonImportManager.setProperties(toscaJson, (values) -> groupType.setProperties(values));
			// Metadata
			final Consumer<Map<String, String>> metadataSetter = metadata -> groupType.setMetadata(metadata);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.METADATA.getElementName(), metadataSetter);
			// Members
			final Consumer<List<String>> membersSetter = members -> groupType.setMembers(members);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.MEMBERS.getElementName(), membersSetter);

			groupType.setType(groupTypeName);

			groupType.setHighestVersion(true);

			groupType.setVersion(ImportUtils.Constants.FIRST_CERTIFIED_VERSION_VERSION);
		}
		return groupType;
	}

}
