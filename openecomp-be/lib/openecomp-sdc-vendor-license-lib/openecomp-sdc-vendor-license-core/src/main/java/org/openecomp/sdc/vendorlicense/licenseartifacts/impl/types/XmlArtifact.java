/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
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
    String xml = "";

    try {
      xml = xmlMapper.writeValueAsString(this);
    } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.WRITE_ARTIFACT_XML, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_JSON);
      throw new CoreException(new JsonErrorBuilder(exception.getMessage()).build());

    }

    return xml.replaceAll(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE, "");
  }


}
