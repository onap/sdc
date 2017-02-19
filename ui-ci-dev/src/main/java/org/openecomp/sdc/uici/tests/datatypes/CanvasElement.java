package org.openecomp.sdc.uici.tests.datatypes;

import org.apache.commons.lang3.tuple.ImmutablePair;

public final class CanvasElement {
	private final String uniqueId;
	private ImmutablePair<Integer, Integer> location;
	private String elementName;

	public String getElementName() {
		return elementName;
	}

	public CanvasElement(String uniqueId, String elementName, ImmutablePair<Integer, Integer> location) {
		super();
		this.uniqueId = uniqueId;
		this.location = location;
		this.elementName = elementName;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public ImmutablePair<Integer, Integer> getLocation() {
		return location;
	}

	public void setLocation(ImmutablePair<Integer, Integer> location) {
		this.location = location;
	}

}
