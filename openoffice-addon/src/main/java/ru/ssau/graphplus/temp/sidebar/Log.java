package ru.ssau.graphplus.temp.sidebar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Log
{
    private static final String msLogFileName = "c:\\tmp\\Sidebar_SearchDemo_Log.txt";

    public static Log Instance ()
    {
        if (maInstance == null)
        {
        	maInstance = new Log();
        }
        
        return maInstance;
    }
    
    
    
    
    private Log ()
    {
    }
    
    
    
    
    public void printf (final String sFormat, final Object ... aArgumentList)
    {
        println(String.format(sFormat, aArgumentList));
    }
    
    
    
    
    public void println (final String sMessage)
    {
        if (msLogFileName == null)
            return;

        final File aFile = new File(msLogFileName);
		try
		{
			final PrintStream aOut = new PrintStream(new FileOutputStream(aFile, true));
			if (aOut != null)
			{	
				aOut.println(sMessage);
				aOut.close();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
    }
    
    
    

    public void PrintStackTrace (final Exception aException)
    {
        if (msLogFileName == null)
            return;

        final File aFile = new File(msLogFileName);
		try
		{
			final PrintStream aOut = new PrintStream(new FileOutputStream(aFile, true));
			if (aOut != null)
			{	
				aException.printStackTrace(aOut);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
    }
    
    
    
    
    private static Log maInstance = null;
}
