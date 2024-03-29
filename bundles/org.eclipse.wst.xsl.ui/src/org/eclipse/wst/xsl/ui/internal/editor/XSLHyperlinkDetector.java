/*******************************************************************************
 * Copyright (c) 2007 Chase Technology Ltd - http://www.chasetechnology.co.uk
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Satchwell (Chase Technology Ltd) - initial API and implementation
 *     David Carver (STAR) - bug 
 *******************************************************************************/
package org.eclipse.wst.xsl.ui.internal.editor;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xsl.core.XSLCore;
import org.eclipse.wst.xsl.core.model.Function;
import org.eclipse.wst.xsl.core.model.Include;
import org.eclipse.wst.xsl.core.model.Parameter;
import org.eclipse.wst.xsl.core.model.StylesheetModel;
import org.eclipse.wst.xsl.core.model.Template;
import org.eclipse.wst.xsl.core.model.Variable;
import org.eclipse.wst.xsl.core.model.XSLAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A hyperlink detector for XSL files.
 * 
 * The detector makes use of the model built by SSE validation (found in
 * <code>XSLCore</code>).
 * 
 * Currently supports hyperlinking for includes, imports or called templates.
 * 
 * @author Doug Satchwell
 */
public class XSLHyperlinkDetector extends AbstractHyperlinkDetector {
  private static final String ELEM_WITH_PARAM = "with-param"; //$NON-NLS-1$
  private static final String ATTR_NAME = "name"; //$NON-NLS-1$
  private static final String ELEM_CALL_TEMPLATE = "call-template"; //$NON-NLS-1$
  private static final String ELEM_INCLUDE = "include"; //$NON-NLS-1$
  private static final String ELEM_IMPORT = "import"; //$NON-NLS-1$
  private static final String ATTR_HREF = "href"; //$NON-NLS-1$

  private static final String ATTR_SELECT = "select"; //$NON-NLS-1$

