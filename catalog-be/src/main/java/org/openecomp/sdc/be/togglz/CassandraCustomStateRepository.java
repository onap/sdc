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

package org.openecomp.sdc.be.togglz;

import com.google.common.annotations.VisibleForTesting;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.FeatureToggleDao;
import org.openecomp.sdc.be.resources.data.togglz.FeatureToggleEvent;
import org.openecomp.sdc.be.resources.data.togglz.ToggleableFeature;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CassandraCustomStateRepository implements StateRepository {

    private final static Logger logger = Logger.getLogger(CassandraCustomStateRepository.class);
    private final FeatureToggleDao featureToggleDao;

    public CassandraCustomStateRepository(FeatureToggleDao featureToggleDao) {
        this.featureToggleDao = featureToggleDao;
    }

    @PostConstruct
    private void init() {
        removeUnusedItems();
    }

    @VisibleForTesting
    void removeUnusedItems() {
        List<FeatureToggleEvent> allEvents = featureToggleDao.getAllFeatures();

        List<FeatureToggleEvent> eventsToDelete = allEvents.stream()
                .filter(e-> ToggleableFeature.getFeatureByName(e.getFeatureName()) == null)
                .collect(Collectors.toList());
        if (!eventsToDelete.isEmpty()) {
            logger.debug("Found Feature toggles not in use [{}], they will be deleted",
                    eventsToDelete.stream().map(FeatureToggleEvent::getFeatureName).collect(Collectors.toList()));
        }
        eventsToDelete.forEach(e->featureToggleDao.delete(e.getFeatureName()));
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        logger.debug("getFeatureState=> Request is received for a Feature {}", feature);
        if (feature == null) {
            throw new IllegalArgumentException("Feature object is null");
        }
        FeatureState state = null;
        FeatureToggleEvent event = featureToggleDao.get(feature.name());

        if (event != null) {
            state = event.getFeatureState();
            logger.debug("State of feature {} is {}", feature, state.getFeature());
        }
        return state;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        if (featureState == null) {
            throw new IllegalArgumentException("FeatureState object is null");
        }
        CassandraOperationStatus status = featureToggleDao.save(new FeatureToggleEvent(featureState));
        logger.debug("setFeatureState=> FeatureState {} is set with status {}", featureState.getFeature(), status);
    }

}
