/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RequirementValidationTest {
    private ResponseFormatManager responseFormatManagerMock;
    private final Component component = createComponent();
    private final RequirementValidationUtilTest requirementValidationUtilTest = new RequirementValidationUtilTest();
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        responseFormatManagerMock = Mockito.mock(ResponseFormatManager.class);
        when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
    }

    @Test
    public void shouldPassRequirementsValidationForHappyScenario() {
        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        requirementsDefinitions.add(createRequirement("reqName", "capType", "node", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateRequirementsResponseEither = requirementValidationUtilTest
                .validateRequirements(requirementsDefinitions, component, false);
        Assert.assertTrue(validateRequirementsResponseEither.isLeft());
    }

    @Test
    public void shouldFailWhenRequirementNameAlreadyExist() {
        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        requirementsDefinitions.add(createRequirement("ReqNameC", "capType", "node", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateRequirementsResponseEither = requirementValidationUtilTest
                .validateRequirements(requirementsDefinitions, component, false);
        Assert.assertTrue(validateRequirementsResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenRequirementNameEmpty() {
        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        requirementsDefinitions.add(createRequirement("", "capType", "node", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateRequirementsResponseEither = requirementValidationUtilTest
                .validateRequirements(requirementsDefinitions, component, false);
        Assert.assertTrue(validateRequirementsResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenRequirementCapabilityEmpty() {
        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        requirementsDefinitions.add(createRequirement("reqName1", "", "node", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateRequirementsResponseEither = requirementValidationUtilTest
                .validateRequirements(requirementsDefinitions, component, false);
        Assert.assertTrue(validateRequirementsResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenRequirementMaxOccurrencesLessThanMinOccurrences() {
        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        requirementsDefinitions.add(createRequirement("reqName1", "capType", "node", "source1",
                "111", "3"));
        Either<Boolean, ResponseFormat> validateRequirementsResponseEither = requirementValidationUtilTest
                .validateRequirements(requirementsDefinitions, component, false);
        Assert.assertTrue(validateRequirementsResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenRequirementNotFoundForUpdate() {
        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        RequirementDefinition requirementsToUpdate = createRequirement("reqName1", "capType", "node", "source1",
                "1", "3");
        requirementsToUpdate.setUniqueId("uniqueId2");

        requirementsDefinitions.add(requirementsToUpdate);
        Either<Boolean, ResponseFormat> validateRequirementsResponseEither = requirementValidationUtilTest
                .validateRequirements(requirementsDefinitions, component, true);
        Assert.assertTrue(validateRequirementsResponseEither.isRight());
    }

    private RequirementDefinition createRequirement(String name, String capability, String node,
                                                    String relationship, String minOccurrences,
                                                    String maxOccurrences) {
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName(name);
        requirementDefinition.setCapability(capability);
        requirementDefinition.setNode(node);
        requirementDefinition.setRelationship(relationship);
        requirementDefinition.setMaxOccurrences(maxOccurrences);
        requirementDefinition.setMinOccurrences(minOccurrences);
        requirementDefinition.setUniqueId("uniqueId");

        return requirementDefinition;
    }

    private Resource createComponent() {
        Resource resource = new Resource();
        resource.setName("Resource1");
        resource.addCategory("Network Layer 2-3", "Router");
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        tgs.add(resource.getName());
        resource.setTags(tgs);

        List<RequirementDefinition> requirementsDefinitions = new ArrayList<>();
        requirementsDefinitions.add(createRequirement("ReqNameC", "reqDesc", "capType", "source1",
                "0", "10"));
        Map<String, List<RequirementDefinition>> requirementsMap = new HashMap<>();
        requirementsMap.put("capTypeC", requirementsDefinitions);
        resource.setRequirements(requirementsMap);

        return resource;
    }

    private class RequirementValidationUtilTest extends RequirementValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return responseFormatManagerMock;
        }
    }
}