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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.datatypes.GroupHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.utils.CsarParserUtils;

public class CsarValidationUtils {

	public static void validateCsarVfArtifact(String csarUUID, Resource resource) throws Exception {

		List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = CsarParserUtils.getListTypeHeatMetaDefinition(csarUUID);
		assertTrue(
				"check group count, expected: " + getGroupCount(listTypeHeatMetaDefinition) + ", actual: "
						+ resource.getGroups().size(),
				getGroupCount(listTypeHeatMetaDefinition) == resource.getGroups().size());
		assertTrue(
				"check artifact count, expected: " + getArtifactCount(listTypeHeatMetaDefinition, false) + ", actual: "
						+ resource.getDeploymentArtifacts().size(),
				getArtifactCount(listTypeHeatMetaDefinition, false) == resource.getDeploymentArtifacts().size());

	}
	
	private static Integer getGroupCount(List<TypeHeatMetaDefinition> listHeatMetaDefenition) {
		int count = 0;
		for (TypeHeatMetaDefinition typeHeatMetaDefinition : listHeatMetaDefenition) {
			count = count + typeHeatMetaDefinition.getGroupHeatMetaDefinition().size();
		}
		return count;
	}
	
	private static Integer getArtifactCount(List<TypeHeatMetaDefinition> listHeatMetaDefenition, Boolean isEnvIncluded) {
		int count = 0;
		List<HeatMetaFirstLevelDefinition> uniqeArtifactList = new ArrayList<>();

		for (TypeHeatMetaDefinition typeHeatMetaDefinition : listHeatMetaDefenition) {
			for (GroupHeatMetaDefinition groupHeatMetaDefinition : typeHeatMetaDefinition
					.getGroupHeatMetaDefinition()) {
				if (isEnvIncluded) {
					count = count + groupHeatMetaDefinition.getArtifactList().size();
				} else {
					for (HeatMetaFirstLevelDefinition fileName : groupHeatMetaDefinition.getArtifactList()) {
						if (!fileName.getFileName().contains(".env") && !uniqeArtifactList.contains(fileName)) {
							uniqeArtifactList.add(fileName);
							count = count + 1;
						}
					}
				}
			}
		}
		return count;
	}
}
