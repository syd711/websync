 package de.websync.util;
 
 import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.Charset;
 
 public class SystemTools
 {
 
   public static Charset getSystemCharset()
   {
     return Charset.defaultCharset();
   }
 
   public static String getLocalIP()
   {
     try
     {
       return InetAddress.getLocalHost().getHostAddress();
     }
     catch (Exception e) {
     }
     return "127.0.0.1";
   }
 
   public static boolean testSocket(int port)
   {
     try
     {
       ServerSocket server = new ServerSocket(port);
       server.close();
       return true;
     }
     catch (IOException e) {
     }
     return false;
   }
 }