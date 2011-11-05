package de.websync.gui;

import de.websync.model.Category;
import de.websync.util.ImageConverter;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class CategoryButtonPanelActionListener implements ActionListener
{
	public static final String ACTION_COMMAND_ADD = "add";
	public static final String ACTION_COMMAND_OVERWRITE = "overwrite";
	public static final String ACTION_COMMAND_SYNC_CATEGORY = "syncCategory";
	public static final String ACTION_COMMAND_RELOAD = "reload";
	private CategoryPanel categoryPanel = null;
	private Category category = null;

	public CategoryButtonPanelActionListener(CategoryPanel categoryPanel)
	{
		this.categoryPanel = categoryPanel;
		this.category = categoryPanel.category;
	}

	public void actionPerformed(ActionEvent arg0)
	{
		try
		{
			String actionCommand = arg0.getActionCommand();

			if (actionCommand.equalsIgnoreCase("add"))
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Bilder Hinzufügen");
				chooser.setDialogType(0);
				chooser.setMultiSelectionEnabled(true);
				chooser.addChoosableFileFilter(new ImageFilter());
				chooser.setAcceptAllFileFilterUsed(false);

				int code = chooser.showDialog(this.categoryPanel, "Öffnen");

				if (code == 0)
				{
					final File[] files = chooser.getSelectedFiles();

					new Thread() {
						public void run()
						{
							try
							{
								MainWindow.instance.setDisabled(true);
								CategoryButtonPanelActionListener.this.categoryPanel
										.setCursor(Cursor
												.getPredefinedCursor(3));

								if (files.length == 0)
								{
									return;
								}
								CategoryButtonPanelActionListener.this.categoryPanel.mainWindow.progressBar
										.setMaximum(files.length + 1);
								int i = 1;
								for (File file : files)
								{
									CategoryButtonPanelActionListener.this.categoryPanel.mainWindow.progressBar
											.setString("Creating thumnail for '"
													+ file.getName() + "'");
									CategoryButtonPanelActionListener.this.categoryPanel.mainWindow.progressBar
											.setValue(i);
									ImageConverter converter = new ImageConverter();
									converter
											.addPicture(
													CategoryButtonPanelActionListener.this.category,
													file);
									i++;
								}

								CategoryButtonPanelActionListener.this.category
										.reload();
							} catch (Exception e)
							{
								e.printStackTrace();
							} finally
							{
								MainWindow.instance.setDisabled(false);
								CategoryButtonPanelActionListener.this.categoryPanel
										.reloadPanel();
								CategoryButtonPanelActionListener.this.categoryPanel
										.setCursor(Cursor
												.getPredefinedCursor(0));
							}
						}
					}.start();
				}

			} else if (actionCommand.equalsIgnoreCase("reload"))
			{
				this.categoryPanel.reloadPanel();
			} else if (actionCommand.equalsIgnoreCase("overwrite"))
			{
				int returnCode = JOptionPane
						.showConfirmDialog(
								this.categoryPanel,
								"This will overwrite all images of the local working copy. Continue?",
								"Warning", 2);

				if (returnCode != 0)
				{
					return;
				}
				this.category
						.executeOverwrite(this.categoryPanel.mainWindow.progressBar);
			} else if (actionCommand.equalsIgnoreCase("syncCategory"))
			{
				this.category
						.executeSynchronize(this.categoryPanel.mainWindow.progressBar);
			}

		} catch (RuntimeException e)
		{
			e.printStackTrace();
		}
	}
}