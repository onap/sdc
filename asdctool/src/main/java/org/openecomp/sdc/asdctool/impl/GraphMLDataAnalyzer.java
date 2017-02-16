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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

public class GraphMLDataAnalyzer {

	private static final String[] COMPONENT_SHEET_HEADER = { "uniqueId", "type", "name", "toscaResourceName",
			"resourceType", "version", "deleted", "hasNonCalculatedReqCap" };
	private static final String[] COMPONENT_INSTANCES_SHEET_HEADER = { "uniqueId", "name", "originUid", "originType",
			"containerUid" };

	public String analyzeGraphMLData(String[] args) {
		String result = null;
		try {
			String mlFileLocation = args[0];
			result = _analyzeGraphMLData(mlFileLocation);
			System.out.println("Analyzed ML file=" + mlFileLocation + ", XLS result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	private String _analyzeGraphMLData(String mlFileLocation) throws Exception {

		// Parse ML file
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(mlFileLocation);
		Document document = (Document) builder.build(xmlFile);

		// XLS data file name
		String outputFile = mlFileLocation.replace(".graphml", ".xls");
		Workbook wb = new HSSFWorkbook();
		FileOutputStream fileOut = new FileOutputStream(outputFile);
		writeComponents(wb, document);
		writeComponentInstances(wb, document);
		wb.write(fileOut);
		fileOut.close();
		return outputFile;
	}

	private void writeComponents(Workbook wb, Document document) {
		Sheet componentsSheet = wb.createSheet("Components");
		Row currentRow = componentsSheet.createRow(0);
		for (int i = 0; i < COMPONENT_SHEET_HEADER.length; i++) {
			currentRow.createCell(i).setCellValue(COMPONENT_SHEET_HEADER[i]);
		}

		List<ComponentRow> components = getComponents(document);
		int rowNum = 1;
		for (ComponentRow row : components) {
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

	private void writeComponentInstances(Workbook wb, Document document) {
		Sheet componentsSheet = wb.createSheet("ComponentInstances");
		Row currentRow = componentsSheet.createRow(0);
		for (int i = 0; i < COMPONENT_INSTANCES_SHEET_HEADER.length; i++) {
			currentRow.createCell(i).setCellValue(COMPONENT_INSTANCES_SHEET_HEADER[i]);
		}
		List<ComponentInstanceRow> components = getComponentInstances(document);
		int rowNum = 1;
		for (ComponentInstanceRow row : components) {
			currentRow = componentsSheet.createRow(rowNum++);
			currentRow.createCell(0).setCellValue(row.getUniqueId());
			currentRow.createCell(1).setCellValue(row.getName());
			currentRow.createCell(2).setCellValue(row.getOriginUid());
			currentRow.createCell(3).setCellValue(row.getOriginType());
			currentRow.createCell(4).setCellValue(row.getContainerUid());
		}
	}

	private List<ComponentRow> getComponents(Document document) {
		List<ComponentRow> res = new ArrayList<>();
		Element root = document.getRootElement();
		ElementFilter filter = new ElementFilter("graph");
		Element graph = root.getDescendants(filter).next();
		filter = new ElementFilter("edge");
		IteratorIterable<Element> edges = graph.getDescendants(filter);
		Set<String> componentsHavingReqOrCap = new HashSet<>();
		filter = new ElementFilter("data");
		for (Element edge : edges) {
			IteratorIterable<Element> dataNodes = edge.getDescendants(filter);
			for (Element data : dataNodes) {
				String attributeValue = data.getAttributeValue("key");
				switch (attributeValue) {
				case "labelE":
					String edgeLabel = data.getText();
					if (edgeLabel.equals("REQUIREMENT") || edgeLabel.equals("CAPABILITY")) {
						componentsHavingReqOrCap.add(edge.getAttributeValue("source"));
					}
					break;
				}
			}
		}

		filter = new ElementFilter("node");
		IteratorIterable<Element> nodes = graph.getDescendants(filter);
		filter = new ElementFilter("data");
		for (Element element : nodes) {
			IteratorIterable<Element> dataNodes = element.getDescendants(filter);
			ComponentRow componentRow = new ComponentRow();
			boolean isComponent = false;
			for (Element data : dataNodes) {
				String attributeValue = data.getAttributeValue("key");
				switch (attributeValue) {
				case "nodeLabel":
					String nodeLabel = data.getText();
					if (nodeLabel.equals("resource") || nodeLabel.equals("service")) {
						isComponent = true;
						componentRow.setType(nodeLabel);
						String componentId = element.getAttributeValue("id");
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
			if (isComponent) {
				res.add(componentRow);
			}
		}
		return res;
	}

	private List<ComponentInstanceRow> getComponentInstances(Document document) {
		List<ComponentInstanceRow> res = new ArrayList<>();
		Element root = document.getRootElement();
		ElementFilter filter = new ElementFilter("graph");
		Element graph = root.getDescendants(filter).next();
		filter = new ElementFilter("node");
		IteratorIterable<Element> nodes = graph.getDescendants(filter);
		filter = new ElementFilter("data");
		for (Element element : nodes) {
			IteratorIterable<Element> dataNodes = element.getDescendants(filter);
			ComponentInstanceRow componentInstRow = new ComponentInstanceRow();
			boolean isComponentInst = false;
			for (Element data : dataNodes) {
				String attributeValue = data.getAttributeValue("key");
				switch (attributeValue) {
				case "nodeLabel":
					String nodeLabel = data.getText();
					if (nodeLabel.equals("resourceInstance")) {
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
			if (isComponentInst) {
				// Assuming the uid is in standard form of
				// <container>.<origin>.<name>
				String uniqueId = componentInstRow.getUniqueId();
				if (uniqueId != null) {
					String[] split = uniqueId.split("\\.");
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

	private class ComponentRow {

		private String uniqueId;
		private String type;
		private String name;
		private String toscaResourceName;
		private String resourceType;
		private String version;
		private Boolean isDeleted;
		private Boolean hasNonCalculatedReqCap;

		public Boolean getHasNonCalculatedReqCap() {
			return hasNonCalculatedReqCap;
		}

		public void setHasNonCalculatedReqCap(Boolean hasNonCalculatedReqCap) {
			this.hasNonCalculatedReqCap = hasNonCalculatedReqCap;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getUniqueId() {
			return uniqueId;
		}

		public void setUniqueId(String uniqueId) {
			this.uniqueId = uniqueId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getToscaResourceName() {
			return toscaResourceName;
		}

		public void setToscaResourceName(String toscaResourceName) {
			this.toscaResourceName = toscaResourceName;
		}

		public String getResourceType() {
			return resourceType;
		}

		public void setResourceType(String resourceType) {
			this.resourceType = resourceType;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public Boolean getIsDeleted() {
			return isDeleted;
		}

		public void setIsDeleted(Boolean deleted) {
			this.isDeleted = deleted;
		}
	}

	private class ComponentInstanceRow {
		private String uniqueId;
		private String name;
		private String originUid;
		private String originType;
		private String containerUid;

		public String getContainerUid() {
			return containerUid;
		}

		public void setContainerUid(String containerUid) {
			this.containerUid = containerUid;
		}

		public String getUniqueId() {
			return uniqueId;
		}

		public void setUniqueId(String uniqueId) {
			this.uniqueId = uniqueId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getOriginUid() {
			return originUid;
		}

		public void setOriginUid(String componentUid) {
			this.originUid = componentUid;
		}

		public String getOriginType() {
			return originType;
		}

		public void setOriginType(String originType) {
			this.originType = originType;
		}
	}
}
