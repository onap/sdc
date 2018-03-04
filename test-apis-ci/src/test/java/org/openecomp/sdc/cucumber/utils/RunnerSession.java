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
