/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.resources.data.togglz;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openecomp.sdc.be.togglz.ToggleableFeature;
import org.togglz.core.repository.FeatureState;

public class FeatureToggleEventTest {
    private final String strategyId = "123456";
    private final String param1 = "param1";
    private final String param1Value = "param1Value";
    private final String param2 = "param2";
    private final String param2Value = "param2Value";

    @Test
    public void createEventFromFeatureStateObject() {
        final String param3 = "param3";
        final String param3Value = "param3Value";
        final String responseParams = "param3=param3Value,param1=param1Value,param2=param2Value";


        FeatureState featureState = new FeatureState(ToggleableFeature.DEFAULT_FEATURE, true);
        featureState.setStrategyId(strategyId)
                .setParameter(param1, param1Value)
                .setParameter(param2, param2Value)
                .setParameter(param3, param3Value);

        FeatureToggleEvent featureToggleEvent = new FeatureToggleEvent(featureState);
        assertThat(featureToggleEvent.getEnabled(), is("true"));
        assertThat(featureToggleEvent.getStrategyId(), is(strategyId));
        assertThat(featureToggleEvent.getParameters(), is(responseParams));
    }

    @Test
    public void createEventWhenStrategyIsNotSetAndStateIsFalse() {
        FeatureState featureState = new FeatureState(ToggleableFeature.DEFAULT_FEATURE, false);

        FeatureToggleEvent featureToggleEvent = new FeatureToggleEvent(featureState);
        assertThat(featureToggleEvent.getEnabled(), is("false"));
        assertThat(featureToggleEvent.getParameters(), isEmptyString());
        assertThat(featureToggleEvent.getStrategyId(), isEmptyOrNullString());
    }

    @Test
    public void getFeatureStateObjectFromEventWithNoParams() {
        FeatureState featureState = new FeatureState(ToggleableFeature.DEFAULT_FEATURE, true);
        FeatureToggleEvent featureToggleEvent = new FeatureToggleEvent(featureState);
        FeatureState fromEvent = featureToggleEvent.getFeatureState();
        assertThat(ToggleableFeature.DEFAULT_FEATURE, is(fromEvent.getFeature()));
        assertThat(fromEvent.isEnabled(), is(true));
        assertThat(fromEvent.getParameterMap().size(), is(0));
        assertThat(fromEvent.getStrategyId(), nullValue());
    }

    @Test
    public void getFeatureStateObjectFromEventWithParams() {
        FeatureState featureState = new FeatureState(ToggleableFeature.DEFAULT_FEATURE, true);
        featureState.setStrategyId(strategyId)
                .setParameter(param1, param1Value)
                .setParameter(param2, param2Value);
        FeatureToggleEvent featureToggleEvent = new FeatureToggleEvent(featureState);
        FeatureState fromEvent = featureToggleEvent.getFeatureState();
        assertThat(fromEvent.getFeature(), is(ToggleableFeature.DEFAULT_FEATURE));
        assertThat(fromEvent.isEnabled(), is(true));
        assertThat(fromEvent.getStrategyId(), is(strategyId));
        assertThat(fromEvent.getParameterMap().size(), is(2));
        assertThat(fromEvent.getParameterMap().get(param1), is(param1Value));
        assertThat(fromEvent.getParameterMap().get(param2), is(param2Value));
    }

    @Test
    public void getFeatureStateObjectFromEventWhenStateIsWrong() {
        FeatureState featureState = new FeatureState(ToggleableFeature.DEFAULT_FEATURE, true);
        FeatureToggleEvent featureToggleEvent = new FeatureToggleEvent(featureState);
        featureToggleEvent.setFeatureName("wrong");
        assertThat(featureToggleEvent.getFeatureState(), nullValue());
    }

}
