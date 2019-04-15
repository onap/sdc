/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.artifact;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public enum PayloadTypeEnum {
    HEAT_YAML {
        @Override
        public Either<Boolean, ActionStatus> isValid(byte[] payload) {
            YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
            if (isNotValidYaml(payload, yamlToObjectConverter)) {
                return Either.right(ActionStatus.INVALID_YAML);
            }

            DeploymentArtifactHeatConfiguration heatConfiguration =
                    yamlToObjectConverter.convert(payload, DeploymentArtifactHeatConfiguration.class);
            if (heatConfiguration == null || heatConfiguration.getHeat_template_version() == null) {
                log.debug("HEAT doesn't contain required \"heat_template_version\" section.");
                return Either.right(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT);
            }
            return Either.left(true);
        }

        @Override
        public boolean isHeatRelated() {
            return true;
        }

        private boolean isNotValidYaml(byte[] payload, YamlToObjectConverter yamlToObjectConverter) {
            return !yamlToObjectConverter.isValidYaml(payload);
        }
    },
    HEAT_ENV {
        @Override
        public Either<Boolean, ActionStatus> isValid(byte[] payload) {
            return isValidYaml(payload);
        }

        @Override
        public boolean isHeatRelated() {
            return true;
        }
    },
    XML {
        @Override
        public Either<Boolean, ActionStatus> isValid(byte[] payload) {
            try {
                XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                setFeatures(reader);
                reader.parse(new InputSource(new ByteArrayInputStream(payload)));
            } catch (ParserConfigurationException | IOException | SAXException exception) {
                log.debug("Xml is invalid : {}", exception.getMessage(), exception);
                return Either.right(ActionStatus.INVALID_XML);
            }
            return Either.left(true);
        }

        private void setFeatures(XMLReader reader) throws SAXNotSupportedException {
            try {
                reader.setFeature("http://apache.org/xml/features/validation/schema", false);
                reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (SAXNotRecognizedException exception) {
                log.debug("Xml parser couldn't set feature: \"http://apache.org/xml/features/validation/schema\", false",
                        exception.getMessage(), exception);
            }
        }
    },
    JSON {
        @Override
        public Either<Boolean, ActionStatus> isValid(byte[] payload) {
            try {
                gson.fromJson(new String(payload), Object.class);
            } catch (Exception e) {
                log.debug("Json is invalid : {}", e.getMessage(), e);
                return Either.right(ActionStatus.INVALID_JSON);
            }
            return Either.left(true);
        }
    },
    YAML {
        @Override
        public Either<Boolean, ActionStatus> isValid(byte[] payload) {
            return isValidYaml(payload);
        }
    },
    NOT_DEFINED {
        @Override
        public Either<Boolean, ActionStatus> isValid(byte[] payload) {
            return Either.left(true);
        }
    };

    private static final Logger log = Logger.getLogger(PayloadTypeEnum.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public abstract Either<Boolean, ActionStatus> isValid(byte[] payload);

    public boolean isHeatRelated() {
        return false;
    }

    private static Either<Boolean, ActionStatus> isValidYaml(byte[] payload) {
        YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
        if (yamlToObjectConverter.isValidYaml(payload)) {
            log.debug("Invalid YAML format");
            return Either.left(true);
        }
        return Either.right(ActionStatus.INVALID_YAML);
    }

}
