 package de.websync;
 
 import de.websync.gui.MainWindow;
 import de.websync.util.LoggingTools;
 
 public class Controller
 {
   private static Controller controllerInstance = null;
 
   private MainWindow mainWindow = null;
 
   public static Controller getInstance()
   {
     if (controllerInstance == null)
     {
       controllerInstance = new Controller();
       return controllerInstance;
     }
 
     return controllerInstance;
   }
 
   private void initController()
     throws Exception
   {
     LoggingTools.initLog4J("conf/log4j.xml");
     ConfigHandler.getInstance().init();
     MainWindow mainWindow = new MainWindow();
     this.mainWindow = mainWindow;
   }
 
   public static void main(String[] args)
     throws Exception
   {
     Controller controller = getInstance();
     controller.initController();
   }
 
   public MainWindow getMainWindow()
   {
     return this.mainWindow;
   }
 }