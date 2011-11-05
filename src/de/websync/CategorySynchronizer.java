 package de.websync;
 
 import de.websync.model.Category;
 import de.websync.model.ImageInfo;
 import de.websync.util.FtpConnector;
import de.websync.util.StringTools;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import javax.swing.JProgressBar;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
 
 public class CategorySynchronizer
 {
   private FTPClient ftpClient = null;
   private Category category = null;
   private String remoteRoot = null;
   public static final String DIR_THUMBS = "thumbs";
   public static final String DIR_IMAGES = "images";
 
   public static CategorySynchronizer newInstance(Category category)
   {
     return new CategorySynchronizer(category);
   }
 
   private CategorySynchronizer(Category category)
   {  	 
     this.category = category;
   }
 
   public void connect() throws Exception
   {
     String username = ConfigHandler.getInstance().getFTPUsername();
     String password = ConfigHandler.getInstance().getFTPPassword();
     String host = ConfigHandler.getInstance().getFTPHost();
     this.remoteRoot = ConfigHandler.getInstance().getFTPRoot();
     Logger.getLogger(getClass()).info("Logging onto folder " + ConfigHandler.getInstance().getFTPRoot());
 
     this.ftpClient = FtpConnector.connectAndGotodir(host, 21, username, password, this.remoteRoot, 0);
     this.ftpClient.setFileType(2);
     this.ftpClient.enterLocalPassiveMode();
     Logger.getLogger(getClass()).info("Connected with " + host);
 
     initRemoteEnv();
   }
 
   private void initRemoteEnv()
     throws Exception
   {
     this.ftpClient.mkd("category_" + this.category.getId());
     this.ftpClient.changeWorkingDirectory("category_" + this.category.getId());
     this.ftpClient.mkd("images");
     this.ftpClient.mkd("thumbs");
     Logger.getLogger(getClass()).info("Remote creation of " + this.category + " finished.");
   }
 
   public void disconnect() throws Exception
   {
     if (this.ftpClient != null)
       this.ftpClient.disconnect();
   }
 
   public void downloadCategory(JProgressBar bar) throws Exception
   {
     this.ftpClient.changeWorkingDirectory("thumbs");
     FTPFile[] files = this.ftpClient.listFiles();
 
     bar.setMaximum(files.length);
     int i = 0;
     for (FTPFile file : files)
     {
       i++;
       if (file == null) {
         continue;
       }
       if (file.isDirectory())
       {
         Logger.getLogger(getClass()).debug("Skipping directory " + file.getName());
       }
       else
       {
         bar.setValue(i);
         bar.setString("Downloading '" + file.getName() + "'");
         File tnFile = new File(this.category.getThumbnailDirectory(), file.getName());
         File imgFile = new File(this.category.getImageDirectory(), file.getName());
 
         FileOutputStream out = null;
         try
         {
           out = new FileOutputStream(tnFile);
           this.ftpClient.retrieveFile(file.getName(), out);
           Logger.getLogger(getClass()).info("Downloaded thumb file " + tnFile.getAbsolutePath());
         }
         catch (Exception e)
         {
           Logger.getLogger(getClass()).error("Error thumb downloading " + file.getName(), e);
         }
         finally
         {
           this.ftpClient.cdup();
           if (out != null) {
             out.close();
           }
         }
 
         this.ftpClient.changeWorkingDirectory("images");
         out = null;
         try
         {
           out = new FileOutputStream(imgFile);
           this.ftpClient.retrieveFile(file.getName(), out);
           Logger.getLogger(getClass()).info("Downloading image file " + imgFile.getAbsolutePath());
         }
         catch (Exception e)
         {
           Logger.getLogger(getClass()).error("Error image downloading " + file.getName(), e);
         }
         finally
         {
           this.ftpClient.cdup();
           if (out != null)
             out.close();
         }
         
         this.ftpClient.changeWorkingDirectory("thumbs");
       }
     }
   }
 
   public void synchronizeImageInfo(ImageInfo info) throws Exception {
     this.ftpClient.changeWorkingDirectory("thumbs");
     Logger.getLogger(getClass()).info("Changed to " + this.ftpClient.printWorkingDirectory());
     FileInputStream in = null;
     try
     {
       this.ftpClient.deleteFile(info.getPlainName());
       Logger.getLogger(getClass()).info("Deleted FTP file " + info.getPlainName()); 
       
       if (!info.isDeleted())
       {
         in = new FileInputStream(info.getThumbnailFile());
         this.ftpClient.storeFile(info.getPlainName(), in);
         in.close();
         Logger.getLogger(this.getClass()).info("Uploaded thumb file " + info.getThumbnailFile().getAbsolutePath() + "/" + StringTools.formatBytes(info.getThumbnailFile().length()));
       }
       else
       {
         info.delete();
       }
 
     }
     catch (Exception e)
     {
       Logger.getLogger(getClass()).error("Error uploading " + info.getName(), e);
     }
     finally
     {
       this.ftpClient.cdup();
       if (in != null) {
         in.close();
       }
     }
 
     this.ftpClient.changeWorkingDirectory("images");
     Logger.getLogger(getClass()).info("Changed to " + this.ftpClient.printWorkingDirectory());
     in = null;
     try
     {
       this.ftpClient.deleteFile(info.getPlainName());
 
       if (!info.isDeleted())
       {
         in = new FileInputStream(info.getImageFile());
         this.ftpClient.storeFile(info.getPlainName(), in);
         in.close();
         Logger.getLogger(getClass()).info("Uploaded file " + info.getImageFile().getAbsolutePath() + "/" + StringTools.formatBytes(info.getImageFile().length()));
       }
 
     }
     catch (Exception e)
     {
       Logger.getLogger(getClass()).error("Error uploading " + info.getName(), e);
     }
     finally
     {
       this.ftpClient.cdup();
       if (in != null) {
         in.close();
       }
       info.setUploaded();
     }
   }
 
   public void synchronizeHTMLFile(File file)
   {
     FileInputStream in = null;
     try
     {
       this.ftpClient.deleteFile(file.getName());
       in = new FileInputStream(file);
       this.ftpClient.storeFile(file.getName(), in);
       in.close();
       Logger.getLogger(getClass()).info("Uploaded " + file.getName());
     }
     catch (Exception e)
     {
       Logger.getLogger(getClass()).error("Error uploading " + file.getName(), e);
       try
       {
         if (in != null) {
           in.close();
         }
         this.ftpClient.changeWorkingDirectory(this.category.getName() + "_" + this.category.getId());
       }
       catch (IOException ie)
       {
         ie.printStackTrace();
       }
     }
     finally
     {
       try
       {
         if (in != null) {
           in.close();
         }
         this.ftpClient.changeWorkingDirectory(this.category.getName() + "_" + this.category.getId());
       }
       catch (IOException e)
       {
         e.printStackTrace();
       }
     }
   }
 
   public void goUp() throws IOException
   {
     this.ftpClient.cdup();
     Logger.getLogger(getClass()).info("Working on " + this.ftpClient.printWorkingDirectory());
   }
 
   public void goCategory()
     throws IOException
   {
     this.ftpClient.changeWorkingDirectory("category_" + this.category.getId());
     Logger.getLogger(getClass()).info("Working on " + this.ftpClient.printWorkingDirectory());
   }
 
   public void setMode(int type)
     throws IOException
   {
     this.ftpClient.setFileType(type);
   }
 }