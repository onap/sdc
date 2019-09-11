/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.core.converter.impl.pnfd;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.pnfd.PnfdTransformationEngine;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationBlock;
import org.openecomp.core.converter.pnfd.model.TransformationDescription;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Engine that manages the PNF Descriptor transformation process.
 */
public abstract class AbstractPnfdTransformationEngine implements PnfdTransformationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPnfdTransformationEngine.class);

    protected final ServiceTemplate templateTo;
    protected final ServiceTemplateReaderService templateFrom;
    private final PnfdTransformationDescriptorReader pnfdTransformationDescriptorReader =
        new PnfdTransformationDescriptorReader();
    protected TransformationDescription transformationDescription;
    protected Map<TransformationBlock, List<Transformation>> transformationGroupByBlockMap;
    private final String descriptorResourcePath;

    public AbstractPnfdTransformationEngine(final ServiceTemplateReaderService templateFrom,
                                            final ServiceTemplate templateTo) {
        this(templateFrom, templateTo, "pnfdTransformationTemplate/model-driven-conversion.yaml");
    }

    //used for tests purposes
    AbstractPnfdTransformationEngine(final ServiceTemplateReaderService templateFrom,
                                     final ServiceTemplate templateTo,
                                     final String descriptorResourcePath) {
        this.templateFrom = templateFrom;
        this.templateTo = templateTo;
        this.descriptorResourcePath = descriptorResourcePath;
    }

    /**
     * Reads the transformation description yaml file.
     */
    protected void readDefinition() {
        transformationDescription = pnfdTransformationDescriptorReader.parse(getDefinitionFileInputStream());
    }

    /**
     * Gets the transformation definition yaml file path.
     * @return The transformation definition yaml path.
     */
    private InputStream getDefinitionFileInputStream() {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(descriptorResourcePath);
        if (resourceAsStream  == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("Could not find resource '%s'", descriptorResourcePath));
            }
            return null;
        }
        return resourceAsStream;
    }

    /**
     * Executes all transformations specified in the descriptor.
     */
    protected abstract void executeTransformations();

}
