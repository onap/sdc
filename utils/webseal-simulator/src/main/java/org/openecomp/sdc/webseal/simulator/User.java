package org.openecomp.sdc.webseal.simulator;

public class User {

	private String firstName;
	private String lastName;
	private String email;
	private String userId;
	private String role;
	
	private String password;
	
	public User(){		
	}
	
	public User(String userId){
		setUserId(userId);
	}

	public User(String firstName,String lastName,String email,String userId, String role, String password){
		setUserId(userId);
		setFirstName(firstName);
		setLastName(lastName);
		setEmail(email);
		setPassword(password);
		setRole(role);
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getUserRef() {
		return "<a href='?userId="+getUserId()+"&password="+getPassword()+"'>"+getFirstName()+" "+getLastName()+"</a>";
	}
	
	public String getUserCreateRef() {
		return "<a href='create?userId="+getUserId()+"&firstName="+getFirstName()+"&lastName="+getLastName()+"&role=" + getRole() + "&email=" + getEmail() + "' target='resultFrame'>create</a>";
	}

	@Override
	public String toString() {
		return "User [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", userId=" + userId
				+ ", role=" + role + ", password=" + password + "]";
	}

}
