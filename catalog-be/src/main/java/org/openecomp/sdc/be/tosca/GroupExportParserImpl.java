package org.openecomp.sdc.be.tosca;

import fj.data.Either;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.SdcResourceNotFoundException;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.utils.ComponentUtilities;
import org.openecomp.sdc.be.model.utils.GroupUtils;
import org.openecomp.sdc.be.tosca.model.*;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.openecomp.sdc.be.model.utils.ComponentUtilities.getComponentInstanceNameByInstanceId;

@Service
public class GroupExportParserImpl implements GroupExportParser {

    private static final Logger log = Logger.getLogger(GroupExportParserImpl.class);

    private Map<String, DataTypeDefinition> dataTypes;
    private ApplicationDataTypeCache dataTypeCache;
    private PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();
    
    @Autowired
	public GroupExportParserImpl(ApplicationDataTypeCache dataTypeCache) {
		this.dataTypeCache = dataTypeCache;
		this.dataTypes = getDataTypes();
	}
    
    private Map<String, DataTypeDefinition> getDataTypes()  {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.error("Failed to retrieve all data types {}", dataTypesEither.right().value()); 
			throw new SdcResourceNotFoundException(); 
		}
		
		return dataTypesEither.left().value();
	}
    
	@Override
	public Map<String, ToscaGroupTemplate> getGroups(Component component) {
		List<GroupDefinition> groups = component.getGroups();

		if (isEmpty(groups)) {
			return null;
		}
		
		return groups.stream()
				.collect(toMap(GroupDefinition::getName,
						  group -> getToscaGroupTemplate(component, group)));
	}
	
	@Override
	public ToscaGroupTemplate getToscaGroupTemplate(GroupInstance groupInstance, String componentInstanceInvariantName) {
		
		String groupName = groupInstance.getName();
		if (StringUtils.isNotEmpty(componentInstanceInvariantName)) {
			String prefix = componentInstanceInvariantName + Constants.GROUP_POLICY_NAME_DELIMETER;
			if (groupName.startsWith(prefix)) {
				groupName = groupName.substring(prefix.length());
			}
		}
		String invariantUUID = groupInstance.getInvariantUUID();
		String groupUUID = groupInstance.getGroupUUID();
		String version = groupInstance.getVersion();
		List<GroupInstanceProperty> groupInstanceProperties = groupInstance.convertToGroupInstancesProperties();
		String groupType = groupInstance.getType();
		String customizationUUID = groupInstance.getCustomizationUUID();

		IToscaMetadata toscaMetadata = getToscaGroupTemplateMetadata(groupName, invariantUUID, groupUUID, version, groupType, customizationUUID);		
		Map<String, Object> properties = getToscaGroupTemplateProperties(groupInstanceProperties);
		
        return new ToscaGroupTemplate(groupType, toscaMetadata, properties);
	}

	private ToscaGroupTemplate getToscaGroupTemplate(Component component, GroupDefinition groupDefinition) {

		String groupName = groupDefinition.getName();
		String invariantUUID = groupDefinition.getInvariantUUID();
		String groupUUID = groupDefinition.getGroupUUID();
		String version = groupDefinition.getVersion();
		String groupType = groupDefinition.getType();
		List<PropertyDataDefinition> groupDefinitionProperties = groupDefinition.getProperties();
		
		List<String> members = getToscaGroupTemplateMembers(component, groupDefinition.getMembers());
		IToscaMetadata toscaMetadata = getToscaGroupTemplateMetadata(groupName, invariantUUID, groupUUID, version,groupType, null);		
		Map<String, Object> properties = getToscaGroupTemplateProperties(groupDefinitionProperties);
		Map<String, ToscaTemplateCapability> capabilities = getToscaGroupTemplateCapabilities(groupDefinition);

		return new ToscaGroupTemplate(groupType, members, toscaMetadata, properties, capabilities);
	}
	
	private Map<String, ToscaTemplateCapability> getToscaGroupTemplateCapabilities(GroupDefinition group) {
		if (isEmpty(group.getCapabilities())) {
			return null;
		}

		Map<String, ToscaTemplateCapability> toscaGroupTemplateCapabilities = group.getCapabilities().values()
				.stream()
				.flatMap(Collection::stream)
				.filter(c -> isNotEmptyProperties(c.getProperties()))
				.collect(toMap(c-> getCapabilityName(c, group), this::getToscaTemplateCapability));

		if (isNotEmpty(toscaGroupTemplateCapabilities)) {
			return toscaGroupTemplateCapabilities;
		} else {
			return null;
		}
	}

	private String getCapabilityName(CapabilityDefinition capability, GroupDefinition group) {
    	if(ComponentUtilities.isNotUpdatedCapReqName(group.getNormalizedName() + ".", capability.getName(), capability.getPreviousName())){
    		return capability.getName();
		}
		return capability.getPreviousName();
	}

	private boolean isNotEmptyProperties(List<ComponentInstanceProperty> properties) {
    	return isNotEmpty(properties) && properties.stream()
				.filter(isComponentInstancePropertiesNotEmpty())
				.findFirst()
				.isPresent();
	}

	private ToscaTemplateCapability getToscaTemplateCapability(CapabilityDefinition capability) {
		ToscaTemplateCapability toscaTemplateCapability = new ToscaTemplateCapability();
		Map<String, Object> toscaCapabilityProperties = capability.getProperties().stream()
				.filter(isComponentInstancePropertiesNotEmpty())
				.collect(toMap(ComponentInstanceProperty::getName,
										  this::fetchCapabilityValue));
		if(isNotEmpty(toscaCapabilityProperties)) {
			toscaTemplateCapability.setProperties(toscaCapabilityProperties);
		}
		return toscaTemplateCapability;
	}

	private Predicate<? super ComponentInstanceProperty> isComponentInstancePropertiesNotEmpty() {
		return c -> {
			return (c.getName() != null && (c.getValue() != null || c.getDefaultValue() != null));
		};
	}

	private String fetchCapabilityValue(ComponentInstanceProperty componentInstanceProperty) {
		if(componentInstanceProperty.getValue() != null) {
			return componentInstanceProperty.getValue();
		}else {
			return componentInstanceProperty.getDefaultValue();
		}
	}

	private List<String> getToscaGroupTemplateMembers(Component component, Map<String, String> members) {
		if (members == null) {
			return null;
		}
		return members.values()
				.stream()
				.map(memberId -> getMemberNameByMemberId(component, memberId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}

	private Optional<String> getMemberNameByMemberId(Component component, String memberId) {
    	return getComponentInstanceNameByInstanceId(component, memberId);
	}

	private IToscaMetadata getToscaGroupTemplateMetadata(String groupName,String invariantUUID,
			                                             String groupUUID,String version,String type, String customizationUUID) {
				
		IToscaMetadata toscaMetadata = getToscaMetadataByType(type);
		
		toscaMetadata.setName(groupName);
		toscaMetadata.setInvariantUUID(invariantUUID);
		toscaMetadata.setUUID(groupUUID);
		toscaMetadata.setVersion(version);
		toscaMetadata.setCustomizationUUID(customizationUUID);
		return toscaMetadata;
	}

	private IToscaMetadata getToscaMetadataByType(String type) {
		IToscaMetadata toscaMetadata;
		if (GroupUtils.isVfModule(type)) {
			toscaMetadata = new VfModuleToscaMetadata();
		} else {
			toscaMetadata = new ToscaMetadata();
		}
		return toscaMetadata;
	}

	private Map<String, Object> getToscaGroupTemplateProperties(List<? extends PropertyDataDefinition> tempProperties) {
				
		if (isEmpty(tempProperties)) {
			return null;
		}
				
		Map<String, Object> props = new HashMap<>();

		tempProperties.forEach(input -> 
			propertyConvertor.convertAndAddValue(dataTypes, props, input, getPropertyValue(input))
		);

		if (props.isEmpty()) {
			return null;
		} else {
			return props;
		}	
	}

	private Supplier<String> getPropertyValue(PropertyDataDefinition propertyDataDefinition) {
		return () -> {
			if (isNotEmpty(propertyDataDefinition.getValue())) {
				return propertyDataDefinition.getValue();
			} else {
				return propertyDataDefinition.getDefaultValue();
			}
		};
	}
}
