package org.openecomp.sdc.be.model.utils;

import com.google.common.base.Strings;
import fj.data.Either;
import org.apache.commons.collections.SetUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;


/**
 * Types comparison utils
 * The class is required since origin class "equals" methods
 * take in account fields that should be ignored during update of that types.
 * @author dr2032
 *
 */
public class TypeCompareUtils {

    private TypeCompareUtils() {
    }
    
    public static <R> Either<R, StorageOperationStatus> typeAlreadyExists() {
        return Either.right(StorageOperationStatus.OK);
    }

    public static boolean isGroupTypesEquals(GroupTypeDefinition gt1, GroupTypeDefinition gt2) {
        if (gt1 == gt2) {
            return true;
        }
        if (gt1 == null || gt2 == null) {
            return false;
        }
        
        /*
         * We compare here attributes, capabilities and not inherited properties of group types.
         * So even if properties of group type parent were changed it will not effect on comparison of these group types.
         */
        return Objects.equals(gt1.getType(), gt2.getType()) &&
                Objects.equals(gt1.getName(), gt2.getName()) &&
                Objects.equals(gt1.getIcon(), gt2.getIcon()) &&
                Objects.equals(gt1.getVersion(), gt2.getVersion()) &&
                Objects.equals(gt1.getDerivedFrom(), gt2.getDerivedFrom()) &&
                Objects.equals(gt1.getMembers(), gt2.getMembers()) &&
                Objects.equals(gt1.getMetadata(), gt2.getMetadata()) &&
                capabilitiesEqual(gt1.getCapabilities(), gt2.getCapabilities()) && 
                propertiesEquals(collectNotInheritedProperties(gt1.getProperties(), gt1.getUniqueId()), 
                                collectNotInheritedProperties(gt2.getProperties(), gt2.getUniqueId()));
    }
    
    public static boolean isCapabilityTypesEquals(CapabilityTypeDefinition ct1, CapabilityTypeDefinition ct2) {
        if (ct1 == ct2) {
            return true;
        }
        
        if (ct1 == null || ct2 == null) {
            return false;
        }
        
        return Objects.equals(ct1.getType(), ct2.getType()) &&
               Objects.equals(ct1.getDerivedFrom(), ct2.getDerivedFrom()) &&
               Objects.equals(ct1.getDescription(), ct2.getDescription()) &&
               SetUtils.isEqualSet(ct1.getValidSourceTypes(), ct2.getValidSourceTypes()) &&
               propertiesEquals(ct1.getProperties(), ct2.getProperties());
    }

    public static boolean isRelationshipTypesEquals(RelationshipTypeDefinition rs1, RelationshipTypeDefinition rs2) {
        if (rs1 == rs2) {
            return true;
        }

        if (rs1 == null || rs2 == null) {
            return false;
        }

        return Objects.equals(rs1.getType(), rs2.getType()) &&
                Objects.equals(rs1.getDerivedFrom(), rs2.getDerivedFrom()) &&
                Objects.equals(rs1.getDescription(), rs2.getDescription()) &&
                SetUtils.isEqualSet(rs1.getValidSourceTypes(), rs2.getValidSourceTypes()) &&
                propertiesEquals(rs1.getProperties(), rs2.getProperties());
    }
    
    private static boolean propertiesEquals(Map<String, PropertyDefinition> props1, Map<String, PropertyDefinition> props2) {
        if (props1 == props2) {
            return true;
        }
        
        if (isEmpty(props1) && isEmpty(props2)) {
            return true;
        }
        else if(props1 == null || props2 == null) {
            return false;
        }
        else if(props1.size() != props2.size())
        {
            return false;
        }

        return props2.entrySet().stream()
                                .allMatch(entry -> propertyEquals(props1.get(entry.getKey()), entry.getValue()));
        
    }
    
