 package de.websync.util;
 
 import java.io.File;
 import java.util.Comparator;
 
 public class FileComparator
   implements Comparator<File>
 {
   public int compare(File o1, File o2)
   {
     if (o1.lastModified() < o2.lastModified()) {
       return 1;
     }
     return -1;
   }
 }