package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HeatStringConverterTest {

    @Test
    public void convertString_strWithQuotes_returnStringAsIs()  {
        String str = "'i'm string with \"quote\"'";
        String convert = HeatStringConverter.getInstance().convert(str, null, null);
        assertEquals(str, convert);
    }

}