  /**
   * Try to create hyperlinks for viewer and region
   * 
   * @see AbstractHyperlinkDetector
   * @param textViewer
   * @param region
   * @param canShowMultipleHyperlinks
   * @return array of hyperlinks for current region
   */
  public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
    IHyperlink hyperlink = null;
    if (region != null && textViewer != null) {
      return detectHyperlinks(textViewer.getDocument(), region, canShowMultipleHyperlinks);
    }
    return new IHyperlink[] { hyperlink };
  }

  /**
   * Try to create hyperlinks for document and region
   * 
   * @param document
   * @param region
   * @param canShowMultipleHyperlinks
   * @return array of hyperlinks for current region
   */
  public IHyperlink[] detectHyperlinks(IDocument document, IRegion region, boolean canShowMultipleHyperlinks) {
    IHyperlink hyperlink = null;

    if (region != null && document != null) {
      int cursorPos = region.getOffset();

      Node currentNode = XSLCore.getCurrentNode(document, cursorPos);

      Element xslEl = null;
      Attr xslAttr = null;
      if (XSLCore.XSL_NAMESPACE_URI.equals(currentNode.getNamespaceURI())) {
        if (currentNode.getNodeType() == Node.ATTRIBUTE_NODE) {
          Attr att = (Attr) currentNode;
          xslEl = att.getOwnerElement();
          xslAttr = att;
        }

        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
          Element el = (Element) currentNode;
          xslEl = el;
          xslAttr = XSLCore.getCurrentAttrNode(el, cursorPos);
        }
      }
      hyperlink = createHyperLink(document, hyperlink, xslEl, xslAttr, cursorPos);
    }
    return hyperlink == null ? null : new IHyperlink[] { hyperlink };
  }

  private IHyperlink createHyperLink(IDocument document, IHyperlink hyperlink, Element xslEl, Attr xslAttr, int cursorPos) {
    if (xslEl != null && xslAttr != null) {
      IRegion hyperlinkRegion = getHyperlinkRegion(xslAttr);
      IFile file = getFileForDocument(document);
      if (file != null) {
        if (ELEM_CALL_TEMPLATE.equals(xslEl.getLocalName()) && ATTR_NAME.equals(xslAttr.getLocalName())) {
          hyperlink = createCallTemplateHyperLink(file, xslAttr.getValue(), hyperlinkRegion);
        }

        if (ELEM_WITH_PARAM.equals(xslEl.getLocalName()) && ATTR_NAME.equals(xslAttr.getLocalName())) {
          hyperlink = createWithParamHyperLink(file, xslEl, xslAttr, hyperlinkRegion);
        }

        if ((ELEM_INCLUDE.equals(xslEl.getLocalName()) || ELEM_IMPORT.equals(xslEl.getLocalName())) && ATTR_HREF.equals(xslAttr.getLocalName())) {
          hyperlink = createHrefHyperLink(file, xslEl, xslAttr, hyperlinkRegion);
        }

        if (ATTR_SELECT.equals(xslAttr.getLocalName())) {
          hyperlink = createSelectHyperLink(file, xslEl, xslAttr, hyperlinkRegion, cursorPos);
        }
      }

    }
    return hyperlink;
  }

  private IHyperlink createCallTemplateHyperLink(IFile currentFile, String templateName, IRegion hyperlinkRegion) {
    IHyperlink hyperlink = null;
    StylesheetModel sf = XSLCore.getInstance().getStylesheet(currentFile);
    if (sf != null) {
      List<Template> templates = sf.getTemplatesByName(templateName);
      if (templates != null && templates.size() == 1) {
        Template template = templates.get(0);
        hyperlink = new SourceFileHyperlink(hyperlinkRegion, template.getStylesheet().getFile(), template.getAttribute(ATTR_NAME));
      }
    }
    return hyperlink;
  }

  private IHyperlink createWithParamHyperLink(IFile currentFile, Element elem, Attr attr, IRegion hyperlinkRegion) {
    IHyperlink hyperlink = null;
    StylesheetModel sf = XSLCore.getInstance().getStylesheet(currentFile);
    if (sf != null) {
      Node parentNode = elem.getParentNode();
      Attr parentAttribute = (Attr) parentNode.getAttributes().getNamedItem(ATTR_NAME);
      String templateName = parentAttribute.getValue();
      List<Template> templates = sf.getTemplatesByName(templateName);

      if (templates != null && templates.size() == 1) {
        Template template = templates.get(0);
        List<Parameter> parameters = template.getParameters();
        for (Parameter param : parameters) {
          String paramName = attr.getValue();
          XSLAttribute parameterNameAttr = param.getAttribute(ATTR_NAME);
          if (parameterNameAttr != null && parameterNameAttr.getValue().equals(paramName)) {
            hyperlink = new SourceFileHyperlink(hyperlinkRegion, template.getStylesheet().getFile(), param.getAttribute(ATTR_NAME));
          }
        }
      }
    }
    return hyperlink;
  }

  private IHyperlink createHrefHyperLink(IFile currentFile, Element elem, Attr attr, IRegion hyperlinkRegion) {

    IHyperlink hyperlink = null;
    StylesheetModel sf = XSLCore.getInstance().getStylesheet(currentFile);

    if (sf != null) {

      for (Include include : sf.getIncludes()) {

        if (attr.getValue().equals(include.getHref())) {
          hyperlink = new SourceFileHyperlink(hyperlinkRegion, include.getHrefAsFile());
          break;
        }
      }
    }

    return hyperlink;
  }

  private IHyperlink createSelectHyperLink(IFile currentFile, Element elem, Attr attr, IRegion hyperlinkRegion, int cursorPos) {

    IHyperlink hyperlink = null;
    StylesheetModel sf = XSLCore.getInstance().getStylesheet(currentFile);
    if (sf != null) {

      int relativePos = cursorPos - hyperlinkRegion.getOffset();
      String name = getVariable(attr.getValue(), relativePos);

      if (name != null) {
        for (Variable var : sf.getGlobalVariables()) {
          if (name.equals(var.getName())) {
            IFile file = var.getStylesheet().getFile();
            hyperlink = new SourceFileHyperlink(hyperlinkRegion, file, var.getAttribute(ATTR_NAME));
            break;
          }
        }

        Node parentNode = elem.getParentNode();

        if (parentNode != null && parentNode instanceof Element) {
          Element parent = (Element) parentNode;

          for (Entry<Variable, Element> entry : sf.getLocalVariables().entrySet()) {
            if (XSLCore.isAncestorOf(entry.getValue(), parent) && name.equals(entry.getKey().getName())) {
              hyperlink = new SourceFileHyperlink(hyperlinkRegion, currentFile, entry.getKey().getAttribute(ATTR_NAME));
              break;
            }
          }
        }
      }

      name = getFunction(attr.getValue(), relativePos);

      if (name != null) {
        // TODO: function depends on parameters!
        for (Function func : sf.getFunctions()) {
          if (name.equals(func.getName())) {
            IFile file = func.getStylesheet().getFile();
            hyperlink = new SourceFileHyperlink(hyperlinkRegion, file, func.getAttribute(ATTR_NAME));
            break;
          }
        }
      }
    }
    return hyperlink;
  }

  private static String getVariable(String text, int selectionPos) {

    int start = text.substring(0, selectionPos + 1).lastIndexOf('$');
    if (start == -1)
      return null;

    // text.substring(selectionPos).

    Pattern p = Pattern.compile("^[a-zA-ZäöüÄÖÜß0-9_\\-]+"); //$NON-NLS-1$
    Matcher m = p.matcher(text.substring(start + 1));

    if (!m.find())
      return null;
    String name = m.group();

    if (name != null && name.length() + 1 >= selectionPos - start) {
      return name;
    }

    return null;
  }

  private static String getFunction(String text, int selectionPos) {

    int end = text.substring(selectionPos).indexOf('(') + selectionPos;
    if (end == -1)
      return null;

    // text.substring(selectionPos).

    Pattern p = Pattern.compile("[a-zA-ZäöüÄÖÜß0-9_:\\-]+$"); //$NON-NLS-1$
    Matcher m = p.matcher(text.substring(0, end));

    if (!m.find())
      return null;
    String name = m.group();

    if (name != null && name.length() + 1 >= end - selectionPos) {
      return name;
    }

    return null;
  }

  private IRegion getHyperlinkRegion(Node node) {
    IRegion hyperRegion = null;

    if (node != null) {
      short nodeType = node.getNodeType();
      if (nodeType == Node.DOCUMENT_TYPE_NODE) {
        // handle doc type node
        IDOMNode docNode = (IDOMNode) node;
        hyperRegion = new Region(docNode.getStartOffset(), docNode.getEndOffset() - docNode.getStartOffset());
      } else if (nodeType == Node.ATTRIBUTE_NODE) {
        // handle attribute nodes
        IDOMAttr att = (IDOMAttr) node;
        // do not include quotes in attribute value region
        int regOffset = att.getValueRegionStartOffset();
        ITextRegion valueRegion = att.getValueRegion();
        if (valueRegion != null) {
          int regLength = valueRegion.getTextLength();
          String attValue = att.getValueRegionText();
          if (StringUtils.isQuoted(attValue)) {
            ++regOffset;
            regLength = regLength - 2;
          }
          hyperRegion = new Region(regOffset, regLength);
        }
      }
    }
    return hyperRegion;
  }

  private IFile getFileForDocument(IDocument document) {
    IFile file = null;
    IStructuredModel sModel = null;
    try {
      sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
      if (sModel != null) {
        IPath path = new Path(sModel.getBaseLocation());
        file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
      }
    } finally {
      if (sModel != null)
        sModel.releaseFromRead();
    }
    return file != null && file.exists() ? file : null;
  }

}
