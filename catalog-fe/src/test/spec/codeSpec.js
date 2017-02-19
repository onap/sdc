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

describe("General", function() {
	
	
	
	describe("File Handler", function() {
		
		var jsHandler = new JsHandler();
		var fileHandler = jsHandler.getFileHandler();
		fileHandler.fileContents.fileToUpload = mysqlTypeYml;
		fileHandler.fileContents.artifactToUpload = installMySqlSH;
		
		
		describe("checkFile Method", function() {
	
			it("checkFile Valid", function() {
				spyOn(window, 'alert');
				
				affix('#dummyElement').val('stam.zip');
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.chef, 'dummyElement');
				expect(isValid).toBeTruthy();
				
				
				$('#dummyElement').val('stam.sh');
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.shell, 'dummyElement');
				expect(isValid).toBeTruthy();
				
				$('#dummyElement').val('stam.pp');
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.puppet, 'dummyElement');
				expect(isValid).toBeTruthy();
				
				$('#dummyElement').val('stam.yang');
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.yang, 'dummyElement');
				expect(isValid).toBeTruthy();
				
				
		
			});
			
			it("checkFile inValid", function() {
				spyOn(window, 'alert');
				
				affix('#dummyElement').val('stam.zip');
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.shell, 'dummyElement');
				expect(isValid).toBeFalsy();
				
				$('#dummyElement').val('stam.sh');
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.chef, 'dummyElement');
				expect(isValid).toBeFalsy();
				
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.puppet, 'dummyElement');
				expect(isValid).toBeFalsy();
				
				var isValid = fileHandler.checkFile(fileHandler.filesExtensions.yang, 'dummyElement');
				expect(isValid).toBeFalsy();
												
			});
			
		});
		
		
		
		describe("getZipName Method", function() {
			it("getZipName", function() {
				spyOn(fileHandler, 'getFileName').and.returnValue('mysql-type.yml');
				expect(fileHandler.getZipName() == 'mysql-type.zip').toBeTruthy();
			});
		});
		/*
		describe("generateArtifactsList Method", function() {
			beforeEach(function() {
				jsHandler.getDomHandler().exisitngArifactsCounter = 2;
			});
			
			it("generateArtifactsList", function() {
				spyOn(fileHandler, 'getFileName').and.returnValue('mysql-type.yml');
				var artifactList = fileHandler.generateArtifactsList();
//				expect(fileHandler.generateArtifactsList() == 'mysql-type.zip').toBeTruthy();
			});
		});*/
		
		describe("getArtifactsListFromYml Method", function() {
			it("getArtifactsListFromYml", function() {
				var expectedArtifacts = fileHandler.getArtifactsListFromYml();
				expect(expectedArtifacts.length == 3).toBeTruthy();
				expect($.inArray('scripts/install_mysql.sh', expectedArtifacts) != -1).toBeTruthy();
				expect($.inArray('scripts/start_mysql.sh', expectedArtifacts) != -1).toBeTruthy();
				expect($.inArray('images/mysql.png', expectedArtifacts) != -1).toBeTruthy();
			});
		});
		
		describe("uploadFileJson Method", function() {
			var sentJson, sendEncodedMd5;
			
			beforeEach(function() {
				sentJson = '';
				sendEncodedMd5 = '';
				spyOn(fileHandler, 'sendAjaxToServer').and.callFake(function(strifiedJson, encodedMd5) {
					sentJson = JSON.parse(strifiedJson);
					sendEncodedMd5 = encodedMd5;

				});
				spyOn(fileHandler, 'getZipName').and.callFake(function() { return 'fakeZipName.zip' });
				spyOn(fileHandler, 'generateArtifactsList').and.callFake(function() { return 'fakeArtifactsList' });
				spyOn(fileHandler, 'generateZip').and.callFake(function() { return 'fakeEncodedZipData' });
			});
			
			it("sendAjaxToServer method is called", function() {
				fileHandler.uploadFileJson();
				expect(fileHandler.sendAjaxToServer).toHaveBeenCalled();
			});
			
			it("json contains relevant fields", function() {
				fileHandler.uploadFileJson();
				
				expect(sentJson.payloadName == 'fakeZipName.zip').toBeTruthy();
				expect(sentJson.isEncoded).toBeTruthy();
				expect(sentJson.isCompressed).toBeTruthy();
				expect(sentJson.artifactList == 'fakeArtifactsList').toBeTruthy();
				expect(sentJson.payloadData == 'fakeEncodedZipData').toBeTruthy();
				
				
				
			});
			
			it("md5 is validated", function() {
				fileHandler.uploadFileJson();
				
				var strifiedJson = JSON.stringify(sentJson);
				var md5 = CryptoJS.MD5(strifiedJson);
				var encodedMd5 = btoa(md5);
				expect(encodedMd5 == sendEncodedMd5).toBeTruthy();
				
			});
		});
		
		describe("generateZip Method", function() {
			
			it("generateZip", function() {
				
				
				spyOn(fileHandler, 'getFileName').and.callFake(function(controName) {
					var fileName;
					if( controName == 'fileToUpload'){
						fileName = 'mysql-type.yml';
					}
					else if( controName ==  'imageToUpload'){
						fileName = 'root.png';
					}
					else if( controName ==  'artifactToUpload'){
						fileName = 'install_mysql.sh';
					}
					else{
						fileName = controName+'.sh';
					}
				    return fileName;
			    });
				
				var encodedZip = fileHandler.generateZip();
				var decodedZip = atob(encodedZip);
				var myZip = new JSZip(decodedZip);
				
				var componentContentInZip = myZip.file('mysql-type.yml').asText();
				expect(componentContentInZip == mysqlTypeYml).toBeTruthy();
				
				var artifactContentInZip = myZip.folder('scripts').file('install_mysql.sh').asText();
				expect(artifactContentInZip == installMySqlSH).toBeTruthy();
				
				
				
			});
		});
	});
	
	
});


