 package de.websync.util;
 
 import java.io.File;
 import org.apache.log4j.Logger;
 
 public class HTMLGenerator
 {
   private File template = null;
   private File target = null;
 
   private String content = null;
 
   public HTMLGenerator(File template, File target) throws Exception
   {
     this.template = template;
     this.target = target;
 
     Logger.getLogger(getClass()).info("Read template " + this.template.getAbsolutePath());
     this.content = FileTools.readBufferedFileContent(this.template.getAbsolutePath());
 
     if (target.exists())
     {
       target.delete();
     }
   }
 
   public void replace(String varName, String value)
   {
     this.content = StringTools.replaceVariable(varName, value, this.content);
   }
 
   public void generate() throws Exception
   {
     FileTools.createFile(this.target, this.content);
     Logger.getLogger(getClass()).info("Written file " + this.target.getAbsolutePath());
   }
 }