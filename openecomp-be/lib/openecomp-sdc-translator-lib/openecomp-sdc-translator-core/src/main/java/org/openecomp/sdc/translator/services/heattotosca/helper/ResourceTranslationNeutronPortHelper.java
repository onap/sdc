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
package org.openecomp.sdc.translator.services.heattotosca.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceTranslationNeutronPortHelper {

    public static final String IP_COUNT_REQUIRED = "ip_count_required";
    public static final String FLOATING_IP_COUNT_REQUIRED = "floating_ip_count_required";
    public static final String NETWORK = "network";
    public static final String NETWORK_ROLE_TAG = "network_role_tag";
    public static final String FIXED_IPS = "fixed_ips";
    public static final String IP_VERSION = "ip_version";
    public static final String IP_ADDRESS = "ip_address";
    public static final String GET_INPUT = "get_input";
    public static final String ALLOWED_ADDRESS_PAIRS = "allowed_address_pairs";
    public static final String FLOATING_IP = "_floating_ip";
    public static final String FLOATING_V6_IP = "_floating_v6_ip";
    public static final String IPS = "_ips";
    public static final String V6_IPS = "_v6_ips";
    public static final String NET_NAME = "_net_name";
    public static final String NET_ID = "_net_id";
    public static final String NET_FQDN = "_net_fqdn";
    public static final String IPV4_REGEX = "\\w*_ip_\\d+";
    public static final String IPV6_REGEX = "\\w*_v6_ip_\\d+";
    public static final String MAC_COUNT_REQUIRED = "mac_count_required";
    public static final String MAC_ADDRESS = "mac_address";
    public static final String IS_REQUIRED = "is_required";
    public static final String IP_REQUIREMENTS = "ip_requirements";
    public static final String MAC_REQUIREMENTS = "mac_requirements";
    public static final int DEFAULT_IP_VERSION = 4;

    public void setAdditionalProperties(Map<String, Object> properties) {
        setRequirements(properties);
        setNetworkRoleTag(properties);
    }

    private void setRequirements(Map<String, Object> properties) {
        properties.putAll(addRequirements());
        setIpRequirements(properties);
        setMacRequirements(properties);
    }

    private void setIpRequirements(Map<String, Object> properties) {
        setFixedIpCount(properties);
        setFloatingIpCount(properties);
        addDefaultIpRequirement(properties);
    }

    private Map<String, Object> addRequirements() {
        Map<String, Object> properties = new HashMap<>();
        List<Map<String, Object>> ipRequirementsList = new ArrayList<>();
        properties.put(IP_REQUIREMENTS, ipRequirementsList);
        properties.put(MAC_REQUIREMENTS, createMacRequirement());
        return properties;
    }

    private Map<String, Object> createMacRequirement() {
        Map<String, Object> macRequirements = new HashMap<>();
        Map<String, Object> macIsRequired = new HashMap<>();
        macIsRequired.put(IS_REQUIRED, Boolean.FALSE);
        macRequirements.put(MAC_COUNT_REQUIRED, macIsRequired);
        return macRequirements;
    }

    private void setMacRequirements(Map<String, Object> properties) {
        updateMacCountRequired(properties);
    }

    private void updateMacCountRequired(Map<String, Object> properties) {
        if (properties.containsKey(MAC_ADDRESS)) {
            Map<String, Object> macRequirements = (Map<String, Object>) properties.get(MAC_REQUIREMENTS);
            Map<String, Object> macIsRequired = (Map<String, Object>) macRequirements.get(MAC_COUNT_REQUIRED);
            macIsRequired.put(IS_REQUIRED, Boolean.TRUE);
        }
    }

    private void setFloatingIpCount(Map<String, Object> properties) {
        handleIpCountRequired(properties, ALLOWED_ADDRESS_PAIRS, FLOATING_IP_COUNT_REQUIRED);
    }

    private void setFixedIpCount(Map<String, Object> properties) {
        handleIpCountRequired(properties, FIXED_IPS, IP_COUNT_REQUIRED);
    }

    private void addDefaultIpRequirement(Map<String, Object> properties) {
        List<Map<String, Object>> ipRequirementsList = ((List<Map<String, Object>>) properties.get(IP_REQUIREMENTS));
        if (ipRequirementsList.isEmpty()) {
            ipRequirementsList.add(createIPRequirement(DEFAULT_IP_VERSION));
        }
    }

    private Map<String, Object> createIPRequirement(Object version) {
        Map<String, Object> ipRequirements = new HashMap<>();
        Map<String, Object> isRequired = new HashMap<>();
        Map<String, Object> floatingIsRequired = new HashMap<>();
        isRequired.put(IS_REQUIRED, Boolean.FALSE);
        floatingIsRequired.put(IS_REQUIRED, Boolean.FALSE);
        ipRequirements.put(IP_COUNT_REQUIRED, isRequired);
        ipRequirements.put(FLOATING_IP_COUNT_REQUIRED, floatingIsRequired);
        ipRequirements.put(IP_VERSION, version);
        return ipRequirements;
    }

    private void handleIpCountRequired(Map<String, Object> properties, String ipType, String ipCountRequired) {
        Object propertyValue = properties.get(ipType);
        if (propertyValue == null) {
            return;
        }
        if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
            handleMapProperty(ipType, ipCountRequired, properties, (Map.Entry<String, Object>) ((Map) propertyValue).entrySet().iterator().next());
        } else if (propertyValue instanceof List && !((List) propertyValue).isEmpty()) {
            handleListProperty(ipType, ipCountRequired, properties, (List) propertyValue);
        }
    }

    private void handleListProperty(String ipType, String ipCountRequired, Map<String, Object> properties, List propertyValue) {
        for (int i = 0; i < propertyValue.size(); i++) {
            handleIpAddress(ipType, ipCountRequired, properties, propertyValue.get(i));
        }
    }

    private void handleMapProperty(String ipType, String ipCountRequired, Map<String, Object> properties, Map.Entry<String, Object> mapEntry) {
        updateIpCountRequired(ipType, ipCountRequired, properties, mapEntry.getValue());
    }

    private void handleIpAddress(String ipType, String ipCountRequired, Map<String, Object> properties, Object ipMap) {
        if (ipMap instanceof Map && !((Map) ipMap).isEmpty()) {
            Object ipAddressMap = ((Map) ipMap).get(IP_ADDRESS);
            if (ipAddressMap instanceof Map && !((Map) ipAddressMap).isEmpty()) {
                Object ipInput = ((Map) ipAddressMap).get(GET_INPUT);
                updateIpCountRequired(ipType, ipCountRequired, properties, ipInput);
            }
        }
    }

    private void updateIpCountRequired(String ipType, String ipCountRequired, Map<String, Object> properties, Object ipInput) {
        Object ipVersion = getVersion(ipInput, ipType);
        updateIpCountRequiredForVersion(ipCountRequired, properties, ipVersion);
    }

    private void updateIpCountRequiredForVersion(String ipCountRequired, Map<String, Object> properties, Object ipVersion) {
        if (ipVersion != null) {
            HashMap<Object, Map<String, Object>> ipRequirementsMap = getIPRequirements(properties);
            Map<String, Object> ipRequirement = ipRequirementsMap.get(ipVersion);
            if (ipRequirement == null) {
                ipRequirement = addIPRequirement(properties, ipVersion);
            }
            updateIpCountRequired(ipCountRequired, ipRequirement);
        }
    }

    private Map<String, Object> addIPRequirement(Map<String, Object> properties, Object ipVersion) {
        List<Map<String, Object>> ipRequirementsList = ((List<Map<String, Object>>) properties.get(IP_REQUIREMENTS));
        Map<String, Object> newIpRequirement = createIPRequirement(ipVersion);
        ipRequirementsList.add(newIpRequirement);
        return newIpRequirement;
    }

    private void updateIpCountRequired(String ipCountRequired, Map<String, Object> ipRequirement) {
        Map<String, Object> isIPCountRequired = (Map<String, Object>) ipRequirement.get(ipCountRequired);
        isIPCountRequired.put(IS_REQUIRED, Boolean.TRUE);
    }

    private HashMap<Object, Map<String, Object>> getIPRequirements(Map<String, Object> properties) {
        HashMap<Object, Map<String, Object>> ipRequirementsMap = new HashMap<>();
        List<Map<String, Object>> ipRequirementsList = ((List<Map<String, Object>>) properties.get(IP_REQUIREMENTS));
        ipRequirementsList.stream().forEach(e -> ipRequirementsMap.put(e.get(IP_VERSION), e));
        return ipRequirementsMap;
    }

    private void setNetworkRoleTag(Map<String, Object> properties) {
        Object propertyValue = properties.get(NETWORK);
        if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
            Map.Entry<String, String> mapEntry = (Map.Entry<String, String>) ((Map) propertyValue).entrySet().iterator().next();
            if (mapEntry.getValue() instanceof String && getNetworkRole(mapEntry.getValue()) != null) {
                properties.put(NETWORK_ROLE_TAG, getNetworkRole(mapEntry.getValue()));
            }
        }
    }

    private Object getVersion(Object value, String type) {
        Object version = null;
        if (type.equals(FIXED_IPS)) {
            version = getIpVersion(value);
        } else if (type.equals(ALLOWED_ADDRESS_PAIRS)) {
            version = getFloatingIpVersion(value);
        }
        return version;
    }

    private Object getFloatingIpVersion(Object value) {
        Object ipVersion = null;
        // Allowed ONLY String parameter
        if (value instanceof String) {
            if (((String) value).endsWith(FLOATING_V6_IP)) {
                ipVersion = 6;
            } else if (((String) value).endsWith(FLOATING_IP)) {
                ipVersion = 4;
            }
        }
        return ipVersion;
    }

    private Object getIpVersion(Object value) {
        // Allowed List or String parameter
        Object ipVersion = null;
        if (value instanceof List && !((List) value).isEmpty()) {
            value = ((List) value).get(0);
        }
        if (value instanceof String) {
            if (((String) value).endsWith(V6_IPS) || ((String) value).matches(IPV6_REGEX)) {
                ipVersion = 6;
            } else {
                ipVersion = 4;
            }
        }
        return ipVersion;
    }

    private Object getNetworkRole(String value) {
        Object networkRole = null;
        if (value.endsWith(NET_NAME)) {
            networkRole = value.substring(0, value.length() - NET_NAME.length());
        } else if (value.endsWith(NET_ID)) {
            networkRole = value.substring(0, value.length() - NET_ID.length());
        } else if (value.endsWith(NET_FQDN)) {
            networkRole = value.substring(0, value.length() - NET_FQDN.length());
        }
        return networkRole;
    }
}
