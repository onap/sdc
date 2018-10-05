package org.openecomp.sdc.be.datamodel;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceRelationsTest {

	private NameIdPair getNameIdPairWrapper() {
		return new NameIdPair("mock", "mock");
	}

	@Test
	public void testServiceRelations()  {

		NameIdPairWrapper testNameIdPairWrapper;
		NameIdPair nameIdPair = new NameIdPair("mock", "mock");

		testNameIdPairWrapper =  new NameIdPairWrapper();
		testNameIdPairWrapper.init(nameIdPair);

		Set<NameIdPairWrapper> result = new HashSet<NameIdPairWrapper>();
		result.add(testNameIdPairWrapper);


		ServiceRelations testServiceRelations = new ServiceRelations();
		ServiceRelations testServiceRelationsWithRelations = new ServiceRelations(result);
		testServiceRelations.setRelations(result);
		Set<NameIdPairWrapper> getResult = testServiceRelations.getRelations();
		assertThat(getResult).isEqualTo(result);
	}

}