/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
package org.onap.sdc.tosca.services;

import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;

public class StrictMapAppenderConstructor extends Constructor {

    /**
     * Instantiates a new Strict map appender constructor.
     *
     * @param theRoot the the root
     */
    public StrictMapAppenderConstructor(Class<?> theRoot, LoaderOptions loaderOptions) {
        super(theRoot, loaderOptions);
    }

    @Override
    protected Map<Object, Object> createDefaultMap(int initSize) {
        return new StrictMap(super.createDefaultMap(initSize));
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
        try {
            return super.constructMapping(node);
        } catch (IllegalStateException exception) {
            throw new ParserException("while parsing MappingNode", node.getStartMark(), exception.getMessage(), node.getEndMark());
        }
    }

}
