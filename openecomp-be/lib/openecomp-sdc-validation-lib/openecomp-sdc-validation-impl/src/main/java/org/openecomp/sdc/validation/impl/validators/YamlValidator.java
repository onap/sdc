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

package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.interfaces.Validator;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class YamlValidator implements Validator {

  private static final Logger logger = LoggerFactory.getLogger(YamlValidator.class);

  @Override
  public void validate(GlobalValidationContext globalContext) {

    Collection<String> files = globalContext.files(
        (fileName, globalValidationContext) -> (fileName.endsWith(".yaml")
            || fileName.endsWith(".yml") || fileName.endsWith(".env")));

    files.stream().forEach(fileName -> validate(fileName, globalContext));
  }

  private void validate(String fileName, GlobalValidationContext globalContext) {
    InputStream rowContent = globalContext.getFileContent(fileName);
    if (rowContent == null) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
              Messages.EMPTY_YAML_FILE.getErrorMessage()));
      return; /* no need to continue validation */
    }

    try {
      convert(rowContent, Map.class);
    } catch (Exception exception) {

      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
              getParserExceptionReason(exception)));
      logger.error("Exception in yaml parser. message:" + exception.getMessage());
    }
  }

  private String getParserExceptionReason(Exception exception) {
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


  private <T> T convert(InputStream content, Class<T> type) {
    return new YamlUtil().yamlToObject(content, type);
  }
}
