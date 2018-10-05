package org.openecomp.sdc.be.datamodel;

import org.junit.Test;

import java.util.Set;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ForwardingPathsTest {

	@Test
	public void testForwardingPaths()  {
		ForwardingPaths testForwardingPaths = new ForwardingPaths();
		Set<String> path= new HashSet<>(Arrays.asList("test"));
		testForwardingPaths.setForwardingPathToDelete(path);
		Set<String> getPath = testForwardingPaths.getForwardingPathToDelete();
		assertThat(getPath).isEqualTo(path);
	}

}