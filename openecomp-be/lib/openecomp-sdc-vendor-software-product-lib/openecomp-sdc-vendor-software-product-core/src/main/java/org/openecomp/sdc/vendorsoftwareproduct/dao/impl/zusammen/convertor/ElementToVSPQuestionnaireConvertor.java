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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;

public class ElementToVSPQuestionnaireConvertor  extends ElementConvertor {
  
  @Override
  public VspQuestionnaireEntity convert( Element element) {
    
    if(element == null) {
      return null;
    }
    
    VspQuestionnaireEntity entity = new VspQuestionnaireEntity();
    entity.setQuestionnaireData(new String(FileUtils.toByteArray(element.getData())));
    return entity;
  }
}
