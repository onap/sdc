package org.onap.sdc.tosca.services;

import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class MyPropertyUtils extends PropertyUtils {
    //Unsorted properties
    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bnAccess) {
        return new LinkedHashSet<>(getPropertiesMap(type,
                BeanAccess.FIELD).values());
    }

    @Override
    public Property getProperty(Class<?> type, String name) {
        String updatedName = name;
        if (YamlUtil.DEFAULT.equals(updatedName)) {
            updatedName = YamlUtil.DEFAULT_STR;
        }
        return super.getProperty(type, updatedName);
    }

}
