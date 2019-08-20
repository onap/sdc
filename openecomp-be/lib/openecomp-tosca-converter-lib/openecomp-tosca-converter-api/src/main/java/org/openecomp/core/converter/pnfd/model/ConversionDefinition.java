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

package org.openecomp.core.converter.pnfd.model;

import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;

public class ConversionDefinition {

    private ConversionQuery conversionQuery;
    private String toAttributeName;
    private PnfdConversionStrategy pnfdConversionStrategy;
    private String toGetInput;

    public ConversionDefinition(final ConversionQuery conversionQuery, final String toAttributeName,
        final PnfdConversionStrategy pnfdConversionStrategy, final String toGetInput) {
        this.conversionQuery = conversionQuery;
        this.toAttributeName = toAttributeName;
        this.pnfdConversionStrategy = pnfdConversionStrategy;
        this.toGetInput = toGetInput;
    }

    public ConversionQuery getConversionQuery() {
        return conversionQuery;
    }

    public String getToAttributeName() {
        return toAttributeName;
    }

    public PnfdConversionStrategy getPnfdConversionStrategy() {
        return pnfdConversionStrategy;
    }

    public String getToGetInput() {
        return toGetInput;
    }
}
