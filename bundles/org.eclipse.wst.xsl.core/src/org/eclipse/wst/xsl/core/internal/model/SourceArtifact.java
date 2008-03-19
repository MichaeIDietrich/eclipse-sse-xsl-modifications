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
package org.eclipse.wst.xsl.core.internal.model;

import org.eclipse.core.runtime.PlatformObject;

public class SourceArtifact extends PlatformObject
{
	final SourceFile sourceFile;
	int lineNumber;
	int columnNumber;
	
	public SourceArtifact(SourceFile parentSourceFile)
	{
		this.sourceFile = parentSourceFile;
	}

	public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}

	public void setColumnNumber(int columnNumber)
	{
		this.columnNumber = columnNumber;
	}

	public SourceFile getSourceFile()
	{
		return sourceFile;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public int getColumnNumber()
	{
		return columnNumber;
	}
	
	@Override
	public String toString() {
		return "file="+sourceFile+", line="+lineNumber+", col="+columnNumber;
	}
}