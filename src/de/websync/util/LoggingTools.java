package de.websync.util;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;


/**
 * Utility class used for Log4J logging.
 */
public class LoggingTools
{
	private static final String LOG4J_FILE = "conf/log4j.xml";
	private static String LOG4J_CONF_FILENAME = null;

	public static void initLog4J() throws Exception
	{
		initLog4J(LOG4J_FILE);
	}

	public static void initLog4J(String filename) throws Exception
	{
		if (filename == null)
		{
			initLog4J();
			return;
		}

		LOG4J_CONF_FILENAME = filename;

		InputStream in = null;
		try
		{
			in = new FileInputStream(filename);
		}
		catch (Exception e)
		{
			in = ClassLoader.getSystemResourceAsStream(filename);

			if (in == null)
				in = LoggingTools.class.getResourceAsStream(filename);
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			DOMConfigurator.configure(doc.getDocumentElement());

			Logger.getLogger(LoggingTools.class).debug("Initialized Log4J using " + filename);
		}
		catch (Exception e1)
		{
			throw e1;
		}
	}

}
