/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * Error body to return to client per IETF RFC 7807.
 */
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Body for errors such as http 500")
@Data
public class TestErrorBody implements Serializable {

  private static final long serialVersionUID = 3504501412736665763L;

  private String code;
  private Integer httpStatus;
  private String message;

  TestErrorBody() {

  }

  public TestErrorBody(String code, Integer httpStatus, String message) {
    this();
    this.code = code;
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
