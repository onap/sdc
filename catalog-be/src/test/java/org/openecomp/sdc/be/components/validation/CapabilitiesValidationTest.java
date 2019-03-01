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
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CapabilitiesValidationTest  {
    private ResponseFormatManager responseFormatManagerMock;
    private final Component component = createComponent();
    private final CapabilitiesValidationUtilTest capabilitiesValidationUtilTest = new CapabilitiesValidationUtilTest();
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        responseFormatManagerMock = Mockito.mock(ResponseFormatManager.class);
        when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
    }

    @Test
    public void shouldPassCapabilitiesValidationForHappyScenario() {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(createCapability("capName", "capDesc", "capType", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateCapabilitiesResponseEither = capabilitiesValidationUtilTest
                .validateCapabilities(capabilityDefinitions, component, false);
        Assert.assertTrue(validateCapabilitiesResponseEither.isLeft());
    }

    @Test
    public void shouldFailWhenCapabilityNameAlreadyExist() {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(createCapability("capNameC", "capDesc", "capType", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateCapabilitiesResponseEither = capabilitiesValidationUtilTest
                .validateCapabilities(capabilityDefinitions, component, false);
        Assert.assertTrue(validateCapabilitiesResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenCapabilityNameEmpty() {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(createCapability("", "capDesc", "capType", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateCapabilitiesResponseEither = capabilitiesValidationUtilTest
                .validateCapabilities(capabilityDefinitions, component, false);
        Assert.assertTrue(validateCapabilitiesResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenCapabilityTypeEmpty() {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(createCapability("capName1", "capDesc", "", "source1",
                "0", "10"));
        Either<Boolean, ResponseFormat> validateCapabilitiesResponseEither = capabilitiesValidationUtilTest
                .validateCapabilities(capabilityDefinitions, component, false);
        Assert.assertTrue(validateCapabilitiesResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenCapabilityMaxOccurrencesLessThanMinOccurrences() {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(createCapability("capName1", "capDesc", "capType", "source1",
                "111", "3"));
        Either<Boolean, ResponseFormat> validateCapabilitiesResponseEither = capabilitiesValidationUtilTest
                .validateCapabilities(capabilityDefinitions, component, false);
        Assert.assertTrue(validateCapabilitiesResponseEither.isRight());
    }

    @Test
    public void shouldFailWhenCapabilityNotFoundForUpdate() {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        CapabilityDefinition capabilityToUpdate = createCapability("capName1", "capDesc", "capType", "source1",
                "1", "3");
        capabilityToUpdate.setUniqueId("uniqueId2");

        capabilityDefinitions.add(capabilityToUpdate);
        Either<Boolean, ResponseFormat> validateCapabilitiesResponseEither = capabilitiesValidationUtilTest
                .validateCapabilities(capabilityDefinitions, component, true);
        Assert.assertTrue(validateCapabilitiesResponseEither.isRight());
    }

    private CapabilityDefinition createCapability(String name, String description, String type,
                                                  String validSourceTypes, String minOccurrences,
                                                  String maxOccurrences) {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(name);
        capabilityDefinition.setDescription(description);
        capabilityDefinition.setType(type);
        capabilityDefinition.setValidSourceTypes(Collections.singletonList(validSourceTypes));
        capabilityDefinition.setMaxOccurrences(maxOccurrences);
        capabilityDefinition.setMinOccurrences(minOccurrences);
        capabilityDefinition.setUniqueId("uniqueId");


        return capabilityDefinition;
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

        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(createCapability("capNameC", "capDesc", "capType", "source1",
                "0", "10"));
            Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
            capabilityMap.put("capTypeC", capabilityDefinitions);
            resource.setCapabilities(capabilityMap);

        return resource;
    }

    private class CapabilitiesValidationUtilTest extends CapabilitiesValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return responseFormatManagerMock;
        }
    }
}