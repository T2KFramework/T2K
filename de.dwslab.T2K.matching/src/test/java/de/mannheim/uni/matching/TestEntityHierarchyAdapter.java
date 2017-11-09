package de.mannheim.uni.matching;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import java.util.Collection;

public class TestEntityHierarchyAdapter extends MatchingHierarchyAdapater<TestEntity, TestInstance> {

	@Override
	public Collection<TestInstance> getParts(TestEntity instance) {
		return instance.getParts();
	}

}
