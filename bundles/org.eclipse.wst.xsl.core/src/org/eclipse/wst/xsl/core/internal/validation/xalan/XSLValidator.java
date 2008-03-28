/*******************************************************************************
 * Copyright (c) 2007 Chase Technology Ltd - http://www.chasetechnology.co.uk
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Satchwell (Chase Technology Ltd) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsl.core.internal.validation.xalan;


import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.uriresolver.internal.URI;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;
import org.eclipse.wst.xsl.core.internal.XSLCorePlugin;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.eclipse.wst.xsl.core.internal.compiler.xslt10.processor.TransformerFactoryImpl;
import org.apache.xml.utils.DefaultErrorHandler;

/**
 * TODO: Add Javadoc
 * 
 * @author Doug Satchwell
 * 
 */
public class XSLValidator {
	private static XSLValidator instance;

	private static final byte[] XPATH_LOCK = new byte[0];

	private XSLValidator() {

	}

	/**
	 * TODO: Add Javadoc
	 * 
	 * @param uri
	 * @param xslFile
	 * @return
	 * @throws CoreException
	 */
	public ValidationReport validate(String uri, IFile xslFile) throws CoreException {
		// The string that is passed in for the URI needs to be encoded to be a valid
		// URI otherwise on those systems where spaces are significant it can't find
		// files that have spaces in the file name.
		ErrorListener errorListener = new XSLValidationReport();

		synchronized (XPATH_LOCK) {
			TransformerFactoryImpl transformer = new TransformerFactoryImpl();
			transformer.setErrorListener(errorListener);

		   try {
	 	     if (transformer.getFeature(SAXSource.FEATURE))
			    {
			      // If so, we can safely cast.
			      SAXTransformerFactory saxfactory = ((SAXTransformerFactory) transformer);
			      Templates compiled = saxfactory.newTemplates(new SAXSource(new InputSource(uri)));
			    }
		   } catch (Exception ex) {
			   XSLCorePlugin.log(ex);
		   }
			
		}
		return (ValidationReport)errorListener;
	}

	
	/**
	 * TODO: Add Javadoc
	 * 
	 * @return
	 */
	public static XSLValidator getInstance() {
		if (instance == null)
			instance = new XSLValidator();
		return instance;
	}
}