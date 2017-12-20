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

package org.openecomp.sdc.generator.aai;

import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CATEGORY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_DESCRIPTION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_PARTNER_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.PARTNER_NAME;

import org.openecomp.sdc.generator.aai.model.Resource;
import org.openecomp.sdc.generator.aai.model.Service;
import org.openecomp.sdc.generator.logging.CategoryLogLevel;
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
          Class.forName("org.openecomp.sdc.generator.aai.AaiModelGeneratorImpl").newInstance());
    } catch (Exception exception) {
      MDC.put(PARTNER_NAME, GENERATOR_PARTNER_NAME);
      MDC.put(ERROR_CATEGORY, CategoryLogLevel.ERROR.name());
      MDC.put(ERROR_CODE, GENERATOR_ERROR_CODE);
      MDC.put(ERROR_DESCRIPTION, GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED);
      StringWriter sw = new StringWriter();
      log.error(sw.toString(), exception);
    }
    return null;
  }

  public String generateModelFor(Service service);

  public String generateModelFor(Resource resource);


}
