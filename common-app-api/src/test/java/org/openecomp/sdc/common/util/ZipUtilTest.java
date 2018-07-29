package org.openecomp.sdc.common.util;

import org.junit.Test;

import java.util.Map;

public class ZipUtilTest {

	// private ZipUtil createTestSubject() {
	// return new ZipUtil();
	// }

	@Test
	public void testReadZip() throws Exception {
		byte[] zipAsBytes = new byte[] { ' ' };
		Map<String, byte[]> result;

		// default test
		result = ZipUtil.readZip(zipAsBytes);
	}

	@Test
	public void testMain() throws Exception {
		String[] args = new String[] { "" };

		// default test
		ZipUtil.main(args);
	}

	@Test
	public void testZipBytes() throws Exception {
		byte[] input = new byte[] { ' ' };
		byte[] result;

		// default test
		result = ZipUtil.zipBytes(input);
	}

	@Test
	public void testUnzip() throws Exception {
		byte[] zipped = new byte[] { ' ' };
		byte[] result;

		// default test
		result = ZipUtil.unzip(zipped);
	}
}