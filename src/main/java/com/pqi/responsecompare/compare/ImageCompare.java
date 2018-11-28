package com.pqi.responsecompare.compare;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.ImageIcon;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Difference;
import org.w3c.dom.Node;

import com.pqi.responsecompare.request.TestCase;

public class ImageCompare extends XmlCompare {

	static Logger logger = Logger.getLogger(ImageCompare.class);
	
	private TestCase test = null;
	
	public ImageCompare(TestCase test) {
		super(test);
		this.test = test;
	}
	
	public void results() throws Exception {
		// Only run super.results() if we're not dealing with HTML. WAPI calls produce HTML.
		if (!test.isWapi()) super.results();
		
		String image1 = "", image2 = "";
		IOFileFilter filter = new WildcardFileFilter(test.getTestCaseID() + "*.png");
		Collection<File> files = FileUtils.listFiles(new File(pathGenerator.getResponseDir()), filter, null);
		
		if (files.size() == 0) {
			Assert.fail(test.getTestCaseID() + "_*.png" + " was not found.");
		}
			
		for (File file : files) {
			image1 = file.getAbsolutePath();
			image2 = file.getAbsolutePath().replace("response", "expectedresponse");
			assertTrue(responseFile + ".xml" + " Images should be the same but they are different. First image: " + image1 + ", Second image: " + image2,
					compareImages(image1, image2, false));
			}
	}
	
	public void testMyTest() {
		
	}
	
	public ImageCompare(String testMethodName) {
		super(testMethodName);
	}

	public ImageCompare(String testMethodName, String fileName) {
		super(testMethodName);
		this.fileName = fileName;
	}

	public boolean compareImages(String paramFirstImage,
			String paramSecondImage, boolean paramExpectDiff) throws Exception {
		boolean imageEqual = false, compareResult = true;
		Image image1 = (new ImageIcon(paramFirstImage)).getImage();
		Image image2 = (new ImageIcon(paramSecondImage)).getImage();
		PixelGrabber grab1 = new PixelGrabber(image1, 0, 0, -1, -1, false);
		PixelGrabber grab2 = new PixelGrabber(image2, 0, 0, -1, -1, false);
		int data1[] = null;
		int width = 0;
		int height = 0;
		if (grab1.grabPixels()) {
			width = grab1.getWidth();
			height = grab1.getHeight();
			data1 = new int[width * height];
			data1 = (int[]) (int[]) grab1.getPixels();
		}
		int data2[] = null;
		if (grab2.grabPixels()) {
			width = grab2.getWidth();
			height = grab2.getHeight();
			data2 = new int[width * height];
			data2 = (int[]) (int[]) grab2.getPixels();
		}
		imageEqual = Arrays.equals(data1, data2);
		if (paramExpectDiff) {
			// Differences were expected but the images were the same
			if (imageEqual)
				compareResult = false;
		} else {
			// Differences were not expected but the images are different
			if (!imageEqual)
				compareResult = false;
		}

		/*
		 * // Don't care to support a diff image for now, kind of useless
		 * if(paramDiffImage != null) { BufferedImage result = new
		 * BufferedImage(width, height, 1); for(int x = 0; x < width; x++) {
		 * for(int y = 0; y < height; y++) result.setRGB(x, y, data1[width * y +
		 * x] ^ data2[width * y + x]);
		 * 
		 * }
		 * 
		 * File outputfile = new File(paramDiffImage); ImageIO.write(result,
		 * getFileExtension(paramDiffImage), outputfile); }
		 */
		return compareResult;
	}

	/*
	 * // Only used if we want to save a diff image private String
	 * getFileExtension(String fileName) { String tmpFile = (new
	 * File(fileName)).getPath(); int whereDot = tmpFile.lastIndexOf('.'); if(0
	 * < whereDot && whereDot <= tmpFile.length() - 2) return
	 * tmpFile.substring(whereDot + 1, tmpFile.length()); else return ""; }
	 */

	public void main(String[] args) throws Exception {
		logger.info(compareImages("c:/temp/image1.png",
				"c:/temp/image2.png", true));
	}

	public int differenceFound(String expected, String actual, Node control,
			Node test, Difference difference) {
		int myInt = 0;
		return myInt;
	}

	public int differenceFound(Difference difference) {
		int myInt = 0;
		return myInt;
	}
}
