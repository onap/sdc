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

package org.openecomp.sdc.be.dao.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Utility to resize images.
 * 
 * @author luc boutier
 */
public final class ImageResizeUtil {
	private ImageResizeUtil() {
	}

	/**
	 * Resize an image with default quality settings.
	 * 
	 * @param originalImage
	 *            The image to resize.
	 * @param width
	 *            The target width.
	 * @param height
	 *            The target height.
	 * @param preserveDimensions
	 *            Flag to know if we should preserve original image dimensions.
	 * @return The resized image.
	 */
	public static BufferedImage resizeImage(final BufferedImage originalImage, final int width, final int height,
			final boolean preserveDimensions) {
		return resizeImage(originalImage, width, height, preserveDimensions, false);
	}

	/**
	 * <p>
	 * Resize an image with high quality settings.
	 * </p>
	 * <ul>
	 * <li>g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	 * RenderingHints.VALUE_INTERPOLATION_BILINEAR);</li>
	 * <li>g.setRenderingHint(RenderingHints.KEY_RENDERING,
	 * RenderingHints.VALUE_RENDER_QUALITY);</li>
	 * <li>g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	 * RenderingHints.VALUE_ANTIALIAS_ON);</li>
	 * </ul>
	 * 
	 * @param originalImage
	 *            The image to resize.
	 * @param width
	 *            The target width.
	 * @param height
	 *            The target height.
	 * @param preserveDimensions
	 *            Flag to know if we should preserve original image dimensions.
	 * @return The resized image.
	 */
	public static BufferedImage resizeImageWithHint(BufferedImage originalImage, final int width, final int height,
			final boolean preserveDimensions) {
		return resizeImage(originalImage, width, height, preserveDimensions, true);
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, final int width, final int height,
			final boolean preserveDimensions, final boolean enableHighQuality) {
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

		int targetWidth = width;
		int targetHeight = height;

		if (preserveDimensions) {
			int[] targetDimentions = computeDimensions(width, height, originalImage.getWidth(),
					originalImage.getHeight());
			targetWidth = targetDimentions[0];
			targetHeight = targetDimentions[1];
		}

		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, type);

		Graphics2D g = resizedImage.createGraphics();
		if (enableHighQuality) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		g.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
		g.dispose();

		return resizedImage;
	}

	/**
	 * Compute target width and height based on requested width and height but
	 * making sure the original dimensions of the image will be preserved.
	 * 
	 * @param width
	 *            The ideal (and max) target width.
	 * @param height
	 *            The ideal (and max) target height.
	 * @param originalWidth
	 *            The original width.
	 * @param originalHeight
	 *            The original height.
	 * @return An array of int that contains the ideal width and height to
	 *         preserve dimensions.
	 */
	public static int[] computeDimensions(final int width, final int height, final int originalWidth,
			final int originalHeight) {
		int targetWidth = width;
		int targetHeight = height;

		float targetDimensions = (float) width / (float) height;
		float sourceDimensions = (float) originalWidth / (float) originalHeight;
		if (targetDimensions > sourceDimensions) {
			targetWidth = (int) (width * sourceDimensions / targetDimensions);
		} else {
			targetHeight = (int) (height * targetDimensions / sourceDimensions);
		}

		return new int[] { targetWidth, targetHeight };
	}
}
