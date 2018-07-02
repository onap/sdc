/*
 * Copyright © 2016-2018 European Support Limited
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

package org.onap.sdc.tosca.datatypes.model.extension;

import org.onap.sdc.tosca.datatypes.model.NodeFilter;


public class ServiceFilter extends NodeFilter {

    Object tosca_id;

    public Object getTosca_id() {
        return tosca_id;
    }

    public void setTosca_id(Object toscaId) {
        this.tosca_id = toscaId;
    }

}
