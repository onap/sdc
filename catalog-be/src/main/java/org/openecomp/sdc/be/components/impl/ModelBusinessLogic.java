/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
package org.openecomp.sdc.be.components.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for handling the business logic of a Model.
 */
@Component("modelBusinessLogic")
public class ModelBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelBusinessLogic.class);
    private final ModelOperation modelOperation;

    @Autowired
    public ModelBusinessLogic(final ModelOperation modelOperation) {
        this.modelOperation = modelOperation;
    }

    public Model createModel(final Model model) {
        LOGGER.debug("creating model {}", model);
        return modelOperation.createModel(model, false);
    }

    public Optional<Model> findModel(final String modelName) {
        if (StringUtils.isEmpty(modelName)) {
            return Optional.empty();
        }
        return modelOperation.findModelByName(modelName);
    }

    /**
     * Loads the list of models.
     *
     * @return the list of models
     */
    public List<Model> listModels() {
        return modelOperation.findAllModels();
    }
    
    public List<Model> listModels(final ModelTypeEnum modelType) {
        return modelOperation.findModels(modelType);
    }

    public void createModelImports(final String modelName, final InputStream modelImportsZip) {
        if (StringUtils.isEmpty(modelName)) {
            throw ModelOperationExceptionSupplier.invalidModel(modelName).get();
        }
        if (modelImportsZip == null) {
            throw ModelOperationExceptionSupplier.emptyModelImports().get();
        }
        if (findModel(modelName).isEmpty()) {
            throw ModelOperationExceptionSupplier.invalidModel(modelName).get();
        }

        final var fileBytes = readBytes(modelImportsZip);
        final Map<String, byte[]> zipFilesPathContentMap = unzipInMemory(fileBytes);
        if (zipFilesPathContentMap.isEmpty()) {
            throw ModelOperationExceptionSupplier.emptyModelImports().get();
        }

        modelOperation.createModelImports(modelName, zipFilesPathContentMap);
    }

    private Map<String, byte[]> unzipInMemory(final byte[] fileBytes) {
        try {
            return ZipUtils.readZip(fileBytes, false);
        } catch (final ZipException e) {
            throw ModelOperationExceptionSupplier.couldNotReadImports().get();
        }
    }

    private byte[] readBytes(final InputStream modelImportsZip) {
        try (final InputStream in = modelImportsZip; final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            final var buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toByteArray();
        } catch (final IOException e) {
            LOGGER.debug("Could not read the model imports zip", e);
            throw ModelOperationExceptionSupplier.couldNotReadImports().get();
        }
    }
}
