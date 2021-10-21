package org.openecomp.sdc.be.components.attribute;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class GetOutputUtilsTest {

    @Test
    public void isGetOutputValueForOutputTest(){
        String testID = "abcdef";

        GetOutputValueDataDefinition testSubject = new GetOutputValueDataDefinition();
        testSubject.setOutputId(testID);
        assertTrue(GetOutputUtils.isGetOutputValueForOutput(testSubject,testID));

        testSubject.setOutputId("");
        assertFalse(GetOutputUtils.isGetOutputValueForOutput(testSubject,testID));

        GetOutputValueDataDefinition outputIndex = new GetOutputValueDataDefinition();
        outputIndex.setOutputId(testID);
        testSubject.setGetOutputIndex(outputIndex);
        assertTrue(GetOutputUtils.isGetOutputValueForOutput(testSubject,testID));
    }

}
