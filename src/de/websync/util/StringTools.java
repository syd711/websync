package de.websync.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * @since 28.11.2005
 */
public class StringTools
{
	public final static String LINE_DELIMITER = System.getProperty("line.separator");
	public static boolean IS_UNIX = System.getProperty("os.name").indexOf("Win") == -1;

	public static final String INVALID_TAG_CHARS = "´^`$,.:;@|#-_ öäüÖÄÜ";
	public static final String INVALID_PACKAGE_CHARS = "?=)(/\\&%$§\"!-üöä#'*+'~ß^´;:-_<>| ";
	public static final String INVALID_FILENAME_CHARS = ":\"?<>|";
	public static final String INVALID_HOSTNAME_CHARS = "´^~!@#$^&*()=+[]{}\\|;:'\",";
	public static final String VALID_HEX_CHARS = "0123456789ABCDEFabcdef";

	public static final String[] FILE_KEYWORDS = { "${sequence}", "${timestamp}", "${source_name}",
		"${source_name_prefix}", "${source_name_suffix}", "${year}", "${month}", "${dayofmonth}", "${hour}",
		"${minute}", "${second}" };

	public static final String VARIABLE_PREFIX = "@{";

	public static final SimpleDateFormat SDF = new SimpleDateFormat();

	public static String getTextString(String string)
	{
		if (string == null)
			return "";

		return string;
	}
	
	public static String[] JVM_HEAP_SIZES = {"",  "64 MB", "128 MB", "512 MB", "768 MB", 
		"1040 MB", "2048 MB", "3072 MB", "4096 MB" };

	public static List<String> tokenizeXML(String xml)
	{
		List<String> result = new ArrayList<String>();
		while (xml.contains("<?xml"))
		{
			String token = xml.substring(xml.lastIndexOf("<?xml"), xml.length());
			result.add(token);
			xml = xml.substring(0, xml.lastIndexOf("<?xml"));
		}
		return result;
	}
	
	public static String[] LOCALES = null;	
	static
	{
		Locale[] LOCALESS = Locale.getAvailableLocales();
		String[] locales = new String[LOCALESS.length+1];
		locales[0] = "";
		for (int i = 1; i <= LOCALESS.length; i++)
		{
			Locale locale = LOCALESS[i-1];
			locales[i] = locale.getDisplayName(Locale.ENGLISH);
		}
		Arrays.sort( locales );
		LOCALES = locales;
	}
	
	public static String[] CHARSETS = null;
	static
	{
		Charset[] CHARSETSS = Charset.availableCharsets().values().toArray(new Charset[0]);
		String[] codepage = new String[CHARSETSS.length+1];
		codepage[0] = "";
		for (int i = 1; i <= CHARSETSS.length; i++)
		{
			codepage[i] = CHARSETSS[i-1].displayName();
		}
		Arrays.sort( codepage );
		CHARSETS = codepage;
	}
	
	public static String[] TIMEZONES = null;
	static
	{
		String[] timeZones= TimeZone.getAvailableIDs();
		List<String> timeZoneList = new ArrayList<String>();
		timeZoneList.add( "" );
		for (String string : timeZones)
		{
			timeZoneList.add( string );
		}
		TIMEZONES = timeZoneList.toArray( new String[0] );
		Arrays.sort( TIMEZONES );
	}
	

	/**
	 * format number of bytes as "x bytes" or "x KB"
	 * 
	 * @param bytes
	 * @return
	 */
	public static String formatBytes(long bytes)
	{
		StringBuffer buffer = new StringBuffer();
		if (bytes > 1024)
		{
			long kb = bytes / 1024;
			buffer.append(kb + " KB ");
		}

		if (bytes > 1024)
			buffer.append(bytes % 1024 + " Bytes");
		else
			buffer.append(bytes + " Bytes");

		return buffer.toString();
	}

