/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsImageInfosTab.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.galleries.preview.image.client;

import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsPushButton;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The widget to display the information of the selected image.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsImageInfosTab extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsImageInfosTabUiBinder extends UiBinder<Widget, CmsImageInfosTab> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsImageInfosTabUiBinder uiBinder = GWT.create(I_CmsImageInfosTabUiBinder.class);

    /** The panel holding the content. */
    @UiField
    FlowPanel m_panel;

    /** The select button. */
    @UiField
    CmsPushButton m_selectButton;

    /** The mode of the gallery. */
    private GalleryMode m_dialogMode;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the mode of the gallery
     * @param height the height of the tab
     * @param width the width of the height
     * @param infos the map with image infos to display
     */
    public CmsImageInfosTab(GalleryMode dialogMode, int height, int width, Map<String, String> infos) {

        initWidget(uiBinder.createAndBindUi(this));

        m_dialogMode = dialogMode;

        // buttons        
        m_selectButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));

    }

    /**
     * Returns the gallery dialog mode.<p>
     *
     * @return the gallery dialog mode
     */
    public GalleryMode getDialogMode() {

        return m_dialogMode;
    }

    /**
     * Will be triggered when the select button is clicked.<p>
     * 
     * @param event the clicked event
     */
    @UiHandler("m_selectButton")
    public void onSelectClick(ClickEvent event) {

        // TODO:implement

    }
}