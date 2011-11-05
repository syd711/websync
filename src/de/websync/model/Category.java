package de.websync.model;

import de.websync.CategorySynchronizer;
import de.websync.ConfigHandler;
import de.websync.gui.CategoryPanel;
import de.websync.gui.MainWindow;
import de.websync.util.FileComparator;
import de.websync.util.HTMLGenerator;
import java.awt.Cursor;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import org.apache.log4j.Logger;

public class Category
{
	private int id = -1;
	private String name = null;

	private CategoryPanel categoryPanel = null;

	private List<ImageInfo> localFiles = new ArrayList<ImageInfo>();

	public Category(int id, String name)
	{
		this.id = id;
		this.name = name;
		reload();
	}

	private int getUploadableFileSize()
	{
		int i = 0;
		for (ImageInfo info : localFiles)
		{
			if (!info.isDeleted())
			{
				i++;
			}
		}
		return i;
	}

	public void executeSynchronize(final JProgressBar bar)
	{
		final Category category = this;
		new Thread() {
			public void run()
			{
				MainWindow.instance.setDisabled(true);
				MainWindow.instance.setCursor(Cursor.getPredefinedCursor(3));
				CategorySynchronizer sync = CategorySynchronizer.newInstance(category);
				try
				{
					bar.setString("Connecting to FTP server");
					sync.connect();
					bar.setString("Executiong HTML file generation...");
					Category.this.deleteTemplateFiles();

					StringBuilder builder = new StringBuilder();
					for (ImageInfo info : Category.this.localFiles)
					{
						if (info.isDeleted())
						{
							continue;
						}
						String link = "<div align=\"center\"><a target=\"pics\" href=\"" + info.getImageHTMLLink() + "\"><img class=\"pic\" border=\"1\" src=\""
								+ info.getThumbnailJPGLink() + "\" alt=\"\" /></a></div><br><br>";
						builder.append(link);
						builder.append("\n");
					}

					File htmlFile = new File(Category.this.getCategoryFolder().getParentFile(), "ithumbs_" + Category.this.id + ".html");
					File scriptFile = new File(Category.this.getCategoryFolder().getParentFile(), "ithumbs.script");
					HTMLGenerator gen = new HTMLGenerator(scriptFile, htmlFile);
					gen.replace("${LINKS}", builder.toString());
					gen.generate();
					Logger.getLogger(this.getClass()).info("Generated HTML file " + htmlFile.getAbsolutePath());

					int fn = 0;
					for (ImageInfo info : Category.this.localFiles)
					{
						if (info.isDeleted())
						{
							continue;
						}

						String backward = "";
						String forward = "";

						if (fn > 0)
						{
							String prevPicHTMLName = "pic_" + Category.this.id + "_" + (fn - 1) + ".html";
							backward = "<a href=\"" + prevPicHTMLName + "\"><img src=\"img/left.jpg\" width=\"26\" height=\"26\" alt=\"\" border=\"0\"></a>";
						}
						if (fn != getUploadableFileSize() - 1)
						{
							String nextPicHTMLName = "pic_" + Category.this.id + "_" + (fn + 1) + ".html";
							forward = "<a href=\"" + nextPicHTMLName + "\"><img src=\"img/right.jpg\" width=\"26\" height=\"26\" alt=\"\" border=\"0\"></a>";
						}

						String picFilename = "pic_" + Category.this.id + "_" + fn + ".html";
						gen = new HTMLGenerator(new File(Category.this.getCategoryFolder().getParentFile(), "pic.script"), new File(Category.this.getCategoryFolder()
								.getParentFile(), picFilename));

						String picture = info.getImageJPGLink();
						gen.replace("${PICTURE}", picture);

						gen.replace("${BACK}", backward);
						gen.replace("${FORWARD}", forward);

						gen.generate();
						fn++;
					}

					File[] htmlFiles = Category.this.getCategoryFolder().getParentFile().listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name)
						{
							return name.endsWith(".html");
						}
					});
					int i = 0;
					int max = Category.this.localFiles.size() + htmlFiles.length;
					bar.setMaximum(max + 1);

					sync.goUp();
					sync.setMode(0);
					for (int k = 0; k < htmlFiles.length; k++)
					{
						File file = htmlFiles[k];
						bar.setValue(i);
						i++;
						bar.setString("Synchronizing HTML files (" + k + " of " + max + ")");
						sync.synchronizeHTMLFile(file);
					}
					sync.goCategory();
					sync.setMode(2);

					int imgCount = 0;
					for (ImageInfo info : Category.this.localFiles)
					{
						imgCount++;
						bar.setValue(i);
						i++;
						bar.setString("Synchronizing image files (" + imgCount + " of " + Category.this.localFiles.size() + ")");
						Logger.getLogger(getClass()).info("Synchronizing " + info);
						sync.synchronizeImageInfo(info);
					}
				} catch (Exception e)
				{
					MainWindow.instance.setDisabled(false);
					MainWindow.instance.setCursor(Cursor.getPredefinedCursor(0));
					Logger.getLogger(getClass()).error("Error during sync: " + e.getMessage(), e);
					JOptionPane.showMessageDialog(Category.this.categoryPanel, e.getMessage(), "Error during synchronize", 0);
				} finally
				{
					MainWindow.instance.setDisabled(false);
					MainWindow.instance.setCursor(Cursor.getPredefinedCursor(0));
					try
					{
						sync.disconnect();
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					Category.this.categoryPanel.reloadPanel();
				}

			}

		}

		.start();
	}

	private void deleteTemplateFiles()
	{
		File[] files = getCategoryFolder().getParentFile().listFiles();
		for (File file : files)
		{
			if ((file.getName().startsWith("ithumb")) && (file.getName().endsWith(".html")))
			{
				file.delete();
			}
			if ((file.getName().startsWith("pic_")) && (file.getName().endsWith(".html")))
				file.delete();
		}
	}

	public void executeOverwrite(final JProgressBar bar)
	{
		final Category category = this;
		new Thread() {
			public void run()
			{
				MainWindow.instance.setDisabled(true);
				MainWindow.instance.setCursor(Cursor.getPredefinedCursor(3));
				CategorySynchronizer sync = CategorySynchronizer.newInstance(category);
				try
				{
					bar.setString("Connecting to FTP server");
					sync.connect();

					for (ImageInfo info : Category.this.localFiles)
					{
						info.delete();
						bar.setString("Deleting " + info.getPlainName());
					}

					sync.downloadCategory(bar);
				} catch (Exception e)
				{
					MainWindow.instance.setDisabled(false);
					MainWindow.instance.setCursor(Cursor.getPredefinedCursor(0));
					Logger.getLogger(getClass()).error("Error during sync: " + e.getMessage(), e);
					JOptionPane.showMessageDialog(Category.this.categoryPanel, e.getMessage(), "Error during synchronize", 0);
				} finally
				{
					MainWindow.instance.setDisabled(false);
					MainWindow.instance.setCursor(Cursor.getPredefinedCursor(0));
					try
					{
						sync.disconnect();
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					Category.this.categoryPanel.reloadPanel();
				}
			}
		}.start();
	}

	public void reload()
	{
		this.localFiles.clear();
		getThumbnailDirectory().mkdirs();
		getImageDirectory().mkdirs();

		File dir = getThumbnailDirectory();
		File[] pics = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(".jpg");
			}
		});
		Arrays.sort(pics, new FileComparator());

		for (File file : pics)
		{
			File img = new File(getImageDirectory(), file.getName());
			ImageInfo info = new ImageInfo(file, img, this, true);
			this.localFiles.add(info);
		}

		Logger.getLogger(getClass()).info(this + " loaded " + this.localFiles.size() + " images.");
	}

	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public File getThumbnailDirectory()
	{
		File f = new File(getCategoryFolder(), CategorySynchronizer.DIR_THUMBS);
		if (!f.exists())
		{
			f.mkdirs();
			Logger.getLogger(getClass()).info("Created thumb directory " + f.getAbsolutePath());
		}
		return f;
	}

	public File getCategoryFolder()
	{
		return ConfigHandler.getInstance().getCategoryFolder(this.id);
	}

	public File getImageDirectory()
	{
		File f = new File(getCategoryFolder(), CategorySynchronizer.DIR_IMAGES);
		if (!f.exists())
		{
			f.mkdirs();
			Logger.getLogger(getClass()).info("Created category image directory " + f.getAbsolutePath());
		}
		return f;
	}

	public String toString()
	{
		return "" + id;
	}

	public int getLocalSize()
	{
		return this.localFiles.size();
	}

	public List<ImageInfo> getLocalFiles()
	{
		return this.localFiles;
	}

	public void setCategoryPanel(CategoryPanel categoryPanel)
	{
		this.categoryPanel = categoryPanel;
	}
}