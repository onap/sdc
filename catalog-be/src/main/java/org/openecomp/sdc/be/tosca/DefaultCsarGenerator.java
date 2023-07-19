/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.tosca;

import fj.data.Either;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.plugins.CsarZipGenerator;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generates a Network Service CSAR based on a SERVICE component and wraps it in a SDC CSAR entry.
 */
@org.springframework.stereotype.Component("defaultCsarGenerator")
public class DefaultCsarGenerator implements CsarZipGenerator {

    private static final String DEFINITIONS_PATH = "Definitions/";
    public static final String ARTIFACTS_PATH = "Artifacts/";
    private final CommonCsarGenerator commonCsarGenerator;

    @Autowired
    public DefaultCsarGenerator(
        final CommonCsarGenerator commonCsarGenerator) {
        this.commonCsarGenerator = commonCsarGenerator;
    }

    @Override
    public Either<ZipOutputStream, ResponseFormat> generateCsarZip(
        Component component,
        boolean getFromCS,
        ZipOutputStream zip,
        boolean isInCertificationRequest,
        boolean isAsdPackage
    ) throws IOException {
        return commonCsarGenerator.generateCsarZip(component, getFromCS, zip, isInCertificationRequest, isAsdPackage, DEFINITIONS_PATH,
            true, false);
    }

    @Override
    public String getModel() {
        return null;
    }

}
