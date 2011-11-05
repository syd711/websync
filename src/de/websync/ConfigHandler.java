 package de.websync;
 
 import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.websync.model.Category;
import de.websync.util.PropertyHandler;
 
 public class ConfigHandler
 {
   private PropertyHandler prop = null;
   private Map<Integer, Category> categories = new HashMap<Integer, Category>();
 
   private static ConfigHandler instance = new ConfigHandler();
 
   private ConfigHandler()
   {
  	 prop = new PropertyHandler("conf/config.properties");
  	 prop.reloadProperties();
   }
 
   public void init()
   {
     Enumeration<?> enums = this.prop.getProperties().keys();
     while (enums.hasMoreElements())
     {
       String key = String.valueOf(enums.nextElement());
       if (!key.startsWith("category."))
         continue;
       int id = Integer.parseInt(key.substring(key.lastIndexOf(".") + 1, key.length()));
       String name = this.prop.getStringValue(key);
 
       Category category = new Category(id, name);
       this.categories.put(Integer.valueOf(id), category);
       Logger.getLogger(getClass()).info("Loaded " + category);
     }
   }
 
   public static ConfigHandler getInstance()
   {
     return instance;
   }
 
   public String getProjectName()
   {
     return this.prop.getStringValue("project.name");
   }
 
   public File getCategoryFolder(int id)
   {
     File f = new File("projects/" + getProjectName() + "/category_" + id);
     if (!f.exists())
     {
       f.mkdirs();
       Logger.getLogger(getClass()).info("Created category folder " + f.getAbsolutePath());
     }
     return f;
   }
 
   public Collection<Category> getCategories()
   {
     return this.categories.values();
   }
 
   public String getFTPUsername()
   {
     return this.prop.getStringValue("ftp.username");
   }
 
   public String getFTPPassword()
   {
     return this.prop.getStringValue("ftp.password");
   }
 
   public String getFTPHost()
   {
     return this.prop.getStringValue("ftp.host");
   }
 
   public int getMaxThumbnailSize()
   {
     return this.prop.getIntValue("max.thumbnail.size");
   }
 
   public int getMaxPictureSize()
   {
     return this.prop.getIntValue("max.picture.size");
   }
 
   public String getFTPRoot()
   {
     return this.prop.getStringValue("ftp.root.dir");
   }
 }