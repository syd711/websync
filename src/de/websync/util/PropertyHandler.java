package de.websync.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyHandler
{
	private Properties props = new Properties();
	private String filename = null;
	private Class<?> clazz = null;

	public PropertyHandler(Properties properties, String filename)
	{
		this.filename = filename;
		this.props = properties;
	}

	public PropertyHandler(String filename)
	{
		this(filename, false);
	}

	public PropertyHandler(String filename, boolean createNewFile)
	{
		try
		{
			this.filename = filename;
			if (createNewFile)
			{
				new File(filename).createNewFile();
			}
			init();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public File getFile()
	{
		return new File(this.filename);
	}

	public PropertyHandler(Class<?> clazz, String filename)
	{
		this.filename = filename;
		this.clazz = clazz;
		init();
	}

	public void setValue(String key, String value)
	{
		if (value == null)
			value = "";
		this.props.put(key, value);
	}

	public String getStringValue(String key)
	{
		return this.props.getProperty(key);
	}

	public boolean getBooleanValue(String key)
	{
		return Boolean.parseBoolean(this.props.getProperty(key));
	}

	public Properties getPropertiesClone()
	{
		Properties properties = new Properties();
		Iterator it = this.props.keySet().iterator();
		while (it.hasNext())
		{
			String key = String.valueOf(it.next());
			String value = this.props.getProperty(key);
			properties.put(key, value);
		}
		return properties;
	}

	public int getIntValue(String key)
	{
		try
		{
			return new Integer(this.props.getProperty(key)).intValue();
		} catch (NumberFormatException e)
		{
			Logger.getLogger(getClass()).error(
					"NumberFormatException retrieving property int value for key '" + key
							+ "'");
		}
		return -1;
	}

	public void remove(Object key)
	{
		this.props.remove(key);
	}

	public Iterator<Object> iterator()
	{
		return this.props.keySet().iterator();
	}

	public boolean containsKey(String key)
	{
		return this.props.containsKey(key);
	}

	public boolean containsValue(String value)
	{
		return this.props.containsValue(value);
	}

	public void serialize() throws IOException
	{
		OutputStream outStream = new FileOutputStream(this.filename);
		this.props.store(outStream, null);
		outStream.close();
	}

	private void init()
	{
		InputStream in = null;
		try
		{
			if (new File(this.filename).exists())
			{
				in = new FileInputStream(this.filename);
				this.props = new Properties();
				this.props.load(in);
				in.close();
			} else
			{
				this.filename = new File(this.filename).getName();
				throw new Exception(
						"File not found. File will be read as ressource stream.");
			}
		} catch (Exception e1)
		{
			Logger.getLogger(getClass()).error(
					"Failed to load properties file '" + this.filename + " ("
							+ new File(this.filename).getAbsolutePath() + ")': "
							+ e1.getMessage(), e1);

		}
	}

	public Properties getProperties()
	{
		return this.props;
	}

	public String getFilename()
	{
		return this.filename;
	}

	public void setProperties(Properties props)
	{
		this.props = props;
	}

	public void reloadProperties()
	{
		init();
	}
}