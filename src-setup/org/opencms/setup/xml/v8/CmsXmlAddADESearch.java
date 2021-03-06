/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.search.CmsVfsIndexer;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearchAnalyzer;
import org.opencms.search.galleries.CmsGallerySearchFieldConfiguration;
import org.opencms.search.galleries.CmsGallerySearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearchIndex;
import org.opencms.setup.xml.A_CmsXmlSearch;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Adds the gallery search nodes.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddADESearch extends A_CmsXmlSearch {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * Creates a dom4j element from an XML string.<p>
     * 
     * @param xml the xml string 
     * @return the dom4j element 
     * 
     * @throws DocumentException if the XML parsing fails
     */
    public static org.dom4j.Element createElementFromXml(String xml) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document newNodeDocument = reader.read(new StringReader(xml));
        return newNodeDocument.getRootElement();
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add the ADE containerpage and gallery search nodes";
    }

    protected String buildXpathForDocumentType(String documentType) {

        // /opencms/search/documenttypes/documenttype[name='msoffice-ole2']    (0)
        StringBuffer xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_DOCUMENTTYPE);
        xp.append("[");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("='msoffice-ole2']");
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(1))) {
                // create analyzer
                createAnalyzer(document, xpath, CmsGallerySearchAnalyzer.class, "all");
            } else if (xpath.equals(getXPathsToUpdate().get(2))) {
                // create doc type
                createIndex(
                    document,
                    xpath,
                    CmsGallerySearchIndex.class,
                    CmsGallerySearchIndex.GALLERY_INDEX_NAME,
                    "offline",
                    "Offline",
                    "all",
                    "gallery_fields",
                    new String[] {"gallery_source"});
            } else if (xpath.equals(getXPathsToUpdate().get(3))) {
                // create doc type
                createIndexSource(document, xpath, "gallery_source", CmsVfsIndexer.class, new String[] {
                    "/sites/",
                    "/shared/"}, new String[] {
                    "xmlpage-galleries",
                    "xmlcontent-galleries",
                    "jsp",
                    "page",
                    "text",
                    "pdf",
                    "rtf",
                    "html",
                    "image",
                    "generic",
                    "openoffice",
                    "msoffice-ole2",
                    "msoffice-ooxml"});
            } else if (xpath.equals(getXPathsToUpdate().get(4))) {
                // create field config
                CmsSearchFieldConfiguration fieldConf = new CmsSearchFieldConfiguration();
                fieldConf.setName("gallery_fields");
                fieldConf.setDescription("The standard OpenCms search index field configuration.");
                CmsSearchField field = new CmsSearchField();
                // <field name="content" store="compress" index="true" excerpt="true">
                field.setName("content");
                field.setStored("compress");
                field.setIndexed("true");
                field.setInExcerpt("true");
                // <mapping type="content" />
                CmsSearchFieldMapping mapping = new CmsSearchFieldMapping();
                mapping.setType("content");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="title-key" store="true" index="untokenized" boost="0.0">
                field = new CmsSearchField();
                field.setName("title-key");
                field.setStored("true");
                field.setIndexed("untokenized");
                field.setBoost("0.0");
                // <mapping type="property">Title</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Title");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="title" store="false" index="true">
                field = new CmsSearchField();
                field.setName("title");
                field.setStored("false");
                field.setIndexed("true");
                // <mapping type="property">Title</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Title");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="description" store="true" index="true">
                field = new CmsSearchField();
                field.setName("description");
                field.setStored("true");
                field.setIndexed("true");
                // <mapping type="property">Description</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Description");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="meta" store="false" index="true">
                field = new CmsSearchField();
                field.setName("meta");
                field.setStored("false");
                field.setIndexed("true");
                // <mapping type="property">Title</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Title");
                field.addMapping(mapping);
                // <mapping type="property">Description</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Description");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_dateExpired" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_dateExpired");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">dateExpired</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("dateExpired");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_dateReleased" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_dateReleased");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">dateReleased</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("dateReleased");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_length" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_length");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">length</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("length");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_state" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_state");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">state</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("state");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_structureId" store="true" index="false">
                field = new CmsSearchField();
                field.setName("res_structureId");
                field.setStored("true");
                field.setIndexed("false");
                // <mapping type="attribute">structureId</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("structureId");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_userCreated" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_userCreated");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">userCreated</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("userCreated");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_userLastModified" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_userLastModified");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">userLastModified</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("userLastModified");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_locales" store="true" index="true" analyzer="WhitespaceAnalyzer">
                field = new CmsSearchField();
                field.setName("res_locales");
                field.setStored("true");
                field.setIndexed("true");
                try {
                    field.setAnalyzer("WhitespaceAnalyzer");
                } catch (Exception e) {
                    // ignore
                    e.printStackTrace();
                }
                // <mapping type="dynamic" class="org.opencms.search.galleries.CmsGallerySearchFieldMapping">res_locales</mapping>
                mapping = new CmsGallerySearchFieldMapping();
                mapping.setType("dynamic");
                mapping.setParam("res_locales");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="additional_info" store="true" index="false">
                field = new CmsSearchField();
                field.setName("additional_info");
                field.setStored("true");
                field.setIndexed("false");
                // <mapping type="dynamic" class="org.opencms.search.galleries.CmsGallerySearchFieldMapping">additional_info</mapping>
                mapping = new CmsGallerySearchFieldMapping();
                mapping.setType("dynamic");
                mapping.setParam("additional_info");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="container_types" store="true" index="true" analyzer="WhitespaceAnalyzer">
                field = new CmsSearchField();
                field.setName("container_types");
                field.setStored("true");
                field.setIndexed("true");
                try {
                    field.setAnalyzer("WhitespaceAnalyzer");
                } catch (Exception e) {
                    // ignore
                    e.printStackTrace();
                }
                // <mapping type="dynamic" class="org.opencms.search.galleries.CmsGallerySearchFieldMapping">container_types</mapping>
                mapping = new CmsGallerySearchFieldMapping();
                mapping.setType("dynamic");
                mapping.setParam("container_types");
                field.addMapping(mapping);
                fieldConf.addField(field);
                createFieldConfig(document, xpath, fieldConf, CmsGallerySearchFieldConfiguration.class);
            } else if (xpath.equals(getXPathsToUpdate().get(5))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "containerpage");
            } else if (xpath.equals(getXPathsToUpdate().get(6))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "openoffice");
            } else if (xpath.equals(getXPathsToUpdate().get(7))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "msoffice-ole2");
            } else if (xpath.equals(getXPathsToUpdate().get(8))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "msoffice-ooxml");
            } else if (xpath.equals(getXPathsToUpdate().get(9))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "openoffice");
            } else if (xpath.equals(getXPathsToUpdate().get(16))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "msoffice-ole2");
            } else if (xpath.equals(getXPathsToUpdate().get(17))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "msoffice-ooxml");
            } else if (xpath.equals(getXPathsToUpdate().get(18))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "openoffice");
            }
            return true;
        } else {
            if (xpath.equals(getXPathsToUpdate().get(10))) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(11))) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(12))) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(13))) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(14))) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(15))) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(0))) {
                org.dom4j.Element parent = node.getParent();
                int position = parent.indexOf(node);
                parent.remove(node);
                try {
                    parent.elements().add(
                        position,
                        createElementFromXml("      <documenttypes>     \n"
                            + "            <documenttype>\n"
                            + "                <name>generic</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentGeneric</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>*</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype> \n"
                            + "            <documenttype>\n"
                            + "                <name>html</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentHtml</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>image</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentGeneric</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>image</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>jsp</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentPlainText</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>jsp</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>pdf</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentPdf</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>application/pdf</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>rtf</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentRtf</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/rtf</mimetype>\n"
                            + "                    <mimetype>application/rtf</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>text</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentPlainText</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                    <mimetype>text/plain</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype> \n"
                            + "            <documenttype>\n"
                            + "                <name>xmlcontent</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentXmlContent</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>*</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>containerpage</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentContainerPage</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>containerpage</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>                 \n"
                            + "            <documenttype>\n"
                            + "                <name>xmlpage</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentXmlPage</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>xmlpage</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>xmlcontent-galleries</name>\n"
                            + "                <class>org.opencms.search.galleries.CmsGalleryDocumentXmlContent</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>xmlcontent-galleries</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype> \n"
                            + "            <documenttype>\n"
                            + "                <name>xmlpage-galleries</name>\n"
                            + "                <class>org.opencms.search.galleries.CmsGalleryDocumentXmlPage</class>\n"
                            + "                <mimetypes />\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>xmlpage-galleries</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>msoffice-ole2</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentMsOfficeOLE2</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>application/vnd.ms-powerpoint</mimetype>\n"
                            + "                    <mimetype>application/msword</mimetype>     \n"
                            + "                    <mimetype>application/vnd.ms-excel</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>                 \n"
                            + "            <documenttype>\n"
                            + "                <name>msoffice-ooxml</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentMsOfficeOOXML</class>\n"
                            + "                <mimetypes>             \n"
                            + "                    <mimetype>application/vnd.openxmlformats-officedocument.wordprocessingml.document</mimetype>\n"
                            + "                    <mimetype>application/vnd.openxmlformats-officedocument.spreadsheetml.sheet</mimetype>\n"
                            + "                    <mimetype>application/vnd.openxmlformats-officedocument.presentationml.presentation</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>                 \n"
                            + "            <documenttype>\n"
                            + "                <name>openoffice</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentOpenOffice</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>application/vnd.oasis.opendocument.text</mimetype>\n"
                            + "                    <mimetype>application/vnd.oasis.opendocument.spreadsheet</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "        </documenttypes>\n"));
                } catch (DocumentException e) {
                    System.out.println("failed to update document types.");
                }

            }
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/search
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsSearchConfiguration.N_SEARCH).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            m_xpaths = new ArrayList<String>();
            StringBuffer xp = new StringBuffer(256);
            m_xpaths.add(buildXpathForDoctypes()); // 0
            // /opencms/search/analyzers/analyzer[class='org.opencms.search.galleries.CmsGallerySearchAnalyzer']  (1)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_ANALYZERS);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_ANALYZER);
            xp.append("[");
            xp.append(CmsSearchConfiguration.N_CLASS);
            xp.append("='").append(CmsGallerySearchAnalyzer.class.getName()).append("']");
            m_xpaths.add(xp.toString());
            // /opencms/search/indexes/index[name='ADE Gallery Index']   (2)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEX);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='").append(CmsGallerySearchIndex.GALLERY_INDEX_NAME).append("']");
            m_xpaths.add(xp.toString());
            // /opencms/search/indexsources/indexsource[name='gallery_source']    (3)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='gallery_source']");
            m_xpaths.add(xp.toString());
            // /opencms/search/fieldconfigurations/fieldconfiguration[name='gallery_fields']  (4)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATIONS);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATION);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='gallery_fields']");
            m_xpaths.add(xp.toString());

            // /opencms/search/indexsources/indxsource[name='source1']/documenttypes_indexed/name[text()='containerpage']    (5) 
            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "containerpage"));

            // /opencms/search/indexsources/indxsource[name='source1']/documenttypes_indexed/name[text()='openoffice']     (6)
            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "openoffice"));

            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "msoffice-ole2")); // (7) 
            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "msoffice-ooxml")); // (8)
            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "openoffice")); // (9)

            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "msword")); // (10)
            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "msexcel")); // (11)
            m_xpaths.add(buildXpathForIndexedDocumentType("source1", "mspowerpoint")); // (12)

            m_xpaths.add(buildXpathForIndexedDocumentType("gallery_source", "msword")); // (13)
            m_xpaths.add(buildXpathForIndexedDocumentType("gallery_source", "msexcel")); // (14)
            m_xpaths.add(buildXpathForIndexedDocumentType("gallery_source", "mspowerpoint")); // (15)

            m_xpaths.add(buildXpathForIndexedDocumentType("gallery_source", "msoffice-ole2")); // (16) 
            m_xpaths.add(buildXpathForIndexedDocumentType("gallery_source", "msoffice-ooxml")); // (17)
            m_xpaths.add(buildXpathForIndexedDocumentType("gallery_source", "openoffice")); // (18)

        }
        return m_xpaths;
    }

    private String buildXpathForDoctypes() {

        return getCommonPath() + "/" + CmsSearchConfiguration.N_DOCUMENTTYPES;
    }

    private String buildXpathForIndexedDocumentType(String source, String doctype) {

        StringBuffer xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
        xp.append("[");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("='" + source + "']");
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES_INDEXED);
        xp.append("/");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("[text()='" + doctype + "']");
        return xp.toString();
    }

}