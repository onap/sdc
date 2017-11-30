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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class AttFtpClient {

	private static final AttFtpClient instance = new AttFtpClient();

	public static AttFtpClient getInstance() {
		return instance;
	}

	private FTPClient apacheFtpClient;

	private AttFtpClient() {
		apacheFtpClient = new FTPClient();
	};


	public void init(String server, int port, String user, String pass) {

		try {
			apacheFtpClient.connect(server, port);
			showServerReply(apacheFtpClient);
		
			
			int replyCode = apacheFtpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				System.out.println("Connect failed");
				return;
			}

			boolean success = apacheFtpClient.login(user, pass);
			showServerReply(apacheFtpClient);

			if (!success) {
				System.out.println("Could not login to the server");
				return;
			}
			
//			else{
//				apacheFtpClient.enterLocalPassiveMode();
//				apacheFtpClient.setFileType(FTP.BINARY_FILE_TYPE);	
//			}
		} catch (IOException ex) {
			System.out.println("Oops! Something wrong happened");
			ex.printStackTrace();
		}

	}

	public File retrieveLastModifiedFileFromFTP() throws IOException {
		FTPFile[] files1 = retrieveListOfFile();

		// sort list by TimeStamp
		List<FTPFile> sorted = Arrays.asList(files1).stream()
				.sorted((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp())).collect(Collectors.toList());
		printFileDetailsList(sorted);

		// retrieve file from FTP
		FTPFile ftpFile = sorted.get(sorted.size() - 1);

		return retrieveFileFromFTP(ftpFile);

	}

	public FTPFile[] retrieveListOfFile() throws IOException {
		// Lists files and directories
		FTPFile[] files = apacheFtpClient.listFiles("");
		
		printNames(files);
		return files;
	}

	public File retrieveFileFromFTP(FTPFile ftpFile) throws IOException {
		
        File downloadFile1 = new File("tmp");
        OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
        boolean success = apacheFtpClient.retrieveFile(ftpFile.getName(), outputStream1);
        outputStream1.close();

        if (success) {
            System.out.println("File #1 has been downloaded successfully.");
        }
		

		return downloadFile1;

	}

	public void deleteFilesFromFTPserver() throws IOException {
		FTPFile[] files = retrieveListOfFile();
		deleteFiles(files);
	}

	public void terminateClient() throws IOException {

		String status = apacheFtpClient.getStatus();

		// logs out and disconnects from server
		try {
			if (apacheFtpClient.isConnected()) {
				apacheFtpClient.logout();
				apacheFtpClient.disconnect();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void printFileDetailsList(List<FTPFile> list) {
		DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (FTPFile ftpFile : list) {

			String details = ftpFile.getName();
			if (ftpFile.isDirectory()) {
				details = "[" + details + "]";
			}
			details += "\t\t" + ftpFile.getSize();
			details += "\t\t" + dateFormater.format(ftpFile.getTimestamp().getTime());

			System.out.println(details);
		}
	}

	private void printNames(FTPFile[] files) {
		if (files != null && files.length > 0) {
			for (FTPFile aFile : files) {
				System.out.println(aFile);
			}
		}
	}

	private void showServerReply(FTPClient ftpClient) {
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				System.out.println("SERVER: " + aReply);
			}
		}
	}

	public class LastModifiedComparator implements Comparator<FTPFile> {

		public int compare(FTPFile f1, FTPFile f2) {
			return f1.getTimestamp().compareTo(f2.getTimestamp());
		}
	}

	public FTPFile getMaxLastModified(FTPFile[] ftpFiles) {
		return Collections.max(Arrays.asList(ftpFiles), new LastModifiedComparator());
	}

	public static void displayFiles(File[] files) {
		for (File file : files) {
			System.out.printf("File: %-20s Last Modified:" + new Date(file.lastModified()) + "\n", file.getName());
		}
	}

	public void deleteFiles(FTPFile[] files) {

		for (FTPFile file : files) {

			boolean deleted = false;
			try {
				deleted = apacheFtpClient.deleteFile(file.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (deleted) {
				System.out.println("The file was deleted successfully.");
			} else {
				System.out.println("Could not delete the  file, it may not exist.");
			}
		}

	}

}
