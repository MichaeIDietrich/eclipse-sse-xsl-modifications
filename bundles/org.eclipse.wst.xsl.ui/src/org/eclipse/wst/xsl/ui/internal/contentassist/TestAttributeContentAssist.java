/*******************************************************************************
 *Copyright (c) 2008 Standards for Technology in Automotive Retail and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *Contributors:
 *    David Carver (STAR) - bug 240170 - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsl.ui.internal.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.xpath.core.internal.parser.XPathParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class provides content assistance for the xsl <emphasis>test</emphais> attribute.
 *  
 * @author dcarver
 *
 */
public class TestAttributeContentAssist extends SelectAttributeContentAssist {

	private String ATTR_TEST = "test"; //$NON-NLS$
	/**
	 * Constructor for the XSL content assistance for the test attribute.
	 * 
	 * @param node
	 * @param parent
	 * @param documentRegion
	 * @param completionRegion
	 * @param begin
	 * @param length
	 * @param filter
	 * @param textViewer
	 */
	public TestAttributeContentAssist(Node node, Node parent,
			IStructuredDocumentRegion documentRegion,
			ITextRegion completionRegion, int begin, int length, String filter,
			ITextViewer textViewer) {
		super(node, parent, documentRegion, completionRegion, begin, length, filter,
				textViewer);
		// TODO Auto-generated constructor stub
	}
	
	/** 
	 * (non-Javadoc)
	 * @see org.eclipse.wst.xsl.ui.internal.contentassist.SelectAttributeContentAssist#getCompletionProposals()
	 */
	@Override
	public ICompletionProposal[] getCompletionProposals() {
		
		adjustXPathStart();
		
		int offset = getReplacementBeginPosition();
		IDOMAttr attrNode = (IDOMAttr)((IDOMElement)getNode()).getAttributeNode("test");
		
		this.matchString = extractXPathMatchString(attrNode, getRegion(), getReplacementBeginPosition());
		
	    addSelectProposals((Element)getNode().getParentNode(), offset);

		return  super.getCompletionProposals();
    }
	
	/**
	 *  This needs to setup the content assistance correctly. Here is what needs to happen:
	 *  1. Adjust the matchString (This should have been calculated earlier) 
	 *  2. Get the current tokens offset position..this will be the starting offset.
	 *  3. Get the replacement length...this is the difference between the token offset and the next token or end of the string
	 */
	@Override
	protected void adjustXPathStart() {
	    IDOMElement elem = (IDOMElement)getNode();
	    IDOMAttr xpathNode = (IDOMAttr)elem.getAttributeNode(ATTR_TEST);
		
		if (xpathNode != null) {
			XPathParser parser = new XPathParser(xpathNode.getValue());
			int startOffset = xpathNode.getValueRegionStartOffset() + parser.getTokenStartOffset(1, getReplacementBeginPosition() - xpathNode.getValueRegionStartOffset()) - 1;
			replacementLength = getReplacementBeginPosition() - startOffset;
		}
	}
}