	/**
	 * Returns true if the passed String is a valid IP address.
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isIPAddress(String ip)
	{
		try
		{
			String two_five_five = "(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))";
			Pattern IPPattern = Pattern.compile("^(?:" + two_five_five + "\\.){3}" + two_five_five + "$");
			boolean matches = IPPattern.matcher(ip).matches();
			return matches;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static Map<String, String> convertProperties2Map(Properties properties)
	{
		Map<String, String> props = new HashMap<String, String>();
		Iterator<Object> iterat = properties.keySet().iterator();
		while (iterat.hasNext())
		{
			String key = String.valueOf(iterat.next());
			String value = String.valueOf(properties.get(key));

			props.put(key, value);
		}

		return props;
	}

	/**
	 * Retrieves all necessary informations from the mapping thread exception.
	 * 
	 * @param e
	 * @return
	 */
	public static String evaluateException(Exception e)
	{
		String errorMessage = String.valueOf(e.getMessage());
		if (errorMessage.equals("null"))
			errorMessage = "";
		if (!String.valueOf(e).equals("null") && !StringTools.isEmptyString(String.valueOf(e)))
		{
			Throwable t = e.getCause();
			while (t != null)
			{
				String cause = String.valueOf(t);
				if (!cause.startsWith("null") && errorMessage.indexOf(cause) == -1)
					errorMessage = errorMessage + "\n\nRoot Cause:\n" + cause;

				t = t.getCause();
			}
		}

		return errorMessage;
	}

	/**
	 * Concats a path and a file information
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String concatPathAndFile(String a, String b)
	{
		if (!a.endsWith("/") && !b.startsWith("/"))
			a += "/";
		return a + b;
	}

	/**
	 * Validates the path to concat together.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String concatPaths(String a, String b)
	{
		if (!a.endsWith("/") && !b.startsWith("/"))
			a += "/";
		if (!b.endsWith("/") && b.length() > 0)
			b += "/";

		return a + b;
	}

	/**
	 * reads the inputstream and put it into a string
	 * 
	 * @throws IOException
	 */
	public static String readInputStream(InputStream stream) throws IOException
	{
		StringWriter sr = new StringWriter(0);

		int aByte = 0;
		while ((aByte = stream.read()) >= 0)
		{
			sr.write(aByte);
		}

		return sr.getBuffer().toString();
	}

	public static String replaceKeywords(String out)
	{
		if (out.indexOf("${") != -1)
		{
			out = StringTools.replaceVariable("${LF}", "\n", out);
			out = StringTools.replaceVariable("${TAB}", "\t", out);
		}
		return out;
	}

