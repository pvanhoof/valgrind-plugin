package com.facinghell.valgrind.parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.facinghell.valgrind.model.ValgrindErrorKind;
import com.facinghell.valgrind.model.ValgrindInvalidReadError;
import com.facinghell.valgrind.model.ValgrindInvalidWriteError;
import com.facinghell.valgrind.model.ValgrindLeakError;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktrace;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

public class ValgrindXmlParser implements Serializable
{
	private static final long serialVersionUID = -3804982443628621529L;
	private XPath errorPath = XPath.newInstance( "/valgrindoutput/error" );
	private XPath errorKindPath = XPath.newInstance( "kind" );
	private XPath errorStackPath = XPath.newInstance( "stack" );
	private XPath errorStackFramePath = XPath.newInstance( "frame" );
	private XPath errorText = XPath.newInstance( "what" );
	private XPath functionNamePath = XPath.newInstance( "fn" );
	private XPath directoryNamePath = XPath.newInstance( "dir" );
	private XPath fileNamePath = XPath.newInstance( "file" );
	private XPath objectNamePath = XPath.newInstance( "obj" );
	private XPath lineNumberPath = XPath.newInstance( "line" );
	
	public ValgrindXmlParser() throws JDOMException
	{
	}	

	public ValgrindReport parse( final File file ) throws IOException, JDOMException
	{
		ValgrindReport valgrindReport = new ValgrindReport();
		
		Document doc = new SAXBuilder().build( file );	
    
	    for( Object object : errorPath.selectNodes( doc ) )
	    {
	    	ValgrindErrorKind kind;
	    	try
	    	{
	    		kind = ValgrindErrorKind.valueOf( errorKindPath.valueOf(object) );	    	
	    	}
	    	catch( IllegalArgumentException e )
	    	{
	    		System.err.println(e);
	    		continue;	    		
	    	}	    	
	    	
	    	switch(kind)
	    	{
	    	case InvalidRead:
	    		valgrindReport.addInvalidReadError( parseInvalidReadError(object) );
	    		break;
	    	case InvalidWrite:
	    		valgrindReport.addInvalidWriteError( parseInvalidWriteError(object) );
	    		break;
	    	case Leak_DefinitelyLost:
	    		valgrindReport.addLeakError( parseLeakError(object) );
	    		break;	 	    		
	    	}	    	
	    }	

		return valgrindReport;
	}
	
	private ValgrindInvalidReadError parseInvalidReadError( Object object ) throws JDOMException
	{
		ValgrindInvalidReadError error = new ValgrindInvalidReadError();
		error.setKind( ValgrindErrorKind.valueOf(errorKindPath.valueOf(object)) );
		error.setDescription( (errorText.valueOf(object)) );	
		error.setStacktrace( parseStack( errorStackPath.selectSingleNode(object)));
		return error;		
	}

	private ValgrindInvalidWriteError parseInvalidWriteError( Object object ) throws JDOMException
	{
		ValgrindInvalidWriteError error = new ValgrindInvalidWriteError();
		error.setKind( ValgrindErrorKind.valueOf(errorKindPath.valueOf(object)) );
		error.setDescription( (errorText.valueOf(object)) );
		error.setStacktrace( parseStack( errorStackPath.selectSingleNode(object)));
		return error;		
	}
	
	private ValgrindLeakError parseLeakError( Object object ) throws JDOMException
	{
		ValgrindLeakError error = new ValgrindLeakError();
		error.setKind( ValgrindErrorKind.valueOf(errorKindPath.valueOf(object)) );
		error.setDescription( (errorText.valueOf(object)) );
		error.setStacktrace( parseStack( errorStackPath.selectSingleNode(object)));
		return error;	
	}
	
	private ValgrindStacktrace parseStack( Object object ) throws JDOMException
	{
		ValgrindStacktrace stacktrace = new ValgrindStacktrace();
		
		Element element = (Element)object;
		System.err.println( "stack name: " + element.getName() );
		
		for( Object frame : errorStackFramePath.selectNodes( object ) )
		{
			stacktrace.addFrame( parseStacktraceFrame(frame) );			
		}
		
		return stacktrace;
	}
	
	private ValgrindStacktraceFrame parseStacktraceFrame( Object object ) throws JDOMException
	{
		ValgrindStacktraceFrame frame = new ValgrindStacktraceFrame();
		
		frame.setDirectoryName( directoryNamePath.valueOf(object) );
		frame.setFileName( fileNamePath.valueOf(object) );
		frame.setObjectName( objectNamePath.valueOf(object) );
		frame.setFunctionName( functionNamePath.valueOf(object) );
		
		String lineNumberString = lineNumberPath.valueOf(object).trim();
		if ( !lineNumberString.isEmpty() )
			frame.setLineNumber( Integer.valueOf(lineNumberString) );
		
		return frame;		
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("hello world");
		try
		{
			ValgrindXmlParser parser = new ValgrindXmlParser();
			File file = new File("vg.xml");
			
			ValgrindReport valgrindReport = parser.parse( file );
			
			System.out.println( "invalid reads : " + valgrindReport.getInvalidReadErrorCount() );
			System.out.println( "invalid writes: " + valgrindReport.getInvalidWriteErrorCount() );
			System.out.println( "leaks         : " + valgrindReport.getLeakErrorCount() );
			System.out.println( "total: " + valgrindReport.getErrorCount() );
			
			valgrindReport.print();
		} 
		catch (JDOMException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	  
	}
}