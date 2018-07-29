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

package org.onap.sdc.generator.aai;

import org.onap.sdc.generator.aai.model.Resource;
import org.onap.sdc.generator.aai.model.Service;
import org.onap.sdc.generator.data.GeneratorConstants;
import org.onap.sdc.generator.logging.CategoryLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.StringWriter;

public interface AaiModelGenerator {

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static AaiModelGenerator getInstance() {
    Logger log = LoggerFactory.getLogger(AaiModelGenerator.class.getName());
    try {
      return AaiModelGenerator.class.cast(
          Class.forName("org.onap.sdc.generator.aai.AaiModelGeneratorImpl").newInstance());
    } catch (Exception exception) {
      MDC.put(GeneratorConstants.PARTNER_NAME, GeneratorConstants.GENERATOR_PARTNER_NAME);
      MDC.put(GeneratorConstants.ERROR_CATEGORY, CategoryLogLevel.ERROR.name());
      MDC.put(GeneratorConstants.ERROR_CODE, GeneratorConstants.GENERATOR_ERROR_CODE);
      MDC.put(
          GeneratorConstants.ERROR_DESCRIPTION, GeneratorConstants.GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED);
      StringWriter sw = new StringWriter();
      log.error(sw.toString(), exception);
    }
    return null;
  }

  public String generateModelFor(Service service);

  public String generateModelFor(Resource resource);


}
