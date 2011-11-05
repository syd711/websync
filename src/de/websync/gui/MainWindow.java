 package de.websync.gui;
 
 import de.websync.ConfigHandler;
 import de.websync.model.Category;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.BorderFactory;
import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.JTabbedPane;
import javax.swing.border.Border;
 
 public class MainWindow extends JFrame
 {
	private static final long serialVersionUID = 5155991047205353623L;
	public static int H_SIZE = 20;
   public static int V_SIZE = 20;
 
   public JTabbedPane tabPane = null;
 
   public JProgressBar progressBar = null;
   public static MainWindow instance = null;
 
   private List<CategoryPanel> categoryPanels = new ArrayList<CategoryPanel>();
 
   public MainWindow()
   {
     instance = this;
     addWindowListener(new WindowAdapter()
     {
       public void windowClosing(WindowEvent e)
       {
         System.exit(0);
       }
     });
     getContentPane().setLayout(new BorderLayout());
 
     this.tabPane = new JTabbedPane(1);
     this.tabPane.setBackground(Color.white);
 
     int i = 0;
     for (Category category : ConfigHandler.getInstance().getCategories())
     {
       CategoryPanel panel = new CategoryPanel(category, this);
       this.categoryPanels.add(panel);
       this.tabPane.add(category.getName(), panel);
       i++;
     }
 
     Icon icon = new ImageIcon("conf/logo.jpg");
     JLabel top = new JLabel(icon);
     top.setBackground(Color.black);
     top.setForeground(Color.black);
 
     getContentPane().setBackground(Color.black);
     top.setBorder(BorderFactory.createRaisedBevelBorder());
     getContentPane().add(top, "North");
     getContentPane().add(this.tabPane, "Center");
 
     Border border = BorderFactory.createLineBorder(Color.white, 3);
     this.progressBar = new JProgressBar(0, 100);
     this.progressBar.setBorder(border);
     this.progressBar.setStringPainted(true);
     getContentPane().add(this.progressBar, "South");
 
     pack();
 
     setSize(800, 600);
     centerWindow(this);
     setTitle("WebSync Project '" + ConfigHandler.getInstance().getProjectName() + "'");
     setResizable(true);
     setVisible(true);
   }
 
   public void setDisabled(boolean enable)
   {
     this.tabPane.setEnabled(!enable);
     for (CategoryPanel panel : this.categoryPanels)
     {
       panel.setDisabled(enable);
     }
   }
 
   public Component centerWindow(Component frame)
   {
     Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
//     frame.setLocation(dimension.g);//TODO
     return frame;
   }
 
   public CategoryPanel getSelectedLetterPanel()
   {
     return (CategoryPanel)this.tabPane.getSelectedComponent();
   }
 }