package org.openecomp.sdc.be.components.csar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.AnnotationBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.NodeFilterUploadCreator;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.parser.ParserException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.openecomp.sdc.be.components.impl.ImportUtils.*;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.*;

/**
 * A handler class designed to parse the YAML file of the service template for a JAVA object
 */
@Component
public class YamlTemplateParsingHandler {

    private static final Pattern propertyValuePattern = Pattern.compile("[ ]*\\{[ ]*(str_replace=|token=|get_property=|concat=|get_attribute=)+");
    private static final int SUB_MAPPING_CAPABILITY_OWNER_NAME_IDX = 0;
    private static final int SUB_MAPPING_CAPABILITY_NAME_IDX = 1;
    private static final Logger log = Logger.getLogger(YamlTemplateParsingHandler.class);


    private Gson gson = new Gson();
    private TitanDao titanDao;
    private GroupTypeBusinessLogic groupTypeBusinessLogic;
    private AnnotationBusinessLogic annotationBusinessLogic;

    public YamlTemplateParsingHandler(TitanDao titanDao,
                                      GroupTypeBusinessLogic groupTypeBusinessLogic, AnnotationBusinessLogic annotationBusinessLogic) {
        this.titanDao = titanDao;
        this.groupTypeBusinessLogic = groupTypeBusinessLogic;
        this.annotationBusinessLogic = annotationBusinessLogic;
    }

    public ParsedToscaYamlInfo parseResourceInfoFromYAML(String fileName, String resourceYml, Map<String, String> createdNodesToscaResourceNames,
                                                         Map<String, NodeTypeInfo> nodeTypesInfo, String nodeName) {
        log.debug("#parseResourceInfoFromYAML - Going to parse yaml {} ", fileName);
        Map<String, Object> mappedToscaTemplate = getMappedToscaTemplate(fileName, resourceYml, nodeTypesInfo, nodeName);
        ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
        findToscaElement(mappedToscaTemplate, TOPOLOGY_TEMPLATE, ToscaElementTypeEnum.ALL)
                .left()
                .on(err -> failIfNotTopologyTemplate(fileName));

        parsedToscaYamlInfo.setInputs(getInputs(mappedToscaTemplate));
        parsedToscaYamlInfo.setInstances(getInstances(fileName, mappedToscaTemplate, createdNodesToscaResourceNames));
        parsedToscaYamlInfo.setGroups(getGroups(fileName, mappedToscaTemplate));
        log.debug("#parseResourceInfoFromYAML - The yaml {} has been parsed ", fileName);
        return parsedToscaYamlInfo;
    }

    private Map<String, Object> getMappedToscaTemplate(String fileName, String resourceYml, Map<String, NodeTypeInfo> nodeTypesInfo, String nodeName) {
        Map<String, Object> mappedToscaTemplate;
        if (isNodeExist(nodeTypesInfo, nodeName)) {
            mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
        } else {
            mappedToscaTemplate = loadYaml(fileName, resourceYml);
        }
        return mappedToscaTemplate;
    }

    private Map<String, Object> loadYaml(String fileName, String resourceYml) {
        Map<String, Object> mappedToscaTemplate = null;
        try {
            mappedToscaTemplate = loadYamlAsStrictMap(resourceYml);
        } catch (ParserException e) {
            log.debug("#getMappedToscaTemplate - Failed to load YAML file {}", fileName, e);
            rollbackWithException(ActionStatus.TOSCA_PARSE_ERROR, fileName, e.getMessage());
        }
        return mappedToscaTemplate;
    }

    private boolean isNodeExist(Map<String, NodeTypeInfo> nodeTypesInfo, String nodeName) {
        return nodeTypesInfo != null && nodeName != null && nodeTypesInfo.containsKey(nodeName);
    }

    private Map<String, InputDefinition> getInputs(Map<String, Object> toscaJson) {
        Map<String, InputDefinition> inputs = ImportUtils.getInputs(toscaJson, annotationBusinessLogic.getAnnotationTypeOperations())
                .left()
                .on(err -> new HashMap<>());
        annotationBusinessLogic.validateAndMergeAnnotationsAndAssignToInput(inputs);
        return inputs;
    }

    private Map<String, UploadComponentInstanceInfo> getInstances(String yamlName, Map<String, Object> toscaJson, Map<String, String> createdNodesToscaResourceNames) {

        Map<String, Object> nodeTemlates = findFirstToscaMapElement(toscaJson, NODE_TEMPLATES)
                .left()
                .on(err -> failIfNoNodeTemplates(yamlName));

        Map<String, UploadComponentInstanceInfo> componentInstances = getInstances(toscaJson, createdNodesToscaResourceNames, nodeTemlates);
        if (MapUtils.isEmpty(componentInstances)) {
            failIfNotTopologyTemplate(yamlName);
        }
        return componentInstances;
    }

