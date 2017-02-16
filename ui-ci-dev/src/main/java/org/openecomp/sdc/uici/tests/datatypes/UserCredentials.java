package org.openecomp.sdc.uici.tests.datatypes;

import org.openecomp.sdc.be.model.User;

public class UserCredentials extends User {

	private String password;

	public UserCredentials(String userId, String password, String firstname, String lastname) {
		super();
		setUserId(userId);
		this.password = password;
		setFirstName(firstname);
		setLastName(lastname);
	}

	public UserCredentials() {
		super();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
