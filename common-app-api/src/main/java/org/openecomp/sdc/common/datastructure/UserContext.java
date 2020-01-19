package org.openecomp.sdc.common.datastructure;


import java.util.Set;

public class UserContext {


    /**
     * a pojo which holds the business logic layer to be aware of the user context as received in the authentication cookie
     * Story https://jira.web.labs.att.com/browse/ASDC-232
     * Author: Idan Agam
     */


    private String userId;
    private String firstName;
    private String lastName;

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

    private Set<String> userRoles;


    public UserContext(String userId, Set<String> userRoles, String firstName, String lastName) {
        this.userId = userId;
        this.userRoles = userRoles;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserContext(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<String> userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public String toString() {
        return "UserContext{" + "userId='" + userId + '\'' + ", firstName='" + firstName + '\'' + ", lastname='" + lastName + '\'' + ", userRoles=" + userRoles + '}';
    }
}