    private Map<String, UploadComponentInstanceInfo> getInstances(Map<String, Object> toscaJson, Map<String, String> createdNodesToscaResourceNames, Map<String, Object> nodeTemlates) {
        Map<String, UploadComponentInstanceInfo> moduleComponentInstances;
        Map<String, Object> substitutionMappings = getSubstitutionMappings(toscaJson);
        moduleComponentInstances = nodeTemlates.entrySet()
                .stream()
                .map(node -> buildModuleComponentInstanceInfo(node, substitutionMappings, createdNodesToscaResourceNames))
                .collect(Collectors.toMap(UploadComponentInstanceInfo::getName, i -> i));
        return moduleComponentInstances;
    }

    private Map<String, Object> getSubstitutionMappings(Map<String, Object> toscaJson) {
        Map<String, Object> substitutionMappings = null;
        Either<Map<String, Object>, ResultStatusEnum> eitherSubstitutionMappings = findFirstToscaMapElement(toscaJson, SUBSTITUTION_MAPPINGS);
        if (eitherSubstitutionMappings.isLeft()) {
            substitutionMappings = eitherSubstitutionMappings.left().value();
        }
        return substitutionMappings;
    }

    @SuppressWarnings("unchecked")
    private Map<String, GroupDefinition> getGroups(String fileName, Map<String, Object> toscaJson) {

        Map<String, Object> foundGroups = findFirstToscaMapElement(toscaJson, GROUPS)
                .left()
                .on(err -> logGroupsNotFound(fileName));

        if (MapUtils.isNotEmpty(foundGroups)) {
            Map<String, GroupDefinition> groups = foundGroups
                    .entrySet()
                    .stream()
                    .map(this::createGroup)
                    .collect(Collectors.toMap(GroupDefinition::getName, g -> g));
            Map<String, Object> substitutionMappings = getSubstitutionMappings(toscaJson);
            if (capabilitiesSubstitutionMappingsExist(substitutionMappings)) {
                groups.entrySet().forEach(entry -> updateCapabilitiesNames(entry.getValue(), getNamesToUpdate(entry.getKey(),
                        (Map<String, List<String>>) substitutionMappings.get(CAPABILITIES.getElementName()))));
            }
            return groups;
        }
        return new HashMap<>();
    }

    private Map<String, Object> logGroupsNotFound(String fileName) {
        log.debug("#logGroupsNotFound - Groups were not found in the yaml template {}.", fileName);
        return new HashMap<>();
    }

    private void updateCapabilitiesNames(GroupDefinition group, Map<String, String> capabilityNames) {
        if (MapUtils.isNotEmpty(group.getCapabilities())) {
            group.getCapabilities().values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(cap -> capabilityNames.containsKey(cap.getName()))
                    .forEach(cap -> cap.setName(capabilityNames.get(cap.getName())));
        }
    }

    private Map<String, String> getNamesToUpdate(String name, Map<String, List<String>> pair) {
        return pair.entrySet().stream()
                .filter(e -> e.getValue().get(SUB_MAPPING_CAPABILITY_OWNER_NAME_IDX).equalsIgnoreCase(name))
                .collect(Collectors.toMap(e -> e.getValue().get(SUB_MAPPING_CAPABILITY_NAME_IDX), Map.Entry::getKey,  (n1 ,n2) -> n1));
    }

    private boolean capabilitiesSubstitutionMappingsExist(Map<String, Object> substitutionMappings) {
        return substitutionMappings != null && substitutionMappings.containsKey(CAPABILITIES.getElementName());
    }

    private GroupDefinition createGroup(Map.Entry<String, Object> groupNameValue) {
        GroupDefinition group = new GroupDefinition();
        group.setName(groupNameValue.getKey());
        try {
            if (groupNameValue.getValue() != null && groupNameValue.getValue() instanceof Map) {
                Map<String, Object> groupTemplateJsonMap = (Map<String, Object>) groupNameValue.getValue();
                validateAndFillGroup(group, groupTemplateJsonMap);
                validateUpdateGroupProperties(group, groupTemplateJsonMap);
                validateUpdateGroupCapabilities(group, groupTemplateJsonMap);
            } else {
                rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            }
        } catch (ClassCastException e) {
            log.debug("#createGroup - Failed to create the group {}. The exception occure", groupNameValue.getKey(), e);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return group;
    }

    private Map<String, CapabilityDefinition> addCapabilities(Map<String, CapabilityDefinition> cap, Map<String, CapabilityDefinition> otherCap) {
        cap.putAll(otherCap);
        return cap;
    }

    private Map<String, CapabilityDefinition> addCapability(CapabilityDefinition cap) {
        Map<String, CapabilityDefinition> map = Maps.newHashMap();
        map.put(cap.getName(), cap);
        return map;
    }

    private void setMembers(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(MEMBERS.getElementName())) {
            Object members = groupTemplateJsonMap.get(MEMBERS.getElementName());
            if (members != null) {
                if (members instanceof List) {
                    setMembersFromList(groupInfo, (List<?>) members);
                } else {
                    log.debug("The 'members' member is not of type list under group {}", groupInfo.getName());
                    rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
                }
            }
        }
    }

    private void setMembersFromList(GroupDefinition groupInfo, List<?> membersAsList) {
        groupInfo.setMembers(membersAsList
                .stream()
                .collect(Collectors.toMap(Object::toString, member -> "")));
    }

