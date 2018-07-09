package org.openecomp.sdc.be.dao.utils;

import java.awt.image.BufferedImage;

import org.assertj.core.data.Index;
import org.junit.Test;

import mockit.Deencapsulation;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ImageResizeUtilTest {

	@Test
	public void testResizeImage() throws Exception {
		BufferedImage originalImage = new BufferedImage(1, 1, 1);

		BufferedImage result = ImageResizeUtil.resizeImage(originalImage, 1, 1, false);

		assertThat(result.getWidth()).isEqualTo(1);
		assertThat(result.getHeight()).isEqualTo(1);
	}

	@Test
	public void testResizeImageWithHint() throws Exception {
		int expectedWidth = 10;
		int expectedHeight = 5;
		BufferedImage originalImage = new BufferedImage(9, 3, 1);

		BufferedImage result = ImageResizeUtil.resizeImageWithHint(originalImage, expectedWidth, expectedHeight, false);

		assertThat(result.getWidth()).isEqualTo(expectedWidth);
		assertThat(result.getHeight()).isEqualTo(expectedHeight);
	}

	@Test
	public void testShouldPreserveDimsAfterResizeImageWithHint() throws Exception {
		int imgOrigWidth = 9;
		int imgOrigHeight = 3;
		int targetWidth = 10;
		int targetHeight = 5;
		BufferedImage originalImage = new BufferedImage(imgOrigWidth, imgOrigHeight, 1);

		BufferedImage result = ImageResizeUtil.resizeImageWithHint(originalImage, targetWidth, targetHeight, true);

		assertThat(result.getWidth()).isEqualTo(targetWidth);
		assertThat(result.getHeight()).isEqualTo(imgOrigHeight);
	}

	@Test
	public void testResizeImage_1() throws Exception {
		int imgOrigWidth = 9;
		int imgOrigHeight = 3;
		int targetWidth = 10;
		int targetHeight = 5;
		BufferedImage originalImage = new BufferedImage(imgOrigWidth, imgOrigHeight, 1);

		BufferedImage result = Deencapsulation.invoke(ImageResizeUtil.class, "resizeImage",
				originalImage, targetWidth, targetHeight, true, false);
		assertThat(result.getWidth()).isEqualTo(targetWidth);
		assertThat(result.getHeight()).isEqualTo(imgOrigHeight);
	}

	@Test
	public void testComputeDimensionsWhenBothResultInNaN() throws Exception {
		int [] result = ImageResizeUtil.computeDimensions(0, 0, 0, 0);

		assertThat(result).contains(0, Index.atIndex(0)).contains(0, Index.atIndex(1));
	}

	@Test
	public void testComputeTargetDimensionsWhenDecimalPartPresent() throws Exception {
		int [] result = ImageResizeUtil.computeDimensions(3, 2, 5, 2);

		assertThat(result).contains(3, Index.atIndex(0)).contains(1, Index.atIndex(1));
	}

	@Test
	public void testComputeDimensionsWhenTargetDimsAreGreaterIntegers() throws Exception {
		int [] result = ImageResizeUtil.computeDimensions(4, 2, 6, 3);

		assertThat(result).contains(4, Index.atIndex(0)).contains(2, Index.atIndex(1));
	}

	@Test
	public void testComputeDimensionsWhenTargetDimsAreSmallerIntegers() throws Exception {
		int [] result = ImageResizeUtil.computeDimensions(10, 5, 9, 3);

		assertThat(result).contains(10, Index.atIndex(0)).contains(3, Index.atIndex(1));
	}
}