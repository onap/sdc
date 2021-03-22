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
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import java.util.HashMap;
import java.util.Map;
import org.openecomp.convertor.ElementConvertor;

/**
 * Created by ayalaben on 9/11/2017
 */
public class ElementToServiceModelMapConvertor extends ElementConvertor<Map<String, String>> {

    @Override
    public Map<String, String> convert(Element element) {
        //TODO : after merge with 1802 change to heat fle name/TOSCA file name
        HashMap<String, String> map = new HashMap<>();
        map.put("Service Model Definition Entry", element.getInfo().getProperty("base"));
        return map;
    }
}
