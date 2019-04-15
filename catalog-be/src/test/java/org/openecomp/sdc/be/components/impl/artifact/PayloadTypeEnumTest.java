/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.artifact;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_ENV;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_YAML;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.JSON;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.NOT_DEFINED;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.XML;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.YAML;

public class PayloadTypeEnumTest {

    private static final byte[] INVALID_YAML = "invalidYaml".getBytes();

    @Test
    public void testGivenValidContentForHeatYamlType_WhenCheckValid_ThenResultTrue() {
        byte[] input = "heat_template_version: 1".getBytes();
        Either<Boolean, ActionStatus> result = HEAT_YAML.isValid(input);
        assertTrue(result.left().value());
    }

    @Test
    public void testGivenInvalidYamlContentForHeatYamlType_WhenCheckValid_ThenInvalidYamlResultReturned() {
        Either<Boolean, ActionStatus> result = HEAT_YAML.isValid(INVALID_YAML);
        assertEquals(ActionStatus.INVALID_YAML, result.right().value());
    }

    @Test
    public void testGivenValidYamlButInvalidHeatTempleContentForHeatYamlType_WhenCheckValid_ThenInvalidHeatReturned() {
        byte[] input = "validYaml: butNotTopologyTemplate".getBytes();
        Either<Boolean, ActionStatus> result = HEAT_YAML.isValid(input);
        assertEquals(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, result.right().value());
    }

    @Test
    public void testGivenHeatYamlType_WhenCheckIfHeatRelated_ThenResultTrue() {
        assertTrue(HEAT_YAML.isHeatRelated());
    }

    @Test
    public void testGivenValidYamlForHeatEnvType_WhenCheckValid_ThenResultTrue() {
        byte[] input = "validYaml: yes".getBytes();
        Either<Boolean, ActionStatus> result = HEAT_ENV.isValid(input);
        assertTrue(result.left().value());
    }

    @Test
    public void testGivenInvalidYamlContentForHeatEnvType_WhenCheckValid_ThenInvalidYamlResultReturned() {
        Either<Boolean, ActionStatus> result = HEAT_ENV.isValid(INVALID_YAML);
        assertEquals(ActionStatus.INVALID_YAML, result.right().value());
    }

    @Test
    public void testGivenHeatEnvType_WhenCheckIfHeatRelated_ThenResultTrue() {
        assertTrue(HEAT_ENV.isHeatRelated());
    }

    @Test
    public void testGivenValidYamlForYamlType_WhenCheckValid_ThenResultTrue() {
        byte[] input = "validYaml: yes".getBytes();
        Either<Boolean, ActionStatus> result = YAML.isValid(input);
        assertTrue(result.left().value());
    }

    @Test
    public void testGivenInvalidYamlContentForYamlType_WhenCheckValid_ThenInvalidYamlResultReturned() {
        Either<Boolean, ActionStatus> result = YAML.isValid(INVALID_YAML);
        assertEquals(ActionStatus.INVALID_YAML, result.right().value());
    }

    @Test
    public void testGivenYamlType_WhenCheckIfHeatRelated_ThenResultFalse() {
        assertFalse(YAML.isHeatRelated());
    }

    @Test
    public void testGivenValidJsonForJsonType_WhenCheckValid_ThenResultTrue() {
        byte[] input = "{\"validYaml\": \"yes\"}".getBytes();
        Either<Boolean, ActionStatus> result = JSON.isValid(input);
        assertTrue(result.left().value());
    }

    @Test
    public void testGivenInvalidJsonContentForJsonType_WhenCheckValid_ThenInvalidJsonResultReturned() {
        byte[] input = "invalidJson,with,extraCommas".getBytes();
        Either<Boolean, ActionStatus> result = JSON.isValid(input);
        assertEquals(ActionStatus.INVALID_JSON, result.right().value());
    }

    @Test
    public void testGivenJsonType_WhenCheckIfHeatRelated_ThenResultFalse() {
        assertFalse(JSON.isHeatRelated());
    }

    @Test
    public void testGivenValidXmlForXmlType_WhenCheckValid_ThenResultTrue() {
        byte[] input = "<root>validXml</root>".getBytes();
        Either<Boolean, ActionStatus> result = XML.isValid(input);
        assertTrue(result.left().value());
    }

    @Test
    public void testGivenInvalidXmlContentForXmlType_WhenCheckValid_ThenInvalidXmlResultReturned() {
        byte[] input = "<root>inValidXmlWithNoClosingTag".getBytes();
        Either<Boolean, ActionStatus> result = XML.isValid(input);
        assertEquals(ActionStatus.INVALID_XML, result.right().value());
    }

    @Test
    public void testGivenXmlType_WhenCheckIfHeatRelated_ThenResultFalse() {
        assertFalse(XML.isHeatRelated());
    }

    @Test
    public void testGivenAnyInputForNotDefinedType_WhenCheckValid_ThenResultTrue() {
        byte[] input = "Any input can be defined here <<<<<:::::::////////".getBytes();
        Either<Boolean, ActionStatus> result = NOT_DEFINED.isValid(input);
        assertTrue(result.left().value());
    }

    @Test
    public void testGivenNotDefinedType_WhenCheckIfHeatRelated_ThenResultFalse() {
        assertFalse(NOT_DEFINED.isHeatRelated());
    }
}