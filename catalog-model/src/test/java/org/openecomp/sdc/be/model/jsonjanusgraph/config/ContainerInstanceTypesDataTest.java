/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.jsonjanusgraph.config;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class ContainerInstanceTypesDataTest {

    private ContainerInstanceTypesData containerInstanceTypesData;
    private EnumSet<ResourceTypeEnum> serviceAllowedTypes;
    private EnumMap<ResourceTypeEnum, EnumSet<ResourceTypeEnum>> resourceAllowedTypeConfig;
    private EnumSet<ResourceTypeEnum> allResourceTypes;

    @BeforeAll
    public static void beforeClass() {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
            "src/test/resources/config"));
    }

    @BeforeEach
    public void setUp() {
        containerInstanceTypesData = new ContainerInstanceTypesData();

        allResourceTypes = EnumSet.allOf(ResourceTypeEnum.class);

        serviceAllowedTypes = EnumSet.of(ResourceTypeEnum.VF,
            ResourceTypeEnum.CR,
            ResourceTypeEnum.CP,
            ResourceTypeEnum.PNF,
            ResourceTypeEnum.CVFC,
            ResourceTypeEnum.VL,
            ResourceTypeEnum.Configuration,
            ResourceTypeEnum.ServiceProxy,
            ResourceTypeEnum.ABSTRACT
        );

        resourceAllowedTypeConfig =
            new EnumMap<>(ResourceTypeEnum.class);

        resourceAllowedTypeConfig.put(ResourceTypeEnum.VF,
            EnumSet.of(ResourceTypeEnum.VFC,
                ResourceTypeEnum.VF,
                ResourceTypeEnum.CR,
                ResourceTypeEnum.CP,
                ResourceTypeEnum.PNF,
                ResourceTypeEnum.CVFC,
                ResourceTypeEnum.VL,
                ResourceTypeEnum.Configuration,
                ResourceTypeEnum.ServiceProxy,
                ResourceTypeEnum.ABSTRACT));

        resourceAllowedTypeConfig.put(ResourceTypeEnum.CVFC,
            EnumSet.of(ResourceTypeEnum.VFC,
                ResourceTypeEnum.VF,
                ResourceTypeEnum.CR,
                ResourceTypeEnum.CP,
                ResourceTypeEnum.PNF,
                ResourceTypeEnum.CVFC,
                ResourceTypeEnum.VL,
                ResourceTypeEnum.ServiceProxy,
                ResourceTypeEnum.ABSTRACT));

        resourceAllowedTypeConfig.put(ResourceTypeEnum.PNF,
            EnumSet.of(ResourceTypeEnum.VF,
                ResourceTypeEnum.CR,
                ResourceTypeEnum.CP,
                ResourceTypeEnum.PNF,
                ResourceTypeEnum.CVFC,
                ResourceTypeEnum.VL,
                ResourceTypeEnum.Configuration,
                ResourceTypeEnum.ServiceProxy,
                ResourceTypeEnum.ABSTRACT));

        resourceAllowedTypeConfig.put(ResourceTypeEnum.CR,
            EnumSet.of(ResourceTypeEnum.VF,
                ResourceTypeEnum.CR,
                ResourceTypeEnum.CP,
                ResourceTypeEnum.PNF,
                ResourceTypeEnum.CVFC,
                ResourceTypeEnum.VL,
                ResourceTypeEnum.Configuration,
                ResourceTypeEnum.ServiceProxy,
                ResourceTypeEnum.ABSTRACT));

        resourceAllowedTypeConfig.put(ResourceTypeEnum.VL,
            EnumSet.of(ResourceTypeEnum.VL));
    }

    @Test
    public void isAllowedForServiceComponent() {
        for (final ResourceTypeEnum allowedType : serviceAllowedTypes) {
            assertThat(String.format("%s should be allowed", allowedType.getValue()),
                containerInstanceTypesData.isAllowedForServiceComponent(allowedType, ""), is(true));
        }
    }

    @Test
    public void isAllowedForResourceComponent() {
        for (final ResourceTypeEnum componentResourceType : allResourceTypes) {
            final EnumSet<ResourceTypeEnum> allowedResourceType = resourceAllowedTypeConfig.get(componentResourceType);
            for (final ResourceTypeEnum resourceType : allResourceTypes) {
                if (allowedResourceType == null) {
                    final String msg = String
                        .format("'%s' resource type should not be allowed", resourceType.getValue());
                    assertThat(msg, containerInstanceTypesData
                        .isAllowedForResourceComponent(componentResourceType, resourceType), is(false));
                    continue;
                }
                final boolean isAllowed = allowedResourceType.contains(resourceType);
                final String msg = String
                    .format("'%s' resource type should %s be allowed", resourceType.getValue(), isAllowed ? "" : "not");
                assertThat(msg, containerInstanceTypesData
                    .isAllowedForResourceComponent(componentResourceType, resourceType), is(
                    isAllowed));
            }
        }
    }

    @Test
    public void getComponentAllowedListTest() {
        List<String> actualAllowedList = containerInstanceTypesData
            .getComponentAllowedList(ComponentTypeEnum.SERVICE, null);
        assertThat("Allowed Instance Resource Type List should be as expected",
            actualAllowedList, containsInAnyOrder(actualAllowedList.toArray()));

        for (final ResourceTypeEnum resourceType : allResourceTypes) {
            actualAllowedList = containerInstanceTypesData
                .getComponentAllowedList(ComponentTypeEnum.RESOURCE, resourceType);
            final EnumSet<ResourceTypeEnum> expectedAllowedSet = resourceAllowedTypeConfig.get(resourceType);
            if (CollectionUtils.isEmpty(expectedAllowedSet)) {
                assertThat("Allowed Instance Resource Type List should be as expected",
                    actualAllowedList, is(empty()));
                continue;
            }
            assertThat("Allowed Instance Resource Type List should be as expected",
                actualAllowedList,
                containsInAnyOrder(expectedAllowedSet.stream().map(ResourceTypeEnum::getValue).toArray()));
        }
    }

    @Test
    public void getComponentAllowedListTestEmptyList() {
        List<String> actualAllowedList = containerInstanceTypesData
            .getComponentAllowedList(ComponentTypeEnum.PRODUCT, null);
        final String msg = "Allowed Instance Resource Type List should be empty";
        assertThat(msg, actualAllowedList, is(emptyList()));

        actualAllowedList = containerInstanceTypesData
            .getComponentAllowedList(ComponentTypeEnum.SERVICE, ResourceTypeEnum.VF);
        assertThat(msg, actualAllowedList, is(emptyList()));

        actualAllowedList = containerInstanceTypesData
            .getComponentAllowedList(ComponentTypeEnum.SERVICE, ResourceTypeEnum.ServiceProxy);
        assertThat(msg, actualAllowedList, is(emptyList()));

        actualAllowedList = containerInstanceTypesData
            .getComponentAllowedList(ComponentTypeEnum.RESOURCE, null);
        assertThat(msg, actualAllowedList, is(emptyList()));
    }

}