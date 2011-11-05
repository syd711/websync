 package de.websync.gui;
 
 import de.websync.model.ImageInfo;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Map;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import org.apache.log4j.Logger;
 
 public class CategoryPanelMouseListener
   implements MouseListener
 {
   private CategoryPanel panel = null;
 
   public CategoryPanelMouseListener(CategoryPanel panel)
   {
     this.panel = panel;
   }
 
   public void mouseClicked(MouseEvent e)
   {
     if (!this.panel.addButton.isEnabled()) {
       return;
     }
     if (e.getButton() == 1)
     {
       if (e.getClickCount() == 2)
       {
         JLabel source = (JLabel)e.getSource();
         ImageInfo f = (ImageInfo)this.panel.panel2Info.get(source);
         Logger.getLogger(getClass()).info("Selected " + f.getName());
 
         if (!f.isDeleted())
         {
           int selection = JOptionPane.showConfirmDialog(this.panel, "Delete '" + f.getName() + "'?");
 
           if (selection == 0)
           {
             f.setDeleted();
             this.panel.reloadPanel();
           }
         }
 
         if (f.isDeleted())
         {
           int selection = JOptionPane.showConfirmDialog(this.panel, "Undo deletion of '" + f.getName() + "'?");
 
           if (selection == 0)
           {
             f.undelete();
             this.panel.reloadPanel();
           }
         }
       }
     }
   }
 
   public void mouseEntered(MouseEvent e)
   {
   }
 
   public void mouseExited(MouseEvent e)
   {
   }
 
   public void mousePressed(MouseEvent e)
   {
   }
 
   public void mouseReleased(MouseEvent e)
   {
   }
 }