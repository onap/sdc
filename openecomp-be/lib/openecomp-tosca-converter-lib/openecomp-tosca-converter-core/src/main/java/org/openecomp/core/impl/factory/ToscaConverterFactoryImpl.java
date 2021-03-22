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
package org.openecomp.core.impl.factory;

import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.converter.factory.ToscaConverterFactory;
import org.openecomp.core.impl.ToscaConverterImpl;

public class ToscaConverterFactoryImpl extends ToscaConverterFactory {

    @Override
    public ToscaConverter createInterface() {
        return new ToscaConverterImpl();
    }
}
