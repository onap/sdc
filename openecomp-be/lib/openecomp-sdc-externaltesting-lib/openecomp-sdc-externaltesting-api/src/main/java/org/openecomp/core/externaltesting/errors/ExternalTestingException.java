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

package org.openecomp.core.externaltesting.errors;

@SuppressWarnings("unused")
public class ExternalTestingException extends RuntimeException {

  private static final long serialVersionUID = -4357810130868566088L;

  private String title;
  private int code;
  private String detail;

  public ExternalTestingException() {

  }

  public ExternalTestingException(String title, int code, String detail) {
    super();
    this.title = title;
    this.code = code;
    this.detail = detail;
  }

  public String getTitle() {
    return title;
  }

  public int getCode() {
    return code;
  }

  public String getDetail() {
    return detail;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }
}