	public static boolean isNumber(String value)
	{
		try
		{
			if (value == null)
				return false;

			Integer.parseInt(value);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	public static boolean isBigDecimal(String value)
	{
		try
		{
			if (value == null)
				return false;

			new BigDecimal(value);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	public static boolean isBoolean(String value)
	{
		try
		{
			if (value == null)
				return false;

			Boolean.parseBoolean(value);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}


	/**
	 * Ensures that path contains no backslashes and ends with a '/'.
	 * 
	 * @param path
	 * @return
	 */
	public static String formatPath(String path)
	{
		if (!path.trim().endsWith("/"))
			path = path.trim() + "/";

		if (path.indexOf(":") != -1 && path.startsWith("/"))
			path = path.substring(3, path.length());

		return path.replaceAll("\\\\", "/");
	}

	/**
	 * Simplyfies regular expressions.
	 * 
	 * @param pattern
	 * @return
	 */
	public static String formatPattern(String pattern)
	{
		if (pattern.contains("${REGEX"))
			return pattern;

		if (pattern.contains(VARIABLE_PREFIX))
			return pattern;

		if (pattern.startsWith("*"))
		{
			pattern = "." + pattern;
			return pattern;
		}
		if (pattern.equals(".*"))
			return pattern;
		if (pattern.equals(".*.dat"))
			return pattern;

		pattern = pattern.replaceAll("\\.", "\\\\.");
		while (pattern.indexOf(".*") == -1 && pattern.indexOf("*") != -1)
			pattern = pattern.replaceAll("\\*", ".*");
		while (pattern.indexOf("[") != -1)
			pattern = pattern.replaceAll("\\[", ".");
		while (pattern.indexOf("]") != -1)
			pattern = pattern.replaceAll("\\]", ".");

		return pattern;
	}

	public static boolean validateHostname(String host)
	{
		String invalidCharacters = INVALID_HOSTNAME_CHARS;

		for (int i = 0; i < invalidCharacters.length(); i++)
		{
			char[] chars = host.toCharArray();
			for (int j = 0; j < chars.length; j++)
			{
				if (chars[j] == invalidCharacters.charAt(i))
					return false;
			}
		}
		return true;
	}



	/**
	 * Validates if a packagename is correct.
	 * 
	 * @param packageName
	 * @return
	 */
	public static boolean validatePackageString(String packageName)
	{
		String invalidCharacters = INVALID_PACKAGE_CHARS;

		if (packageName.endsWith("."))
			return false;

		for (int i = 0; i < packageName.length(); i++)
		{
			char character = packageName.charAt(i);

			if (isNumber(character) && i == 0)
				return false;

			for (int j = 0; j < invalidCharacters.length(); j++)
			{
				char invalidCharacter = invalidCharacters.charAt(j);
				if (invalidCharacter == character)
					return false;

				if (character == '.' && !((i + 1) > packageName.length()) && isNumber(packageName.charAt(i + 1)))
					return false;
			}
		}
		return true;
	}

	/**
	 * Validates if a packagename is correct.
	 * 
	 * @param packageName
	 * @return
	 */
	public static boolean validateTagString(String tag)
	{
		String invalidCharacters = INVALID_TAG_CHARS;

		if (tag == null)
			return false;

		for (int i = 0; i < tag.length(); i++)
		{
			char character = tag.charAt(i);

			if (isNumber(character) && i == 0)
				return false;

			for (int j = 0; j < invalidCharacters.length(); j++)
			{
				char invalidCharacter = invalidCharacters.charAt(j);
				if (invalidCharacter == character)
					return false;

				if (character == '.' && !((i + 1) > tag.length()) && isNumber(tag.charAt(i + 1)))
					return false;
			}
		}
		return true;
	}

	private static boolean isNumber(char character)
	{
		String validCharacters = "0987654321";

		for (int i = 0; i < validCharacters.length(); i++)
		{
			if (character == validCharacters.charAt(i))
				return true;
		}
		return false;
	}

	public static boolean isValidContextName(String context)
	{
		return validatePackageString(context);
	}

	public static String replaceRegularExpressionVariable(String pattern, String sourceName)
	{
		if (isEmptyString(sourceName) || isEmptyString(pattern))
			return pattern;
		if (!pattern.contains("${REGEX:"))
			return pattern;

		String regex = pattern.substring(pattern.indexOf("${REGEX:") + 8, pattern.indexOf("}"));
		List<MatchResult> results = new ArrayList<MatchResult>();
		for (Matcher m = Pattern.compile(regex).matcher(sourceName); m.find();)
			results.add(m.toMatchResult());

		for (MatchResult matchResult : results)
		{
			if (matchResult.start() == -1 || matchResult.end() == -1)
				continue;
			if (sourceName.length() < matchResult.start() || sourceName.length() < matchResult.end())
				continue;
			if (matchResult.start() == matchResult.end())
				continue;

			String match = sourceName.substring(matchResult.start(), matchResult.end());
			String pre = pattern.substring(0, pattern.indexOf("${REGEX:"));
			String post = pattern.substring(pattern.indexOf("}") + 1, pattern.length());
			pattern = pre + match + post;
		}

		return pattern;
	}

	/**
	 * Validates if a folder name is valid.
	 * 
	 * @param folder
	 * @return
	 */
	public static boolean validateFolder(String folder)
	{

		if (folder.indexOf(":") == 1) // e.g. C:\...
			folder = folder.substring(2, folder.length());
		String invalidCharacters = INVALID_FILENAME_CHARS;

		for (int i = 0; i < invalidCharacters.length(); i++)
		{
			char[] chars = folder.toCharArray();
			for (int j = 0; j < chars.length; j++)
			{
				if (chars[j] == invalidCharacters.charAt(i))
					return false;
			}
		}
		return true;
	}

	/**
	 * Validates emails addresses.
	 * 
	 * @param folder
	 * @return
	 */
	public static boolean validateEmail(String mail)
	{

		// Set the email pattern string
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");

		// Match the given string with the pattern
		Matcher m = p.matcher(mail);

		// check whether match is found
		return m.matches();
	}

	/**
	 * Replaces a variable string in text string with a value string.
	 * 
	 * @param variable
	 * @param text
	 * @return
	 */
	public static String replaceVariable(String variable, String value, String text)
	{
		if (text == null)
			return text;

		while (text.indexOf(variable) != -1)
		{
			String pre = text.substring(0, text.indexOf(variable));
			String after = text.substring(text.indexOf(variable) + variable.length(), text.length());
			text = pre + value + after;
		}
		return text;
	}

	/**
	 * get a string object from a calendar
	 * 
	 * @param aCalendar DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String getCalendarString(Calendar aCalendar)
	{
		String aYear = Integer.toString(aCalendar.get(Calendar.YEAR));
		String aMonth = Integer.toString(aCalendar.get(Calendar.MONTH) + 1);
		String aDay = Integer.toString(aCalendar.get(Calendar.DATE));
		String aHour = Integer.toString(aCalendar.get(Calendar.HOUR_OF_DAY));
		String aMinute = Integer.toString(aCalendar.get(Calendar.MINUTE));
		String aSecond = Integer.toString(aCalendar.get(Calendar.SECOND));

		if (aMonth.length() == 1)
			aMonth = "0" + aMonth;

		if (aDay.length() == 1)
			aDay = "0" + aDay;

		if (aHour.length() == 1)
			aHour = "0" + aHour;

		if (aMinute.length() == 1)
			aMinute = "0" + aMinute;

		if (aSecond.length() == 1)
			aSecond = "0" + aSecond;

		return aDay + "." + aMonth + "." + aYear + " " + aHour + ":" + aMinute + ":" + aSecond;
	}

	/**
	 * @param map
	 * @return
	 */
	public static String formatMap(Map<?, ?> map)
	{
		StringBuffer buffer = new StringBuffer();

		Iterator<?> it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			Object value = map.get(key);
			buffer.append(key + " = " + value + "\n");
		}
		return buffer.toString();
	}

	/**
	 * right fill up a string
	 * 
	 * @param aString input value
	 * @param aFillCharacter String to append from right
	 * @param aSize wished length
	 * @return the append string with wished length
	 */
	public static final String fillString(String aString, String aFillCharacter, int aSize)
	{

		if (aString == null)
			aString = "";

		int fillCount = aSize - aString.length();

		for (int i = 1; i <= fillCount; i++)
		{
			aString = aString + aFillCharacter;
		}

		return aString.substring(0, aSize);
	}

	/**
	 * left fill up a string
	 * 
	 * @param aString input value
	 * @param aFillCharacter String to append from left
	 * @param aSize wished length
	 * @return the append string with wished length
	 */
	public static final String fillStringLeft(String aString, String aFillCharacter, int aSize)
	{
		if (aString == null)
			aString = "";

		int fillCount = aSize - aString.length();
		String leftSide = "";

		for (int i = 1; i <= fillCount; i++)
		{
			leftSide = leftSide + aFillCharacter;
		}

		return leftSide.substring(0, fillCount) + aString;
	}

	/**
	 * Replace a String with another String
	 * 
	 * @param sentence DOCUMENT ME!
	 * @param firstWord DOCUMENT ME!
	 * @param secondWord DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static final String replaceString(String sentence, String firstWord, String secondWord)
	{

		int i = 0;

		if ((i = sentence.indexOf(firstWord, 0)) != -1)

			return sentence.substring(0, i) + secondWord
				+ replaceString(sentence.substring(i + firstWord.length(), sentence.length()), firstWord, secondWord);

		return sentence;
	}

	/**
	 * Converts a numeric decimal string into a defined format.
	 * 
	 * @param numberStr The string number to be formatted.
	 * @param format The number format.
	 * @return The converted number string or an empty string if the number was
	 *         invalid
	 */
	public static final String decimalNumberFormat(String numberStr, String format)
	{

		final char DECIMAL_SEPARATOR = '.';
		final char GROUP_SEPARATOR = ',';
		final char INVALID_DECIMAL_SEPARATOR = ',';
		String retNumberStr = "";

		if (numberStr != null)
		{

			try
			{

				// Set the decimal sparators independent from locales.
				DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setDecimalSeparator(DECIMAL_SEPARATOR);
				dfs.setGroupingSeparator(GROUP_SEPARATOR);
				numberStr = numberStr.replace(INVALID_DECIMAL_SEPARATOR, DECIMAL_SEPARATOR);

				double decNum = Double.parseDouble(numberStr);
				NumberFormat formatter = new DecimalFormat(format, dfs);
				retNumberStr = formatter.format(decNum);
			}
			catch (NumberFormatException nExc)
			{
				// return simply null if the number format fails
			}
		}

		return retNumberStr;
	}

	/**
	 * The 'replace' method replaces one or more keywords in a string with the
	 * received replacement strings in order of the array. That means the first
	 * keyword will be replaced by the first replacement string and so on.
	 * 
	 * @param baseStr A string containing the placeholders to replace.
	 * @param keyword An array of zero to n keywords.
	 * @param replacement An array of zero to n replacement strings.
	 * @return The baseStr having replaced all placeholders defined by the
	 *         keywords.
	 */
	public static String replace(String baseStr, String[] keyword, String[] replacement)
	{

		StringBuffer strBuf = new StringBuffer();
		int i = 0;
		int count = 0;

		if (keyword != null)
		{

			for (count = 0; count < keyword.length; count++)
			{
				i = baseStr.indexOf(keyword[count], 0);

				if (i >= 0)
				{
					strBuf.append(baseStr);
					strBuf.replace(i, i + keyword[count].length(), replacement[count]);
					baseStr = strBuf.toString();
					strBuf.delete(0, strBuf.length());
				}

				// End-if (i >= 0)
			}

			// End-for
		}

		// End-if (keyword != null)
		return baseStr;
	}

	public static String getAttributeValue(Map<String, String> aAttributeMap, String aTag)
	{
		return String.valueOf(aAttributeMap.get(aTag));
	}

	public static String getOptionalAttributeValue(Map<String, String> aAttributeMap, String aTag, String aDefaultValue)
	{
		String value = String.valueOf(aAttributeMap.get(aTag));
		if (value.equals("null"))
		{
			value = aDefaultValue;
		}
		return value;
	}

	public static Boolean getOptionalBooleanValue(Map<String, String> aAttributeMap, String aTag, String aDefaultValue)
	{
		String value = String.valueOf(aAttributeMap.get(aTag));
		if (value.equals("null"))
		{
			if (aDefaultValue == null)
				return null;

			value = aDefaultValue;
		}
		return new Boolean(value);
	}

	public static Integer getOptionalIntegerValue(Map<String, String> aAttributeMap, String aTag, String aDefaultValue)
	{
		String value = String.valueOf(aAttributeMap.get(aTag));
		if (value.equals("null"))
		{
			if (aDefaultValue == null)
				return null;

			value = aDefaultValue;
		}
		return new Integer(value);
	}

	/**
	 * @return true if string is null or contains no characters
	 */
	public static boolean isEmptyString(String value)
	{
		return (value == null || value.trim().length() == 0);
	}

	/**
	 * Checks how often the passed symbol occurs in the given string.
	 * 
	 * @param symbol
	 * @param string
	 * @return
	 */
	public static int getSymbolNumber(char symbol, String string)
	{
		char[] stringarray = string.toCharArray();
		int count = 0;
		for (int i = 0; i < stringarray.length; i++)
		{
			if (stringarray[i] == symbol)
				count++;
		}
		return count;
	}

	/**
	 * This value converts a characterbased value into a DataBase convert
	 * character based value.
	 * 
	 * @param aString
	 * @return a Database-conform String
	 */
	public static String toDBString(String aString)
	{
		return aString.replaceAll("'", "''");
	}

	/**
	 * Checks if the firstVersion is bigger than the second Version
	 * 
	 * @param descriptor
	 * @return
	 */
	public static boolean isVersionGreaterThan(int[] firstVersion, int[] secondVersion)
	{
		if (firstVersion[0] > secondVersion[0])
			return true;
		else if (firstVersion[0] == secondVersion[0])
		{
			if (firstVersion[1] > secondVersion[1])
				return true;
			else if (firstVersion[1] == secondVersion[1])
			{
				if (firstVersion[2] > secondVersion[2])
					return true;
				else if (firstVersion[2] == secondVersion[2])
				{
					if (firstVersion[3] > secondVersion[3])
						return true;
				}
			}

		}

		return false;
	}

	/**
	 * Splits the version into an int array.
	 * 
	 * @param version
	 * @return
	 */
	public static int[] getVersion(String version)
	{
		StringTokenizer tokenizer = new StringTokenizer(version, ".");
		int[] versions = new int[4];
		int i = 0;

		while (tokenizer.hasMoreElements())
		{
			String token = tokenizer.nextToken();
			versions[i] = Integer.parseInt(token);
			i++;
		}
		return versions;
	}

	public static String incVersion(String version, int major, int minor, int build, int patch)
	{
		int[] versions = getVersion(version);
		versions[0] += major;
		if (major > 0)
		{
			versions[1] = 0;
			versions[2] = 0;
		}
		versions[1] += minor;
		if (minor > 0)
		{
			versions[2] = 0;
		}
		versions[2] += build;

		String versionValue = String.valueOf(versions[0] + "." + versions[1] + "." + versions[2]);
		String patchValue = "";

		if (patch != -1)
			patchValue = "." + String.valueOf(versions[3] + patch);

		return versionValue + patchValue;
	}

	public static String decVersion(String version, int major, int minor, int build, int patch)
	{
		int[] versions = getVersion(version);
		versions[0] -= major;
		if (major > 0)
		{
			versions[1] = 0;
			versions[2] = 0;
		}
		versions[1] -= minor;
		if (minor > 0)
		{
			versions[2] = 0;
		}
		versions[2] -= build;

		String versionValue = String.valueOf(versions[0] + "." + versions[1] + "." + versions[2]);
		String patchValue = "";

		if (patch != -1)
			patchValue = "." + String.valueOf(versions[3] - patch);

		return versionValue + patchValue;
	}



	/**
	 * @param filename
	 * @return
	 */
	public static String getFileSize(String filename)
	{
		String filelength = null;
		File file = new File(filename);
		double length = file.length();

		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

		if (length >= 1024d * 1024d * 0.8d)
		{
			filelength = df.format(length / (1024d * 1024d)) + "MB";
		}
		else if (length >= 1024d * 0.8d)
		{
			filelength = df.format(length / 1024d) + "KB";
		}
		else
			filelength = length + "Bytes";

		return filelength;
	}

	/**
	 * Replace the DATE variable
	 * 
	 * @param value
	 * @return
	 */
	public static String replaceDateVariable(String value)
	{
		while (value.contains("${DATE:"))
		{
			String variable = value.substring(value.indexOf("${"), value.indexOf("}") + 1);
			String pattern = value.substring(value.indexOf("${DATE:") + 7, value.indexOf("}"));
			SimpleDateFormat format = new SimpleDateFormat();
			format.applyPattern(pattern);
			value = StringTools.replaceVariable(variable, format.format(new Date()), value);
		}

		return value;
	}

	/**
	 * @param returnValues
	 * @param string
	 */
	public static void logMap(Map<?, ?> returnValues, String string)
	{

		Iterator<?> it = returnValues.keySet().iterator();
		StringBuffer buffer = new StringBuffer("\n" + string + "\n=================================\n");
		while (it.hasNext())
		{
			Object key = it.next();
			Object value = returnValues.get(key);
			buffer.append(key);
			buffer.append(":\t");
			buffer.append(value);
			buffer.append("\n");
		}
		Logger.getLogger(StringTools.class).debug(buffer.toString());
	}

	/**
	 * checks string for '0'
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isAllZero(String s)
	{
		char[] ch = s.toCharArray();
		for (int i = 0; i < ch.length; i++)
		{
			if (ch[i] != '0')
				return false;
		}
		return true;
	}

	/**
	 * display date in default simple date format
	 * 
	 * @param date
	 * @return
	 */
	public static String simpleDateString(Date date)
	{
		return SDF.format(date);
	}

	public static String formatXML( String s )
	{
		if( s == null )
			return null;
		
		s = s.replaceAll("&", "&amp;");
		// s = s.replaceAll( "'", "&apos;" );
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		s = s.replaceAll("\"", "&quot;");
		return s;
	}

	public static String formatDouble(double d)
	{
		String number = String.valueOf( d );
		if( number.indexOf(".") != -1 )
		{
			number = number.substring( 0, number.indexOf(".")+2 );
		}
		return number;
	}
	
	
}
