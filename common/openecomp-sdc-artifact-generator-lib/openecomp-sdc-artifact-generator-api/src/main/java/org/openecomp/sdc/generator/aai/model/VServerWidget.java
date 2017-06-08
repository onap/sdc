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

package org.openecomp.sdc.generator.aai.model;

import org.openecomp.sdc.generator.aai.types.Cardinality;
import org.openecomp.sdc.generator.aai.types.Model;
import org.openecomp.sdc.generator.aai.types.ModelType;
import org.openecomp.sdc.generator.aai.types.ModelWidget;

@Model(widget = Widget.Type.VSERVER, cardinality = Cardinality.UNBOUNDED, dataDeleteFlag = true)
@ModelWidget(type = ModelType.WIDGET, name = "vserver")
public class VServerWidget extends Widget {

  /**
   * Instantiates a new V server widget.
   */
  public VServerWidget() {
    addWidget(new FlavorWidget());
    addWidget(new ImageWidget());
    addWidget(new TenantWidget());
    addWidget(new VfcWidget());
  }

  public boolean addWidget(Widget widget) {
    return widgets.add(widget);
  }

}
