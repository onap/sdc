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
package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.ui.model.UIConstraint;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConstraintConvertorTest {

    @Test
    public void convertStatic(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size: {equal: some static}\n");
        assertNotNull(uiConstraint);
        assertEquals(uiConstraint.getConstraintOperator(),"equal");
        assertEquals(uiConstraint.getValue(),"some static");
        assertEquals(uiConstraint.getServicePropertyName().trim(),"mem_size");
    }

    @Test
    public void convertFromStatic(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "equal" , ConstraintConvertor.STATIC_CONSTRAINT, "some static");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size: {equal: some static}\n", constraint);
    }

    @Test
    public void convertSelfProperty(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size:\n {equal: { get_property: [SELF, size] }}");
        assertNotNull(uiConstraint);
        assertEquals(uiConstraint.getConstraintOperator(),"equal");
        assertEquals(uiConstraint.getValue(),"size");
        assertEquals(uiConstraint.getServicePropertyName().trim(),"mem_size");
        assertEquals(uiConstraint.getSourceName().trim(),"SELF");
        assertEquals(uiConstraint.getSourceType(), ConstraintConvertor.PROPERTY_CONSTRAINT);
    }

    @Test
    public void convertFromSelfProperty(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "equal" , ConstraintConvertor.PROPERTY_CONSTRAINT, "SELF" ,"some static");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size:\n" + "  equal:\n" + "    get_property: [SELF, some static]\n", constraint);
    }

    @Test
    public void convertCIProperty(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size:\n" + "  equal: { get_property: [A, size]}");
        assertNotNull(uiConstraint);
        assertEquals(uiConstraint.getConstraintOperator(),"equal");
        assertEquals(uiConstraint.getValue(),"size");
        assertEquals(uiConstraint.getServicePropertyName().trim(),"mem_size");
        assertEquals(uiConstraint.getSourceName().trim(),"A");
    }


    @Test
    public void convertFromCIProperty(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "equal" , ConstraintConvertor.PROPERTY_CONSTRAINT, "A" ,"size");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size:\n" + "  equal:\n" + "    get_property: [A, size]\n", constraint);
    }

    @Test
    public void convertServiceTemplateInput(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size: {equal: {get_input: InputName}}\n");
        assertNotNull(uiConstraint);
    }

    @Test
    public void convertFromServiceTemplateInput(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "equal" , ConstraintConvertor.SERVICE_INPUT_CONSTRAINT, "InputName");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size:\n  equal: {get_input: InputName}\n", constraint);
    }

    @Test
    public void convertGreaterThanStatic(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size: {greater_than: 2}\n");
        assertNotNull(uiConstraint);
        assertEquals(uiConstraint.getConstraintOperator(),"greater_than");
        assertEquals(uiConstraint.getValue(),2);
        assertEquals(uiConstraint.getServicePropertyName().trim(),"mem_size");
    }

    @Test
    public void convertFromGreaterThanStatic(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "greater_than" , ConstraintConvertor.STATIC_CONSTRAINT, 2);
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size: {greater_than: 2}\n", constraint);
    }

    @Test
    public void convertLessThanServiceProperty(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size: {less_then: {get_input: InputName}}");
        assertNotNull(uiConstraint);
    }

    @Test
    public void convertFromLessThanServiceProperty(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "less_then" , ConstraintConvertor.SERVICE_INPUT_CONSTRAINT, "InputName");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size:\n" + "  less_then: {get_input: InputName}\n", constraint);
    }

    @Test
    public void convertFromEqualStaticMap(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "equal" , ConstraintConvertor.STATIC_CONSTRAINT, "{x: xx,"+
                                                                                                                          " y: yy}\n");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size:\n" + "  equal: {x: xx, y: yy}\n", constraint);
    }

    @Test
    public void convertStringToMap(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size:\n" + "  equal: {x: xx, y: yy}\n");
        assertNotNull(uiConstraint);
        assertTrue(uiConstraint.getValue() instanceof Map);
    }

    @Test
    public void convertFromEqualStaticList(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = new UIConstraint("mem_size", "equal" , ConstraintConvertor.STATIC_CONSTRAINT, "[x, y]\n");
        String constraint  = constraintConvertor.convert(uiConstraint);
        assertNotNull(constraint);
        assertEquals("mem_size:\n" + "  equal: [x, y]\n", constraint);
    }

    @Test
    public void convertStringToList(){
        ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        UIConstraint uiConstraint = constraintConvertor.convert("mem_size:\n" + "  equal: [x, y]\n");
        assertNotNull(uiConstraint);
        assertTrue(uiConstraint.getValue() instanceof List);
    }
}
