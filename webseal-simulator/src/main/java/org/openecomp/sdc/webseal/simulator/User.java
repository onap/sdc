package org.openecomp.sdc.webseal.simulator;

public class User {

	private String firstName;
	private String lastName;
	private String email;
	private String userId;
	
	private String password;
	
	public User(){		
	}
	
	public User(String userId){
		setUserId(userId);
	}
	
	public User(String firstName,String lastName,String email,String userId,String password){
		setUserId(userId);
		setFirstName(firstName);
		setLastName(lastName);
		setEmail(email);
		setPassword(password);
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
	
	public String getUserRef() {
		return "<a href='?userId="+getUserId()+"&password="+getPassword()+"'>"+getFirstName()+" "+getLastName()+"</a>";
	}

	
}
