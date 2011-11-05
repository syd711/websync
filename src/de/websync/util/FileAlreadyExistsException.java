package de.websync.util;

public class FileAlreadyExistsException extends Exception
{
	private static final long serialVersionUID = -1064039465795592995L;

	public FileAlreadyExistsException(String msg)
	{
		super(msg);
	}
}
