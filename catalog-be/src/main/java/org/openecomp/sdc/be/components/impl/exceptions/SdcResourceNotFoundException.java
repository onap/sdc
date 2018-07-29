package org.openecomp.sdc.be.components.impl.exceptions;

public class SdcResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4431147390120646573L;

	public SdcResourceNotFoundException(){}

	public SdcResourceNotFoundException(String message){
		super(message);
	}
}