    @SuppressWarnings("unchecked")
    private void validateUpdateGroupProperties(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Object propertiesElement = groupTemplateJsonMap.get(PROPERTIES.getElementName());
            if (propertiesElement instanceof Map){
                mergeGroupProperties(groupInfo, (Map<String, Object>) propertiesElement);
            }
        }
    }

    private void mergeGroupProperties(GroupDefinition groupInfo, Map<String, Object> parsedProperties) {
        if(CollectionUtils.isNotEmpty(groupInfo.getProperties())){
            validateGroupProperties(parsedProperties, groupInfo);
            groupInfo.getProperties().forEach(p -> mergeGroupProperty(p, parsedProperties));
        }
    }

    private void mergeGroupProperty(PropertyDataDefinition property, Map<String, Object> parsedProperties) {
        if(parsedProperties.containsKey(property.getName())){
            Object propValue = parsedProperties.get(property.getName());
            if (valueNotContainsPattern(propertyValuePattern, propValue)) {
                setPropertyValueAndGetInputsValues(property, propValue);
            }
        }
    }

    private void setPropertyValueAndGetInputsValues(PropertyDataDefinition property, Object propValue) {
        if(propValue != null){
            UploadPropInfo uploadPropInfo = buildProperty(property.getName(), propValue);
            property.setValue(convertPropertyValue(ToscaPropertyType.isValidType(property.getType()), uploadPropInfo.getValue()));
            property.setGetInputValues(uploadPropInfo.getGet_input());
        }
    }

    private String convertPropertyValue(ToscaPropertyType type, Object value) {
        String convertedValue = null;
        if (value != null) {
            if (type == null || value instanceof Map || value instanceof List) {
                convertedValue = gson.toJson(value);
            } else {
                convertedValue = value.toString();
            }
        }
        return convertedValue;
    }

    private void setDescription(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(DESCRIPTION.getElementName())) {
            groupInfo.setDescription(
                    (String) groupTemplateJsonMap.get(DESCRIPTION.getElementName()));
        }
    }

    private void validateAndFillGroup(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        String type = (String) groupTemplateJsonMap.get(TYPE.getElementName());
        if(StringUtils.isEmpty(type)){
            log.debug("#validateAndFillGroup - The 'type' member is not found under group {}", groupInfo.getName());
            rollbackWithException(ActionStatus.GROUP_MISSING_GROUP_TYPE, groupInfo.getName());
        }
        groupInfo.setType(type);
        GroupTypeDefinition groupType =  groupTypeBusinessLogic.getLatestGroupTypeByType(type);
        if (groupType == null) {
            log.debug("#validateAndFillGroup - The group type {} not found", groupInfo.getName());
            rollbackWithException(ActionStatus.GROUP_TYPE_IS_INVALID, type);
        }
        groupInfo.convertFromGroupProperties(groupType.getProperties());
        groupInfo.convertCapabilityDefinitions(groupType.getCapabilities());
        setDescription(groupInfo, groupTemplateJsonMap);
        setMembers(groupInfo, groupTemplateJsonMap);
    }

    @SuppressWarnings("unchecked")
    private void validateUpdateGroupCapabilities(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {

        if (groupTemplateJsonMap.containsKey(CAPABILITIES.getElementName())) {
            Object capabilities = groupTemplateJsonMap.get(CAPABILITIES.getElementName());
            if (capabilities instanceof List) {
                validateUpdateCapabilities(groupInfo, ((List<Object>) capabilities).stream()
                        .map(o -> buildGroupCapability(groupInfo, o))
                        .collect(Collectors.toMap(CapabilityDefinition::getType, this::addCapability, this::addCapabilities)));
            } else if (capabilities instanceof Map) {
                validateUpdateCapabilities(groupInfo, ((Map<String, Object>) capabilities).entrySet()
                        .stream()
                        .map(e -> buildGroupCapability(groupInfo, e))
                        .collect(Collectors.toMap(CapabilityDefinition::getType, this::addCapability, this::addCapabilities)));
            } else {
                log.debug("#setCapabilities - Failed to import the capabilities of the group {}. ", groupInfo.getName());
                rollbackWithException(ActionStatus.INVALID_YAML);
            }
        }
    }

    private void validateUpdateCapabilities(GroupDefinition groupInfo, Map<String, Map<String, CapabilityDefinition>> capabilityInfo) {
        validateGroupCapabilities(groupInfo, capabilityInfo);
        groupInfo.updateCapabilitiesProperties(capabilityInfo);
    }

    private void validateGroupCapabilities(GroupDefinition group, Map<String, Map<String, CapabilityDefinition>> parsedCapabilities) {
        if (MapUtils.isNotEmpty(parsedCapabilities)) {
            if (MapUtils.isEmpty(group.getCapabilities())) {
                failOnMissingCapabilityTypes(group, Lists.newArrayList(parsedCapabilities.keySet()));
            }
            List<String> missingCapTypes = parsedCapabilities.keySet().stream().filter(ct -> !group.getCapabilities().containsKey(ct)).collect(toList());
            if (CollectionUtils.isNotEmpty(missingCapTypes)) {
                failOnMissingCapabilityTypes(group, missingCapTypes);
            }
            group.getCapabilities().entrySet().forEach(e -> validateCapabilities(group, e.getValue(), parsedCapabilities.get(e.getKey())));
        }
    }

    private void validateCapabilities(GroupDefinition group, List<CapabilityDefinition> capabilities, Map<String, CapabilityDefinition> parsedCapabilities) {
        List<String> allowedCapNames = capabilities.stream().map(CapabilityDefinition::getName).distinct().collect(toList());
        List<String> missingCapNames = parsedCapabilities.keySet().stream().filter(c -> !allowedCapNames.contains(c)).collect(toList());
        if (CollectionUtils.isNotEmpty(missingCapNames)) {
            failOnMissingCapabilityNames(group, missingCapNames);
        }
        validateCapabilitiesProperties(capabilities, parsedCapabilities);
    }

    private void validateCapabilitiesProperties(List<CapabilityDefinition> capabilities, Map<String, CapabilityDefinition> parsedCapabilities) {
        capabilities.forEach(c -> validateCapabilityProperties(c, parsedCapabilities.get(c.getName())));
    }

    private void validateCapabilityProperties(CapabilityDefinition capability, CapabilityDefinition parsedCapability) {
        if(parsedCapability != null && parsedCapability.getProperties() != null){
            List<String> parsedPropertiesNames = parsedCapability.getProperties()
                .stream()
                .map(ComponentInstanceProperty::getName).collect(toList());
            validateProperties(capability.getProperties().stream().map(PropertyDataDefinition::getName).collect(toList()), parsedPropertiesNames, ActionStatus.PROPERTY_NOT_FOUND, capability.getName(), capability.getType());
        }
    }

    private void  validateGroupProperties(Map<String, Object> parsedProperties, GroupDefinition groupInfo) {
        List<String> parsedPropertiesNames = parsedProperties.entrySet()
                        .stream()
                        .map(Map.Entry::getKey).collect(toList());
        validateProperties(groupInfo.getProperties().stream().map(PropertyDataDefinition::getName).collect(toList()), parsedPropertiesNames, ActionStatus.GROUP_PROPERTY_NOT_FOUND, groupInfo.getName(), groupInfo.getType());
    }

    private void validateProperties(List<String> validProperties, List<String> parsedProperties, ActionStatus actionStatus, String name, String type) {
        if (CollectionUtils.isNotEmpty(parsedProperties)) {
            verifyMissingProperties(actionStatus, name, type, parsedProperties
                    .stream()
                    .filter(n -> !validProperties.contains(n))
                    .collect(toList()));
        }
    }

    private void verifyMissingProperties(ActionStatus actionStatus, String name, String type, List<String> missingProperties) {
        if (CollectionUtils.isNotEmpty(missingProperties)) {
            log.debug("#validateProperties - Failed to validate properties. The properties {} are missing on {} of the type {}. ", missingProperties.toString(), name, type);
            rollbackWithException(actionStatus, missingProperties.toString(), missingProperties.toString(), name, type);
        }
    }

    @SuppressWarnings("unchecked")
    private CapabilityDefinition buildGroupCapability(GroupDefinition groupInfo, Object capObject) {
        if (!(capObject instanceof Map)) {
            log.debug("#convertToGroupCapability - Failed to import the capability {}. ", capObject);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return buildGroupCapability(groupInfo, ((Map<String, Object>) capObject).entrySet().iterator().next());
    }

    @SuppressWarnings("unchecked")
    private CapabilityDefinition buildGroupCapability(GroupDefinition groupInfo, Map.Entry<String, Object> capEntry) {
        CapabilityDefinition capability = new CapabilityDefinition();
        capability.setOwnerType(CapabilityDataDefinition.OwnerType.GROUP);
        capability.setName(capEntry.getKey());
        capability.setParentName(capEntry.getKey());
        capability.setOwnerId(groupInfo.getName());
        if (!(capEntry.getValue() instanceof Map)) {
            log.debug("#convertMapEntryToCapabilityDefinition - Failed to import the capability {}. ", capEntry.getKey());
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        Map<String, Object> capabilityValue = (Map<String, Object>) capEntry.getValue();
        String type = (String) capabilityValue.get(TYPE.getElementName());
        if (StringUtils.isEmpty(type)) {
            log.debug("#convertMapEntryToCapabilityDefinition - Failed to import the capability {}. Missing capability type. ", capEntry.getKey());
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        capability.setType(type);
        if (!(capabilityValue.get(PROPERTIES.getElementName()) instanceof Map)) {
            log.debug("#convertMapEntryToCapabilityDefinition - Failed to import the capability {}. ", capEntry.getKey());
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        Map<String, Object> properties = (Map<String, Object>) capabilityValue.get(PROPERTIES.getElementName());
        capability.setProperties(properties.entrySet().stream().map(this::convertToProperty).collect(toList()));
        return capability;
    }

    private ComponentInstanceProperty convertToProperty(Map.Entry<String, Object> e) {
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName(e.getKey());
        property.setValue((String) e.getValue());
        return property;
    }

    @SuppressWarnings("unchecked")
    private UploadComponentInstanceInfo buildModuleComponentInstanceInfo(
            Map.Entry<String, Object> nodeTemplateJsonEntry, Map<String, Object> substitutionMappings,
            Map<String, String> createdNodesToscaResourceNames) {

        UploadComponentInstanceInfo nodeTemplateInfo = new UploadComponentInstanceInfo();
        nodeTemplateInfo.setName(nodeTemplateJsonEntry.getKey());
        try {
            if (nodeTemplateJsonEntry.getValue() instanceof String) {
                String nodeTemplateJsonString = (String) nodeTemplateJsonEntry.getValue();
                nodeTemplateInfo.setType(nodeTemplateJsonString);
            } else if (nodeTemplateJsonEntry.getValue() instanceof Map) {
                Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) nodeTemplateJsonEntry.getValue();
                setToscaResourceType(createdNodesToscaResourceNames, nodeTemplateInfo, nodeTemplateJsonMap);
                setRequirements(nodeTemplateInfo, nodeTemplateJsonMap);
                setCapabilities(nodeTemplateInfo, nodeTemplateJsonMap);
                updateProperties(nodeTemplateInfo, nodeTemplateJsonMap);
                setDirectives(nodeTemplateInfo, nodeTemplateJsonMap);
                setNodeFilter(nodeTemplateInfo, nodeTemplateJsonMap);
                setSubstitutions(substitutionMappings, nodeTemplateInfo);
            } else {
                rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            }
        } catch (ClassCastException e) {
            BeEcompErrorManager.getInstance().logBeSystemError("Import Resource - create capability");
            log.debug("error when creating capability, message:{}", e.getMessage(), e);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return nodeTemplateInfo;
    }

    @SuppressWarnings("unchecked")
    private void setSubstitutions(Map<String, Object> substitutionMappings, UploadComponentInstanceInfo nodeTemplateInfo) {
        if (substitutionMappings != null) {
            if (substitutionMappings.containsKey(CAPABILITIES.getElementName())) {
                nodeTemplateInfo.setCapabilitiesNamesToUpdate(getNamesToUpdate(nodeTemplateInfo.getName(), (Map<String, List<String>>) substitutionMappings
                        .get(CAPABILITIES.getElementName())));
            }
            if (substitutionMappings.containsKey(REQUIREMENTS.getElementName())) {
                nodeTemplateInfo.setRequirementsNamesToUpdate(getNamesToUpdate(
                        nodeTemplateInfo.getName(), (Map<String, List<String>>) substitutionMappings
                                .get(REQUIREMENTS.getElementName())));
            }
        }
    }

    private void updateProperties(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Map<String, List<UploadPropInfo>> properties = buildPropModuleFromYaml(nodeTemplateJsonMap);
            if (!properties.isEmpty()) {
                nodeTemplateInfo.setProperties(properties);
            }
        }
    }

    private void setCapabilities(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(CAPABILITIES.getElementName())) {
            Map<String, List<UploadCapInfo>> eitherCapRes = createCapModuleFromYaml(nodeTemplateJsonMap);
            if (!eitherCapRes.isEmpty()) {
                nodeTemplateInfo.setCapabilities(eitherCapRes);
            }
        }
    }

    private void setRequirements(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(REQUIREMENTS.getElementName())) {
            Map<String, List<UploadReqInfo>> regResponse = createReqModuleFromYaml(nodeTemplateJsonMap);
            if (!regResponse.isEmpty()) {
                nodeTemplateInfo.setRequirements(regResponse);
            }
        }
    }

    private void setToscaResourceType(Map<String, String> createdNodesToscaResourceNames,
                                      UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(TYPE.getElementName())) {
            String toscaResourceType = (String) nodeTemplateJsonMap.get(TYPE.getElementName());
            if (createdNodesToscaResourceNames.containsKey(toscaResourceType)) {
                toscaResourceType = createdNodesToscaResourceNames.get(toscaResourceType);
            }
            nodeTemplateInfo.setType(toscaResourceType);
        }
    }

    private void setDirectives(UploadComponentInstanceInfo nodeTemplateInfo,
            Map<String, Object> nodeTemplateJsonMap) {
        List<String> directives =
                (List<String>) nodeTemplateJsonMap.get(TypeUtils.ToscaTagNamesEnum.DIRECTIVES.getElementName());
        nodeTemplateInfo.setDirectives(directives);
    }

    private void setNodeFilter(UploadComponentInstanceInfo nodeTemplateInfo,
            Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.NODE_FILTER.getElementName())) {
            nodeTemplateInfo.setUploadNodeFilterInfo(
                    new NodeFilterUploadCreator().createNodeFilterData(nodeTemplateJsonMap.get(
                            TypeUtils.ToscaTagNamesEnum.NODE_FILTER.getElementName())));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<UploadReqInfo>> createReqModuleFromYaml(Map<String, Object> nodeTemplateJsonMap) {
        Map<String, List<UploadReqInfo>> moduleRequirements = new HashMap<>();
        Either<List<Object>, ResultStatusEnum> requirementsListRes =
                findFirstToscaListElement(nodeTemplateJsonMap, REQUIREMENTS);

        if (requirementsListRes.isLeft()) {
            for (Object jsonReqObj : requirementsListRes.left().value()) {
                String reqName = ((Map<String, Object>) jsonReqObj).keySet().iterator().next();
                Object reqJson = ((Map<String, Object>) jsonReqObj).get(reqName);
                addModuleNodeTemplateReq(moduleRequirements, reqJson, reqName);
            }
        } else {
            Either<Map<String, Object>, ResultStatusEnum> requirementsMapRes =
                    findFirstToscaMapElement(nodeTemplateJsonMap, REQUIREMENTS);
            if (requirementsMapRes.isLeft()) {
                for (Map.Entry<String, Object> entry : requirementsMapRes.left().value().entrySet()) {
                    String reqName = entry.getKey();
                    Object reqJson = entry.getValue();
                    addModuleNodeTemplateReq(moduleRequirements, reqJson, reqName);
                }
            }
        }
        return moduleRequirements;
    }

    private void addModuleNodeTemplateReq(Map<String, List<UploadReqInfo>> moduleRequirements, Object requirementJson, String requirementName) {

        UploadReqInfo requirement = buildModuleNodeTemplateReg(requirementJson);
        requirement.setName(requirementName);
        if (moduleRequirements.containsKey(requirementName)) {
            moduleRequirements.get(requirementName).add(requirement);
        } else {
            List<UploadReqInfo> list = new ArrayList<>();
            list.add(requirement);
            moduleRequirements.put(requirementName, list);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<UploadCapInfo>> createCapModuleFromYaml(Map<String, Object> nodeTemplateJsonMap) {
        Map<String, List<UploadCapInfo>> moduleCap = new HashMap<>();
        Either<List<Object>, ResultStatusEnum> capabilitiesListRes =
                findFirstToscaListElement(nodeTemplateJsonMap, CAPABILITIES);
        if (capabilitiesListRes.isLeft()) {
            for (Object jsonCapObj : capabilitiesListRes.left().value()) {
                String key = ((Map<String, Object>) jsonCapObj).keySet().iterator().next();
                Object capJson = ((Map<String, Object>) jsonCapObj).get(key);
                addModuleNodeTemplateCap(moduleCap, capJson, key);
            }
        } else {
            Either<Map<String, Object>, ResultStatusEnum> capabilitiesMapRes =
                    findFirstToscaMapElement(nodeTemplateJsonMap, CAPABILITIES);
            if (capabilitiesMapRes.isLeft()) {
                for (Map.Entry<String, Object> entry : capabilitiesMapRes.left().value().entrySet()) {
                    String capName = entry.getKey();
                    Object capJson = entry.getValue();
                    addModuleNodeTemplateCap(moduleCap, capJson, capName);
                }
            }
        }
        return moduleCap;
    }

    private void addModuleNodeTemplateCap(Map<String, List<UploadCapInfo>> moduleCap, Object capJson, String key) {

        UploadCapInfo capabilityDef = buildModuleNodeTemplateCap(capJson);
        capabilityDef.setKey(key);
        if (moduleCap.containsKey(key)) {
            moduleCap.get(key).add(capabilityDef);
        } else {
            List<UploadCapInfo> list = new ArrayList<>();
            list.add(capabilityDef);
            moduleCap.put(key, list);
        }
    }

    @SuppressWarnings("unchecked")
    private UploadCapInfo buildModuleNodeTemplateCap(Object capObject) {
        UploadCapInfo capTemplateInfo = new UploadCapInfo();

        if (capObject instanceof String) {
            String nodeTemplateJsonString = (String) capObject;
            capTemplateInfo.setNode(nodeTemplateJsonString);
        } else if (capObject instanceof Map) {
            fillCapability(capTemplateInfo, (Map<String, Object>) capObject);
        }
        return capTemplateInfo;
    }

    private void fillCapability(UploadCapInfo capTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(NODE.getElementName())) {
            capTemplateInfo.setNode((String) nodeTemplateJsonMap.get(NODE.getElementName()));
        }
        if (nodeTemplateJsonMap.containsKey(TYPE.getElementName())) {
            capTemplateInfo.setType((String) nodeTemplateJsonMap.get(TYPE.getElementName()));
        }
        if (nodeTemplateJsonMap.containsKey(VALID_SOURCE_TYPES.getElementName())) {
            Either<List<Object>, ResultStatusEnum> validSourceTypesRes =
                    findFirstToscaListElement(nodeTemplateJsonMap, VALID_SOURCE_TYPES);
            if (validSourceTypesRes.isLeft()) {
                capTemplateInfo.setValidSourceTypes(validSourceTypesRes.left().value().stream()
                        .map(Object::toString).collect(toList()));
            }
        }
        if (nodeTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Map<String, List<UploadPropInfo>> props = buildPropModuleFromYaml(nodeTemplateJsonMap);
            if (!props.isEmpty()) {
                List<UploadPropInfo> properties = props.values().stream().flatMap(Collection::stream).collect(toList());
                capTemplateInfo.setProperties(properties);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private UploadReqInfo buildModuleNodeTemplateReg(Object regObject) {

        UploadReqInfo regTemplateInfo = new UploadReqInfo();
        if (regObject instanceof String) {
            String nodeTemplateJsonString = (String) regObject;
            regTemplateInfo.setNode(nodeTemplateJsonString);
        } else if (regObject instanceof Map) {
            Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) regObject;
            if (nodeTemplateJsonMap.containsKey(NODE.getElementName())) {
                regTemplateInfo.setNode((String) nodeTemplateJsonMap.get(NODE.getElementName()));
            }
            if (nodeTemplateJsonMap.containsKey(CAPABILITY.getElementName())) {
                regTemplateInfo.setCapabilityName(
                        (String) nodeTemplateJsonMap.get(CAPABILITY.getElementName()));
            }
        }
        return regTemplateInfo;
    }

    private Map<String, List<UploadPropInfo>> buildPropModuleFromYaml(Map<String, Object> nodeTemplateJsonMap) {

        Map<String, List<UploadPropInfo>> moduleProp = new HashMap<>();
        Either<Map<String, Object>, ResultStatusEnum> toscaProperties =
                findFirstToscaMapElement(nodeTemplateJsonMap, PROPERTIES);
        if (toscaProperties.isLeft()) {
            Map<String, Object> jsonProperties = toscaProperties.left().value();
            for (Map.Entry<String, Object> jsonPropObj : jsonProperties.entrySet()) {
                if (valueNotContainsPattern(propertyValuePattern, jsonPropObj.getValue())) {
                    addProperty(moduleProp, jsonPropObj);
                }
            }
        }
        return moduleProp;
    }

    private void addProperty(Map<String, List<UploadPropInfo>> moduleProp, Map.Entry<String, Object> jsonPropObj) {
        UploadPropInfo propertyDef = buildProperty(jsonPropObj.getKey(), jsonPropObj.getValue());
        if (moduleProp.containsKey(propertyDef.getName())) {
            moduleProp.get(propertyDef.getName()).add(propertyDef);
        } else {
            List<UploadPropInfo> list = new ArrayList<>();
            list.add(propertyDef);
            moduleProp.put(propertyDef.getName(), list);
        }
    }

    @SuppressWarnings("unchecked")
    private UploadPropInfo buildProperty(String propName, Object propValue) {

        UploadPropInfo propertyDef = new UploadPropInfo();
        propertyDef.setValue(propValue);
        propertyDef.setName(propName);
        if (propValue instanceof Map) {
            if (((Map<String, Object>) propValue).containsKey(TYPE.getElementName())) {
                propertyDef.setType(((Map<String, Object>) propValue)
                        .get(TYPE.getElementName()).toString());
            }
            if (containsGetInput(propValue)) {
                fillInputRecursively(propName, (Map<String, Object>) propValue, propertyDef);
            }

            if (((Map<String, Object>) propValue).containsKey(DESCRIPTION.getElementName())) {
                propertyDef.setDescription(((Map<String, Object>) propValue)
                        .get(DESCRIPTION.getElementName()).toString());
            }
            if (((Map<String, Object>) propValue)
                    .containsKey(DEFAULT_VALUE.getElementName())) {
                propertyDef.setValue(((Map<String, Object>) propValue)
                        .get(DEFAULT_VALUE.getElementName()));
            }
            if (((Map<String, Object>) propValue).containsKey(IS_PASSWORD.getElementName())) {
                propertyDef.setPassword(Boolean.getBoolean(((Map<String, Object>) propValue)
                        .get(IS_PASSWORD.getElementName()).toString()));
            } else {
                propertyDef.setValue(propValue);
            }
        } else if (propValue instanceof List) {
            List<Object> propValueList = (List<Object>) propValue;

            fillInputsListRecursively(propertyDef, propValueList);
            propertyDef.setValue(propValue);
        }

        return propertyDef;
    }

    @SuppressWarnings("unchecked")
    private boolean containsGetInput(Object propValue) {
        return ((Map<String, Object>) propValue).containsKey(GET_INPUT.getElementName())
                || ImportUtils.containsGetInput(propValue);
    }

    @SuppressWarnings("unchecked")
    private void fillInputsListRecursively(UploadPropInfo propertyDef, List<Object> propValueList) {
        for (Object objValue : propValueList) {

            if (objValue instanceof Map) {
                Map<String, Object> objMap = (Map<String, Object>) objValue;
                if (objMap.containsKey(GET_INPUT.getElementName())) {
                    fillInputRecursively(propertyDef.getName(), objMap, propertyDef);
                } else {
                    Set<String> keys = objMap.keySet();
                    findAndFillInputsListRecursively(propertyDef, objMap, keys);
                }
            } else if (objValue instanceof List) {
                List<Object> propSubValueList = (List<Object>) objValue;
                fillInputsListRecursively(propertyDef, propSubValueList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void findAndFillInputsListRecursively(UploadPropInfo propertyDef, Map<String, Object> objMap,
                                                  Set<String> keys) {
        for (String key : keys) {
            Object value = objMap.get(key);
            if (value instanceof Map) {
                fillInputRecursively(key, (Map<String, Object>) value, propertyDef);
            } else if (value instanceof List) {
                List<Object> propSubValueList = (List<Object>) value;
                fillInputsListRecursively(propertyDef, propSubValueList);
            }
        }
    }

    private void fillInputRecursively(String propName, Map<String, Object> propValue, UploadPropInfo propertyDef) {

        if (propValue.containsKey(GET_INPUT.getElementName())) {
            Object getInput = propValue.get(GET_INPUT.getElementName());
            GetInputValueDataDefinition getInputInfo = new GetInputValueDataDefinition();
            List<GetInputValueDataDefinition> getInputs = propertyDef.getGet_input();
            if (getInputs == null) {
                getInputs = new ArrayList<>();
            }
            if (getInput instanceof String) {

                getInputInfo.setInputName((String) getInput);
                getInputInfo.setPropName(propName);

            } else if (getInput instanceof List) {
                fillInput(propName, getInput, getInputInfo);
            }
            getInputs.add(getInputInfo);
            propertyDef.setGet_input(getInputs);
            propertyDef.setValue(propValue);
        } else {
            findAndFillInputRecursively(propValue, propertyDef);
        }
    }

    @SuppressWarnings("unchecked")
    private void findAndFillInputRecursively(Map<String, Object> propValue, UploadPropInfo propertyDef) {
        for (String propName : propValue.keySet()) {
            Object value = propValue.get(propName);
            if (value instanceof Map) {
                fillInputRecursively(propName, (Map<String, Object>) value, propertyDef);

            } else if (value instanceof List) {
                fillInputsRecursively(propertyDef, propName, (List<Object>) value);
            }
        }
    }

    private void fillInputsRecursively(UploadPropInfo propertyDef, String propName, List<Object> inputs) {
        inputs.stream()
                .filter(o -> o instanceof Map)
                .forEach(o -> fillInputRecursively(propName, (Map<String, Object>)o, propertyDef));
    }

    @SuppressWarnings("unchecked")
    private void fillInput(String propName, Object getInput, GetInputValueDataDefinition getInputInfo) {
        List<Object> getInputList = (List<Object>) getInput;
        getInputInfo.setPropName(propName);
        getInputInfo.setInputName((String) getInputList.get(0));
        if (getInputList.size() > 1) {
            Object indexObj = getInputList.get(1);
            if (indexObj instanceof Integer) {
                getInputInfo.setIndexValue((Integer) indexObj);
            } else if (indexObj instanceof Float) {
                int index = ((Float) indexObj).intValue();
                getInputInfo.setIndexValue(index);
            } else if (indexObj instanceof Map && ((Map<String, Object>) indexObj)
                    .containsKey(GET_INPUT.getElementName())) {
                Object index = ((Map<String, Object>) indexObj)
                        .get(GET_INPUT.getElementName());
                GetInputValueDataDefinition getInputInfoIndex = new GetInputValueDataDefinition();
                getInputInfoIndex.setInputName((String) index);
                getInputInfoIndex.setPropName(propName);
                getInputInfo.setGetInputIndex(getInputInfoIndex);
            }
            getInputInfo.setList(true);
        }
    }

    private boolean valueNotContainsPattern(Pattern pattern, Object propValue) {
        return propValue == null || !pattern.matcher(propValue.toString()).find();
    }

    private Map<String, Object> failIfNoNodeTemplates(String fileName) {
        titanDao.rollback();
        throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, fileName);
    }

    private Object failIfNotTopologyTemplate(String fileName) {
        titanDao.rollback();
        throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, fileName);
    }

    private void rollbackWithException(ActionStatus actionStatus, String... params) {
        titanDao.rollback();
        throw new ComponentException(actionStatus, params);
    }

    private void failOnMissingCapabilityTypes(GroupDefinition groupDefinition, List<String> missingCapTypes) {
        log.debug("#failOnMissingCapabilityTypes - Failed to validate the capabilities of the group {}. The capability types {} are missing on the group type {}. ", groupDefinition.getName(), missingCapTypes.toString(), groupDefinition.getType());
        if(CollectionUtils.isNotEmpty(missingCapTypes)) {
            rollbackWithException(ActionStatus.MISSING_CAPABILITY_TYPE, missingCapTypes.toString());
        }
    }

    private void failOnMissingCapabilityNames(GroupDefinition groupDefinition, List<String> missingCapNames) {
        log.debug("#failOnMissingCapabilityNames - Failed to validate the capabilities of the group {}. The capabilities with the names {} are missing on the group type {}. ", groupDefinition.getName(), missingCapNames.toString(), groupDefinition.getType());
        rollbackWithException(ActionStatus.MISSING_CAPABILITIES, missingCapNames.toString(), CapabilityDataDefinition.OwnerType.GROUP.getValue(), groupDefinition.getName());
    }

}
