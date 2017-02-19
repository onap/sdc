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

package org.openecomp.core.validation.types;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FileValidationContext {
  private String fileName;
  private MessageContainer messageContainer = new MessageContainer();
  private byte[] content;

  public FileValidationContext(String fileName, byte[] fileContent) {
    this.fileName = fileName;
    this.content = fileContent;
  }


  MessageContainer getMessageContainer() {
    return this.messageContainer;
  }

  public InputStream getContent() {
    return new ByteArrayInputStream(content);
  }

  public String getFileName() {
    return this.fileName;
  }

  public boolean isEmpty() {
    return content == null || content.length == 0;
  }
}
