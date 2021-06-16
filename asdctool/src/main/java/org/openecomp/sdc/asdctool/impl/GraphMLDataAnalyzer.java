/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.asdctool.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class GraphMLDataAnalyzer {

    public static final String GRAPH_ML_EXTENSION = ".graphml";
    public static final String EXCEL_EXTENSION = ".xls";
    private static final String[] COMPONENT_SHEET_HEADER = {"uniqueId", "type", "name", "toscaResourceName", "resourceType", "version", "deleted",
        "hasNonCalculatedReqCap"};
    private static final String[] COMPONENT_INSTANCES_SHEET_HEADER = {"uniqueId", "name", "originUid", "originType", "containerUid"};
    private static final Logger log = LoggerFactory.getLogger(GraphMLDataAnalyzer.class);

    public String analyzeGraphMLData(final String[] args) {
        String result;
        try {
            final String mlFileLocation = args[0];
            result = analyzeGraphMLData(mlFileLocation);
            log.info("Analyzed ML file={}, XLS result={}", mlFileLocation, result);
        } catch (Exception e) {
            log.error("Analyze GraphML Data failed!", e);
            return null;
        }
        return result;
    }

    private String analyzeGraphMLData(final String mlFileLocation) throws SAXException, DocumentException, IOException {
        // Parse ML file
        final SAXReader xmlReader = new SAXReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        final Document document = xmlReader.read(new File(mlFileLocation));

        // XLS data file name
        final String outputFile = mlFileLocation.replace(GRAPH_ML_EXTENSION, EXCEL_EXTENSION);
        try (final var wb = new HSSFWorkbook(); final var fileOut = new FileOutputStream(outputFile)) {
            writeComponents(wb, document);
            writeComponentInstances(wb, document);
            wb.write(fileOut);
        } catch (final Exception e) {
            log.error("Analyze GraphML Data failed!", e);
        }
        return outputFile;
    }

    private void writeComponents(final Workbook wb, final Document document) {
        final Sheet componentsSheet = wb.createSheet("Components");
        Row currentRow = componentsSheet.createRow(0);
        for (int i = 0; i < COMPONENT_SHEET_HEADER.length; i++) {
            currentRow.createCell(i).setCellValue(COMPONENT_SHEET_HEADER[i]);
        }
        int rowNum = 1;
        for (final ComponentRow row : getComponents(document)) {
            currentRow = componentsSheet.createRow(rowNum++);
            currentRow.createCell(0).setCellValue(row.getUniqueId());
            currentRow.createCell(1).setCellValue(row.getType());
            currentRow.createCell(2).setCellValue(row.getName());
            currentRow.createCell(3).setCellValue(row.getToscaResourceName());
            currentRow.createCell(4).setCellValue(row.getResourceType());
            currentRow.createCell(5).setCellValue(row.getVersion());
            currentRow.createCell(6).setCellValue(row.getIsDeleted() != null ? row.getIsDeleted().toString() : "false");
            currentRow.createCell(7).setCellValue(row.getHasNonCalculatedReqCap());
        }
    }

    private void writeComponentInstances(final Workbook wb, final Document document) {
        final Sheet componentsSheet = wb.createSheet("ComponentInstances");
        Row currentRow = componentsSheet.createRow(0);
        for (int i = 0; i < COMPONENT_INSTANCES_SHEET_HEADER.length; i++) {
            currentRow.createCell(i).setCellValue(COMPONENT_INSTANCES_SHEET_HEADER[i]);
        }
        int rowNum = 1;
        for (final ComponentInstanceRow row : getComponentInstances(document)) {
            currentRow = componentsSheet.createRow(rowNum++);
            currentRow.createCell(0).setCellValue(row.getUniqueId());
            currentRow.createCell(1).setCellValue(row.getName());
            currentRow.createCell(2).setCellValue(row.getOriginUid());
            currentRow.createCell(3).setCellValue(row.getOriginType());
            currentRow.createCell(4).setCellValue(row.getContainerUid());
        }
    }

    private List<ComponentRow> getComponents(final Document document) {
        final List<ComponentRow> res = new ArrayList<>();
        final Element root = document.getRootElement();
        final Element graph = (Element) root.elementIterator("graph").next();
        final Iterator<Element> edges = graph.elementIterator("edge");
        final Set<String> componentsHavingReqOrCap = new HashSet<>();
        while (edges.hasNext()) {
            final Element edge = edges.next();
            final Iterator<Element> dataNodes = edge.elementIterator("data");
            while (dataNodes.hasNext()) {
                final Element data = dataNodes.next();
                final String attributeValue = data.attributeValue("key");
                if ("labelE".equals(attributeValue)) {
                    final String edgeLabel = data.getText();
                    if ("REQUIREMENT".equals(edgeLabel) || "CAPABILITY".equals(edgeLabel)) {
                        componentsHavingReqOrCap.add(edge.attributeValue("source"));
                    }
                }
            }
        }
        final Iterator<Element> nodes = graph.elementIterator("node");
        while (nodes.hasNext()) {
            final Element element = nodes.next();
            final Iterator<Element> dataNodes = element.elementIterator("data");
            final ComponentRow componentRow = new ComponentRow();
            boolean isComponent = false;
            while (dataNodes.hasNext()) {
                final Element data = dataNodes.next();
                final String attributeValue = data.attributeValue("key");
                if (StringUtils.isNotEmpty(attributeValue)) {
                    switch (attributeValue) {
                        case "nodeLabel":
                            final String nodeLabel = data.getText();
                            if ("resource".equals(nodeLabel) || "service".equals(nodeLabel)) {
                                isComponent = true;
                                componentRow.setType(nodeLabel);
                                final String componentId = element.attributeValue("id");
                                componentRow.setHasNonCalculatedReqCap(componentsHavingReqOrCap.contains(componentId));
                            }
                            break;
                        case "uid":
                            componentRow.setUniqueId(data.getText());
                            break;
                        case "name":
                            componentRow.setName(data.getText());
                            break;
                        case "toscaResourceName":
                            componentRow.setToscaResourceName(data.getText());
                            break;
                        case "resourceType":
                            componentRow.setResourceType(data.getText());
                            break;
                        case "version":
                            componentRow.setVersion(data.getText());
                            break;
                        case "deleted":
                            componentRow.setIsDeleted(Boolean.parseBoolean(data.getText()));
                            break;
                        default:
                            break;
                    }
                }
            }
            if (isComponent) {
                res.add(componentRow);
            }
        }
        return res;
    }

    private List<ComponentInstanceRow> getComponentInstances(final Document document) {
        final List<ComponentInstanceRow> res = new ArrayList<>();
        final Element root = document.getRootElement();
        final Element graph = (Element) root.elementIterator("graph").next();
        final Iterator<Element> nodes = graph.elementIterator("node");
        while (nodes.hasNext()) {
            final Iterator<Element> dataNodes = nodes.next().elementIterator("data");
            final ComponentInstanceRow componentInstRow = new ComponentInstanceRow();
            boolean isComponentInst = false;
            while (dataNodes.hasNext()) {
                final Element data = dataNodes.next();
                final String attributeValue = data.attributeValue("key");
                if (StringUtils.isNotEmpty(attributeValue)) {
                    switch (attributeValue) {
                        case "nodeLabel":
                            final String nodeLabel = data.getText();
                            if ("resourceInstance".equals(nodeLabel)) {
                                isComponentInst = true;
                            }
                            break;
                        case "uid":
                            componentInstRow.setUniqueId(data.getText());
                            break;
                        case "name":
                            componentInstRow.setName(data.getText());
                            break;
                        case "originType":
                            componentInstRow.setOriginType(data.getText());
                            break;
                        default:
                            break;
                    }
                }
            }
            if (isComponentInst) {
                // Assuming the uid is in standard form of <container>.<origin>.<name>
                final String uniqueId = componentInstRow.getUniqueId();
                if (uniqueId != null) {
                    final String[] split = uniqueId.split("\\.");
                    if (split.length == 3) {
                        componentInstRow.setContainerUid(split[0]);
                        componentInstRow.setOriginUid(split[1]);
                    }
                }
                res.add(componentInstRow);
            }
        }
        return res;
    }
}
