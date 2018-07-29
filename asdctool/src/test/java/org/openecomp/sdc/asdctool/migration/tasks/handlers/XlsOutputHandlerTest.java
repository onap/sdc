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
import static org.mockito.Mockito.*;

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
