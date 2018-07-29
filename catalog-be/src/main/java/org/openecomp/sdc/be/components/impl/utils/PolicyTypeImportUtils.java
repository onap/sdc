package org.openecomp.sdc.be.components.impl.utils;

import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PolicyTypeImportUtils {

    private PolicyTypeImportUtils() {
    }

    public static boolean isPolicyTypesEquals(PolicyTypeDefinition pt1, PolicyTypeDefinition pt2) {
        if (pt1 == pt2) {
            return true;
        }
        if (pt1 == null || pt2 == null) {
            return false;
        }
        return Objects.equals(pt1.getType(), pt2.getType()) &&
                Objects.equals(pt1.getName(), pt2.getName()) &&
                Objects.equals(pt1.getIcon(), pt2.getIcon()) &&
                Objects.equals(pt1.getVersion(), pt2.getVersion()) &&
                Objects.equals(pt1.getDerivedFrom(), pt2.getDerivedFrom()) &&
                Objects.equals(pt1.getTargets(), pt2.getTargets()) &&
                Objects.equals(pt1.getMetadata(), pt2.getMetadata()) &&
                Objects.equals(pt1.getDescription(), pt2.getDescription()) &&
                PolicyTypeImportUtils.isPolicyPropertiesEquals(pt1.getProperties(), pt2.getProperties());
    }

    private static boolean isPolicyPropertiesEquals(List<PropertyDefinition> pt1Props, List<PropertyDefinition> pt2Props) {
        if (pt1Props == pt2Props) {
            return true;
        }
        if (pt1Props == null && isEmpty(pt2Props) || pt2Props == null && isEmpty(pt1Props)) {
            return true;
        }
        if (isPropertiesListSizesNotEquals(pt1Props, pt2Props)) {
            return false;
        }
        Map<String, PropertyDefinition> pt1PropsByName = MapUtil.toMap(pt1Props, PropertyDefinition::getName);
        long numberOfEqualsProperties = pt2Props.stream().filter(pt2Prop -> policyPropertyEquals(pt1PropsByName.get(pt2Prop.getName()), pt2Prop)).count();
        return numberOfEqualsProperties == pt1Props.size();
    }

    private static boolean policyPropertyEquals(PropertyDefinition pt1Prop, PropertyDefinition pt2Prop) {
        if (pt1Prop == pt2Prop) {
            return true;
        }
        if (pt1Prop == null || pt2Prop == null) {
            return false;
        }
        return Objects.equals(pt1Prop.getDefaultValue(), pt2Prop.getDefaultValue()) &&
               Objects.equals(pt1Prop.isDefinition(), pt2Prop.isDefinition()) &&
               Objects.equals(pt1Prop.getDescription(), pt2Prop.getDescription()) &&
               Objects.equals(pt1Prop.isPassword(), pt2Prop.isPassword()) &&
               Objects.equals(pt1Prop.isRequired(), pt2Prop.isRequired()) &&
               Objects.equals(pt1Prop.getSchemaType(), pt2Prop.getSchemaType()) &&
               Objects.equals(pt1Prop.getType(), pt2Prop.getType());
    }

    private static boolean isPropertiesListSizesNotEquals(List<PropertyDefinition> pt1Props, List<PropertyDefinition> pt2Props) {
        return isEmpty(pt1Props) && isNotEmpty(pt2Props) ||
               isEmpty(pt2Props) && isNotEmpty(pt1Props) ||
               pt1Props.size() != pt2Props.size();
    }

}
