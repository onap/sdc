/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.cucumber.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Store Data here that is relevant for the whole runner and not just a single scenario.
 * @author ms172g
 *
 */
public class RunnerSession {
	private static final RunnerSession instance = new RunnerSession();
	private Map<String, String> stringElements;
	private Map<String, Integer> intElements;
	private Map<String, Object> elements;
	private RunnerSession(){
		stringElements = new HashMap<>();
		intElements = new HashMap<>();
		elements = new HashMap<>();
	}
	public static RunnerSession getSession(){
		return instance;
	}

	public void putInSession(String key, String value){
		stringElements.put(key, value);
	}
	
	public String getString(String key){
		return stringElements.get(key);
		
	}
	
	public void putInSession(String key, Integer value){
		intElements.put(key, value);
	}
	
	
	public Integer getInt(String key){
		return intElements.get(key);
	}
	
	public void putInSession(String key, Object value) {
		elements.put(key, value);
		
	}
	
	public Object get(String key){
		return elements.get(key);
	}
	
	public void clean(){
		intElements.clear();
		stringElements.clear();
	}
	
	
	
	
	
}
