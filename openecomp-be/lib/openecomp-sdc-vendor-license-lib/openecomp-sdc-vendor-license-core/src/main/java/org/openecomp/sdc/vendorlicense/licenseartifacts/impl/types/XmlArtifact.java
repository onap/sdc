/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.errors.JsonErrorBuilder;

public abstract class XmlArtifact {

    XmlMapper xmlMapper = new XmlMapper();

    abstract void initMapper();

    /**
     * To xml string.
     *
     * @return the string
     */
    public String toXml() {
        initMapper();
        configureXmlMapper(xmlMapper);
        String xml;
        try {
            xml = xmlMapper.writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            final String traceId = currentTraceId();
            System.err.println("[IT_TRACE][XmlArtifact] Failed to serialize XmlArtifact. traceId=" + traceId
                + " type=" + getClass().getName() + " error=" + exception);
            exception.printStackTrace(System.err);
            throw new CoreException(new JsonErrorBuilder("Failed to write xml value as string ").build(), exception);
        }
        return xml.replaceAll(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE, "");
    }

    private static void configureXmlMapper(XmlMapper mapper) {
        if (mapper == null) {
            return;
        }
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static String currentTraceId() {
        try {
            final Class<?> mdcClass = Class.forName("org.slf4j.MDC");
            final var getMethod = mdcClass.getMethod("get", String.class);
            return (String) getMethod.invoke(null, "itTraceId");
        } catch (Exception ignored) {
            return null;
        }
    }
}
