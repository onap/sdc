package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomizationUUIDVerificator {

	public static void validateCustomizationUUIDuniqueness(List customizationUUIDs) {
		boolean hasNoDuplicates = CustomizationUUIDVerificator.containsUnique(customizationUUIDs);
		assertTrue("There are duplicate customizationUUIDs in list",hasNoDuplicates==true);
	}

	public static <T> boolean containsUnique(List<T> list){
	    Set<T> set = new HashSet<>();
	
	    for (T t: list){
	        if (!set.add(t))
	            return false;
	    }
	
	    return true;
	}

}
