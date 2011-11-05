 package de.websync.gui;
 
 import de.websync.util.FileTools;
 import java.io.File;
 import javax.swing.filechooser.FileFilter;
 
 public class ImageFilter extends FileFilter
 {
   public boolean accept(File f)
   {
     if (f.isDirectory())
     {
       return true;
     }
 
     String extension = FileTools.getFileExtension(f);
     if (extension != null)
     {
       if ((extension.contains("jpg")) || (extension.contains("JPG"))) {
         return true;
       }
     }
     return false;
   }
 
   public String getDescription()
   {
     return "JPG";
   }
 }