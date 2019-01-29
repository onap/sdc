package org.openecomp.sdc.be.datamodel.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ConstraintConvertor {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintConvertor.class);

    public static final String EQUAL_OPERATOR = ConstraintType.EQUAL.getTypes().get(1);
    public static final String GREATER_THAN_OPERATOR = ConstraintType.GREATER_THAN.getTypes().get(1);
    public static final String LESS_THAN_OPERATOR = ConstraintType.LESS_THAN.getTypes().get(1);
    public static final String STATIC_CONSTRAINT = "static";
    public static final String PROPERTY_CONSTRAINT = "property";
    public static final String SERVICE_INPUT_CONSTRAINT = "service_input";
    public static final String SELF = "SELF";
    private static final Set<String> SUPPORTED_CONSTRAINT_LIST =
            ImmutableSet.of(EQUAL_OPERATOR, GREATER_THAN_OPERATOR, LESS_THAN_OPERATOR);
    private static final String GET_INPUT = "get_input";
    private static final String GET_PROPERTY = "get_property";
    private static final Set<String> SUPPORTED_FUNCTIONS = ImmutableSet.of(GET_INPUT, GET_PROPERTY);


    public UIConstraint convert(String inConstraint) {
        Yaml yamlSource = new Yaml();
        UIConstraint uiConstraint = new UIConstraint();
        Object content1 = yamlSource.load(inConstraint);
        if (content1 instanceof Map) {
            Map map1 = (Map) content1;
            Object key = map1.keySet().iterator().next();
            uiConstraint.setServicePropertyName(key.toString());
            Object content2 = map1.get(key);
            if (content2 instanceof Map && handleServiceConstraint(uiConstraint, (Map) content2)) {
                return uiConstraint;
            }
        }
        return null;
    }

    private boolean handleServiceConstraint(UIConstraint uiConstraint, Map content2) {
        Map map2 = content2;
        Object key2 = map2.keySet().iterator().next();
        final String operator = key2.toString();
        if (SUPPORTED_CONSTRAINT_LIST.contains(operator)) {
            uiConstraint.setConstraintOperator(operator);
        }
        Object content3 = map2.get(key2);
        if (content3 instanceof String || content3 instanceof Number || content3 instanceof Boolean) {
            uiConstraint.setValue(content3);
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            return true;
        } else if (content3 instanceof List) {
            List list1 = (List) content3;
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            uiConstraint.setValue(list1);
            return true;
        } else if (content3 instanceof Map) {
            Map map3 = (Map) content3;
            Map.Entry entry = (Map.Entry) map3.entrySet().iterator().next();
            final String firstKey = entry.getKey().toString().trim();
            if (handleSupportedFunctions(uiConstraint, entry, firstKey)) {
                return true;
            }
            uiConstraint.setValue(content3);
            return true;
        }
        return false;
    }

    private boolean handleSupportedFunctions(UIConstraint uiConstraint, Map.Entry entry, String firstKey) {
        if (SUPPORTED_FUNCTIONS.contains(firstKey)) {
            if (GET_INPUT.equals(firstKey)) {
                uiConstraint.setSourceType(SERVICE_INPUT_CONSTRAINT);
                uiConstraint.setValue(entry.getValue());
                return true;
            } else if (GET_PROPERTY.equals(firstKey)) {
                uiConstraint.setSourceType(PROPERTY_CONSTRAINT);
                final List<String> value = (List<String>) entry.getValue();
                uiConstraint.setSourceName(value.get(0));
                uiConstraint.setValue(value.get(1));
                return true;
            }
        }
        return false;
    }

    public List<String> convertToList(List<UIConstraint> uiConstraints) {
        List<String> retVal = new ArrayList<>();
        for (UIConstraint uiConstraint : uiConstraints) {
            String constraint = convert(uiConstraint);
            if (constraint != null) {
                retVal.add(constraint);
            }
        }
        return retVal;
    }

    public String convert(UIConstraint uiConstraint) {
        try {
            Map map1 = new HashMap();
            Map map2 = new HashMap();

            map1.put(uiConstraint.getServicePropertyName(), map2);
            if (uiConstraint.getSourceType().equals(STATIC_CONSTRAINT)) {
                Object value = uiConstraint.getValue();
                if (value instanceof String) {
                    value = new Yaml().load(value.toString());
                }
                map2.put(uiConstraint.getConstraintOperator(), value);
            } else if (uiConstraint.getSourceType().equals(PROPERTY_CONSTRAINT)) {
                List list1 = Arrays.asList(uiConstraint.getSourceName(), uiConstraint.getValue());
                Map map3 = ImmutableMap.of(GET_PROPERTY, list1);
                map2.put(uiConstraint.getConstraintOperator(), map3);
            } else if (uiConstraint.getSourceType().equals(SERVICE_INPUT_CONSTRAINT)) {
                Map map3 = ImmutableMap.of(GET_INPUT, uiConstraint.getValue());
                map2.put(uiConstraint.getConstraintOperator(), map3);
            }


            Yaml yamlSource = new Yaml();
            return yamlSource.dump(map1);
        } catch (NullPointerException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }
}
