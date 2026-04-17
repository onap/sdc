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


import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.Transient;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;


import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.togglz.ToggleableFeature;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

@Getter
@Setter
@NoArgsConstructor
@Entity(defaultKeyspace = AuditingTypesConstants.REPO_KEYSPACE)
@CqlName(AuditingTypesConstants.FEATURE_TOGGLE_STATE)
public class FeatureToggleEvent {

    private static final Logger logger = Logger.getLogger(FeatureToggleEvent.class);
    @PartitionKey
    @CqlName("feature_name")
    private String featureName;
    @CqlName("enabled")
    private String enabled;
    @CqlName("strategy_id")
    private String strategyId;
    @CqlName("parameters")
    private String parameters;

    public FeatureToggleEvent(FeatureState featureState) {
        this();
        this.featureName = featureState.getFeature().name();
        this.enabled = String.valueOf(featureState.isEnabled());
        this.strategyId = featureState.getStrategyId();
        this.parameters = Joiner.on(",").withKeyValueSeparator("=").join(featureState.getParameterMap());
    }

    @Transient
    public FeatureState getFeatureState() {
        Feature feature = ToggleableFeature.getFeatureByName(featureName);
        if (feature == null) {
            return null;
        }
        FeatureState featureState = new FeatureState(feature, Boolean.valueOf(enabled));
        featureState.setStrategyId(strategyId);
        setParameters(featureState);
        return featureState;
    }

    private void setParameters(FeatureState featureState) {
        try {
            Map<String, String> paramMap = Splitter.on(",").withKeyValueSeparator("=").split(parameters);
            paramMap.keySet().forEach(p -> featureState.setParameter(p, paramMap.get(p)));
        } catch (IllegalArgumentException e) {
            logger.warn(EcompLoggerErrorCode.DATA_ERROR, "FeatureToggle", "FeatureState Object generating", e.getMessage());
        }
    }
}
