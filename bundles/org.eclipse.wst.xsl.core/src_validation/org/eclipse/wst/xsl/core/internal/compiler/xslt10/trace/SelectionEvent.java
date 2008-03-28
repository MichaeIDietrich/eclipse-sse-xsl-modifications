/*******************************************************************************
 * Copyright (c) 2008 Standards for Technology in Automotive Retail
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Carver - STAR - bug 224197 - initial API and implementation
 *                    based on work from Apache Xalan 2.7.0
 *******************************************************************************/
/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: SelectionEvent.java,v 1.3 2008/03/28 02:38:17 dacarver Exp $
 */
package org.eclipse.wst.xsl.core.internal.compiler.xslt10.trace;

import org.eclipse.wst.xsl.core.internal.compiler.xslt10.templates.ElemTemplateElement;
import org.eclipse.wst.xsl.core.internal.compiler.xslt10.transformer.TransformerImpl;
import org.eclipse.wst.xsl.core.internal.compiler.xslt10.xpath.XPath;
import org.apache.xpath.objects.XObject;

import org.w3c.dom.Node;

/**
 * Event triggered by selection of a node in the style stree.
 * 
 * @xsl.usage advanced
 */
public class SelectionEvent implements java.util.EventListener {

	/**
	 * The node in the style tree where the event occurs.
	 */
	public final ElemTemplateElement m_styleNode;

	/**
	 * The XSLT processor instance.
	 */
	public final TransformerImpl m_processor;

	/**
	 * The current context node.
	 */
	public final Node m_sourceNode;

	/**
	 * The attribute name from which the selection is made.
	 */
	public final String m_attributeName;

	/**
	 * The XPath that executed the selection.
	 */
	public final XPath m_xpath;

	/**
	 * The result of the selection.
	 */
	public final XObject m_selection;

	/**
	 * Create an event originating at the given node of the style tree.
	 * 
	 * @param processor
	 *            The XSLT TransformerFactory.
	 * @param sourceNode
	 *            The current context node.
	 * @param styleNode
	 *            node in the style tree reference for the event. Should not be
	 *            null. That is not enforced.
	 * @param attributeName
	 *            The attribute name from which the selection is made.
	 * @param xpath
	 *            The XPath that executed the selection.
	 * @param selection
	 *            The result of the selection.
	 */
	public SelectionEvent(TransformerImpl processor, Node sourceNode,
			ElemTemplateElement styleNode, String attributeName, XPath xpath,
			XObject selection) {

		this.m_processor = processor;
		this.m_sourceNode = sourceNode;
		this.m_styleNode = styleNode;
		this.m_attributeName = attributeName;
		this.m_xpath = xpath;
		this.m_selection = selection;
	}
}