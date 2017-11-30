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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.CommonImportManager.ElementTypeEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IPolicyTypeOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("policyTypeImportManager")
public class PolicyTypeImportManager {

	@Resource
	private IPolicyTypeOperation policyTypeOperation;
	@Resource
	private ComponentsUtils componentsUtils;
	@Autowired
	protected IGroupOperation groupOperation;
	@Autowired
	private ToscaOperationFacade toscaOperationFacade;

	@Resource
	private CommonImportManager commonImportManager;

	public Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> createPolicyTypes(String groupTypesYml) {
		return commonImportManager.createElementTypes(groupTypesYml, elementTypeYml -> createPolicyTypesFromYml(elementTypeYml), groupTypesList -> createPolicyTypesByDao(groupTypesList), ElementTypeEnum.PolicyType);
	}

	private Either<List<PolicyTypeDefinition>, ActionStatus> createPolicyTypesFromYml(String policyTypesYml) {

		return commonImportManager.createElementTypesFromYml(policyTypesYml, (policyTypeName, groupTypeJsonData) -> createPolicyType(policyTypeName, groupTypeJsonData));
	}

	private Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> createPolicyTypesByDao(List<PolicyTypeDefinition> policyTypesToCreate) {
		return commonImportManager.createElementTypesByDao(policyTypesToCreate, policyType -> validatePolicyType(policyType), policyType -> new ImmutablePair<>(ElementTypeEnum.PolicyType, policyType.getType()),
				policyTypeName -> policyTypeOperation.getLatestPolicyTypeByType(policyTypeName), policyType -> policyTypeOperation.addPolicyType(policyType), null);
	}

	private Either<ActionStatus, ResponseFormat> validatePolicyType(PolicyTypeDefinition policyType) {
		Either<ActionStatus, ResponseFormat> result = Either.left(ActionStatus.OK);
		if (policyType.getTargets() != null) {
			if (policyType.getTargets().isEmpty()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.TARGETS_EMPTY, policyType.getType());
				result = Either.right(responseFormat);
			}
			if (result.isLeft()) {
				for (String targetName : policyType.getTargets()) {

					boolean isValid = toscaOperationFacade.getLatestByToscaResourceName(targetName).isLeft();;
					if (!isValid) {
						isValid = groupOperation.isGroupExist(targetName, false);
					}
					if (!isValid) {
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.TARGETS_NON_VALID, policyType.getType(), targetName);
						result = Either.right(responseFormat);
						break;
					}
				}
			}
		}

		return result;
	}

	private PolicyTypeDefinition createPolicyType(String groupTypeName, Map<String, Object> toscaJson) {

		PolicyTypeDefinition policyType = new PolicyTypeDefinition();

		if (toscaJson != null) {
			// Description
			final Consumer<String> descriptionSetter = description -> policyType.setDescription(description);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DESCRIPTION.getElementName(), descriptionSetter);
			// Derived From
			final Consumer<String> derivedFromSetter = derivedFrom -> policyType.setDerivedFrom(derivedFrom);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DERIVED_FROM.getElementName(), derivedFromSetter);
			// Properties
			commonImportManager.setProperties(toscaJson, (values) -> policyType.setProperties(values));
			// Metadata
			final Consumer<Map<String, String>> metadataSetter = metadata -> policyType.setMetadata(metadata);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.METADATA.getElementName(), metadataSetter);
			// Targets
			final Consumer<List<String>> targetsSetter = targets -> policyType.setTargets(targets);
			commonImportManager.setField(toscaJson, ToscaTagNamesEnum.TARGETS.getElementName(), targetsSetter);

			policyType.setType(groupTypeName);

			policyType.setHighestVersion(true);

			policyType.setVersion(ImportUtils.Constants.FIRST_CERTIFIED_VERSION_VERSION);
		}
		return policyType;
	}

}
