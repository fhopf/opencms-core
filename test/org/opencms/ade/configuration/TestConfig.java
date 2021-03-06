/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.ComparisonFailure;
import junit.framework.Test;

import com.google.common.collect.Lists;

/**
 * Lightweight tests for the ADE configuration mechanism which mostly do not read the configuration data from the VFS.<p>
 */
public class TestConfig extends OpenCmsTestCase {

    protected static final List<CmsPropertyConfig> NO_PROPERTIES = Collections.<CmsPropertyConfig> emptyList();

    protected static final List<CmsDetailPageInfo> NO_DETAILPAGES = Collections.<CmsDetailPageInfo> emptyList();

    protected static final List<CmsModelPageConfig> NO_MODEL_PAGES = Collections.<CmsModelPageConfig> emptyList();

    protected static final List<CmsResourceTypeConfig> NO_TYPES = Collections.<CmsResourceTypeConfig> emptyList();

    /**
     * Test constructor.<p>
     * 
     * @param arg0
     */
    public TestConfig(String arg0) {

        super(arg0);
    }

    /**
     * Returns the test suite.<p>
     * 
     * @return the test suite 
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestConfig.class, "ade-config", "/");
    }

    public void createFolder(String rootPath, boolean deep) throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        if (!deep) {
            cms.createResource(rootPath, CmsResourceTypeFolder.getStaticTypeId());
        } else {
            List<String> parents = new ArrayList<String>();
            String currentPath = rootPath;
            while (currentPath != null) {
                parents.add(currentPath);
                currentPath = CmsResource.getParentFolder(currentPath);
            }
            parents = Lists.reverse(parents);
            System.out.println("Creating folders up to " + rootPath + "(" + parents.size() + ")");

            for (String parent : parents) {
                System.out.println("Creating folder: " + parent);
                try {
                    cms.createResource(parent, CmsResourceTypeFolder.getStaticTypeId());
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    // nop 
                }

            }
        }
    }

    public void testCreatable() throws Exception {

        String baseDirectory = "/sites/default/testCreatable";
        String contentDirectory = baseDirectory + "/.content";
        String plain = "plain";
        String binary = "binary";
        String plainDir = contentDirectory + "/" + plain;
        String binaryDir = contentDirectory + "/" + binary;

        createFolder(plainDir, true);
        createFolder(binaryDir, true);

        CmsObject cms = rootCms();
        String username = "User_testCreatable";
        CmsUser dummyUser = cms.createUser(username, "password", "description", Collections.<String, Object> emptyMap());
        cms.chacc(plainDir, I_CmsPrincipal.PRINCIPAL_USER, username, "+r+v-w");
        cms.chacc(binaryDir, I_CmsPrincipal.PRINCIPAL_USER, username, "+r+v+w");

        CmsObject dummyUserCms = rootCms();
        cms.addUserToGroup(username, OpenCms.getDefaultUsers().getGroupUsers());
        dummyUserCms.loginUser(username, "password");
        dummyUserCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        CmsFolderOrName folder = new CmsFolderOrName(baseDirectory + "/.content", "plain");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("plain", false, folder, "pattern_%(number)", null);
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("binary", false, new CmsFolderOrName(baseDirectory
            + "/.content", "binary"), "binary_%(number).html", null);

        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1, typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        List<CmsResourceTypeConfig> creatableTypes = config1.getCreatableTypes(dummyUserCms);
        assertEquals(1, creatableTypes.size());
        assertEquals(binary, creatableTypes.get(0).getTypeName());

        creatableTypes = config1.getCreatableTypes(cms);
        assertEquals(2, creatableTypes.size());
        assertEquals(set("plain", "binary"), set(
            creatableTypes.get(0).getTypeName(),
            creatableTypes.get(1).getTypeName()));

    }

    public void testCreateElements() throws Exception {

        String typename = "plain";

        String baseDirectory = "/sites/default/testCreateElements";
        String contentDirectory = baseDirectory + "/.content";
        String articleDirectory = contentDirectory + "/" + typename;

        try {
            createFolder(articleDirectory, true);
        } catch (CmsVfsResourceAlreadyExistsException e) {
            System.out.println("***" + e);
        }
        CmsFolderOrName folder = new CmsFolderOrName(baseDirectory + "/.content", typename);
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig(typename, false, folder, "file_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        assertSame(config1.getResourceType(typename), typeConf1);
        typeConf1.createNewElement(getCmsObject());
        CmsObject cms = rootCms();
        List<CmsResource> files = cms.getFilesInFolder(articleDirectory);
        assertEquals(1, files.size());
        assertTrue(files.get(0).getName().startsWith("file_"));

        typeConf1.createNewElement(getCmsObject());
        files = cms.getFilesInFolder(articleDirectory);
        assertEquals(2, files.size());
        assertTrue(files.get(0).getName().startsWith("file_"));
        assertTrue(files.get(1).getName().startsWith("file_"));
    }

    public void testDefaultFolderName() throws Exception {

        CmsFolderOrName folder = null;
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, null, "pattern_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.setIsModuleConfig(true);
        config1.initialize(rootCms());
        typeConf1 = config1.getResourceType("foo");
        CmsFolderOrName folderOrName = typeConf1.getFolderOrName();
        String folderPath = typeConf1.getFolderPath(getCmsObject());
        assertPathEquals("/sites/default/.content/foo", folderPath);
    }

    public void testDefaultFolderName2() throws Exception {

        CmsFolderOrName folder = null;
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, null, "pattern_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.setIsModuleConfig(true);
        config1.initialize(rootCms());

        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, null, "patternx_%(number)", null);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/blah",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.setParent(config1);
        config2.initialize(rootCms());

        typeConf2 = config2.getResourceType("foo");
        String folderPath = typeConf2.getFolderPath(getCmsObject());
        assertPathEquals("/sites/default/.content/foo", folderPath);
    }

    public void testDetailPages2() throws Exception {

        CmsDetailPageInfo a1 = new CmsDetailPageInfo(getId("/sites/default/a1"), "/sites/default/a1", "a");
        CmsDetailPageInfo a2 = new CmsDetailPageInfo(getId("/sites/default/a2"), "/sites/default/a2", "a");
        CmsDetailPageInfo a3 = new CmsDetailPageInfo(getId("/sites/default/a3"), "/sites/default/a3", "a");
        CmsDetailPageInfo a4 = new CmsDetailPageInfo(getId("/sites/default/a4"), "/sites/default/a4", "a");

        CmsDetailPageInfo b1 = new CmsDetailPageInfo(getId("/sites/default/b1"), "/sites/default/b1", "b");
        CmsDetailPageInfo b2 = new CmsDetailPageInfo(getId("/sites/default/b2"), "/sites/default/b2", "b");

        List<CmsDetailPageInfo> parentDetailPages = list(a1, a2, b1, b2);
        List<CmsDetailPageInfo> childDetailPages = list(a3, a4);

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            NO_TYPES,
            NO_PROPERTIES,
            parentDetailPages,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            NO_TYPES,
            NO_PROPERTIES,
            childDetailPages,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsDetailPageInfo> pages = config2.getAllDetailPages(false);
        //assertEquals(4, pages.size());
        assertEquals(set(a3.getUri(), a4.getUri(), b1.getUri(), b2.getUri()), set(
            pages.get(0).getUri(),
            pages.get(1).getUri(),
            pages.get(2).getUri(),
            pages.get(3).getUri()));
        assertEquals(list(a3.getUri(), a4.getUri()), list(
            config2.getDetailPagesForType("a").get(0).getUri(),
            config2.getDetailPagesForType("a").get(1).getUri()));
    }

    public void testInheritedFolderName1() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/somefolder/.content", "blah");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config2.initialize(rootCms());
        config2.setParent(config1);

        assertPathEquals("/somefolder/.content/blah", config2.getResourceType(typeConf1.getTypeName()).getFolderPath(
            getCmsObject()));
    }

    public void testInheritedFolderName2() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/somefolder/.content", "blah");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, null, "blah", null);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        CmsResourceTypeConfig rtc = config2.getResourceType(typeConf1.getTypeName());
        String folderPath = rtc.getFolderPath(getCmsObject());
        assertPathEquals("/somefolder/.content/blah", folderPath);
    }

    public void testInheritNamePattern() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/.content", "foldername");
        String pattern1 = "pattern1";
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, pattern1, null);
        CmsFolderOrName folder2 = new CmsFolderOrName("/sites/default/.content", "foldername2");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, folder, null, null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        assertEquals(pattern1, config2.getResourceType("foo").getNamePattern());

    }

    public void testInheritProperties() throws Exception {

        List<CmsPropertyConfig> propConf1 = list(
            createPropertyConfig("A", "A1"),
            createPropertyConfig("B", "B1"),
            createPropertyConfig("C", "C1"),
            createPropertyConfig("D", "D1"));
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder",
            NO_TYPES,
            propConf1,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        List<CmsPropertyConfig> propConf2 = list(
            createDisabledPropertyConfig("C"),
            createPropertyConfig("D", "D2"),
            createPropertyConfig("A", "A2"));
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            NO_TYPES,
            propConf2,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsPropertyConfig> resultConf = config2.getPropertyConfiguration();
        assertEquals(3, resultConf.size());
        assertEquals("D", resultConf.get(0).getName());
        assertEquals("D2", resultConf.get(0).getPropertyData().getDescription());

        assertEquals("A", resultConf.get(1).getName());
        assertEquals("A2", resultConf.get(1).getPropertyData().getDescription());
        assertEquals("B", resultConf.get(2).getName());
        assertEquals("B1", resultConf.get(2).getPropertyData().getDescription());

    }

    public void testInheritResourceTypes1() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("bar", false, new CmsFolderOrName(
            "/.content",
            "foldername2"), "pattern2_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(typeConf1.getTypeName(), resourceTypeConfig.get(1).getTypeName());
        assertEquals(typeConf2.getTypeName(), resourceTypeConfig.get(0).getTypeName());
    }

    public void testModelPages1() throws Exception {

        CmsObject cms = getCmsObject();

        CmsModelPageConfig a1 = new CmsModelPageConfig(cms.readResource("/a1"), false, false);
        CmsModelPageConfig a2 = new CmsModelPageConfig(cms.readResource("/a2"), false, false);

        CmsModelPageConfig b1 = new CmsModelPageConfig(cms.readResource("/b1"), false, false);
        CmsModelPageConfig b2 = new CmsModelPageConfig(cms.readResource("/b2"), false, false);

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(a1, a2));
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(b1, b2));
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsModelPageConfig> modelpages = config2.getModelPages();
        assertEquals(b1.getResource().getStructureId(), modelpages.get(0).getResource().getStructureId());
        assertEquals(b2.getResource().getStructureId(), modelpages.get(1).getResource().getStructureId());
        assertEquals(a1.getResource().getStructureId(), modelpages.get(2).getResource().getStructureId());
        assertEquals(a2.getResource().getStructureId(), modelpages.get(3).getResource().getStructureId());
        assertEquals(b1.getResource().getStructureId(), config2.getDefaultModelPage().getResource().getStructureId());
    }

    public void testModelPages2() throws Exception {

        CmsObject cms = getCmsObject();

        CmsModelPageConfig a1 = new CmsModelPageConfig(cms.readResource("/a1"), false, false);
        CmsModelPageConfig a2 = new CmsModelPageConfig(cms.readResource("/a2"), false, false);

        CmsModelPageConfig b1 = new CmsModelPageConfig(cms.readResource("/b1"), false, false);
        CmsModelPageConfig b2 = new CmsModelPageConfig(cms.readResource("/b2"), true, false);

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(a1, a2));
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(b1, b2));
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsModelPageConfig> modelpages = config2.getModelPages();
        assertEquals(b2.getResource().getStructureId(), config2.getDefaultModelPage().getResource().getStructureId());
    }

    public void testOverrideResourceType() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, new CmsFolderOrName(
            "/.content",
            "foldername2"), "pattern2_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(1, resourceTypeConfig.size());
        assertEquals(typeConf2.getNamePattern(), resourceTypeConfig.get(0).getNamePattern());
    }

    public void testParseConfiguration() throws Exception {

        CmsObject cms = rootCms();
        CmsConfigurationReader configReader = new CmsConfigurationReader(rootCms());
        CmsADEConfigData configData = configReader.parseSitemapConfiguration(
            "/",
            cms.readResource("/sites/default/test.config"));
        configData.initialize(rootCms());
        assertFalse(configData.isModuleConfiguration());
        assertEquals(1, configData.getResourceTypes().size());
        CmsResourceTypeConfig v8article = configData.getResourceType("v8article");
        assertNotNull(v8article);
        assertEquals("asdf", v8article.getNamePattern());
        CmsFolderOrName folder = v8article.getFolderOrName();
        assertTrue(folder.isName());
        assertEquals("asdf", folder.getFolderName());
        assertEquals(1, configData.getAllDetailPages().size());
        assertEquals(1, configData.getModelPages().size());

        List<CmsPropertyConfig> props = configData.getPropertyConfiguration();
        assertEquals(1, props.size());
        CmsPropertyConfig prop1 = configData.getPropertyConfiguration().get(0);
        assertEquals("prop1", prop1.getName());
        assertEquals(false, prop1.isDisabled());
        assertEquals("displayname", prop1.getPropertyData().getNiceName());
        assertEquals("description", prop1.getPropertyData().getDescription());
        assertEquals("string", prop1.getPropertyData().getWidget());
        assertEquals("default", prop1.getPropertyData().getDefault());
        assertEquals("widgetconfig", prop1.getPropertyData().getWidgetConfiguration());
        assertEquals("ruleregex", prop1.getPropertyData().getRuleRegex());
        assertEquals("string", prop1.getPropertyData().getType());
        assertEquals("ruletype", prop1.getPropertyData().getRuleType());
        assertEquals("error", prop1.getPropertyData().getError());
        assertEquals(true, prop1.getPropertyData().isPreferFolder());
    }

    public void testParseModuleConfiguration() throws Exception {

        CmsObject cms = rootCms();
        CmsConfigurationReader configReader = new CmsConfigurationReader(rootCms());
        CmsADEConfigData configData = configReader.parseSitemapConfiguration(
            "/",
            cms.readResource("/sites/default/testmod.config"));
        configData.initialize(rootCms());
        assertTrue(configData.isModuleConfiguration());
        assertEquals(1, configData.getResourceTypes().size());
        CmsResourceTypeConfig anothertype = configData.getResourceType("anothertype");
        assertNotNull(anothertype);
        assertEquals("abc_%(number)", anothertype.getNamePattern());
        CmsFolderOrName folder = anothertype.getFolderOrName();
        assertTrue(folder.isFolder());
        assertPathEquals("/sites/default", folder.getFolder().getRootPath());
        assertEquals(0, configData.getAllDetailPages().size());
        assertEquals(0, configData.getModelPages().size());
        List<CmsPropertyConfig> props = configData.getPropertyConfiguration();
        assertEquals(1, props.size());
        CmsPropertyConfig prop1 = configData.getPropertyConfiguration().get(0);
        assertEquals("propertyname1", prop1.getName());
        assertEquals(false, prop1.isDisabled());
        assertEquals("displayname1", prop1.getPropertyData().getNiceName());
        assertEquals("description1", prop1.getPropertyData().getDescription());
        assertEquals("string", prop1.getPropertyData().getWidget());
        assertEquals("default1", prop1.getPropertyData().getDefault());
        assertEquals("widgetconfig1", prop1.getPropertyData().getWidgetConfiguration());
        assertEquals("ruleregex1", prop1.getPropertyData().getRuleRegex());
        assertEquals("string", prop1.getPropertyData().getType());
        assertEquals("ruletype1", prop1.getPropertyData().getRuleType());
        assertEquals("error1", prop1.getPropertyData().getError());
        assertEquals(true, prop1.getPropertyData().isPreferFolder());
    }

    public void testRemoveResourceType() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("bar", false, new CmsFolderOrName(
            "/.content",
            "foldername2"), "pattern2_%(number)", null);
        CmsResourceTypeConfig typeConf3 = new CmsResourceTypeConfig("baz", false, folder, "blah", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1, typeConf2, typeConf3),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        CmsResourceTypeConfig removeType = new CmsResourceTypeConfig("bar", true, null, null, null);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(removeType),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(2, resourceTypeConfig.size());
        assertEquals(typeConf1.getTypeName(), resourceTypeConfig.get(0).getTypeName());
        assertEquals(typeConf3.getTypeName(), resourceTypeConfig.get(1).getTypeName());
    }

    public void testReorderResourceTypes() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("bar", false, new CmsFolderOrName(
            "/.content",
            "foldername2"), "pattern2_%(number)", null);
        CmsResourceTypeConfig typeConf3 = new CmsResourceTypeConfig("baz", false, folder, "blah", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1, typeConf2, typeConf3),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf3.copy(), typeConf1.copy()),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(3, resourceTypeConfig.size());
        assertEquals(typeConf3.getTypeName(), resourceTypeConfig.get(0).getTypeName());
        assertEquals(typeConf1.getTypeName(), resourceTypeConfig.get(1).getTypeName());
        assertEquals(typeConf2.getTypeName(), resourceTypeConfig.get(2).getTypeName());
    }

    public void testResolveFolderName1() throws Exception {

        CmsFolderOrName folder = new CmsFolderOrName("/somefolder/somesubfolder/.content", "blah");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)", null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        assertPathEquals(
            "/somefolder/somesubfolder/.content/blah",
            config1.getResourceType(typeConf1.getTypeName()).getFolderPath(getCmsObject()));
    }

    public void testXsdFormatters1() throws Exception {

        CmsFolderOrName folder = null;
        CmsTestConfigData config1 = new CmsTestConfigData("/", NO_TYPES, NO_PROPERTIES, NO_DETAILPAGES, NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsFormatterConfiguration formatterConfig = config1.getFormatters("article1");
        List<CmsFormatterBean> formatters = formatterConfig.getAllFormatters();
        assertEquals(2, formatters.size());
        CmsFormatterBean formatter1 = formatters.get(0);
        CmsFormatterBean formatter2 = formatters.get(1);
        assertEquals(1, formatter1.getMinWidth());
        assertEquals(2, formatter1.getMaxWidth());
        assertEquals(3, formatter2.getMinWidth());
        assertEquals(4, formatter2.getMaxWidth());
    }

    protected void assertPathEquals(String path1, String path2) {

        if ((path1 == null) && (path2 == null)) {
            return;
        }
        if ((path1 == null) || (path2 == null)) {
            throw new ComparisonFailure("comparison failure", path1, path2);
        }
        assertEquals(CmsStringUtil.joinPaths("/", path1, "/"), CmsStringUtil.joinPaths("/", path2, "/"));
    }

    protected CmsPropertyConfig createDisabledPropertyConfig(String name) {

        CmsXmlContentProperty prop = createXmlContentProperty(name, null);
        return new CmsPropertyConfig(prop, true);
    }

    protected CmsPropertyConfig createPropertyConfig(String name, String description) {

        CmsXmlContentProperty prop = createXmlContentProperty(name, description);
        return new CmsPropertyConfig(prop, false);
    }

    protected CmsXmlContentProperty createXmlContentProperty(String name, String description) {

        return new CmsXmlContentProperty(name, "string", "string", "", "", "", "", "", description, null, null);
    }

    protected void dumpTree() throws CmsException {

        CmsObject cms = rootCms();
        CmsResource root = cms.readResource("/");
        dumpTree(cms, root, 0);
    }

    protected void dumpTree(CmsObject cms, CmsResource res, int indentation) throws CmsException {

        writeIndentation(indentation);
        System.out.println(res.getName());
        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res);
        if (resType.isFolder()) {
            List<CmsResource> children = cms.getResourcesInFolder(res.getRootPath(), CmsResourceFilter.ALL);
            for (CmsResource child : children) {
                dumpTree(cms, child, indentation + 6);
            }
        }
    }

    protected CmsUUID getId(String rootPath) throws CmsException {

        CmsObject cms = rootCms();
        return cms.readResource(rootPath).getStructureId();
    }

    protected <X> List<X> list(X... elems) {

        List<X> result = new ArrayList<X>();
        for (X x : elems) {
            result.add(x);
        }
        return result;
    }

    protected CmsObject rootCms() throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        return cms;
    }

    protected <X> Set<X> set(X... elems) {

        Set<X> result = new HashSet<X>();
        for (X x : elems) {
            result.add(x);
        }
        return result;
    }

    protected void writeIndentation(int indent) {

        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
    }

}
