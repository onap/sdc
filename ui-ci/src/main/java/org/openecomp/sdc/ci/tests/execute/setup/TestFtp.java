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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFtp {

	public static void main(String[] args) throws IOException {
		AttFtpClient instance = AttFtpClient.getInstance();
		
		 String server = "localhost";
	      int port = 2121;
	      String user = "admin";
	      String pass = "admin";
	      AttFtpClient.getInstance().init(server, port, user, pass);
	      
	      try {
	    	  AttFtpClient.getInstance().retrieveListOfFile();
	    	  
	    	  File retrieveLastModifiedFileFromFTP = instance.retrieveLastModifiedFileFromFTP();
	    	  String content = new String(Files.readAllBytes(Paths.get(retrieveLastModifiedFileFromFTP.getPath())), StandardCharsets.UTF_8);
//	    	  instance.deleteFilesFromFTPserver();
	    	  System.out.println(content);
	    	  readFile(retrieveLastModifiedFileFromFTP);
			
		} finally {
			instance.terminateClient();
		}
	      
	     

	      
	      
	      
		
	}
	 public static void readFile(File retrieveLastModifiedFileFromFTP) {

	        StringBuilder sb = new StringBuilder();
	        BufferedReader br = null;
	        FileReader fileReader = null;
	        try {
	        	fileReader = new FileReader(retrieveLastModifiedFileFromFTP.getPath());
	            br = new BufferedReader(fileReader);
	            String line;
	            while ((line = br.readLine()) != null) {
	                if (sb.length() > 0) {
	                    sb.append("\n");
	                }
	                sb.append(line);
	            }
	        } catch (IOException e) {
	            System.out.println(e);
	        } finally {
	            try {
	                if (br != null) {
	                    br.close();
	                }
	                if(fileReader != null) {
	                	fileReader.close();
	                }
	            } catch (IOException ex) {
	                System.out.println(ex);
	            }
	        }
	        String contents = sb.toString();
	        System.out.println(contents);		
		 
	 }

}
