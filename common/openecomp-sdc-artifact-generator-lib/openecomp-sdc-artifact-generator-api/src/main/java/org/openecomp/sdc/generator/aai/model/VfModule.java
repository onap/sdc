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

import java.util.List;

@Model(widget = Widget.Type.VFMODULE, cardinality = Cardinality.UNBOUNDED, dataDeleteFlag = true)
public class VfModule extends Resource {

  Widget vserver = null;
  boolean addlintf = false;
  boolean addvolume = false;

  List<String> members;

  public void setMembers(List<String> members) {
    this.members = members;
  }

  /**
   * Adds Widget.
   *
   * @param widget the widget
   * @return the boolean
   */
  public boolean addWidget(Widget widget) {
    if (widget.memberOf(members)) {
      if (vserver == null && widget.getId().equals(new VServerWidget().getId())) {
        vserver = widget;
        if (addlintf) {
          vserver.addWidget(new LIntfWidget());
        }
        if (addvolume) {
          vserver.addWidget(new VolumeWidget());
        }
      } else if (widget.getId().equals(new LIntfWidget().getId())) {
        if (vserver != null) {
          vserver.addWidget(widget);
          return true;
        } else {
          addlintf = true;
        }
        return false;
      } else if (widget.getId().equals(new VolumeWidget().getId())) {
        if (vserver != null) {
          vserver.addWidget(widget);
        } else {
          addvolume = true;
        }
        return true;
      }
      if (widget.getId().equals(new OamNetwork().getId())) {
        return false;
      }
      return widgets.add(widget);
    }
    return false;
  }

}
