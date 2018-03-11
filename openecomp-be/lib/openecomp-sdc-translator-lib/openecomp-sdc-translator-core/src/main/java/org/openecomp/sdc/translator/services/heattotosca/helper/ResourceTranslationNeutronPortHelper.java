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

  public void setAdditionalProperties(Map<String, Object> properties) {
    setNetworkRoleTag(properties);
    setDefaultRequirments(properties);
    setFixedIpCount(properties);
    setFloatingIpCount(properties);
    setMacCount(properties);
  }

  private void setDefaultRequirments(Map<String, Object> properties){
    List<Map<String, Object>> ipRequirementsList = new ArrayList<>();
    ipRequirementsList.add(createIPRequirment(4));
    ipRequirementsList.add(createIPRequirment(6));
    properties.put(IP_REQUIREMENTS , ipRequirementsList);
    properties.put(MAC_REQUIREMENTS , createMacRequirment());
  }

  private Map<String, Object> createIPRequirment(Object version){
    Map<String, Object> ipRequirements = new HashMap();
    Map<String, Object> isRequired = new HashMap();
    Map<String, Object> floatingIsRequired = new HashMap();
    isRequired.put(IS_REQUIRED, Boolean.FALSE);
    floatingIsRequired.put(IS_REQUIRED, Boolean.FALSE);
    ipRequirements.put(IP_COUNT_REQUIRED, isRequired);
    ipRequirements.put(FLOATING_IP_COUNT_REQUIRED, floatingIsRequired);
    ipRequirements.put(IP_VERSION, version);
    return ipRequirements;
  }

  private Map<String, Object> createMacRequirment(){
    Map<String, Object> macRequirements = new HashMap();
    Map<String, Object> macIsRequired = new HashMap();
    macIsRequired.put(IS_REQUIRED, Boolean.FALSE);
    macRequirements.put(MAC_COUNT_REQUIRED, macIsRequired);
    return macRequirements;
  }

  private void setMacCount(Map<String, Object> properties) {
    if(properties.containsKey(MAC_ADDRESS)) {
      Map<String, Object> macRequirements = (Map<String, Object>) properties.get(MAC_REQUIREMENTS);
      Map<String, Object> macIsRequired = new HashMap();
      macIsRequired.put(IS_REQUIRED, Boolean.TRUE);
      macRequirements.put(MAC_COUNT_REQUIRED, macIsRequired);
      properties.put(MAC_REQUIREMENTS, macRequirements);
    }
  }

  private void setFloatingIpCount(Map<String, Object> properties) {
    setIPCount(properties, ALLOWED_ADDRESS_PAIRS, FLOATING_IP_COUNT_REQUIRED );
  }

  private void setFixedIpCount(Map<String, Object> properties) {
    setIPCount(properties, FIXED_IPS, IP_COUNT_REQUIRED );
  }

  private void setIPCount(Map<String, Object> properties, String ipType, String ipCountRequired ){
    HashMap <Object, Map<String, Object>> ipRequirmentsMap = getIPRequirments(properties);
    Map<String, Object> ipRequirement = new HashMap();
    ipRequirement.put(IS_REQUIRED, Boolean.TRUE);

    Object propertyValue = properties.get(ipType);
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map.Entry<String, Object> mapEntry =
              (Map.Entry<String, Object>) ((Map) propertyValue).entrySet().iterator().next();
      Object ipVersion = getVersion(mapEntry.getValue(), ipType);
      if (ipVersion != null) {
        ipRequirement = ipRequirmentsMap.get(ipVersion);
        if (ipRequirement != null) {
          Map<String, Object> isIPCountRequired = (Map<String, Object>)ipRequirement.get(ipCountRequired);
          isIPCountRequired.put(IS_REQUIRED, Boolean.TRUE);
        }
      }
    }
    else if (propertyValue instanceof List && !((List) propertyValue).isEmpty()) {
      for (int i = 0; i < ((List) propertyValue).size(); i++) {
        Object ipMap = ((List) propertyValue).get(i);
        if(ipMap instanceof Map && !((Map) ipMap).isEmpty()) {
          Object ipAddressMap = ((Map) ipMap).get(IP_ADDRESS);
          if (ipAddressMap instanceof Map && !((Map) ipAddressMap).isEmpty()) {
            Object ipList = ((Map) ipAddressMap).get(GET_INPUT);
            if (ipList instanceof List && !((List) ipList).isEmpty()) {
              Object ipVersion = getVersion(((List) ipList).get(0), ipType);
              if(ipVersion!= null) {
                ipRequirement = ipRequirmentsMap.get(ipVersion);
                if (ipRequirement != null) {
                  Map<String, Object> isIPCountRequired = (Map<String, Object>)ipRequirement.get(ipCountRequired);
                  isIPCountRequired.put(IS_REQUIRED, Boolean.TRUE);
                }
              }
            }
            else if (ipList instanceof String && !((String) ipList).isEmpty()) {
              Object ipVersion = getVersion(ipList, ipType);
              if (ipVersion != null) {
                ipRequirement = ipRequirmentsMap.get(ipVersion);
                if (ipRequirement != null) {
                  Map<String, Object> isIPCountRequired = (Map<String, Object>)ipRequirement.get(ipCountRequired);
                  isIPCountRequired.put(IS_REQUIRED, Boolean.TRUE);
                }
              }
            }
          }
        }
      }
    }
  }

  private HashMap <Object, Map<String, Object>> getIPRequirments (Map<String, Object> properties) {

    HashMap<Object, Map<String, Object>> ipRequirmentsMap = new HashMap();
    List<Map<String, Object>> ipRequirmentsList = ((List<Map<String,Object>>) properties.get(IP_REQUIREMENTS));
    ipRequirmentsList.stream().forEach(e->ipRequirmentsMap.put(e.get(IP_VERSION),e));
    return ipRequirmentsMap;
  }

  private void setNetworkRoleTag(Map<String, Object> properties) {
    Object propertyValue = properties.get(NETWORK);
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map.Entry<String, String> mapEntry =
              (Map.Entry<String, String>) ((Map) propertyValue).entrySet().iterator().next();
      if (mapEntry.getValue() instanceof String && getNetworkRole(mapEntry.getValue())!=null) {
        properties.put(NETWORK_ROLE_TAG, getNetworkRole(mapEntry.getValue()));
      }
    }
  }

  private Object getVersion(Object value, String type) {

    Object version = null;
    if(type.equals(FIXED_IPS)){
      version =  getIpVersion(value);
    }
    else if(type.equals(ALLOWED_ADDRESS_PAIRS)){
      version =  getFloatingIpVersion(value);
    }
    return version;
  }

  private Object getFloatingIpVersion(Object value) {
    Object ipVersion = null;
    if(value instanceof String) {
      if (((String) value).endsWith(FLOATING_V6_IP)) {
        ipVersion = 6;
      }
      else {
        ipVersion = 4;
      }
    }
    return ipVersion;
  }

  private Object getIpVersion(Object value) {
    Object ipVersion = null;
    if(value instanceof String) {
      if (((String) value).endsWith(V6_IPS) || ((String) value).matches(IPV6_REGEX)) {
        ipVersion = 6;
      }
      else {
        ipVersion = 4;
      }
    }
    return ipVersion;
  }

  private Object getNetworkRole(String value) {
    Object networkRole = null;
    if(value.endsWith(NET_NAME)) {
      networkRole = value.substring(0, value.length() - NET_NAME.length());
    }
    else if(value.endsWith(NET_ID)) {
      networkRole = value.substring(0, value.length() - NET_ID.length());
    }
    else if(value.endsWith(NET_FQDN)) {
      networkRole = value.substring(0, value.length() - NET_FQDN.length());
    }
    return networkRole;
  }
}
