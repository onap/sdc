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

package org.openecomp.sdc.be.tosca.model;

public class ToscaAttribute {

    private String type;
    private String description;
    private Object _defaultp_;
    private String status;
    private EntrySchema entry_schema;

    public ToscaAttribute() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDefaultp() {
        return _defaultp_;
    }

    public void setDefaultp(Object defaultp) {
        this._defaultp_ = defaultp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EntrySchema getEntry_schema() {
        return entry_schema;
    }

    public void setEntry_schema(EntrySchema entry_schema) {
        this.entry_schema = entry_schema;
    }
}
