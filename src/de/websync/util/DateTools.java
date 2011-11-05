 package de.websync.util;
 
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.StringTokenizer;
 import java.util.TimeZone;
 
 public class DateTools
 {
   public static String formatTime(Timestamp timestamp)
   {
     SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
     if (timestamp == null) {
       return "--";
     }
     Date date = new Date(timestamp.getTime());
     return formatter.format(date);
   }
 
   public static String formatDate(Timestamp timestamp)
   {
     SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
     if (timestamp == null) {
       return "--";
     }
     Date date = new Date(timestamp.getTime());
     return formatter.format(date);
   }
 
   public static String formatShortTime(Timestamp timestamp)
   {
     SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
     if (timestamp == null) {
       return "--";
     }
     Date date = new Date(timestamp.getTime());
     return formatter.format(date);
   }
 
   public static String formatDate(Date date)
   {
     DateFormat df = DateFormat.getDateInstance(2);
     if (date == null) {
       return "--";
     }
     return df.format(date);
   }
 
   public static String formatShortDate(Date date)
   {
     DateFormat df = DateFormat.getDateInstance(3);
     if (date == null) {
       return "--";
     }
     String formattedDate = df.format(date);
     return formattedDate.substring(0, formattedDate.lastIndexOf('.') + 1);
   }
 
   public static String formatDateTime(Date date)
   {
     if (date == null) {
       return "";
     }
     String returnValue = "";
     DateFormat df = DateFormat.getDateInstance(2);
     df = DateFormat.getTimeInstance(2);
     returnValue = returnValue + df.format(date);
     return returnValue;
   }
 
   public static String formatTime(Date date)
   {
     DateFormat df = DateFormat.getTimeInstance(2);
     return df.format(date);
   }
 
 
 
   public static final boolean isBetweenTimeFrame(String time1, String time2)
   {
     String systemTime = null;
     systemTime = getSystemDateTime("TIME");
 
     if (systemTime == null) {
       return false;
     }
     int systemHH = 0;
     int systemMM = 0;
     int startHH = 0;
     int stopHH = 0;
     int startMM = 0;
     int stopMM = 0;
     StringTokenizer timeString = new StringTokenizer(systemTime, ":");
 
     if (timeString.hasMoreTokens()) {
       systemHH = Integer.parseInt(timeString.nextToken());
     }
     if (timeString.hasMoreTokens()) {
       systemMM = Integer.parseInt(timeString.nextToken());
     }
 
     timeString = new StringTokenizer(time1, ":");
 
     if (timeString.hasMoreTokens()) {
       startHH = Integer.parseInt(timeString.nextToken());
     }
     if (timeString.hasMoreTokens()) {
       startMM = Integer.parseInt(timeString.nextToken());
     }
 
     timeString = new StringTokenizer(time2, ":");
 
     if (timeString.hasMoreTokens()) {
       stopHH = Integer.parseInt(timeString.nextToken());
     }
     if (timeString.hasMoreTokens()) {
       stopMM = Integer.parseInt(timeString.nextToken());
     }
 
     if (startHH > stopHH)
     {
       if ((systemHH > startHH) || (systemHH < stopHH))
       {
         return true;
       }
       if ((systemHH == startHH) && (systemMM >= startMM))
       {
         return true;
       }
       if ((systemHH == stopHH) && (systemMM <= stopMM))
       {
         return true;
       }
     }
     if (startHH < stopHH)
     {
       if ((systemHH > startHH) && (systemHH < stopHH))
       {
         return true;
       }
       if ((systemHH == startHH) && (systemMM >= startMM))
       {
         return true;
       }
       if ((systemHH == stopHH) && (systemMM <= stopMM))
       {
         return true;
       }
     }
     if (startHH == stopHH)
     {
       if ((systemHH == startHH) && (systemMM >= startMM) && (systemMM <= stopMM))
       {
         return true;
       }
     }
     return false;
   }
 
   public static final String getSystemDateTime(String aPicture)
   {
     String styleString = "";
     String aDateTime = "";
 
     if (aPicture.equals("DATE")) {
       styleString = "dd.MM.yyyy";
     }
     if (aPicture.equals("TIMESTAMP")) {
       styleString = "dd.MM.yyyy HH:mm:ss";
     }
     if (aPicture.equals("TIME")) {
       styleString = "HH:mm:ss";
     }
     Calendar cal = Calendar.getInstance(TimeZone.getDefault());
     SimpleDateFormat sdf = new SimpleDateFormat(styleString);
     sdf.setTimeZone(TimeZone.getDefault());
     aDateTime = sdf.format(cal.getTime());
 
     return aDateTime;
   }
 
   public static boolean isToday(Date date)
   {
     GregorianCalendar cal = new GregorianCalendar();
     String dateStringToday = cal.get(5) + ":" + cal.get(2) + ":" + cal.get(1);
 
     GregorianCalendar dateCal = new GregorianCalendar();
     dateCal.setTime(date);
     String dateStringFile = dateCal.get(5) + ":" + dateCal.get(2) + ":" + dateCal.get(1);
 
     return dateStringToday.equals(dateStringFile);
   }
 }