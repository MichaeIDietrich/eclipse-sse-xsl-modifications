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
package org.eclipse.wst.xsl.ui.internal.editor;


import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xsl.core.model.XSLAttribute;
import org.eclipse.wst.xsl.core.model.XSLNode;
import org.eclipse.wst.xsl.ui.internal.Messages;
import org.eclipse.wst.xsl.ui.internal.XSLUIPlugin;

/**
 * @author Doug Satchwell
 *
 */
public class SourceFileHyperlink implements IHyperlink
{
	private IRegion hyperLinkRegion;
	private IFile linkedFile;
	private XSLNode sourceArtifact;

	/**
	 * @param hyperLinkRegion
	 * @param linkedFile
	 */
	public SourceFileHyperlink(IRegion hyperLinkRegion, IFile linkedFile)
	{
		this.hyperLinkRegion = hyperLinkRegion;
		this.linkedFile = linkedFile;
	}

	/**
	 * @param hyperLinkRegion
	 * @param linkedFile
	 * @param node
	 */
	public SourceFileHyperlink(IRegion hyperLinkRegion, IFile linkedFile, XSLNode node)
	{
		this.hyperLinkRegion = hyperLinkRegion;
		this.linkedFile = linkedFile;
		this.sourceArtifact = node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion()
	{
		return hyperLinkRegion;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText()
	{
		return NLS.bind(Messages.Open, linkedFile.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
	 */
	public void open()
	{
		if (linkedFile != null && linkedFile.exists())
		{
			try
			{
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart editor = IDE.openEditor(page, linkedFile, true);
				ITextEditor textEditor = (ITextEditor)editor.getAdapter(ITextEditor.class);
				if (sourceArtifact != null && textEditor != null)
				{
					IDocument openedDocument = (IDocument)editor.getAdapter(IDocument.class);
					if (openedDocument != null)
					{
						Position position = getNodePosition(openedDocument);
						
						textEditor.getSite().getPage().getNavigationHistory().markLocation(textEditor);
						
	          textEditor.selectAndReveal(position.getOffset(), position.getLength());
						//textEditor.setHighlightRange(position.getOffset(), position.getLength(), true);
						
						textEditor.getSite().getPage().getNavigationHistory().markLocation(textEditor);
					}
				}
			}
			catch (PartInitException pie)
			{
				XSLUIPlugin.log(pie);
			}
		}
	}
  
  private Position getNodePosition(IDocument relatedDocument) {
    
    try {
      int lineOffset = relatedDocument.getLineOffset(sourceArtifact.getLineNumber());
      
      // if attribute node, select attribute value
      if (sourceArtifact instanceof XSLAttribute) {
          
        int end = lineOffset + sourceArtifact.getColumnNumber() + sourceArtifact.getLength();
        
        int length = ((XSLAttribute) sourceArtifact).getValue().length();
        
        //since the length implementation of an attribute is inaccurate, we need to search for it
        for (; end > sourceArtifact.getOffset(); end--) {
          
          char c = relatedDocument.getChar(end);
          if (c == '"' || c == '\'') {
            
            int offset = end - length;
            return new Position(offset, length);
          }
        }
      }
      
      int offset = lineOffset + sourceArtifact.getColumnNumber();
      
      return new Position(offset, sourceArtifact.getLength());
    } catch (BadLocationException e) {
      XSLUIPlugin.log(e);
    }
    
    return new Position(0);
  }

}
