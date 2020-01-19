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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.FeatureToggleDao;
import org.openecomp.sdc.be.resources.data.togglz.FeatureToggleEvent;
import org.openecomp.sdc.be.resources.data.togglz.ToggleableFeature;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CassandraCustomStateRepositoryTest {
    private final String strategyId = "strategyId";
    private final String paramName1 = "paramName1";
    private final String paramName2 = "paramName2";
    private final String paramVal1 = "paramVal1";
    private final String paramVal2 = "paramVal2";

    @Mock
    private FeatureToggleDao featureToggleDao;

    @InjectMocks
    private CassandraCustomStateRepository cassandraRepo;

    @Before
    public void setUp() {
        cassandraRepo = new CassandraCustomStateRepository(featureToggleDao);
    }

    @Test
    public void getFeatureStateSuccess() {
        FeatureState stateToReturn = new FeatureState(ToggleableFeature.DEFAULT_FEATURE, true);
        when(featureToggleDao.get(any())).thenReturn(new FeatureToggleEvent(stateToReturn));
        FeatureState state = cassandraRepo.getFeatureState(ToggleableFeature.DEFAULT_FEATURE);
        assertEquals(state.getFeature().name(), stateToReturn.getFeature().name());
        assertTrue(state.isEnabled());
        assertNull(state.getStrategyId());
        assertEquals(0, state.getParameterMap().size());
    }

    @Test
    public void getFeatureStateWithParamsSuccess() {
        when(featureToggleDao.get(any())).thenReturn(createEvent(ToggleableFeature.DEFAULT_FEATURE));
        FeatureState state = cassandraRepo.getFeatureState(ToggleableFeature.DEFAULT_FEATURE);
        assertEquals(state.getFeature().name(), ToggleableFeature.DEFAULT_FEATURE.name());
        assertEquals(strategyId, state.getStrategyId());
        assertEquals(paramVal1, state.getParameter(paramName1));
        assertEquals(paramVal2, state.getParameter(paramName2));
        assertTrue(state.isEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getFeatureStateForFeatureNull() {
        cassandraRepo.getFeatureState(null);
    }

    @Test
    public void getFeatureStateWhenEntryNotFound() {
        when(featureToggleDao.get(any())).thenReturn(null);
        cassandraRepo.getFeatureState(ToggleableFeature.DEFAULT_FEATURE);
    }

    @Test
    public void setFeatureStateSuccess() {
        when(featureToggleDao.save(any())).thenReturn(CassandraOperationStatus.OK);
        cassandraRepo.setFeatureState(new FeatureState(ToggleableFeature.DEFAULT_FEATURE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFeatureStateWhenStateIsNull() {
        cassandraRepo.setFeatureState(null);
    }

    @Test
    public void removeUnusedItems() {
        List<FeatureToggleEvent> allEvents = Arrays.asList(
                createEvent(ToggleableFeature.DEFAULT_FEATURE),
                createEvent(() -> "should be deleted1"),
                createEvent(() -> "should be deleted2"));

        when(featureToggleDao.getAllFeatures()).thenReturn(allEvents);
        cassandraRepo.removeUnusedItems();
        verify(featureToggleDao, times(2)).delete(contains("should be deleted"));
    }

    @Test
    public void removeUnusedItemsWhenNoStatesStored() {
        when(featureToggleDao.getAllFeatures()).thenReturn(Collections.emptyList());
        cassandraRepo.removeUnusedItems();
        verify(featureToggleDao, times(0)).delete(any());
    }

    @Test
    public void removeUnusedItemsWhenOnlyExistingStatesStored() {
        when(featureToggleDao.getAllFeatures()).thenReturn(Collections.singletonList(createEvent(ToggleableFeature.DEFAULT_FEATURE)));
        cassandraRepo.removeUnusedItems();
        verify(featureToggleDao, times(0)).delete(any());
    }

    private FeatureToggleEvent createEvent(Feature feature) {
        FeatureState stateToReturn = new FeatureState(feature, true);
        stateToReturn.setStrategyId(strategyId);
        stateToReturn.setParameter(paramName1, paramVal1);
        stateToReturn.setParameter(paramName2, paramVal2);
        return new FeatureToggleEvent(stateToReturn);
    }
}
