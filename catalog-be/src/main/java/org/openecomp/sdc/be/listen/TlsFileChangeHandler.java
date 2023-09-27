/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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
package org.openecomp.sdc.be.listen;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class TlsFileChangeHandler extends FileAlterationListenerAdaptor {
    
    private static final Logger LOGGER = Logger.getLogger(TlsFileChangeHandler.class.getName());
    
    @Override
    public void onFileChange(File pFile) {
        final Configuration config  = ConfigurationManager.getConfigurationManager().getConfiguration();
        if (pFile.getAbsolutePath().equals(config.getTlsCert()) || pFile.getAbsolutePath().equals(config.getTlsKey())) {
            handleTlsCertChanged();
        }
        if (pFile.getAbsolutePath().equals(config.getCaCert())) {
            handleCaCertChanged();
        }
    }
    
    private void handleTlsCertChanged() {
        LOGGER.info("TLS cert/key change detected");
    }
    
    private void handleCaCertChanged() {
        LOGGER.info("CA cert change detected");
    }
    
}
