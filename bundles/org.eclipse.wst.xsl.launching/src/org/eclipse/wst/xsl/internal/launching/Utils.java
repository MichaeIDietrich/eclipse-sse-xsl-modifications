package org.eclipse.wst.xsl.internal.launching;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Utils
{

	public static String getFileLocation(String bundleId, String path) throws CoreException
	{
		String location = null;
		try
		{
			URL url = FileLocator.find(Platform.getBundle(bundleId), new Path(path), null);
			if (url != null)
			{
				URL fileUrl = FileLocator.toFileURL(url);
				File file = new File(fileUrl.getFile());
				location = file.getAbsolutePath();
			}
		}
		catch (IOException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.PLUGIN_ID, IStatus.ERROR, "Error determining jar file location: " + path + " from bundle: " + bundleId, e));
		} 
		return location;
	}

}