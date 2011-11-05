 package de.websync.model;
 
 import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
 
 public class ImageInfo
 {
   private File thumbnailFile = null;
   private File imageFile = null;
   private ImageIcon image = null;
   private Category category = null;
 
   private JLabel label = null;
 
   public ImageIcon getImage()
   {
     return this.image;
   }
 
   public void updateVisualStatus()
   {
     TitledBorder border = new TitledBorder(getPlainName());
     border.setTitleColor(Color.white);
     this.label.setBorder(border);
 
     if (isAdded())
     {
       border = new TitledBorder(getPlainName() + " [not uploaded yet]");
       border.setTitleColor(Color.green);
       this.label.setBorder(border);
     }
 
     if (getName().startsWith("[deleted]"))
     {
       border = new TitledBorder(getPlainName() + " [deleted locally, not synchronized yet]");
       border.setTitleColor(Color.red);
       this.label.setBorder(border);
     }
   }
 
   public void setImage(ImageIcon image)
   {
     this.image = image;
   }
 
   public ImageInfo(File tn, File img, Category category, boolean loadImage)
   {
     this.thumbnailFile = tn;
     this.imageFile = img;
     this.category = category;
 
     if (loadImage)
     {
       Image image = Toolkit.getDefaultToolkit().createImage(tn.getAbsolutePath());
       this.image = new ImageIcon(image);
     }
   }
 
   public File getThumbnailFile()
   {
     return this.thumbnailFile;
   }
 
   public File getImageFile()
   {
     return this.imageFile;
   }
 
   public String getName()
   {
     return this.imageFile.getName();
   }
 
   public boolean isDeleted()
   {
     return getName().startsWith("[deleted]");
   }
 
   public void setDeleted()
   {
     if (isNew())
     {
       delete();
     }
     else
     {
       this.thumbnailFile
         .renameTo(new File(this.thumbnailFile.getParentFile(), "[deleted]_" + getPlainName()));
       this.imageFile.renameTo(new File(this.imageFile.getParentFile(), "[deleted]_" + getPlainName()));
     }
   }
 
   public void undelete()
   {
     this.thumbnailFile.renameTo(new File(this.thumbnailFile.getParentFile(), getPlainName()));
     this.imageFile.renameTo(new File(this.imageFile.getParentFile(), getPlainName()));
   }
 
   private boolean isNew()
   {
     return getName().startsWith("[new]");
   }
 
   public String getPlainName()
   {
     if (isAdded()) {
       return getName().substring("[new]_".length(), getName().length());
     }
     if (isDeleted()) {
       return getName().substring("[deleted]_".length(), getName().length());
     }
     return getName();
   }
 
   public boolean isAdded()
   {
     return getName().startsWith("[new]");
   }
 
   public JLabel getLabel()
   {
     return this.label;
   }
 
   public void setLabel(JLabel label)
   {
     this.label = label;
   }
 
   public String toString()
   {
     return "Image " + getPlainName();
   }
 
   public void delete()
   {
     boolean deleted = this.thumbnailFile.delete();
     if (!deleted)
       Logger.getLogger(getClass()).error("Could not delete local file " + this.thumbnailFile.getAbsolutePath());
     else {
       Logger.getLogger(getClass()).info("Deleted local " + this.thumbnailFile.getName());
     }
 
     deleted = this.imageFile.delete();
 
     if (!deleted)
       Logger.getLogger(getClass()).error("Could not delete local file " + this.imageFile.getAbsolutePath());
     else
       Logger.getLogger(getClass()).info("Deleted local " + this.imageFile.getName());
   }
 
   public void setUploaded()
   {
     if (isAdded())
     {
       File newThumb = new File(this.thumbnailFile.getParentFile(), getPlainName());
       boolean renamed = this.thumbnailFile.renameTo(newThumb);
       if (!renamed) {
         Logger.getLogger(getClass()).error(
           "Error renaming " + this.thumbnailFile.getAbsolutePath() + " to " + newThumb.getAbsolutePath());
       }
       File newImg = new File(this.imageFile.getParentFile(), getPlainName());
       renamed = this.imageFile.renameTo(newImg);
       if (!renamed)
         Logger.getLogger(getClass()).error(
           "Error renaming " + this.imageFile.getAbsolutePath() + " to " + newImg.getAbsolutePath());
     }
   }
 
   public String getThumbnailJPGLink()
   {
     return "category_" + this.category.getId() + "/" + "thumbs" + "/" + getPlainName();
   }
 
   public String getImageHTMLLink()
   {
     return "pic_" + this.category.getId() + "_" + this.category.getLocalFiles().indexOf(this) + ".html";
   }
 
   public String getImageJPGLink()
   {
  	 return "category_" + this.category.getId() + "/" + "images" + "/" + getPlainName();
   }
 }