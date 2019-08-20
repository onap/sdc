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

package org.openecomp.core.converter.impl.pnfd.factory;

import java.util.Optional;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.impl.pnfd.parser.PnfdInputBlockParser;
import org.openecomp.core.converter.impl.pnfd.parser.PnfdNodeTemplateBlockParser;
import org.openecomp.core.converter.pnfd.parser.PnfdBlockParser;

/**
 * Factory for {@link PnfdBlockParser}.
 */
public class PnfdBlockParserFactory {
    private static PnfdBlockParserFactory instance;

    private PnfdBlockParserFactory() {
    }

    public static PnfdBlockParserFactory getInstance() {
        if (instance == null) {
            instance = new PnfdBlockParserFactory();
        }

        return instance;
    }

    public Optional<PnfdBlockParser> get(final Transformation transformation) {
        switch (transformation.getBlock()) {
            case NODE_TEMPLATE:
                return Optional.of(new PnfdNodeTemplateBlockParser(transformation));
            case INPUT:
            case GET_INPUT_FUNCTION:
                return Optional.of(new PnfdInputBlockParser(transformation));
            default:
                return Optional.empty();
        }
    }

}
