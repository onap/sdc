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

package org.openecomp.sdc.asdctool.migration.tasks.handlers;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class XlsOutputHandlerTest {

    @Spy
    private XlsOutputHandler handler = new XlsOutputHandler(null, "mock");

    @Mock
    private Workbook workbook;
    @Mock
    private FileOutputStream xlsFile;

    @Test
    public void verifyThatFileIsNotCreatedIfNoRecordsAdded() throws IOException {
        assertFalse(handler.writeOutputAndCloseFile());
        verify(workbook, times(0)).write(any());
    }

    @Test
    public void verifyThatFileIsCreatedIfSomeRecordsAdded() throws IOException {
        handler.addRecord("mock");
        doReturn(xlsFile).when(handler).getXlsFile();
        assertTrue(handler.writeOutputAndCloseFile());
    }
    
    
    private XlsOutputHandler createTestSubject() {
	return new XlsOutputHandler("mock", "mockPath", new Object());
    }

    @Test
    public void testInitiate() throws Exception {
	XlsOutputHandler testSubject;
	Object[] title = new Object[] { null };
	// default test
	testSubject = createTestSubject();
	testSubject.initiate("mock", title);
    }

    @Test
    public void testAddRecord() throws Exception {
	XlsOutputHandler testSubject;
	Object[] record = new Object[] { null };

	// default test
	testSubject = createTestSubject();
	testSubject.addRecord(record);
    }
}
