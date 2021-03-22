/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.enrichment.impl.external.artifact;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.enrichment.types.ComponentProcessInfo;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.enrichment.inter.ExternalArtifactEnricherInterface;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ProcessArtifactEnricher implements ExternalArtifactEnricherInterface {

    private ComponentDao componentDao;
    private ProcessDao processDao;
    private EnrichedServiceModelDao enrichedServiceModelDao;

    @Override
    public Map<String, List<ErrorMessage>> enrich(EnrichmentInfo enrichmentInfo, ToscaServiceModel serviceModel) throws IOException {
        Map<String, List<ErrorMessage>> errors = new HashMap<>();
        String vspId = enrichmentInfo.getKey();
        Version version = enrichmentInfo.getVersion();
        Collection<ComponentEntity> components = getComponentDao().list(new ComponentEntity(vspId, version, null));
        components.forEach(componentEntry -> errors.putAll(enrichComponent(componentEntry, vspId, version)));
        return errors;
    }

    Map<String, List<ErrorMessage>> enrichComponent(ComponentEntity componentEntry, String vspId, Version version) {
        Map<String, List<ErrorMessage>> errors = new HashMap<>();
        enrichComponentProcessArtifact(componentEntry, vspId, version, errors);
        return errors;
    }

    void enrichComponentProcessArtifact(ComponentEntity componentEntity, String vspId, Version version, Map<String, List<ErrorMessage>> errors) {
        String componentId = componentEntity.getId();
        ProcessEntity processEntity = new ProcessEntity(vspId, version, componentId, null);
        final Collection<ProcessEntity> processes = getProcessDao().list(processEntity);
        processes.forEach(entity -> {
            ProcessEntity artifactEntity = new ProcessEntity(vspId, version, componentId, entity.getId());
            ProcessEntity artifactProcessEntity = getProcessDao().getArtifact(artifactEntity);
            if (artifactProcessEntity != null && ProcessType.Lifecycle_Operations.equals(artifactProcessEntity.getType())
                && artifactProcessEntity.getArtifactName() != null) {
                String componentName = componentEntity.getComponentCompositionData().getName();
                String path = componentName + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName() + File.separator + "Lifecycle Operations"
                    + File.separator + artifactProcessEntity.getArtifactName();
                ComponentProcessInfo componentProcessInfo = new ComponentProcessInfo();
                componentProcessInfo.setName(path);
                componentProcessInfo.setContent(artifactProcessEntity.getArtifact().array());
                ServiceArtifact processServiceArtifact = new ServiceArtifact();
                processServiceArtifact.setVspId(vspId);
                processServiceArtifact.setVersion(version);
                enrichServiceArtifact(componentProcessInfo, processServiceArtifact);
            }
        });
    }

    void enrichServiceArtifact(ComponentProcessInfo componentProcessInfo, ServiceArtifact processServiceArtifact) {
        processServiceArtifact.setName(componentProcessInfo.getName());
        processServiceArtifact.setContentData(FileUtils.toByteArray(componentProcessInfo.getContent()));
        getEnrichedServiceModelDao().storeExternalArtifact(processServiceArtifact);
    }

    private ComponentDao getComponentDao() {
        if (componentDao == null) {
            componentDao = ComponentDaoFactory.getInstance().createInterface();
        }
        return componentDao;
    }

    private ProcessDao getProcessDao() {
        if (processDao == null) {
            processDao = ProcessDaoFactory.getInstance().createInterface();
        }
        return processDao;
    }

    private EnrichedServiceModelDao getEnrichedServiceModelDao() {
        if (enrichedServiceModelDao == null) {
            enrichedServiceModelDao = EnrichedServiceModelDaoFactory.getInstance().createInterface();
        }
        return enrichedServiceModelDao;
    }
}
