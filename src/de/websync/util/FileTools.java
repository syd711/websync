package de.websync.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;


/**
 * Tools for file handling
 * 
 * @author <a
 *         href="mailto:matthias.faust@frametexx.com">matthias.faust@frametexx.com</a>
 * @author <a href="http://www.frametexx.com"
 *         target="_blank">http://www.frametexx.com</a>
 * @since 19.01.2007
 */
public class FileTools
{
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * Reads the file as bytes.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(File file) throws IOException
	{
		FileInputStream fis = null;

		try
		{
			fis = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];
			fis.read(buf);
			fis.close();
			return buf;
		}
		finally
		{
			if (fis != null)
				fis.close();
		}
	}

	public static int countLines(File file) throws IOException
	{
		if (file == null)
		{
			Logger.getLogger(FileTools.class).error("Passed null value to file line count util.");
			return 0;
		}

		if (file.length() == 0)
			return 0;

		FileInputStream fis = new FileInputStream(file);

		int lineCount = 0;
		byte[] buffer = new byte[(int) file.length()];
		for (int charsRead = fis.read(buffer); charsRead >= 0; charsRead = fis.read(buffer))
		{
			for (int charIndex = 0; charIndex < charsRead; charIndex++)
			{
				if (buffer[charIndex] == '\n')
					lineCount++;
			}
		}
		fis.close();
		return lineCount;
	}


	/**
	 * Reads recursive the file entries of a CVS folder, but skips the CVS
	 * folders.
	 * 
	 * @param file
	 * @param recursive
	 * @return
	 */
	public static File[] readVCFolder(File file, boolean recursive)
	{
		if (recursive)
		{
			List<File> result = new ArrayList<File>();
			readVCFolderRecursive(result, file);
			return result.toArray(new File[result.size()]);
		}
		// flat folder reading only
		return file.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return (!f.isDirectory());
			}
		});
	}

	/**
	 * Helper method for recursive CVS folder reading.
	 * 
	 * @param result
	 * @param file
	 * @param recursive
	 */
	private static void readVCFolderRecursive(List<File> result, File file)
	{
		File[] files = file.listFiles();
		int len = files.length;
		for (int i = 0; i < len; i++)
		{
			if (files[i].isDirectory())
			{
				if (!files[i].getName().toUpperCase().equals("CVS"))
					readVCFolderRecursive(result, files[i]);
			}
			else
			{
				result.add(files[i]);
			}
		}
	}

	/**
	 * Reads folder content ignoring the passed pattern.
	 * 
	 * @param file
	 * @param excludeName
	 * @return
	 */
	public static File[] readVCFolder(File file)
	{
		if (file.isDirectory())
		{
			return file.listFiles(new FileFilter()
			{
				public boolean accept(File f)
				{
					return !(f.getName().toUpperCase().equals("CVS") && f.isDirectory());
				}
			});
		}

		return new File[] { file };
	}

	/**
	 * Sorts the Obvergiven List by the following way: If you overgive the
	 * following folders: /test/1/4 /test/1 /test /test/2 it will be sort in
	 * this way: /test/ /test/1 /test/2 /test/1/4
	 * 
	 * @param files List <File> containing file objects
	 */
	public static void sortFileList(List<File> files)
	{
		Comparator<File> fileComparator = new Comparator<File>()
		{
			public int compare(File file1, File file2)
			{
				return (file2.getAbsolutePath().indexOf(file1.getAbsolutePath()) == 0) ? -1 : 1;
			}

		};

		Collections.sort(files, fileComparator);
	}

	public static void replaceAll(File file, Map<String, String> keyValues) throws Exception
	{
		String fileContent = readBufferedFileContent(file.getAbsolutePath());
		Iterator<Entry<String, String>> it = keyValues.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, String> entry = it.next();
			String value = entry.getValue();
			if (value != null)
				fileContent = fileContent.replaceAll(entry.getKey(), value);
		}

		boolean deleted = file.delete();
		if (!deleted)
			throw new Exception("Replacement for file " + file.getAbsolutePath()
				+ " cancelled, because file could not be deleted.");

		RandomAccessFile out = null;
		try
		{
			out = new RandomAccessFile(file, "rw");
			out.writeBytes(fileContent);
		}
		finally
		{
			if (out != null)
				out.close();
		}
	}

	public static void zipFile(List<File> files, String filename, List<String> ignoreFiles) throws IOException
	{
		if (new File(filename).exists())
			throw new IOException("file " + filename + " already exists.");

		ZipOutputStream out = null;
		FileInputStream in = null;

		try
		{
			byte[] buf = new byte[1024];
			File outfile = new File(filename);
			out = new ZipOutputStream(new FileOutputStream(outfile));

			// Compress the files
			for (int i = 0; i < files.size(); i++)
			{
				File sourceFile = files.get(i);
				in = new FileInputStream(sourceFile);
				out.putNextEntry(new ZipEntry(sourceFile.getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}
			out.close();
		}
		finally
		{
			if (in != null)
				in.close();

			if (out != null)
				out.close();
		}
	}
	public static void zipFile(String folder, String filename, List<String> ignoreFiles) throws IOException
	{
		if (new File(filename).exists())
			throw new IOException("file " + filename + " already exists.");

		ZipOutputStream out = null;
		FileInputStream in = null;
		folder = new File(folder).getAbsolutePath();
		try
		{
			byte[] buf = new byte[1024];
			File outfile = new File(filename);
			out = new ZipOutputStream(new FileOutputStream(outfile));

			// Compress the files
			List<File> files = new ArrayList<File>();
			files = FileTools.readFolder(files, folder, ignoreFiles);
			folder = new File(folder).getParentFile().getAbsolutePath();
			for (int i = 0; i < files.size(); i++)
			{
				File sourceFile = files.get(i);
				in = new FileInputStream(sourceFile);

				// Add ZIP entry to output stream.
				String path = sourceFile.getParentFile().getAbsolutePath();
				path = path.substring(folder.length(), path.length());

				if (path.length() != 0 && !path.endsWith("/"))
					path = path + "/";
				if (path.startsWith("\\"))
					path = path.substring(1, path.length());
								
				String entry = path + sourceFile.getName();
				out.putNextEntry(new ZipEntry(entry));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}
			out.close();
		}
		finally
		{
			if (in != null)
				in.close();

			if (out != null)
				out.close();
		}
	}

	/**
	 * Utility method to convert a dos file into a unix file.
	 * 
	 * @param srcFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void dos2Unix2(String srcFile, String destFile) throws Exception
	{
		FileWriter fileWriter = null;
		BufferedWriter fwr = null;
		FileReader frdr = null;
		BufferedReader buff = null;
		try
		{
			fileWriter = new FileWriter(destFile);
			fwr = new BufferedWriter(fileWriter);
			frdr = new FileReader(srcFile);
			buff = new BufferedReader(frdr);
			int bt = -1;

			while ((bt = buff.read()) > -1)
			{
				// Ignore CR
				if (bt != 13)
					fwr.write(bt);
			}
		}
		finally
		{
			if (buff != null)
				buff.close();

			if (fwr != null)
			{
				fwr.flush();
				fwr.close();
			}

			if (frdr != null)
				frdr.close();

			if (fileWriter != null)
				fileWriter.close();
		}
	}

	/**
	 * Creates a new folder descritor file.
	 * 
	 * @param dir
	 * @param name
	 */
	public static void createFolderDescriptor(String dir, String name)
	{
		try
		{
			createFile(new File(dir + "folder.descriptor"), "name = " + name, null);
		}
		catch (Exception e)
		{
			Logger.getLogger(FileTools.class).error("Cannot write folder descriptor file: " + e.getMessage());
		}
	}

	/**
	 * @param file
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static File createFile(File file, String content) throws Exception
	{
		return createFile(file, content, null);
	}

	public static File createFile(String filename, String content) throws Exception
	{
		return createFile(new File(filename), content, null);
	}

	/**
	 * Creates a new file with the given content.
	 * 
	 * @param filename The absolute path and name of the file.
	 * @param content The content that should be initial written into the file.
	 */
	public static File createFile(File newFile, String content, String encoding) throws Exception
	{
		OutputStream filewriter = null;
		BufferedOutputStream bOutputStream = null;
		String bldFilename = newFile.getAbsolutePath() + ".bld";
		try
		{
			if (newFile.exists())
				newFile.delete();

			File bldFile = new File(bldFilename);
			if (bldFile.exists())
				bldFile.delete();

			new File(newFile.getParent()).mkdirs();

			filewriter = new FileOutputStream(bldFile);
			bOutputStream = new BufferedOutputStream(filewriter);

			if ( StringTools.isEmptyString( encoding ))
				bOutputStream.write(content.getBytes());
			else
				bOutputStream.write(content.getBytes(encoding));

			bOutputStream.close();
			filewriter.close();

			bldFile.renameTo(newFile);
			bldFile.delete();

			return newFile;
		}
		finally
		{
			if (filewriter != null)
				filewriter.close();
			if (bOutputStream != null)
				bOutputStream.close();
		}
	}


	public static List<String> readZipFileEntries(File zipFile) throws Exception
	{
		if (!zipFile.exists())
			throw new FileNotFoundException(zipFile.getAbsolutePath() + " does not exist.");

		List<String> names = new ArrayList<String>();
		ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
		FileOutputStream out = null;
		try
		{
			while (true)
			{
				// Nächsten Eintrag lesen
				ZipEntry entry = in.getNextEntry();

				if (entry == null)
				{
					break;
				}
				else if (entry.isDirectory())
				{
					continue;
				}
				else if (!entry.isDirectory())
				{
					names.add(entry.getName());
				}
			}

			return names;
		}
		finally
		{
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	/**
	 * Reads all subfolders for a folder.
	 */
	public static List<File> readFolders(List<File> folderList, File path, List<String> ignoreNames)
	{
		if (!path.exists())
			return folderList;
		if (!path.isDirectory())
			return folderList;
		if (ignoreNames == null)
			;
		ignoreNames = new ArrayList<String>();

		File[] files = path.listFiles();

		for (int i = 0; i < files.length; i++)
		{
			File file = files[i];
			if (file.isDirectory())
			{
				boolean skip = false;
				for (String string : ignoreNames)
				{
					if (file.getName().equalsIgnoreCase(string) || file.getName().matches(string))
					{
						skip = true;
						break;
					}
				}

				if (!skip)
				{
					folderList.add(file);
				}

				readFolders(folderList, file, ignoreNames);
			}
		}
		return folderList;
	}

	/**
	 * Deletes a folder recursivly.
	 * 
	 * @param path Directory that should be deleted.
	 * @return false if the a file under this directory cannot be deleted.
	 */
	public static List<File> readFolder(List<File> fileList, String path, List<String> ignoreNames)
	{
		if (!path.endsWith("/"))
			path = path + "/";
		if (!new File(path).exists())
			return fileList;

		String[] files = new File(path).list();

		for (int i = 0; i < files.length; i++)
		{
			if (new File(path + files[i]).isDirectory())
			{
				readFolder(fileList, path + files[i], ignoreNames);
			}
			else
			{
				boolean skip = false;
				if (ignoreNames != null)
				{
					for (String string : ignoreNames)
					{
						if (files[i].equalsIgnoreCase(string) || files[i].matches(string))
						{
							skip = true;
							break;
						}
					}
				}

				if (!skip)
					fileList.add(new File(path + "/" + files[i]));
			}
		}
		return fileList;
	}

	/**
	 * Deletes a folder recursivly.
	 * 
	 * @param path Directory that should be deleted.
	 * @return false if the a file under this directory cannot be deleted.
	 */
	public static boolean deleteFolder(String path)
	{
		if (!path.endsWith("/"))
			path = path + "/";
		if (!new File(path).exists())
			return false;

		String[] files = new File(path).list();

		for (int i = 0; i < files.length; i++)
		{
			File deletionFile = new File(path + files[i]);
			if (deletionFile.isDirectory())
			{
				boolean success = deleteFolder(path + files[i]);
				if (!success)
					return false;
			}
			else
			{
				new File(path + "/" + files[i]).delete();
			}
		}
		return new File(path).delete();
	}

	public static boolean deleteCVSFiles(String path, boolean recursive)
	{
		File filePath = new File(path);
		List<File> cvsFolders = new ArrayList<File>();

		if (recursive)
		{
			readCVSFolderRecursive(cvsFolders, filePath);
		}

		if (cvsFolders == null)
			return false;

		for (File cvsFolder : cvsFolders)
		{
			for (File deleteFile : cvsFolder.listFiles())
			{
				deleteFile.delete();
			}
			boolean success = cvsFolder.delete();
			if (!success)
				return false;
		}

		return true;
	}

	/**
	 * Helper method for recursive CVS folder reading.
	 * 
	 * @param result
	 * @param file
	 * @param recursive
	 */
	private static void readCVSFolderRecursive(List<File> result, File file)
	{
		File[] files = file.listFiles();
		int len = files.length;
		for (int i = 0; i < len; i++)
		{
			if (files[i].isDirectory())
			{
				if (files[i].getName().toUpperCase().equals("CVS"))
					result.add(files[i]);
				else
					readCVSFolderRecursive(result, files[i]);
			}
		}
	}

	/**
	 * Deletes all files from a CVS (sub-)folder.
	 * 
	 * @param path
	 * @param recursive
	 * @return
	 */
	public static boolean deleteAllButCVSFolder(String path, boolean recursive)
	{
		File[] files = readVCFolder(new File(path), recursive);

		if (files == null)
			return false;

		for (File file : files)
		{
			boolean success = file.delete();
			if (!success)
				return false;
		}
		return true;
	}

	public static int countFileLineNumber(File file)
	{
		int counter = 0;
		FileReader reader = null;
		BufferedReader bufferedReader = null;
		try
		{
			reader = new FileReader(file);
			bufferedReader = new BufferedReader(reader);
			while (bufferedReader.readLine() != null)
			{
				counter++;
			}

			bufferedReader.close();

			return counter;

		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
		finally
		{
			try
			{
				if (bufferedReader != null)
					bufferedReader.close();
				if (reader != null)
					reader.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return -1;

	}

	/**
	 * Recursive algorithm to find files matching the given filename.
	 * 
	 * @param folder
	 * @param filename
	 * @param resultList
	 */
	public static void findFile(String folder, String filename, List<String> resultList)
	{
		if (!folder.endsWith("/"))
			folder = folder + "/";

		String[] files = new File(folder).list();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				if (new File(folder + files[i]).isDirectory())
				{
					findFile(folder + files[i], filename, resultList);
				}
				else
				{
					if (files[i].equalsIgnoreCase(filename))
					{
						resultList.add(folder + files[i]);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param sourceFile DOCUMENT ME!
	 * @param destinationFile DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws FileNotFoundException DOCUMENT ME!
	 * @throws FileAlreadyExistsException DOCUMENT ME!
	 */
	public static boolean moveFile(File sourceFile, File destinationFile) throws FileNotFoundException,
		FileAlreadyExistsException, Exception
	{

		boolean result = false;

		// ****************************************
		// check whether Source File exists
		// ****************************************
		if (sourceFile == null)
			throw new FileNotFoundException();

		if (!sourceFile.exists())
			throw new FileNotFoundException("File " + sourceFile.getName() + " not found in " + getJustPath(sourceFile));
		// throw new FileNotFoundException("File " + getJustFileName(sourceFile)
		// + "." + getJustExtension(sourceFile) + " not found in " +
		// getJustPath(sourceFile));

		// ****************************************
		// check whether Destination File already exists
		// ****************************************
		if (destinationFile == null)
			throw new FileNotFoundException();

		if (destinationFile.exists())
			throw new FileAlreadyExistsException("File " + sourceFile.getName() + " already exists in "
				+ getJustPath(destinationFile));
		// throw new FileAlreadyExistsException("File " +
		// getJustFileName(destinationFile) + "." +
		// getJustExtension(destinationFile) + " already exists in " +
		// getJustPath(destinationFile));

		// ****************************************
		// Try to copy Source File to Destination File
		// ****************************************

		result = FileTools.copyFile(sourceFile, destinationFile, false);

		// ****************************************
		// Try to delete Source File
		// ****************************************
		if (FileTools.deleteFile(sourceFile))
			result = true;
		else
			result = false;

		return result;
	}


	public static void copyFilePlain(File in, File out) throws Exception
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try
		{
			fis = new FileInputStream(in);
			fos = new FileOutputStream(out);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1)
			{
				fos.write(buf, 0, i);
			}
			fis.close();
			fos.close();
		}
		finally
		{
			if (fis != null)
				fis.close();

			if (fos != null)
				fos.close();
		}
	}

	public static boolean appendToFile( File sourceFile, File destinationFile, String encoding, String separator ) throws IOException
	{
		boolean result;
		InputStream filereader = null;
		BufferedInputStream bInputStream = null;

		OutputStream filewriter = null;
		BufferedOutputStream bOutputStream = null;
		try
		{
			result = false;

			// ****************************************
			// check whether Source File exists
			// ****************************************
			if (sourceFile == null)
				throw new FileNotFoundException();

			if (!sourceFile.exists())
				throw new FileNotFoundException("Source file " + sourceFile.getAbsolutePath() + " not found");
			if (!new File(destinationFile.getParent()).exists())
				throw new FileNotFoundException("Target folder " + destinationFile.getParent() + " not found");

			// ****************************************
			// check whether Destination File already exists
			// ****************************************
			if (destinationFile == null)
				throw new FileNotFoundException("No destination file selected for file copy.");

			
			// ****************************************
			// copy Source File to Build File
			// ****************************************
			filereader = new FileInputStream(sourceFile);
			bInputStream = new BufferedInputStream(filereader);

			filewriter = new FileOutputStream(destinationFile,true);
			bOutputStream = new BufferedOutputStream(filewriter);
			
			byte[] buffer = new byte[256];

			while (true)
			{
				int bytesRead = bInputStream.read(buffer);
				if (bytesRead == -1)
					break;

				bOutputStream.write(buffer);
			}
			
			if( separator == null )
				separator = LINE_SEPARATOR;
			bOutputStream.write( separator.getBytes() );

			bInputStream.close();
			bOutputStream.close();
			filewriter.flush();
			filereader.close();
			filewriter.close();
			
			result = true;
		}
		finally
		{
			if (bInputStream != null)
				bInputStream.close();
			if (bOutputStream != null)
				bOutputStream.close();
			if (filereader != null)
				filereader.close();
			if (filewriter != null)
				filewriter.close();
		}

		return result;
	}
	

	/**
	 * copy a file
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 * @throws FileNotFoundException
	 * @throws FileAlreadyExistsException
	 * @throws FileAlreadyExistsException
	 * @throws IOException
	 */
	public static boolean copyFile(File sourceFile, File destinationFile, boolean overwrite)
		throws FileAlreadyExistsException, IOException
	{
		Logger.getLogger(FileTools.class).info("Copying " + sourceFile.getAbsolutePath() + " to " + destinationFile.getAbsolutePath()
				+ " with overwrite = " + overwrite);
		boolean result;
		File buildFile = null;
		InputStream filereader = null;
		BufferedInputStream bInputStream = null;

		OutputStream filewriter = null;
		BufferedOutputStream bOutputStream = null;
		try
		{
			result = false;

			// ****************************************
			// check whether Source File exists
			// ****************************************
			if (sourceFile == null)
				throw new FileNotFoundException();

			if (!sourceFile.exists())
				throw new FileNotFoundException("Source file " + sourceFile.getAbsolutePath() + " not found");
			if (!new File(destinationFile.getParent()).exists())
				throw new FileNotFoundException("Target folder " + destinationFile.getParent() + " not found");

			// ****************************************
			// check whether Destination File already exists
			// ****************************************
			if (destinationFile == null)
				throw new FileNotFoundException("No destination file selected for file copy.");

			if (!overwrite && destinationFile.exists())
				throw new FileAlreadyExistsException("File " + sourceFile.getName() + " already exists in "
					+ getJustPath(destinationFile));

			// ****************************************
			// create Name of Build File
			// ****************************************
			String buildFileName = "_" + FileTools.getFilenameWithoutExtension(destinationFile) + ".BLD";
			buildFile = new File(FileTools.getJustPath(destinationFile), buildFileName);

			// ****************************************
			// copy Source File to Build File
			// ****************************************

			filereader = new FileInputStream(sourceFile);
			bInputStream = new BufferedInputStream(filereader);

			filewriter = new FileOutputStream(buildFile);
			bOutputStream = new BufferedOutputStream(filewriter);
			byte[] buffer = new byte[256];

			while (true)
			{
				int bytesRead = bInputStream.read(buffer);
				if (bytesRead == -1)
					break;

				bOutputStream.write(buffer, 0, bytesRead);
			}

			bInputStream.close();
			bOutputStream.close();
			filewriter.flush();
			filereader.close();
			filewriter.close();

			// ****************************************
			// rename Build File to Destination File
			// ****************************************
			if (buildFile.length() != sourceFile.length())
			{

				// Sometimes, while running as an NT Service we encountered
				// problems renaming the file.
				// The Build File seemed not to flushed. Now if this is the
				// case, we give it up to
				// one minute before we try to rename.
				String actualTime = DateTools.getSystemDateTime("TIME");
				String timeoutString = "";
				int hh = Integer.parseInt(actualTime.substring(0, 2));
				int mm = Integer.parseInt(actualTime.substring(3, 5)) + 1;
				// int ss = Integer.parseInt(actualTime.substring(6, 8));
				int std = (int) Math.floor(mm / 60);

				if (std > 0)
				{
					hh = hh + std;
					mm = mm - (std * 60);
				}

				if (hh >= 24)
					hh = hh - 24;

				if (hh < 10)
					timeoutString = "0";

				timeoutString = timeoutString + Integer.toString(hh) + ":";

				if (mm < 10)
					timeoutString = timeoutString + "0";

				timeoutString = timeoutString + Integer.toString(mm);

				// Wait for FileReader to finish
				while (buildFile.length() != sourceFile.length())
				{

					if (!DateTools.isBetweenTimeFrame(actualTime, timeoutString))

						break;
				}
			}

			if (overwrite && destinationFile.exists())
				destinationFile.delete();

			if (FileTools.renameFile(buildFile, FileTools.getFilenameWithoutExtension(destinationFile), FileTools
				.getFileExtension(destinationFile)))
				result = true;
			else
				result = false;
		}
		finally
		{
			if (bInputStream != null)
				bInputStream.close();
			if (bOutputStream != null)
				bOutputStream.close();
			if (filereader != null)
				filereader.close();
			if (filewriter != null)
				filewriter.close();

			if (buildFile != null && buildFile.exists())
				buildFile.delete();
		}

		return result;
	}

	/**
	 * Returns the string representation of a file.
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws IOException
	 */
	@Deprecated
	// Use bufferd version instead. RAF will use 100% CPU
	public static String readFileContent(String filename) throws IOException
	{
		try
		{
			RandomAccessFile raf = new RandomAccessFile(filename, "r");
			StringBuffer buffer = new StringBuffer();
			String line = null;

			int lineCounter = 0;

			while ((line = raf.readLine()) != null)
			{
				buffer.append(line + "\n");
				lineCounter++;

			}

			raf.close();
			return buffer.toString();
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
	}

	/**
	 * Returns the string representation of a file.
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws IOException
	 */
	public static String readBufferedFileContent(String filename) throws IOException
	{
		BufferedInputStream bin = null;
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(new File(filename));
			bin = new BufferedInputStream(fin);
			StringBuffer stringBuffer = new StringBuffer();

			byte[] buf = new byte[1024];
			int len;
			while ((len = bin.read(buf)) > 0)
			{
				stringBuffer.append(new String(buf, 0, len));
			}

			return stringBuffer.toString();
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (bin != null)
					bin.close();

				if (fin != null)
					fin.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads a file with the given encoding.
	 * 
	 * @param filename
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFileContentWithouLinebreaks(String filename, String encoding) throws IOException
	{
		FileInputStream reader = null;
		try
		{
			reader = new FileInputStream(new File(filename));
			InputStreamReader iReader = null;
			if (encoding == null)
				iReader = new InputStreamReader(reader);
			else
				iReader = new InputStreamReader(reader, encoding);

			BufferedReader bufferedReader = new BufferedReader(iReader);

			StringBuffer buffer = new StringBuffer();
			String line = null;
			while ((line = bufferedReader.readLine()) != null)
				buffer.append(line);

			return buffer.toString();
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Reads a file with the given encoding.
	 * 
	 * @param filename
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFileContent(String filename, String encoding) throws IOException
	{
		FileInputStream reader = null;
		try
		{
			reader = new FileInputStream(new File(filename));
			InputStreamReader iReader = null;
			if (encoding == null)
				iReader = new InputStreamReader(reader);
			else
				iReader = new InputStreamReader(reader, encoding);
			
			char[] buf=new char[1024];
			StringBuilder buffer = new StringBuilder();
			int r=0;
			while ((r = iReader.read(buf)) != -1) {
	            buffer.append(buf,0,r);
	        }

			return buffer.toString();
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	

	@Deprecated
	public static String readFileContentWithouLinebreaks(String filename) throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(filename, "r");
			StringBuffer buffer = new StringBuffer();
			String line = null;

			while ((line = raf.readLine()) != null)
				buffer.append(line);

			raf.close();
			return buffer.toString();
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (raf != null)
					raf.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Delete a file
	 * 
	 * @param fileToDelete
	 * @return
	 * @throws FileNotFoundException
	 */
	public static boolean deleteFile(String fileToDelete) throws FileNotFoundException
	{
		return deleteFile(new File(fileToDelete));
	}

	/**
	 * delete a file
	 * 
	 * @param fileToDelete DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws FileNotFoundException DOCUMENT ME!
	 */
	public static boolean deleteFile(File fileToDelete) throws FileNotFoundException
	{

		boolean result = false;

		if (fileToDelete == null)
			throw new FileNotFoundException();

		if (!fileToDelete.exists())
			throw new FileNotFoundException("File " + fileToDelete.getName() + " not found in "
				+ getJustPath(fileToDelete));

		try
		{
			result = fileToDelete.delete();
		}
		catch (SecurityException se)
		{
			se.printStackTrace();
		}

		return result;
	}

	/**
	 * rename a file
	 * 
	 * @param fileToRename DOCUMENT ME!
	 * @param newFileName DOCUMENT ME!
	 * @param newExtension DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws FileNotFoundException DOCUMENT ME!
	 */
	public static boolean renameFile(File fileToRename, String newFileName, String newExtension)
		throws FileNotFoundException
	{
		File newFile = null;
		if ("".equals(newExtension))
		{
			newFile = new File(FileTools.getJustPath(fileToRename), newFileName);
		}
		else
		{
			newFile = new File(fileToRename.getParent() + "/" + newFileName + newExtension);
		}

		boolean result = false;

		if (fileToRename == null)
			throw new FileNotFoundException();

		if (!fileToRename.exists())
			throw new FileNotFoundException("File " + fileToRename.getName() + " not found in "
				+ getJustPath(fileToRename));

		try
		{
			result = fileToRename.renameTo(newFile);
		}
		catch (SecurityException se)
		{
			se.printStackTrace();
		}

		return result;
	}

	/**
	 * determines file name without path or extension. File name is everything
	 * but - the rightmost part beginning with a "." - the leftmost part ending
	 * with file separation character
	 * 
	 * @param aFileName DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String getJustFileName(String aFileName)
	{
		File tmpFile = new File(aFileName);
		return getFilenameWithoutExtension(tmpFile);
	}

	/**
	 * Determines file name without path or extension. File name is everything
	 * but - the rightmost part beginning with a "." - the leftmost part ending
	 * with file separation character
	 */
	public static String getFilenameWithoutExtension(File aFile)
	{
		int index = aFile.getName().lastIndexOf(".");
		if (index != -1)
			return aFile.getName().substring(0, index);

		return aFile.getName();
	}

	/**
	 * determines file names extension. Extension is the rightmost part
	 * beginning with a "."
	 */
	public static String getFileExtension(String aFileName)
	{
		File tmpFile = new File(aFileName);
		return getFileExtension(tmpFile);
	}

	/**
	 * determines file names extension. Extension is the rightmost part
	 * beginning with a "." *
	 */
	public static String getFileExtension(File aFile)
	{
		if (aFile.getName().lastIndexOf(".") != -1)
			return aFile.getName().substring(aFile.getName().lastIndexOf("."), aFile.getName().length());

		return "";
	}

	/**
	 * determines file name without path.
	 * 
	 * @param aFile DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String getFileName(String file)
	{
		String justFileName = getJustFileName(file);
		String justExtension = getFileExtension(file);
		if ("".equals(justExtension))
		{
			return justFileName;
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append(justFileName);
		buffer.append(".");
		buffer.append(justExtension);

		return buffer.toString();
	}

	/**
	 * determines file names path. Path is the leftmost part ending with file
	 * separation character
	 * 
	 * @param aAbsolutePath DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String getJustPath(String aAbsolutePath)
	{
		File tmpFile = new File(aAbsolutePath);

		return getJustPath(tmpFile);
	}

	/**
	 * determines file names path. Path is the leftmost part ending with file
	 * separation character
	 * 
	 * @param aFile DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String getJustPath(File aFile)
	{
		String path = aFile.getAbsolutePath();
		String result = "";
		int amountOfChar = path.length();
		int x = 0;

		if (aFile.isDirectory())
			result = path;
		else
		{

			// search a separator
			for (x = amountOfChar - 1; x > 0; x--)
			{

				if (path.substring(x, x + 1).equals(System.getProperty("file.separator")))

					break;
			}

			if (x == 0)
			// maybe found a separator
			{

				if (path.substring(x, x + 1).equals(System.getProperty("file.separator")))
					result = System.getProperty("file.separator");
				else
					result = "";
			}
			else
				result = path.substring(0, x);
		}

		return result;
	}

	/**
	 * checks if a directory is empty using a file object
	 * 
	 * @param aDirectory DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static boolean isDirectoryEmpty(File aDirectory)
	{
		String[] filesInDirectory;

		if (!aDirectory.isDirectory())
			aDirectory = new File(FileTools.getJustPath(aDirectory));

		filesInDirectory = aDirectory.list();

		if (filesInDirectory.length > 0)
		{

			for (int i = 0; i < filesInDirectory.length; i++)
			{

				File f = new File(aDirectory.getAbsolutePath(), filesInDirectory[i]);

				if (f.isFile())

					return false;
			}
		}

		return true;
	}

	/**
	 * checks if a directory is empty using a directory name
	 * 
	 * @param aDirectoryName DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static boolean isDirectoryEmpty(String aDirectoryName)
	{
		File aDirectory = new File(aDirectoryName);
		return isDirectoryEmpty(aDirectory);
	}

	/**
	 * Sorts files in chrononical order.
	 * 
	 * @param directory
	 * @param filesInDirectory
	 * @return
	 */
	public static List<File> sortFiles(String directory, File[] filesInDirectory)
	{
		List<File> sortedFiles = new ArrayList<File>();
		for (int i = 0; i < filesInDirectory.length; i++)
		{
			sortedFiles.add(filesInDirectory[i]);
		}
		Collections.sort(sortedFiles, new FileObjectComparator());
		return sortedFiles;
	}

	/**
	 * Sorts files in chrononical order.
	 * 
	 * @param directory
	 * @param filesInDirectory
	 * @return
	 */
	public static List<String> sortFiles(String directory, String[] filesInDirectory)
	{
		List<String> sortedFiles = new ArrayList<String>();
		if (!directory.endsWith("/"))
			directory = directory + "/";

		for (int i = 0; i < filesInDirectory.length; i++)
		{
			sortedFiles.add(directory + filesInDirectory[i]);
		}
		Collections.sort(sortedFiles);
		return sortedFiles;
	}

	/**
	 * Checks if the incoming path is a DOS/windows path e.g. C:\home\maeder and
	 * returns a unix compatible path /home/maeder
	 * 
	 * @param path the path you would like to check. Accepts already UNIX paths
	 *        and null values
	 * @return a Unix compatible path
	 */
	public static String getUnixPath(String path)
	{
		if (path != null)
		{
			// checks if the second paramter is a doublepoint
			if (path.length() >= 2 && path.substring(1, 2).equals(":"))
				path = path.substring(2, path.length());

			return path.replace('\\', '/');
		}

		return null;
	}

	/**
	 * Returns the folder that matches the given name.
	 * 
	 * @param root
	 * @param name
	 * @return
	 */
	public static File searchFolder(File root, String name)
	{
		if (root == null || !root.exists() || !root.isDirectory())
			return null;

		if (root.getName().equals(name))
			return root;

		File[] files = root.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				File hit = searchFolder(file, name);
				if (hit != null)
					return hit;
			}
		}

		return null;
	}

	public static void fileAppend(File file, String line)
	{
		RandomAccessFile output = null;

		try
		{
			output = new RandomAccessFile(file, "rw");
			output.seek(output.length()); // Set Filecursor to the end
			output.writeBytes(line + "\n"); // Write the line
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (output != null)
				try
				{
					output.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
		}
	}

	/**
	 * Checks if file is invalid or corrupted
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static boolean isCorruptArchive(File file)
	{
		FileInputStream fis = null;
		JarInputStream jis = null;
		try
		{
			fis = new FileInputStream(file);
			jis = new JarInputStream(fis);
			@SuppressWarnings("unused")
			ZipEntry entry=null;
			while((entry=jis.getNextEntry())!=null)
			{
			}
			 // we can ignore the entry here.

		}
		catch (Exception e)
		{
			Logger.getLogger(FileTools.class).info(file.getAbsolutePath() + " is a corrupt archive file./n"+e.getMessage());
			return true;
		}
		finally
		{
			if (fis != null)
				try
				{
					fis.close();
				}
				catch (IOException e)
				{
				}

		}
		return false;
	}
}
