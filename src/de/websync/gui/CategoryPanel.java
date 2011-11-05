 package de.websync.gui;
 
 import de.websync.model.Category;
 import de.websync.model.ImageInfo;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import org.apache.log4j.Logger;
 
 public class CategoryPanel extends JPanel
 {
   private JPanel rootPanel = new JPanel();
 
   private JPanel picturePanel = null;
   public Category category = null;
   public MainWindow mainWindow = null;
 
   public JButton addButton = null;
   private JButton overwriteButton = null;
   private JButton syncButton = null;
   private JButton reloadButton = null;
   private CategoryButtonPanelActionListener actionListener = null;
   private CategoryPanelMouseListener mouseListener = null;
 
   public Map<JLabel, ImageInfo> panel2Info = new HashMap();
 
   public CategoryPanel(Category category, MainWindow mainWindow)
   {
     this.category = category;
     this.mainWindow = mainWindow;
     this.actionListener = new CategoryButtonPanelActionListener(this);
     this.mouseListener = new CategoryPanelMouseListener(this);
     category.setCategoryPanel(this);
     init();
   }
 
   private void init()
   {
     setLayout(new BorderLayout());
 
     this.rootPanel = new JPanel();
     this.rootPanel.setBorder(BorderFactory.createLoweredBevelBorder());
     this.rootPanel.setLayout(new BorderLayout());
 
     this.picturePanel = new JPanel();
     this.picturePanel.setBackground(Color.BLACK);
 
     JScrollPane scrollPane = new JScrollPane(this.picturePanel);
     this.rootPanel.add(scrollPane, "Center");
 
     JPanel buttonPanel = new JPanel(new FlowLayout());
     this.addButton = new JButton("Add");
     this.addButton.setActionCommand("add");
     this.addButton.addActionListener(this.actionListener);
     this.overwriteButton = new JButton("Overwrite Local Category Copy");
     this.overwriteButton.setActionCommand("overwrite");
     this.overwriteButton.addActionListener(this.actionListener);
     this.reloadButton = new JButton("Reload Category");
     this.reloadButton.setActionCommand("reload");
     this.reloadButton.addActionListener(this.actionListener);
     this.syncButton = new JButton("FTP Synchronize");
     this.syncButton.setActionCommand("syncCategory");
     this.syncButton.addActionListener(this.actionListener);
     this.syncButton.setMnemonic('s');
     buttonPanel.add(this.addButton);
     buttonPanel.add(this.reloadButton);
     buttonPanel.add(this.overwriteButton);
     buttonPanel.add(this.syncButton);
 
     buttonPanel.setBackground(Color.white);
     add(this.rootPanel, "Center");
     add(buttonPanel, "North");
 
     reloadPanel();
   }
 
   public void reloadPanel()
   {
     if (this.mainWindow.progressBar != null)
     {
       this.mainWindow.progressBar.setValue(0);
       this.mainWindow.progressBar.setString("");
     }
 
     this.category.reload();
 
     Iterator it = this.panel2Info.keySet().iterator();
     while (it.hasNext())
     {
       JLabel label = (JLabel)it.next();
       this.picturePanel.remove(label);
     }
     this.picturePanel.removeAll();
 
     GridLayout layout = new GridLayout(3, 5);
     layout.setHgap(5);
     layout.setVgap(5);
     this.picturePanel.setLayout(layout);
 
     this.panel2Info.clear();
     for (ImageInfo f : this.category.getLocalFiles())
     {
       ImageIcon icon = f.getImage();
       if (icon == null)
         Logger.getLogger(getClass()).error("Error: icon not set for " + f.getName());
       JLabel label = new JLabel(icon);
       label.addMouseListener(this.mouseListener);
       label.setBackground(Color.BLACK);
       label.setToolTipText(f.getPlainName());
       this.picturePanel.add(label);
 
       f.setImage(icon);
       f.setLabel(label);
       f.updateVisualStatus();
       this.panel2Info.put(label, f);
     }
 
     this.picturePanel.revalidate();
     this.mainWindow.repaint();
   }
 
   public void setDisabled(boolean enable)
   {
     this.overwriteButton.setEnabled(!enable);
     this.addButton.setEnabled(!enable);
     this.syncButton.setEnabled(!enable);
     this.reloadButton.setEnabled(!enable);
   }
 }