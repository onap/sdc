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

package org.openecomp.core.converter.impl.pnfd.parser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.QUERY;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.TO_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.Test;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.ConversionStrategyType;
import org.openecomp.core.util.TestResourcesUtil;
import org.openecomp.core.util.YamlTestUtil;

public class ConversionDefinitionYamlParserTest {

    @Test
    public void shouldBuildDefinition() {
        final Map<String, Object> definitionYaml;
        final String definitionYamlFilePath = "transformation/conversionDefinition/conversionDefinitionWithReplaceStrategy.yaml";
        try (final InputStream resourceInputStream = TestResourcesUtil.getFileResourceAsStream(definitionYamlFilePath)) {
            definitionYaml = (Map<String, Object>) YamlTestUtil.read(resourceInputStream);
        } catch (final IOException e) {
            fail(String.format("Could not load %s", definitionYamlFilePath));
            return;
        }
        final ConversionDefinition conversionDefinition = ConversionDefinitionYamlParser.parse(definitionYaml);
        assertConversionDefinition(definitionYaml, conversionDefinition);
    }

    private void assertConversionDefinition(final Map<String, Object> definitionYaml,
            final ConversionDefinition conversionDefinition) {
        assertThat("The conversion definition should have been built"
            , conversionDefinition, notNullValue());
        assertThat("Should have initialized the conversion definition query"
            , conversionDefinition.getConversionQuery(), notNullValue());
        assertThat("The conversion definition should have been built"
            , conversionDefinition.getConversionQuery().getQuery(), equalTo(definitionYaml.get(QUERY.getName())));
        assertThat("Should have initialized the conversion definition to attribute name"
            , conversionDefinition.getToAttributeName(), equalTo(definitionYaml.get(TO_NAME.getName())));
        assertThat("Should have initialized the conversion definition strategy"
            , conversionDefinition.getPnfdConversionStrategy(), notNullValue());
        assertThat("Should have the expected strategy"
            , conversionDefinition.getPnfdConversionStrategy().getStrategyType(), equalTo(ConversionStrategyType.REPLACE));
    }
}