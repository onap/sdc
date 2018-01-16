/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.validation.impl.util;

import org.openecomp.sdc.common.errors.Messages;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.parser.ParserException;

public class YamlValidatorUtil {
  private YamlValidatorUtil() {

  }

  public static String getParserExceptionReason(Exception exception) {

    String reason = null;

    if (exception.getCause() instanceof MarkedYAMLException) {
      if (exception.getCause() != null) {
        if (exception.getCause().getCause() instanceof ParserException) {
          reason = exception.getCause().getCause().getMessage();
        } else {
          reason = exception.getCause().getMessage();
        }
      }
    } else if (exception instanceof MarkedYAMLException) {

      reason = exception.getMessage();

    } else {
      reason = Messages.GENERAL_YAML_PARSER_ERROR.getErrorMessage();
    }
    return reason;
  }
}