    public static boolean propertiesEquals(List<PropertyDefinition> props1, List<PropertyDefinition> props2) {
        if (props1 == props2) {
            return true;
        }
        
        if (isEmpty(props1) && isEmpty(props2)) {
            return true;
        }
        else if(props1 == null || props2 == null) {
            return false;
        }
        else if(props1.size() != props2.size())
        {
            return false;
        }
        
        Map<String, PropertyDefinition> pt1PropsByName = MapUtil.toMap(props1, PropertyDefinition::getName);
        return props2.stream()
                       .allMatch(pt2Prop -> propertyEquals(pt1PropsByName.get(pt2Prop.getName()), pt2Prop));
    }

    private static boolean propertyEquals(PropertyDefinition prop1, PropertyDefinition prop2) {
        if (prop1 == prop2) {
            return true;
        }
        if (prop1 == null || prop2 == null) {
            return false;
        }
        return Objects.equals(prop1.getDefaultValue(), prop2.getDefaultValue()) &&
                prop1.isDefinition() == prop2.isDefinition() &&
                Objects.equals(prop1.getDescription(), prop2.getDescription()) &&
                prop1.isPassword() == prop2.isPassword() &&
                prop1.isRequired() == prop2.isRequired() &&
                Objects.equals(prop1.getSchemaType(), prop2.getSchemaType()) &&
                Objects.equals(prop1.getType(), prop2.getType());
    }

    private static boolean capabilitiesEqual(Map<String, CapabilityDefinition> caps1, Map<String, CapabilityDefinition>  caps2) { 
        if (caps1 == caps2) {
            return true;
        }
        
        if (caps1 == null || caps2 == null) {
            return false;
        }
        
        if(caps1.size() != caps2.size()) {
            return false;
        }
            
        return caps2.entrySet().stream()
                .allMatch(capEntry2 -> capabilityEquals(caps1.get(capEntry2.getKey()), capEntry2.getValue()));
    }

    public static boolean capabilityEquals(CapabilityDefinition capDef1, CapabilityDefinition capDef2) {
        return Objects.equals(capDef1.getName(), capDef2.getName()) &&
                Objects.equals(capDef1.getType(), capDef2.getType()) &&
                Objects.equals(capDef1.getDescription(), capDef2.getDescription()) &&
                propValuesEqual(capDef1.getProperties(), capDef2.getProperties());
    }

    private static boolean propValuesEqual(final List<ComponentInstanceProperty> props1, final List<ComponentInstanceProperty> props2) {
        Map<String, String> propValues1 = toValueMap(props1);
        Map<String, String> propValues2 = toValueMap(props2);
        
        return propValues1.equals(propValues2);
    }

    /**
     * @param props
     * @return
     */
    private static Map<String, String> toValueMap(final List<ComponentInstanceProperty> props) {
        return props.stream()
                    .filter(TypeCompareUtils::isCapabilityPropValue)
                    .collect(Collectors.toMap(ComponentInstanceProperty::getName, p -> p.getValue() != null? p.getValue(): ""));
    }
    
    /**
     * Returns true if the property object was created from property value or false otherwise
     * 
     * We try to segregate original properties values from dummy ones created from relevant properties.
     * Such dummy property value doesn't have their valueUniqueId but it has uniqueId taken from property.
     *    
     * @param property
     * @return
     */
    private static boolean isCapabilityPropValue(ComponentInstanceProperty property) {
        return property.getValueUniqueUid() != null || property.getUniqueId() == null;
    }
    
    /**
     * Collect properties of resource that belongs to it without taking in account properties inherited from resource parents.
     */
    private static List<PropertyDefinition> collectNotInheritedProperties(List<PropertyDefinition> properties, 
                                                                          String resourceId) {
        if (Strings.isNullOrEmpty(resourceId)) {
            return properties;
        }
        
        return properties.stream()
                         .filter(prop-> !isInherited(prop, resourceId))
                         .collect(Collectors.toList());
    }
    
    
    private static boolean isInherited(PropertyDefinition prop, String resourceId) {
        return prop.getUniqueId() != null && 
                !prop.getUniqueId().equals(UniqueIdBuilder.buildPropertyUniqueId(resourceId, prop.getName()));
    }
    
}
