/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.datamodel;

import java.util.HashMap;

public class NameIdPairWrapper extends HashMap<String, Object> {

    public static final String ID = "id";
    public static final String DATA = "data";

    public NameIdPairWrapper() {
    }

    public NameIdPairWrapper(NameIdPair nameIdPair) {
        super();
        init(nameIdPair);
    }

    public void init(NameIdPair nameIdPair) {
        setId(nameIdPair.getId());
        setData(new NameIdPair(nameIdPair));
    }

    public String getId() {
        return get(ID).toString();
    }

    public void setId(String id) {
        super.put(ID, id);
    }

    public NameIdPair getData() {
        return (NameIdPair) get(DATA);
    }

    public void setData(NameIdPair data) {
        put(DATA, data);
    }

    public NameIdPair getNameIdPair() {
        return new NameIdPair(getData().getName(), getData().getId());
    }
}
