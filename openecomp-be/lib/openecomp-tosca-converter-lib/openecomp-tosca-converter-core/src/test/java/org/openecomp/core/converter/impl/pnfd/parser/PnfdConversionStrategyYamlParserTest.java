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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;
import org.openecomp.core.util.TestResourcesUtil;
import org.openecomp.core.util.YamlTestUtil;

public class PnfdConversionStrategyYamlParserTest {

    @Test
    public void parseInvalidYamlObject() {
        final Object replaceStrategyYaml;
        final String strategyYamlFilePath = "transformation/strategy/strategyMissingStrategyAttribute.yaml";
        try (final InputStream resourceInputStream = TestResourcesUtil.getFileResourceAsStream(strategyYamlFilePath)) {
            replaceStrategyYaml = YamlTestUtil.read(resourceInputStream);
        } catch (final IOException e) {
            fail(String.format("Could not load %s", strategyYamlFilePath));
            return;
        }
        final Optional<PnfdConversionStrategy> actualStrategy = PnfdConversionStrategyYamlParser
            .parse((Map<String, Object>) replaceStrategyYaml);
        assertThat("The strategy should not have been built"
            , actualStrategy.isPresent(), is(false));
    }

}