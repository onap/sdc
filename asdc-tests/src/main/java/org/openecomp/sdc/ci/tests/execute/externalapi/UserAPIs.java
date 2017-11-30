/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.execute.externalapi;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.EcompUserRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.UserRestUtils;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserAPIs extends ComponentBaseTest {
	
	@Rule
	public static TestName name = new TestName();

	public UserAPIs() {
		super(name, UserAPIs.class.getName());
	}

	public User adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

	
	@Test
	public void createUserAllPosibleRoles() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		
		List<EcompRole> allRoles = getAllRoles();
	
		for (EcompRole ecompRole2 : allRoles) {
			try {
				
				///get list of users
				List<EcompUser> allusersList = getAllusersList();
				int sizeBeforeChange = allusersList.size();
	
				//create user
				ecompUser.setLoginId(getUser());
				ecompRole.setId((long) ecompRole2.getId());
				ecompRole.setName(ecompRole2.getName());
				System.out.println(ecompRole2.getName());
				Set<EcompRole> setRoles = new HashSet<EcompRole>();
				setRoles.add(ecompRole);
				ecompUser.setRoles(setRoles);
				RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
				BaseRestUtils.checkSuccess(pushUser);
				
				///get list of users verify list size changed
				allusersList = getAllusersList();
				int sizeAfterChange = allusersList.size();
						
				assertTrue("List is Equel" , sizeBeforeChange != sizeAfterChange );
				pushUser = EcompUserRestUtils.pushUser(ecompUser);	
				
			} finally {
				deleteUser(ecompUser.getLoginId());
			}
							
			}
					
	}
	
	@Test
	public void createSameUserTwiceTest() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		try {
			
			///get list of users
			List<EcompUser> allusersList = getAllusersList();
			int sizeBeforeChange = allusersList.size();

			//create user
			ecompUser.setLoginId(getUser());
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			///get list of users verify list size changed
			allusersList = getAllusersList();
			int sizeAfterChange = allusersList.size();
					
			assertTrue("List is Equel" , sizeBeforeChange != sizeAfterChange );
			pushUser = EcompUserRestUtils.pushUser(ecompUser);	
			
		} finally {
			deleteUser(ecompUser.getLoginId());
		}
		
	}
	
	@Test
	public void createSameUserTwiceDiffrentDataTest() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		try {
			///get list of users
			List<EcompUser> allusersList = getAllusersList();
			int sizeBeforeChange = allusersList.size();

			//create user
			ecompUser.setLoginId(getUser());
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			///get list of users verify list size changed
			allusersList = getAllusersList();
			int sizeAfterChange = allusersList.size();
					
			assertTrue("List is Equel" , sizeBeforeChange != sizeAfterChange );
			
			//update role
			ecompRole.setId((long) 2);
			ecompRole.setName("DESIGNER");
			setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			
			pushUser = EcompUserRestUtils.pushUser(ecompUser);	
			
		} finally {
			deleteUser(ecompUser.getLoginId());
		}
		
	}
	
	@Test
	public void updateUserRoleTest() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		try {
			//create user
			ecompUser.setLoginId(getUser());
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			List<EcompRole> userRolesBefore = getUserRoles(ecompUser);
			
			//update role
			ecompRole = new EcompRole();
			ecompRole.setId((long) 2);
			ecompRole.setName("DESIGNER");
			List<EcompRole> list= new ArrayList<EcompRole>();
			list.add(ecompRole);
			
			EcompUserRestUtils.pushUserRoles(ecompUser.getLoginId(), list);
			
			List<EcompRole> userRolesAfter = getUserRoles(ecompUser); 
			
			assertFalse("role wasn't changed", userRolesBefore.equals(userRolesAfter));
		} finally {
			deleteUser(ecompUser.getLoginId());
		}
		
	}

	@Test
	public void addUserCreateResource() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		Resource resource = new Resource();
		
		try {
			//create user
			ecompUser.setLoginId(getUser());
			ecompRole.setId((long) 2);
			ecompRole.setName("DESIGNER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			UserRoleEnum.DESIGNER.setUserId(ecompUser.getLoginId());
			resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
			
		} finally {
			ResourceRestUtils.deleteResource(resource.getUniqueId(), adminUser.getUserId());
			deleteUser(ecompUser.getLoginId());
		}
		
	}
	
	@Test
	public void changeUserRoleWithCheckOutResource() throws Exception {
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		Resource resource = new Resource();		
		try {
			
			//create user
			ecompUser.setLoginId(getUser());
			ecompRole.setId((long) 2);
			ecompRole.setName("DESIGNER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			UserRoleEnum.DESIGNER.setUserId(ecompUser.getLoginId());
			resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
			
			//update role
			ecompRole = new EcompRole();
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			List<EcompRole> list= new ArrayList<EcompRole>();
			list.add(ecompRole);
			
			RestResponse pushUserRoles = EcompUserRestUtils.pushUserRoles(ecompUser.getLoginId(), list);
//			assertTrue("wrong response code :" , pushUserRoles.getErrorCode() == BaseRestUtils.STATUS_CODE_ALREADY_EXISTS);
			
		} finally {
			ResourceRestUtils.deleteResource(resource.getUniqueId(), adminUser.getUserId());
			deleteUser(ecompUser.getLoginId());
		}
	}
	
	@Test
	public void changeUserRoleWithStartTestingResource() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		Resource resource= new Resource();

		try {
			//create user
			ecompUser.setLoginId(getUser());
			ecompRole.setId((long) 2);
			ecompRole.setName("DESIGNER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			UserRoleEnum.DESIGNER.setUserId(ecompUser.getLoginId());
			
			resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
			AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true);
			
			
			//update role
			ecompRole = new EcompRole();
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			List<EcompRole> list= new ArrayList<EcompRole>();
			list.add(ecompRole);
			
			RestResponse pushUserRoles = EcompUserRestUtils.pushUserRoles(ecompUser.getLoginId(), list);
			BaseRestUtils.checkSuccess(pushUserRoles);
			
		} finally {
			ResourceRestUtils.deleteResource(resource.getUniqueId(), adminUser.getUserId());
			deleteUser(ecompUser.getLoginId());
		}
	}
	
	@Test
	public void fillAllEcompFields() throws Exception {
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		try {

			///get list of users
			List<EcompUser> allusersList = getAllusersList();
			int sizeBeforeChange = allusersList.size();

			//create user
			ecompUser.setLoginId(getUser());
			ecompUser.setOrgId((long) 123);
			ecompUser.setManagerId("ci4321");
			ecompUser.setFirstName("firstName");
			ecompUser.setMiddleInitial("middleInitial");
			ecompUser.setLastName("lastName");
			ecompUser.setPhone("phone");
			ecompUser.setEmail("email@email.com");
			ecompUser.setHrid("hrid");
			ecompUser.setOrgUserId("orgUserId");
			ecompUser.setOrgCode("orgCode");
			ecompUser.setOrgManagerUserId("ci1234");
			ecompUser.setJobTitle("jobTitle");
			ecompUser.setActive(true);			
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			///get list of users verify list size changed
			allusersList = getAllusersList();
			int sizeAfterChange = allusersList.size();
					
			assertTrue("List is Equel" , sizeBeforeChange != sizeAfterChange );
			
		} finally {
			
			deleteUser(ecompUser.getLoginId());
		}
	}
	
	@Test
	public void missingMandatoryFieldRole() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		try {
			///get list of users
			List<EcompUser> allusersList = getAllusersList();
			int sizeBeforeChange = allusersList.size();

			//create user
			ecompUser.setLoginId(getUser());
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			BaseRestUtils.checkSuccess(pushUser);
			
			///get list of users verify list size changed
			allusersList = getAllusersList();
			int sizeAfterChange = allusersList.size();
					
			assertTrue("List is Equel" , sizeBeforeChange != sizeAfterChange );
			
		} finally {
			deleteUser(ecompUser.getLoginId());
		}
	}
	
	@Test
	public void missingMandatoryFieldATTid() throws Exception {
		
		EcompUser ecompUser = new EcompUser();
		EcompRole ecompRole = new EcompRole();
		try {
			
			//create user
			ecompUser.setLoginId("");
			ecompRole.setId((long) 1);
			ecompRole.setName("TESTER");
			Set<EcompRole> setRoles = new HashSet<EcompRole>();
			setRoles.add(ecompRole);
			ecompUser.setRoles(setRoles);
			RestResponse pushUser = EcompUserRestUtils.pushUser(ecompUser);
			assertTrue("wrong response code :" , pushUser.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
			
		} finally {
			deleteUser(ecompUser.getLoginId());
		}
		
		
	}

	private List<EcompRole> getUserRoles(EcompUser ecompUser) throws IOException {
		RestResponse userRoles = EcompUserRestUtils.getUserRoles(ecompUser.getLoginId());
		Type listType = new TypeToken<List<EcompRole>>() {}.getType();
		List<EcompRole> roleList = new Gson().fromJson(userRoles.getResponse(), listType);
		return roleList;
	}

	private void deleteUser(String userId) throws IOException {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		defaultUser.setUserId(userId);
		
		UserRestUtils.deleteUser(defaultUser, adminUser, true);
	}
	
	private  List<EcompUser> getAllusersList() throws IOException {
		RestResponse allUsers = EcompUserRestUtils.getAllUsers();
		
		Type listType = new TypeToken<List<EcompUser>>() {}.getType();
		List<EcompUser> usersList = new Gson().fromJson(allUsers.getResponse(), listType);
		
		return usersList;
	}
	
	private  List<EcompRole> getAllRoles() throws IOException {
		RestResponse allRoles = EcompUserRestUtils.getAllAvailableRoles();
		
		Type listType = new TypeToken<List<EcompRole>>() {}.getType();
		List<EcompRole> availableRoles = new Gson().fromJson(allRoles.getResponse(), listType);
		
		return availableRoles;
	}
	
	private String getUser() {
		
		String returnMe = "ci"+ new BigInteger(getRandomNumber(4));
		System.out.println(returnMe);
		
		
		return returnMe;
	}
	
	private static Random rnd = new Random();

	public static String getRandomNumber(int digCount) {
	    StringBuilder sb = new StringBuilder(digCount);
	    for(int i=0; i < digCount; i++)
	        sb.append((char)('0' + rnd.nextInt(10)));
	    return sb.toString();
	}
}
