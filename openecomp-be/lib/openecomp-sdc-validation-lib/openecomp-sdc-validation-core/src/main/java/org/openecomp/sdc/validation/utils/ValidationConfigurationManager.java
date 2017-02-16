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

package org.openecomp.sdc.validation.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.interfaces.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidationConfigurationManager {

  private static final String VALIDATION_CONFIGURATION = "validationConfiguration.json";
  private static final List<Validator> validators = new ArrayList<>();
  private static Logger logger = LoggerFactory.getLogger(ValidationConfigurationManager.class);

  /**
   * Init validators list.
   *
   * @return the list
   */
  public static List<Validator> initValidators() {
    synchronized (validators) {
      if (CollectionUtils.isEmpty(validators)) {
        InputStream validationConfigurationJson =
            FileUtils.getFileInputStream(VALIDATION_CONFIGURATION);
        ValidationConfiguration validationConfiguration =
            JsonUtil.json2Object(validationConfigurationJson, ValidationConfiguration.class);
        List<ValidatorConfiguration> conf = validationConfiguration.getValidatorConfigurationList();
        conf.stream().filter(ValidatorConfiguration::isEnableInd).forEachOrdered(
            validatorConfiguration -> validators.add(validatorInit(validatorConfiguration)));
      }
    }
    return validators;
  }

  private static Validator validatorInit(ValidatorConfiguration validatorConf) {
    Validator validator = null;
    try {
      validator =
          CommonMethods.newInstance(validatorConf.getImplementationClass(), Validator.class);
    } catch (IllegalArgumentException iae) {
      logger.error("Validator:" + validatorConf.getName() + " Class:"
          + validatorConf.getImplementationClass() + " failed in initialization. error:"
          + iae.toString() + " trace:" + Arrays.toString(iae.getStackTrace()));
    }
    return validator;
  }
}
