package de.websync.util;

import de.websync.ConfigHandler;
import de.websync.model.Category;
import de.websync.model.ImageInfo;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

public class ImageConverter
{
	private static final int MAX_TN_HEIGHT = ConfigHandler.getInstance()
			.getMaxThumbnailSize();
	private static final int MAX_IMG_HEIGHT = ConfigHandler.getInstance()
			.getMaxPictureSize();

	public ImageInfo addPicture(Category category, File target) throws Exception
	{
		File imgFile = new File(category.getImageDirectory(), "[new]_"
				+ target.getName().replaceAll(" ", "_"));
		File tnFile = new File(category.getThumbnailDirectory(), "[new]_"
				+ target.getName().replaceAll(" ", "_"));
		ImageInfo file = new ImageInfo(tnFile, imgFile, category, false);

		BufferedImage image = ImageIO.read(target);
		int width = image.getWidth();
		int height = image.getHeight();

		double faktor = width;
		if (width < height)
		{
			faktor = height;
		}
		double percentage = MAX_TN_HEIGHT * 100 / faktor;
		double newHeight = height * percentage / 100.0D;
		double newWidth = width * percentage / 100.0D;

		BufferedImage tnimage = getScaledInstance(image, (int) newWidth,
				(int) newHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
		writeImage(tnFile, tnimage);

		image = ImageIO.read(target);
		percentage = MAX_IMG_HEIGHT * 100 / faktor;
		newHeight = height * percentage / 100.0D;
		newWidth = width * percentage / 100.0D;
		BufferedImage imImage = getScaledInstance(image, (int) newWidth,
				(int) newHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
		writeImage(imgFile, imImage);

		file.setImage(new ImageIcon(tnFile.getAbsolutePath()));
		return file;
	}

	private void writeImage(File imgFile, BufferedImage image) throws Exception
	{
		Logger.getLogger(getClass()).info(
				"Writing jpg " + imgFile.getAbsolutePath());
		imgFile.mkdirs();
		imgFile.delete();

		Iterator iterator = ImageIO.getImageWritersBySuffix("jpeg");
		ImageWriter imageWriter = (ImageWriter) iterator.next();
		JPEGImageWriteParam imageWriteParam = new JPEGImageWriteParam(Locale
				.getDefault());
		imageWriteParam.setCompressionMode(2);
		imageWriteParam.setCompressionQuality(0.9F);
		IIOImage iioImage = new IIOImage(image, null, null);

		imgFile.delete();
		ImageOutputStream out = ImageIO.createImageOutputStream(imgFile);
		imageWriter.setOutput(out);
		imageWriter.write(null, iioImage, imageWriteParam);
		out.close();
		Logger.getLogger(getClass()).info("Written " + imgFile.getAbsolutePath());
	}

	private BufferedImage getScaledInstance(BufferedImage img, int targetWidth,
			int targetHeight, Object hint, boolean higherQuality)
	{
		int type = img.getTransparency() == 1 ? 1 : 2;
		BufferedImage ret = img;
		int h;
		int w;
		if (higherQuality)
		{
			w = img.getWidth();
			h = img.getHeight();
		} else
		{
			w = targetWidth;
			h = targetHeight;
		}

		do
		{
			if ((higherQuality) && (w > targetWidth))
			{
				w /= 2;
				if (w < targetWidth)
				{
					w = targetWidth;
				}
			}

			if ((higherQuality) && (h > targetHeight))
			{
				h /= 2;
				if (h < targetHeight)
				{
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while ((w != targetWidth) || (h != targetHeight));

		return ret;
	}
}