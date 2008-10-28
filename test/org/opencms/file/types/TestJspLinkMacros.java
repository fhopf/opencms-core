/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/types/TestJspLinkMacros.java,v $
 * Date   : $Date: 2008/07/14 10:05:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.types;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the link parseable resource types.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 */
public class TestJspLinkMacros extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestJspLinkMacros(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestJspLinkMacros.class.getName());

        suite.addTest(new TestJspLinkMacros("testLinkParsing"));
        suite.addTest(new TestJspLinkMacros("testLinkGeneration"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test link parsing when using the link macro.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLinkParsing() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing link parsing when using the link macro");

        String sourceName = "/testLinkParsing.jsp";
        String targetName = "/testLinkParsing2.jsp";

        CmsResource res2 = cms.createResource(
            targetName,
            CmsResourceTypeJsp.getStaticTypeId(),
            "Test".getBytes("UTF-8"),
            null);
        CmsResource res = cms.createResource(sourceName, CmsResourceTypeJsp.getStaticTypeId(), ("%(link.strong:"
            + targetName + ")").getBytes("UTF-8"), null);

        CmsFile file = cms.readFile(res);

        List links = ((I_CmsLinkParseable)OpenCms.getResourceManager().getResourceType(file)).parseLinks(cms, file);
        assertEquals(1, links.size());
        assertEquals(
            new CmsLink("link0", CmsRelationType.JSP_STRONG, res2.getStructureId(), res2.getRootPath(), true),
            links.get(0));
    }

    /**
     * Test link parsing when using the link macro.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLinkGeneration() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing link generation when using the link macro");

        String sourceName = "/testLinkParsing.jsp";
        String targetName = "/testLinkParsing2.jsp";

        CmsJspLinkMacroResolver macroResolver = new CmsJspLinkMacroResolver(cms, null, true);
        CmsFile file = cms.readFile(sourceName);
        String result = macroResolver.resolveMacros(CmsEncoder.createString(file.getContents(), "UTF-8"));
        String expected = cms.getSitePath(cms.readResource(targetName));
        assertEquals(expected, result);
    }
}