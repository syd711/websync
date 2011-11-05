 package de.websync.util;
 
 import java.io.File;
 import java.util.Comparator;
 
 public class FileObjectComparator
   implements Comparator<File>
 {
   public int compare(File file1, File file2)
   {
     if (file1.lastModified() < file2.lastModified()) {
       return 1;
     }
     return -1;
   }
 }