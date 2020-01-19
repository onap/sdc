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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.catalog.api.IComponentMessage;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openecomp.sdc.be.catalog.enums.ResultStatusEnum.FAIL;
import static org.openecomp.sdc.be.catalog.enums.ResultStatusEnum.SERVICE_DISABLED;
import static org.openecomp.sdc.be.config.ConfigurationManager.getConfigurationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class DmaapProducerTest {
    private static final Logger LOG = LoggerFactory.getLogger(DmaapProducer.class);

    @Autowired
    ConfigurationManager configurationManager;
 
    @Autowired
    private DmaapProducer dmaapProducer;
   

    //actually sends the message
    
    
    @Test
    public void pushComponentMessageTest() {
        boolean oldVal = isActive();

        Resource resource = new Resource();
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

        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition cat = new CategoryDefinition();
        cat.setName("mockCategory");
        cat.setNormalizedName("mockCategory");
        cat.setUniqueId("uniqueId");
        SubCategoryDefinition subcat = new SubCategoryDefinition();
        subcat.setName("mockSubCategory");
        subcat.setNormalizedName("mockSubCategory");
        subcat.setUniqueId("mockSubCategoryUniqueId");
        cat.addSubCategory(subcat);
        categories.add(cat);
        resource.setCategories(categories);

        IComponentMessage message = new ComponentMessage(resource, ChangeTypeEnum.LIFECYCLE, new CatalogUpdateTimestamp(123, 1234));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(message);
            LOG.info("[DmaapProducer] pushing message =>{}",jsonInString);
            //push msg by configuration => will send the message if configuration enables
            //IStatus status = dmaapProducer.pushComponentMessage(message);
            //will not test network connectivity to avoid build server automation fail

            toggleInactive();
            IStatus status = dmaapProducer.pushMessage(message);
            assertThat(status.getResultStatus()).isEqualTo(SERVICE_DISABLED);

            toggleActive();
            dmaapProducer.shutdown();
            status = dmaapProducer.pushMessage(message);
            assertThat(status.getResultStatus()).isEqualTo(FAIL);

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            getConfigurationManager().getConfiguration().getDmaapProducerConfiguration().setActive(oldVal);
        }
    }

    private boolean isActive(){
        return getConfigurationManager().getConfiguration().getDmaapProducerConfiguration().getActive();
    }

    private void toggleInactive(){
        getConfigurationManager().getConfiguration().getDmaapProducerConfiguration().setActive(false);
    }

    private void toggleActive(){
        getConfigurationManager().getConfiguration().getDmaapProducerConfiguration().setActive(true);
    }

}