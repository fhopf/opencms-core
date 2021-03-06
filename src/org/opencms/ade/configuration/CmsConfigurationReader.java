/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * A class to parse ADE sitemap or module configuration files and create configuration objects from them.<p>
 */
public class CmsConfigurationReader {

    /** The Name node name. */
    public static final String N_FOLDER_NAME = "Name";

    /** The Path node name. */
    public static final String N_FOLDER_PATH = "Path";

    /** The Type node name. */
    public static final String N_RESOURCE_TYPE = "Type";

    /** The resource type configuration objects. */
    private List<CmsResourceTypeConfig> m_resourceTypeConfigs = new ArrayList<CmsResourceTypeConfig>();

    /** The parsed property configuration elements. */
    private List<CmsPropertyConfig> m_propertyConfigs = new ArrayList<CmsPropertyConfig>();

    /** The parsed detail page configuration elements. */
    private List<CmsDetailPageInfo> m_detailPageConfigs = new ArrayList<CmsDetailPageInfo>();

    /** The parsed model page configuration elements. */
    private List<CmsModelPageConfig> m_modelPageConfigs = new ArrayList<CmsModelPageConfig>();

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationReader.class);

    /** The default locale for configuration objects. */
    public static final Locale DEFAULT_LOCALE = new Locale("en");

    /** The CMS context used for reading the configuration data. */
    private CmsObject m_cms;

    /** 
     * Creates a new configuration reader.<p>
     * 
     * @param cms the CMS context which should be used to read the configuration data.<p>
     */
    public CmsConfigurationReader(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Parses a configuration XML content and creates a configuration object from it.<p>
     * 
     * @param basePath the base path 
     * @param content the XML content
     *  
     * @return the created configuration object with the data from the XML content 
     * @throws CmsException if something goes wrong 
     */
    public CmsADEConfigData parseConfiguration(String basePath, CmsXmlContent content) throws CmsException {

        if (!content.hasLocale(DEFAULT_LOCALE)) {
            return CmsADEConfigData.emptyConfiguration();
        }
        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, DEFAULT_LOCALE);
        for (I_CmsXmlContentValueLocation node : root.getSubValues("ResourceType")) {
            parseResourceTypeConfig(basePath, node);
        }
        for (I_CmsXmlContentValueLocation node : root.getSubValues("ModelPage")) {
            try {
                parseModelPage(node);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        for (I_CmsXmlContentLocation node : root.getSubValues("Property")) {
            parseProperty(node);
        }
        for (I_CmsXmlContentLocation node : root.getSubValues("DetailPage")) {
            parseDetailPage(node);
        }
        CmsADEConfigData result = new CmsADEConfigData(
            basePath,
            m_resourceTypeConfigs,
            m_propertyConfigs,
            m_detailPageConfigs,
            m_modelPageConfigs);
        result.setResource(content.getFile());
        if (OpenCms.getResourceManager().getResourceType(content.getFile().getTypeId()).getTypeName().equals(
            CmsADEManager.MODULE_CONFIG_TYPE)) {
            result.setIsModuleConfig(true);
        }
        return result;
    }

    /**
     * Parses a folder which may either be given as a path or as a folder name.<p>
     * 
     * @param basePath the  base path for the configuration 
     * @param location the XML content node from which to parse the folder 
     * @return the folder bean 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsFolderOrName parseFolderOrName(String basePath, I_CmsXmlContentLocation location) throws CmsException {

        if (location == null) {
            return null;
        }
        I_CmsXmlContentValueLocation nameLoc = location.getSubValue(N_FOLDER_NAME);
        I_CmsXmlContentValueLocation pathLoc = location.getSubValue(N_FOLDER_PATH);
        if (nameLoc != null) {
            String name = nameLoc.asString(m_cms);
            return new CmsFolderOrName(basePath == null ? null : CmsStringUtil.joinPaths(basePath, ".content"), name);
        } else if (pathLoc != null) {
            String path = pathLoc.asString(m_cms);
            CmsResource folder = m_cms.readResource(path);
            return new CmsFolderOrName(folder);
        } else {
            return null;
        }
    }

    /**
     * Parses a formatter bean.<p>
     * 
     * @param typeName the type name for which the formatter is being parsed 
     * @param node the node from which to parse the formatter data 
     * 
     * @return the formatter bean from the XML 
     */
    public CmsFormatterBean parseFormatter(String typeName, I_CmsXmlContentLocation node) {

        String type = getString(node.getSubValue("Type"));
        String minWidth = getString(node.getSubValue("MinWidth"));
        String maxWidth = getString(node.getSubValue("MaxWidth"));
        boolean preview = false;
        I_CmsXmlContentValueLocation previewLoc = node.getSubValue("IsPreview");
        preview = (previewLoc != null) && Boolean.parseBoolean(previewLoc.asString(m_cms));
        String jsp = m_cms.getRequestContext().addSiteRoot(getString(node.getSubValue("Jsp")));
        boolean searchContent = true;
        CmsFormatterBean formatterBean = new CmsFormatterBean(type, jsp, minWidth, maxWidth, "" + preview, ""
            + searchContent, null);
        return formatterBean;

    }

    /** 
     * Parses model page data from the XML content.<p>
     * 
     * @param node the XML content node 
     * @throws CmsException if something goes wrong 
     */
    public void parseModelPage(I_CmsXmlContentLocation node) throws CmsException {

        String page = getString(node.getSubValue("Page"));
        I_CmsXmlContentValueLocation disabledLoc = node.getSubValue("Disabled");
        boolean disabled = (disabledLoc != null) && Boolean.parseBoolean(disabledLoc.asString(m_cms));
        I_CmsXmlContentValueLocation defaultLoc = node.getSubValue("IsDefault");
        boolean isDefault = (defaultLoc != null) && Boolean.parseBoolean(defaultLoc.asString(m_cms));
        CmsModelPageConfig modelPage = new CmsModelPageConfig(m_cms.readResource(page), isDefault, disabled);
        m_modelPageConfigs.add(modelPage);

    }

    /**
     * Parses a resource type configuration element from the XML content.<p>
     * 
     * @param basePath the base path of the configuration 
     * @param node the XML configuration node 
     * @throws CmsException if something goes wrong 
     */
    public void parseResourceTypeConfig(String basePath, I_CmsXmlContentLocation node) throws CmsException {

        I_CmsXmlContentValueLocation typeNameLoc = node.getSubValue("TypeName");
        String typeName = typeNameLoc.asString(m_cms);
        I_CmsXmlContentValueLocation disabledLoc = node.getSubValue("Disabled");
        CmsFolderOrName folderOrName = parseFolderOrName(basePath, node.getSubValue("Folder"));
        boolean disabled = (disabledLoc != null) && Boolean.parseBoolean(disabledLoc.asString(m_cms));
        I_CmsXmlContentValueLocation namePatternLoc = node.getSubValue("NamePattern");
        String namePattern = null;
        if (namePatternLoc != null) {
            namePattern = namePatternLoc.asString(m_cms);
        }

        List<CmsFormatterBean> formatters = new ArrayList<CmsFormatterBean>();
        for (I_CmsXmlContentValueLocation formatterLoc : node.getSubValues("Formatter")) {
            CmsFormatterBean formatter = parseFormatter(typeName, formatterLoc);
            formatters.add(formatter);
        }
        CmsFormatterConfiguration formatterConfig = CmsFormatterConfiguration.create(m_cms, formatters);
        CmsResourceTypeConfig typeConfig = new CmsResourceTypeConfig(
            typeName,
            disabled,
            folderOrName,
            namePattern,
            formatterConfig);
        m_resourceTypeConfigs.add(typeConfig);
    }

    /** 
     * Parses the sitemap configuration given the configuration file and base path.<p>
     * 
     * @param basePath the base path 
     * @param configRes the configuration file resource 
     * @return the parsed configuration data 
     * @throws CmsException if something goes wrong 
     */
    public CmsADEConfigData parseSitemapConfiguration(String basePath, CmsResource configRes) throws CmsException {

        LOG.info("Parsing configuration " + configRes.getRootPath());
        CmsFile configFile = m_cms.readFile(configRes);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, configFile);
        return parseConfiguration(basePath, content);
    }

    /**
     * Reads the configurations of all modules and combines them into a single configuration object.<p>
     * 
     * @return the combined configuration object
     */
    public CmsADEConfigData readModuleConfigurations() {

        List<CmsADEConfigData> configurations = new ArrayList<CmsADEConfigData>();
        List<CmsModule> modules = OpenCms.getModuleManager().getAllInstalledModules();
        for (CmsModule module : modules) {
            String configPath = module.getConfigurationPath();
            try {
                CmsResource configFile = m_cms.readResource(configPath);
                LOG.info("Found module configuration " + configPath + " for module " + module.getName());
                CmsADEConfigData config = parseSitemapConfiguration(null, configFile);
                configurations.add(config);
            } catch (CmsVfsResourceNotFoundException e) {
                // ignore 
            } catch (CmsException e) {
                // errors while parsing configuration
                LOG.error(e.getLocalizedMessage(), e);
            } catch (CmsRuntimeException e) {
                // may happen during import of org.opencms.ade.configuration module
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return mergeConfigurations(configurations);
    }

    /**
     * Gets the string value of an XML content location.<p>
     * 
     * @param location an XML content location 
     * 
     * @return the string value of that XML content location 
     */
    protected String getString(I_CmsXmlContentValueLocation location) {

        if (location == null) {
            return null;
        }
        return location.asString(m_cms);
    }

    /**
     * Merges a list of multiple configuration objects into a single configuration object.<p>
     * 
     * @param configurations the list of configuration objects.<p>
     * 
     * @return the merged configuration object 
     */
    protected CmsADEConfigData mergeConfigurations(List<CmsADEConfigData> configurations) {

        if (configurations.isEmpty()) {
            return new CmsADEConfigData();
        }
        for (int i = 0; i < configurations.size() - 1; i++) {
            configurations.get(i + 1).mergeParent(configurations.get(i));
        }
        return configurations.get(configurations.size() - 1);
    }

    /**
     * Parses the detail pages from an XML content node.<p>
     * 
     * @param node the XML content node 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void parseDetailPage(I_CmsXmlContentLocation node) throws CmsException {

        I_CmsXmlContentValueLocation pageLoc = node.getSubValue("Page");
        String page = pageLoc.asString(m_cms);
        CmsResource detailPageRes = m_cms.readResource(page);
        CmsUUID id = detailPageRes.getStructureId();
        String typeName = getString(node.getSubValue("Type"));
        CmsDetailPageInfo detailPage = new CmsDetailPageInfo(id, page, typeName);
        m_detailPageConfigs.add(detailPage);
    }

    /**
     * Parses a single field definition from a content value.<p>
     * 
     * @param field the content value to parse the field from 
     */
    private void parseProperty(I_CmsXmlContentLocation field) {

        String name = getString(field.getSubValue("PropertyName"));
        String widget = getString(field.getSubValue("Widget"));
        String widgetConfig = getString(field.getSubValue("WidgetConfig"));
        String ruleRegex = getString(field.getSubValue("RuleRegex"));
        String ruleType = getString(field.getSubValue("RuleType"));
        String default1 = getString(field.getSubValue("Default"));
        String error = getString(field.getSubValue("Error"));
        String niceName = getString(field.getSubValue("DisplayName"));
        String description = getString(field.getSubValue("Description"));
        String preferFolder = getString(field.getSubValue("PreferFolder"));

        String disabledStr = getString(field.getSubValue("Disabled"));
        boolean disabled = ((disabledStr != null) && Boolean.parseBoolean(disabledStr));

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name,
            "string",
            widget,
            widgetConfig,
            ruleRegex,
            ruleType,
            default1,
            niceName,
            description,
            error,
            preferFolder);

        // since these are real properties, using type vfslist makes no sense, so we always use the "string" type

        CmsPropertyConfig propConfig = new CmsPropertyConfig(prop, disabled);
        m_propertyConfigs.add(propConfig);
    }

}
