package com.opencms.file;

import com.opencms.core.*;
import java.util.*;

/**
 * This class describes the resource-type link.
 *
 * @author
 * @version 1.0
 */

public class CmsResourceTypeLink extends CmsResourceTypePlain {

    public static final String C_TYPE_RESOURCE_NAME = "link";

	public CmsResource createResource(CmsObject cms, String folder, String name, Hashtable properties, byte[] contents) throws CmsException{
		CmsResource res = cms.doCreateFile(folder, name, contents, C_TYPE_RESOURCE_NAME, properties);
		// lock the new file
		cms.lockResource(folder+name);
		return res;
	}
}