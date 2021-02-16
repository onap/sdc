 
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
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.factory;

import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.NsDescriptorGenerator;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.NsDescriptorGeneratorImpl;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.tosca.yaml.ToscaTemplateYamlGenerator;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class NsDescriptorGeneratorFactory {

    private final ToscaExportHandler toscaExportHandler;
    private final ObjectProvider<ToscaTemplateYamlGenerator> toscaTemplateYamlGeneratorProvider;
    private final ObjectProvider<NsDescriptorGeneratorImpl> nsDescriptorGeneratorProvider;

    public NsDescriptorGeneratorFactory(final ToscaExportHandler toscaExportHandler,
                                        final ObjectProvider<ToscaTemplateYamlGenerator> toscaTemplateYamlGeneratorProvider,
                                        final ObjectProvider<NsDescriptorGeneratorImpl> nsDescriptorGeneratorProvider) {
        this.toscaExportHandler = toscaExportHandler;
        this.toscaTemplateYamlGeneratorProvider = toscaTemplateYamlGeneratorProvider;
        this.nsDescriptorGeneratorProvider = nsDescriptorGeneratorProvider;
    }

    public NsDescriptorGenerator create() {
        return nsDescriptorGeneratorProvider.getObject(toscaExportHandler, toscaTemplateYamlGeneratorProvider);
    }
}
