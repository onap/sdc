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

package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.model.UploadNodeFilterCapabilitiesInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterPropertyInfo;
import org.openecomp.sdc.be.utils.TypeUtils;

public class NodeFilterUploadCreator {


    public UploadNodeFilterInfo createNodeFilterData(Object obj) {
        if (!(obj instanceof Map)) {
            return null;
        }
        Map<String, Object> nodeFilterMap = (Map<String, Object>) obj;

        UploadNodeFilterInfo uploadNodeFilterInfo = new UploadNodeFilterInfo();
        final String propertiesElementName = TypeUtils.ToscaTagNamesEnum.PROPERTIES.getElementName();
        if (nodeFilterMap.containsKey(propertiesElementName)) {
            uploadNodeFilterInfo.setProperties(createNodeFilterProperties(nodeFilterMap.get(propertiesElementName)));
        }
        final String capabilitiesElementName = TypeUtils.ToscaTagNamesEnum.CAPABILITIES.getElementName();
        if (nodeFilterMap.containsKey(capabilitiesElementName)) {
            uploadNodeFilterInfo
                    .setCapabilities(createNodeFilterCapabilities(nodeFilterMap.get(capabilitiesElementName)));
        }
        final String toscaId = TypeUtils.ToscaTagNamesEnum.TOSCA_ID.getElementName();
        if (nodeFilterMap.containsKey(toscaId)) {
            uploadNodeFilterInfo.setTosca_id(nodeFilterMap.get(toscaId));
        }
        return uploadNodeFilterInfo;
    }

    private List<UploadNodeFilterPropertyInfo> createNodeFilterProperties(Object o) {
        if (!(o instanceof List)) {
            return null;
        }
        List<UploadNodeFilterPropertyInfo> retVal = new ArrayList<>();
        List<Map<String, Object>> propertiesList = (List<Map<String, Object>>) o;
        for (Map<String, Object> map : propertiesList) {
            final Map.Entry<String, Object> entry = map.entrySet().iterator().next();
            final Object value = entry.getValue();
            if (value instanceof Map) {
                List<String> valueList = new ArrayList<>();
                valueList.add(valueToProperty(entry.getValue()));
                retVal.add(new UploadNodeFilterPropertyInfo(entry.getKey(), valueList));
            } else if (value instanceof List) {
                List<String> propertiesVals =
                        (List<String>) ((List) value).stream().map(this::valueToProperty).collect(Collectors.toList());
                retVal.add(new UploadNodeFilterPropertyInfo(entry.getKey(), propertiesVals));
            }
        }
        return retVal;
    }

    private String valueToProperty(Object o) {

        if (o instanceof Map) {
            return new YamlUtil().objectToYaml(o);
        }
        return null;
    }

    private Map<String, UploadNodeFilterCapabilitiesInfo> createNodeFilterCapabilities(Object o) {
        if (!(o instanceof List)) {
            return null;
        }
        Map<String, UploadNodeFilterCapabilitiesInfo> retVal = new HashMap<>();
        List<Map<String, Object>> capabilitiesMap = (List<Map<String, Object>>) o;
        for (Map<String, Object> map : capabilitiesMap) {
            final Map.Entry<String, Object> entry = map.entrySet().iterator().next();
            UploadNodeFilterCapabilitiesInfo uploadNodeFilterCapabilitiesInfo = new UploadNodeFilterCapabilitiesInfo();
            uploadNodeFilterCapabilitiesInfo.setName(entry.getKey());
            uploadNodeFilterCapabilitiesInfo.setProperties(createCapabilitiesProperties(entry.getValue()));
            retVal.put(entry.getKey(), uploadNodeFilterCapabilitiesInfo);
        }
        return retVal;

    }

    private List<UploadNodeFilterPropertyInfo> createCapabilitiesProperties(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        Map<String, Object> capabilitiesPropertiesMap = (Map<String, Object>) o;
        final String capabilitiesPropertiesElementName = TypeUtils.ToscaTagNamesEnum.PROPERTIES.getElementName();
        if (capabilitiesPropertiesMap.containsKey(capabilitiesPropertiesElementName)) {
            final Object propertiesObject = capabilitiesPropertiesMap.get(capabilitiesPropertiesElementName);
            return createNodeFilterProperties(propertiesObject);
        }
        return null;
    }


}
