/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.ecomp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * The application context for the portal API implementations, which the portal SDK instantiates by class name rather
 * than through Spring.
 */
public class PortalApiApplicationContext implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    static ApplicationContext get() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        setContext(context);
    }

    private static void setContext(ApplicationContext context) {
        applicationContext = context;
    }
}
