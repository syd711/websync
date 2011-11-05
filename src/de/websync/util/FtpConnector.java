/*     */ package de.websync.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import org.apache.commons.net.ftp.FTPClient;
/*     */ import org.apache.commons.net.ftp.FTPReply;
/*     */ 
/*     */ public class FtpConnector
/*     */ {
/*     */   public static FTPClient connectAndGotodir(String server, int port, String username, String password, String absolutePath, int fileType)
/*     */     throws FtpTransferException
/*     */   {
/*  20 */     FTPClient ftp = new FTPClient();
/*     */     try
/*     */     {
/*  24 */       ftp.connect(server, port);
/*     */ 
/*  27 */       int reply = ftp.getReplyCode();
/*  28 */       if (!FTPReply.isPositiveCompletion(reply))
/*     */       {
/*  30 */         disconnect(ftp);
/*  31 */         throw new FtpTransferException("FTP server refused connection.");
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  36 */       disconnect(ftp);
/*  37 */       throw new FtpTransferException("Could not connect to ftp server. " + e.getMessage());
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  42 */       if (!ftp.login(username, password))
/*     */       {
/*  44 */         ftp.logout();
/*  45 */         throw new FtpTransferException("Could not login to the ftp server.");
/*     */       }
/*  47 */       ftp.setFileType(fileType);
/*     */ 
/*  50 */       ftp.enterLocalPassiveMode();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  54 */       disconnect(ftp);
/*  55 */       throw new FtpTransferException("Ftp error:  " + e.getMessage());
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  62 */       if (!ftp.changeWorkingDirectory(absolutePath))
/*     */       {
/*  64 */         ftp.logout();
/*  65 */         disconnect(ftp);
/*  66 */         throw new FtpTransferException("Failed to change to ftp directory: '" + absolutePath + "'");
/*     */       }
/*     */     }
/*     */     catch (IOException e1)
/*     */     {
/*  71 */       disconnect(ftp);
/*  72 */       throw new FtpTransferException("Failed to change to ftp directory: " + absolutePath + ".");
/*     */     }
/*     */ 
/*  75 */     return ftp;
/*     */   }
/*     */ 
/*     */   public static void disconnect(FTPClient ftp)
/*     */   {
/*     */     try
/*     */     {
/*  86 */       if ((ftp != null) && (ftp.isConnected()))
/*     */       {
/*  88 */         ftp.disconnect();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  93 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 102 */     return "FTP Transporter";
/*     */   }
/*     */ }

/* Location:           D:\downloads\websync.jar
 * Qualified Name:     de.websync.util.FtpConnector
 * JD-Core Version:    0.6.0
 */