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

import org.openecomp.sdc.be.resources.data.togglz.ToggleableFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Component
public class ToggleConfiguration implements TogglzConfig {
    @Autowired
    private CassandraCustomStateRepository cassandraCustomStateRepository;

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return ToggleableFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        return new CachingStateRepository(cassandraCustomStateRepository, 10000);
    }

    @Override
    public UserProvider getUserProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }
}