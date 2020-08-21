/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.tosca.yaml;

import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Handles a Tosca Template YAML parsing
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ToscaTemplateYamlGenerator {

    private final ToscaTemplate toscaTemplate;
    private final Representer representer;
    private final DumperOptions dumperOptions;

    public ToscaTemplateYamlGenerator(final ToscaTemplate toscaTemplate) {
        this.toscaTemplate = toscaTemplate;
        this.representer = new NsdTemplateRepresenter();
        initRepresenter();
        this.dumperOptions = new DumperOptions();
        initDumperOptions();
    }

    /**
     * Parses the ToscaTemplate to a String YAML.
     *
     * @return the YAML representing the ToscaTemplate
     */
    public String parseToYamlString() {
        final Yaml yaml = new Yaml(representer, dumperOptions);
        return yaml.dumpAsMap(toscaTemplate);
    }

    private void initDumperOptions() {
        dumperOptions.setAllowReadOnlyProperties(false);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(FlowStyle.FLOW);
        dumperOptions.setCanonical(false);
    }

    private void initRepresenter() {
        representer.addClassTag(toscaTemplate.getClass(), Tag.MAP);
        representer.setPropertyUtils(new UnsortedPropertyUtils());
    }

}
