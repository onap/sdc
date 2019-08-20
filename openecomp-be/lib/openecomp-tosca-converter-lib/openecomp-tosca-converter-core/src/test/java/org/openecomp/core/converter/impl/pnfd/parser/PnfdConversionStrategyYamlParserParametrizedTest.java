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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;
import org.openecomp.core.converter.impl.pnfd.strategy.ReplaceConversionStrategy;
import org.openecomp.core.util.YamlTestUtil;

@RunWith(Parameterized.class)
public class PnfdConversionStrategyYamlParserParametrizedTest {

    private final String strategyYamlFilePath;
    private final Class expectedStrategyClass;

    public PnfdConversionStrategyYamlParserParametrizedTest(final String strategyYamlFilePath, final Class expectedStrategyClass) {
        this.strategyYamlFilePath = strategyYamlFilePath;
        this.expectedStrategyClass = expectedStrategyClass;
    }

    @Parameters(name = "Run {index} for {0}, expecting {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"transformation/strategy/replaceStrategy.yaml", ReplaceConversionStrategy.class}
        });
    }

    @Test
    public void shouldBuildTheExpectedStrategy() {
        final Object replaceStrategyYaml = YamlTestUtil.readOrFail(strategyYamlFilePath);
        final Optional<PnfdConversionStrategy> actualStrategy = PnfdConversionStrategyYamlParser
            .parse((Map<String, Object>) replaceStrategyYaml);

        assertThat("The strategy should have been built"
            , actualStrategy.isPresent(), is(true));
        assertThat("The built strategy should be " + expectedStrategyClass.getName()
            , actualStrategy.get(), instanceOf(expectedStrategyClass));
    }

}