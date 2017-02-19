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

package org.openecomp.core.validation.interfaces;

import org.openecomp.core.validation.types.GlobalValidationContext;

public interface Validator {

  //public void validate(String fileName, GlobalValidationContext globalContext);
  void validate(GlobalValidationContext globalContext);
  //Object convert(FileValidationContext
  // fileValidationContext);

  /*default boolean filter(FileValidationContext filecontext,
  GlobalValidationContext globalValidationContext){
        return true;
  }*/
}
