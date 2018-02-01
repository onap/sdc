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

package org.openecomp.core.validation.types;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class GlobalValidationContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalValidationContext.class);
  private final Map<String, FileValidationContext> fileContextMap = new HashMap<>();
  private final Map<String, MessageContainer> messageContainerMap = new HashMap<>();
  private ErrorMessageCode messageCode;

  public ErrorMessageCode getMessageCode() {
    return messageCode;
  }

  public void setMessageCode(ErrorMessageCode messageCode) {
    this.messageCode = messageCode;
  }


  /**
   * Add message.
   *
   * @param fileName the file name
   * @param level    the level
   * @param message  the message
   */
  public void addMessage(String fileName, ErrorLevel level, String message) {

    LOGGER.debug("'{}' [{}] in file '{}' ", message, level, fileName);

    if (fileContextMap.containsKey(fileName)) {
      fileContextMap.get(fileName).getMessageContainer().getMessageBuilder()
          .setMessage(level.toString() + ": " + message).setLevel(level).create();
    } else {
      MessageContainer messageContainer;
      synchronized (this) {
        messageContainer = messageContainerMap.computeIfAbsent(fileName, k -> new MessageContainer());
      }
      messageContainer.getMessageBuilder().setMessage(level.toString() + ": " + message)
          .setLevel(level).create();
    }
  }

  /**
   * Gets file content.
   *
   * @param fileName the file name
   * @return the file content
   */
  public Optional<InputStream> getFileContent(String fileName) {
    FileValidationContext fileContext = fileContextMap.get(fileName);
    if (fileContext == null || fileContext.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(fileContext.getContent());
  }

  public void addFileContext(String fileName, byte[] fileContent) {
    fileContextMap.put(fileName, new FileValidationContext(fileName, fileContent));
  }

  /**
   * Gets context message containers.
   *
   * @return the context message containers
   */
  public Map<String, MessageContainer> getContextMessageContainers() {

    Map<String, MessageContainer> contextMessageContainer = new HashMap<>();
    fileContextMap.entrySet().stream().filter(entry -> CollectionUtils
        .isNotEmpty(entry.getValue().getMessageContainer().getErrorMessageList())).forEach(
          entry -> contextMessageContainer.put(
             entry.getKey(), entry.getValue()
             .getMessageContainer()));
    messageContainerMap.entrySet().stream()
        .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue().getErrorMessageList()))
        .forEach(entry -> contextMessageContainer.put(entry.getKey(), entry.getValue()));
    return contextMessageContainer;
  }

  public Map<String, FileValidationContext> getFileContextMap() {
    return fileContextMap;
  }

  public Collection<String> files(BiPredicate<String, GlobalValidationContext> func) {
    return fileContextMap.keySet().stream().filter(t -> func.test(t, this))
        .collect(Collectors.toList());
  }

  public Collection<String> getFiles() {
    return this.getFileContextMap().keySet();
  }

}
