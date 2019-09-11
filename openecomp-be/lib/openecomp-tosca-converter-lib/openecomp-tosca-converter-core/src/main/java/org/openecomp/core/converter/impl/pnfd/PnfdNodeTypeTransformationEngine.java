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

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.impl.pnfd.factory.PnfdBlockParserFactory;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationBlock;

/**
 * Engine that manages the PNF Descriptor transformation process for the NodeType block.
 */
public class PnfdNodeTypeTransformationEngine extends AbstractPnfdTransformationEngine {

    public PnfdNodeTypeTransformationEngine(final ServiceTemplateReaderService templateFrom,
                                            final ServiceTemplate templateTo) {
        super(templateFrom, templateTo);
    }

    /**
     * Runs the transformation process.
     */
    @Override
    public void transform() {
        readDefinition();
        executeTransformations();
    }

    /**
     * Execute all transformations specified in the descriptor.
     */
    @Override
    protected void executeTransformations() {
        final Set<Transformation> transformationSet = transformationDescription.getTransformationSet();
        if (CollectionUtils.isEmpty(transformationSet)) {
            return;
        }
        final Set<Transformation> validNodeTypeTransformationSet = transformationSet.stream()
            .filter(transformation -> transformation.getBlock() == TransformationBlock.NODE_TYPE)
            .filter(Transformation::isValid)
            .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(validNodeTypeTransformationSet)) {
            return;
        }
        validNodeTypeTransformationSet.forEach(transformation ->
            PnfdBlockParserFactory.getInstance().get(transformation).ifPresent(pnfdBlockParser ->
                pnfdBlockParser.parse(templateFrom, templateTo)));
    }

}
