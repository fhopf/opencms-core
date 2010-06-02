/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsPreviewService.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync;
import org.opencms.gwt.CmsGwtService;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles all RPC services related to the gallery preview dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.CmsPreviewService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync
 */
public class CmsPreviewService extends CmsGwtService implements I_CmsPreviewServiceAsync {

    /** Serialization uid. */
    private static final long serialVersionUID = -8175522641937277445L;

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync#getPreview(java.lang.String, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void getPreview(String resourcePath, AsyncCallback<CmsPreviewInfoBean> callback) {

        //TODO: implement
    }

    /** 
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync#updateProperties(java.util.Map, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void updateProperties(Map<String, String> properties, AsyncCallback<CmsPreviewInfoBean> callback) {

        //TODO: implement
    }
}