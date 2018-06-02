package org.openecomp.sdc.be.dao.utils;

import java.awt.image.BufferedImage;

import org.junit.Test;

import mockit.Deencapsulation;

public class ImageResizeUtilTest {

	@Test
	public void testResizeImage() throws Exception {
		BufferedImage originalImage = new BufferedImage(1, 1, 1);
		int width = 1;
		int height = 1;
		boolean preserveDimensions = false;
		BufferedImage result;

		// default test
		result = ImageResizeUtil.resizeImage(originalImage, width, height, preserveDimensions);
	}

	@Test
	public void testResizeImageWithHint() throws Exception {
		BufferedImage originalImage = new BufferedImage(1, 1, 1);
		int width = 1;
		int height = 1;
		boolean preserveDimensions = false;
		BufferedImage result;

		// default test
		result = ImageResizeUtil.resizeImageWithHint(originalImage, width, height, preserveDimensions);
	}

	@Test
	public void testResizeImage_1() throws Exception {
		BufferedImage originalImage = new BufferedImage(1, 1, 1);
		int width = 1;
		int height = 1;
		boolean preserveDimensions = true;
		boolean enableHighQuality = false;
		BufferedImage result;

		// default test
		result = Deencapsulation.invoke(ImageResizeUtil.class, "resizeImage",
				originalImage, width, height, preserveDimensions, enableHighQuality);
	}

	@Test
	public void testComputeDimensions() throws Exception {
		int width = 0;
		int height = 0;
		int originalWidth = 0;
		int originalHeight = 0;
		int[] result;

		// default test
		result = ImageResizeUtil.computeDimensions(width, height, originalWidth, originalHeight);
	}
}