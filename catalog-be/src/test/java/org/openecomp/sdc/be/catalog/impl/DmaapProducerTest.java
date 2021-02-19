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

package org.openecomp.sdc.be.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.openecomp.sdc.be.catalog.enums.ResultStatusEnum.FAIL;
import static org.openecomp.sdc.be.catalog.enums.ResultStatusEnum.SERVICE_DISABLED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.catalog.api.IComponentMessage;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.components.distribution.engine.DmaapClientFactory;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapProducerConfiguration;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:application-context-test.xml")
class DmaapProducerTest {

    private static final Logger LOG = LoggerFactory.getLogger(DmaapProducerTest.class);

    private ConfigurationManager configurationManager;
    private DmaapProducer dmaapProducer;
    private DmaapClientFactory dmaapClientFactory;

    @BeforeEach
    public void setup() {
        final ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(),
            "src/test/resources/config/catalog-be");
        configurationManager = new ConfigurationManager(configurationSource);
        final Configuration configuration = new Configuration();
        final DmaapProducerConfiguration dmaapProducerConfiguration = new DmaapProducerConfiguration();
        dmaapProducerConfiguration.setActive(true);
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        configurationManager.getConfiguration().setDmaapProducerConfiguration(dmaapProducerConfiguration);
        ExternalConfiguration.setAppName("catalog-be");
        dmaapProducer = new DmaapProducer(mock(DmaapClientFactory.class), mock(DmaapProducerHealth.class));
    }

    @Test
    void pushComponentMessageTest() {
        boolean oldVal = isActive();
        final Resource resource = new Resource();
        resource.setUniqueId("mockUniqueId");
        resource.setUUID("mockNameUUID");
        resource.setResourceType(ResourceTypeEnum.VF);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setName("mockName");
        resource.setSystemName("mockSystemName");
        resource.setVersion("1.0");
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setLastUpdateDate(System.currentTimeMillis());
        resource.setInvariantUUID("mockInvariantUUID");
        resource.setDescription("mockDescription");
        resource.setHighestVersion(true);
        resource.setArchived(false);

        final List<CategoryDefinition> categories = new ArrayList<>();
        final CategoryDefinition categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName("mockCategory");
        categoryDefinition.setNormalizedName("mockCategory");
        categoryDefinition.setUniqueId("uniqueId");
        final SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
        subCategoryDefinition.setName("mockSubCategory");
        subCategoryDefinition.setNormalizedName("mockSubCategory");
        subCategoryDefinition.setUniqueId("mockSubCategoryUniqueId");
        categoryDefinition.addSubCategory(subCategoryDefinition);
        categories.add(categoryDefinition);
        resource.setCategories(categories);

        final IComponentMessage message = new ComponentMessage(resource, ChangeTypeEnum.LIFECYCLE,
            new CatalogUpdateTimestamp(123, 1234));
        try {
            LOG.info("[DmaapProducer] pushing message =>{}", new ObjectMapper().writeValueAsString(message));
            //push message by configuration => will send the message if configuration enables
            //will not test network connectivity to avoid build server automation fail
            toggleInactive();
            IStatus status = dmaapProducer.pushMessage(message);
            assertThat(status.getResultStatus()).isEqualTo(SERVICE_DISABLED);
            toggleActive();
            dmaapProducer.shutdown();
            status = dmaapProducer.pushMessage(message);
            assertThat(status.getResultStatus()).isEqualTo(FAIL);
        } catch (final JsonProcessingException e) {
            fail("'JsonProcessingException' detected!", e);
        } catch (final Exception e) {
            fail("Unknown exception detected", e);
        } finally {
            configurationManager.getConfiguration().getDmaapProducerConfiguration().setActive(oldVal);
        }
    }

    private boolean isActive() {
        return configurationManager.getConfiguration().getDmaapProducerConfiguration().getActive();
    }

    private void toggleInactive() {
        configurationManager.getConfiguration().getDmaapProducerConfiguration().setActive(false);
    }

    private void toggleActive() {
        configurationManager.getConfiguration().getDmaapProducerConfiguration().setActive(true);
    }

}
