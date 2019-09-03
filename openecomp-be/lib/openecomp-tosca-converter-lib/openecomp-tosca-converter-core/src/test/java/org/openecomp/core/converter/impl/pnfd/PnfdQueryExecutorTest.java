/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.core.converter.impl.pnfd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.core.converter.pnfd.exception.QueryOperationNotSupportedException;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;

public class PnfdQueryExecutorTest {
    @Rule
    public ExpectedException expectedExceptionRule = ExpectedException.none();

    private Map<String, Object> yamlToQuery;

    @Before
    public void setup() {
        yamlToQuery = new HashMap<>();
        final LinkedHashMap<String, Object> nodeTemplateMap = new LinkedHashMap<>();
        yamlToQuery.put("topology_template", nodeTemplateMap);
        final Map nodeTemplate1 = ImmutableMap.of("nodeTemplate1"
            , ImmutableMap.of(
                "type", "tosca.nodes.nfv.PNF",
                "properties", ImmutableMap.of(
                    "layers_protocol", "",
                    "provider", "Mycompany",
                    "version", "1.0"
                )
            )
        );

        final Map nodeTemplate2 = ImmutableMap.of("nodeTemplate2"
            , ImmutableMap.of(
                "type", "tosca.nodes.nfv.PnfExtCp",
                "properties", ImmutableMap.of(
                    "trunk_mode", "false",
                    "role", "leaf",
                    "description", "External connection point to access this pnf",
                    "layers_protocol", ImmutableList.of("ipv4", "ipv6", "otherProtocol")
                )
            )
        );

        nodeTemplateMap.putAll(nodeTemplate1);
        nodeTemplateMap.putAll(nodeTemplate2);
    }


    @Test
    public void queryNestedYaml() {
        //given
        final ImmutableMap<String, Object> query =
            ImmutableMap.of("topology_template",
                ImmutableMap.of("nodeTemplate2",
                    ImmutableMap.of("type", "tosca.nodes.nfv.PnfExtCp")
                )
            );
        //when
        final boolean queryResult = PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
        //then
        assertThat("Element should be found", queryResult, is(true));
    }

    @Test
    public void andQueryWithPropertiesInSameLevel() {
        //given
        final ImmutableMap<String, Object> query =
            ImmutableMap.of(
                "topology_template",
                ImmutableMap.of(
                    "nodeTemplate2",
                    ImmutableMap.of(
                        "type", "tosca.nodes.nfv.PnfExtCp",
                        "properties", ImmutableMap.of(
                            "role", "leaf",
                            "description", "External connection point to access this pnf"
                        )
                    )
                )
            );
        //when
        final boolean queryResult = PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
        //then
        assertThat("Element should be found", queryResult, is(true));
    }

    @Test
    public void andQueryWithPropertiesDifferentLevel() {
        //given
        final ImmutableMap<String, Object> query =
            ImmutableMap.of(
                "topology_template",
                ImmutableMap.of(
                    "nodeTemplate2",
                    ImmutableMap.of(
                        "type", "tosca.nodes.nfv.PnfExtCp"
                    ),
                    "nodeTemplate1",
                    ImmutableMap.of(
                        "type", "tosca.nodes.nfv.PNF"
                    )
                )
            );
        //when
        final boolean queryResult = PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
        //then
        assertThat("Element should be found", queryResult, is(true));
    }

    @Test
    public void queryListPropertyNotSupported() {
        //then
        expectedExceptionRule.expect(QueryOperationNotSupportedException.class);
        expectedExceptionRule.expectMessage("Yaml list query is not supported yet");
        //given query with a list instance
        final ImmutableMap<String, Object> query =
            ImmutableMap.of(
                "topology_template",
                ImmutableMap.of(
                    "nodeTemplate2",
                    ImmutableMap.of(
                        "type", "tosca.nodes.nfv.PnfExtCp",
                        "properties", ImmutableMap.of(
                            "layers_protocol", ImmutableList.of("ipv4", "ipv6", "otherProtocol")
                        )
                    )
                )
            );
        //when
        final boolean queryResult = PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
    }

    @Test
    public void queryStartingListPropertyNotSupported() {
        //then
        expectedExceptionRule.expect(QueryOperationNotSupportedException.class);
        expectedExceptionRule.expectMessage("Yaml list query is not supported yet");
        //given query with a list instance
        final Object query = ImmutableSet.of("test", "test", "test");
        //when
        PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
    }

    @Test
    public void queryWithUnsupportedClassInstance() {
        //given a query with unsupported class instance
        final Object query = ImmutableMap.of("topology_template", new Object());
        //then
        expectedExceptionRule.expect(QueryOperationNotSupportedException.class);
        expectedExceptionRule.expectMessage(String.format("Yaml query operation for '%s' is not supported yet", Object.class));
        //when
        PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
    }

    @Test
    public void queryAString() {
        //given a query with string
        final Object yaml = "query";
        final Object query = "query";
        //when
        final boolean queryResult = PnfdQueryExecutor.find(new ConversionQuery(query), yaml);

        //then
        assertThat("Element should be found", queryResult, is(true));

    }

    @Test
    public void queryWithNullPropertyValue() {
        //given a query with string
        final Map<String, Object> query1 = new HashMap<>();
        query1.put("topology_template", null);

        final Map<String, Object> topologyTemplate = new HashMap<>();
        topologyTemplate.put("nodeTemplate1", null);
        final Object query2 = ImmutableMap.of("topology_template", topologyTemplate);

        final Map<String, Object> query3 = new HashMap<>();
        query3.put("topology_template1", null);
        //when
        final boolean queryResult1 = PnfdQueryExecutor.find(new ConversionQuery(query1), yamlToQuery);
        final boolean queryResult2 = PnfdQueryExecutor.find(new ConversionQuery(query2), yamlToQuery);
        final boolean queryResult3 = PnfdQueryExecutor.find(new ConversionQuery(query3), yamlToQuery);
        //then
        assertThat("Element should be found", queryResult1, is(true));
        assertThat("Element should be found", queryResult2, is(true));
        assertThat("Element should be found", queryResult3, is(false));
    }

    @Test
    public void nullQuery() {
        //given a null query
        final Object query = null;
        //when
        final boolean queryResult = PnfdQueryExecutor.find(new ConversionQuery(query), yamlToQuery);
        //then
        assertThat("Element should be found", queryResult, is(true));

    }

}