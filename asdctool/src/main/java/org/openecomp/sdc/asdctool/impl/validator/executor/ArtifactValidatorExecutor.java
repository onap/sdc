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
package org.openecomp.sdc.asdctool.impl.validator.executor;

import static java.nio.charset.StandardCharsets.UTF_8;

import fj.data.Either;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

public abstract class ArtifactValidatorExecutor {

    private static final Logger log = Logger.getLogger(ArtifactValidatorExecutor.class);
    private final JanusGraphDao janusGraphDao;
    private final ToscaOperationFacade toscaOperationFacade;
    private final String name;

    protected ArtifactValidatorExecutor(JanusGraphDao janusGraphDao, ToscaOperationFacade toscaOperationFacade, String name) {
        this.janusGraphDao = janusGraphDao;
        this.toscaOperationFacade = toscaOperationFacade;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, List<Component>> getVerticesToValidate(VertexTypeEnum type, Map<GraphPropertyEnum, Object> hasProps) {
        Map<String, List<Component>> result = new HashMap<>();
        Either<List<GraphVertex>, JanusGraphOperationStatus> resultsEither = janusGraphDao.getByCriteria(type, hasProps);
        if (resultsEither.isRight()) {
            log.error("getVerticesToValidate failed " + resultsEither.right().value());
            return result;
        }
        log.info("getVerticesToValidate: {} vertices to scan", resultsEither.left().value().size());
        List<GraphVertex> componentsList = resultsEither.left().value();
        componentsList.forEach(vertex -> {
            String ivariantUuid = (String) vertex.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID);
            if (!result.containsKey(ivariantUuid)) {
                List<Component> compList = new ArrayList<>();
                result.put(ivariantUuid, compList);
            }
            List<Component> compList = result.get(ivariantUuid);
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreArtifacts(false);
            Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaElement(vertex.getUniqueId(), filter);
            if (toscaElement.isRight()) {
                log.error("getVerticesToValidate: failed to find element" + vertex.getUniqueId() + " staus is" + toscaElement.right().value());
            } else {
                compList.add(toscaElement.left().value());
            }
        });
        return result;
    }

    public boolean validate(Map<String, List<Component>> vertices, String outputFilePath) {
        boolean result = true;
        long time = System.currentTimeMillis();
        try (Writer writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(File.createTempFile(outputFilePath + this.getName(), "" + time)), UTF_8))) {
            writer.write("name, UUID, invariantUUID, state, version\n");
            Collection<List<Component>> collection = vertices.values();
            for (List<Component> compList : collection) {
                Set<String> artifactEsId = new HashSet<>();
                for (Component component : compList) {
                    Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
                    Optional<ArtifactDefinition> op = toscaArtifacts.values().stream().filter(a -> artifactEsId.contains(a.getEsId())).findAny();
                    if (op.isPresent()) {
                        result = false;
                        writeModuleResultToFile(writer, compList);
                        writer.flush();
                        break;
                    } else {
                        artifactEsId.addAll(toscaArtifacts.values().stream().map(ArtifactDefinition::getEsId).collect(Collectors.toList()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch vf resources ", e);
            return false;
        } finally {
            janusGraphDao.commit();
        }
        return result;
    }

    private void writeModuleResultToFile(Writer writer, List<Component> components) {
        try {
            // "service name, service id, state, version
            for (Component component : components) {
                String sb =
                    component.getName() + "," + component.getUniqueId() + "," + component.getInvariantUUID() + "," + component.getLifecycleState()
                        + "," + component.getVersion() + "\n";
                writer.write(sb);
            }
        } catch (IOException e) {
            log.error("Failed to write module result to file ", e);
        }
    }
}
