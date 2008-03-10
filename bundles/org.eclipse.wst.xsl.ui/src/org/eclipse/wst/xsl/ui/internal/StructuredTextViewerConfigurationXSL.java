/*******************************************************************************
 * Copyright (c) 2008 Standards for Technology in Automotive Retail and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Carver - STAR - bug 213849 - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xsl.ui.internal;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.sse.core.text.IStructuredPartitions;
import org.eclipse.wst.xml.core.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;
import org.eclipse.wst.xml.ui.internal.contentassist.NoRegionContentAssistProcessor;
import org.eclipse.wst.xsl.ui.internal.contentassist.XSLContentAssistProcessor;

/**
 * StructuredTextViewerConfigurationXSL implements content assistance
 * for attributes and other XPath related functionality.
 * 
 * @author dcarver
 *
 */
@SuppressWarnings({ "restriction"})
public class StructuredTextViewerConfigurationXSL extends
		StructuredTextViewerConfigurationXML {

	/**
	 * Configuration for XSL Content Types
	 */
	public StructuredTextViewerConfigurationXSL() {
		super();
	}

	/**
	 *  Return the processors for the current content type.
	 */
	@Override
	protected IContentAssistProcessor[] getContentAssistProcessors(
			ISourceViewer sourceViewer, String partitionType) {
		IContentAssistProcessor[] processors = null;

		if ((partitionType == IStructuredPartitions.DEFAULT_PARTITION) || (partitionType == IXMLPartitions.XML_DEFAULT)) {
			processors = new IContentAssistProcessor[]{new XSLContentAssistProcessor()};
		}
		else if (partitionType == IStructuredPartitions.UNKNOWN_PARTITION) {
			processors = new IContentAssistProcessor[]{new NoRegionContentAssistProcessor()};
		}
		return processors;
	}
	
}