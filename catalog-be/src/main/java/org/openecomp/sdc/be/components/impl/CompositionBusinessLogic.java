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

package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

/**
 * This class holds the logic of arranging resource instance on the canvas for imported VF
 * 
 * @author mshitrit
 *
 */
@Component("compositionBusinessLogic")
public class CompositionBusinessLogic {
	@Autowired
	private VFComponentInstanceBusinessLogic vfComponentInstanceBusinessLogic;

	private static final int VFC_CANVAS_ELEMENT_SIZE = 50;
	private static final int CP_CANVAS_ELEMENT_SIZE = 21;
	private static final int CANVAS_WIDTH = 1000;
	private static final int CANVAS_HEIGHT = 700;
	private static final int SPACE_BETWEEN_ELEMENTS = VFC_CANVAS_ELEMENT_SIZE * 4;
	private static final double CP_RADIUS_FACTOR = 0.4;

	enum RelativePosition {
		LEFT, RIGHT, UP, DOWN
	};

	protected Either<List<ComponentInstance>, ResponseFormat> setPositionsForComponentInstances(Resource resource, String userId) {
		Either<List<ComponentInstance>, ResponseFormat> result = Either.left(resource.getComponentInstances());

		boolean isNotAllPositionsCalculated = resource.getComponentInstances() == null
				|| resource.getComponentInstances().stream().filter(p -> (p.getPosX() == null || p.getPosX().isEmpty()) || (p.getPosY() == null || p.getPosY().isEmpty())).findAny().isPresent();

		if (isNotAllPositionsCalculated &&  resource.getComponentInstances() != null) {
			// Arrange Icons In Spiral Pattern
			Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstanceLocations = buildSpiralPatternPositioningForComponentInstances(resource);

			// Set Relative Locations According to Canvas Size
			componentInstanceLocations.entrySet().stream().forEach(e -> setRelativePosition(e));

			// Update in DB
			result = vfComponentInstanceBusinessLogic.updateComponentInstance(ComponentTypeEnum.RESOURCE_PARAM_NAME, resource.getUniqueId(), userId, resource.getComponentInstances(), false, false);

		}
		return result;

	}

	private void setRelativePosition(Entry<ImmutablePair<Double, Double>, ComponentInstance> entry) {
		int xCenter = CANVAS_WIDTH / 2;
		int yCenter = CANVAS_HEIGHT / 2;

		ImmutablePair<Double, Double> matrixPosition = entry.getKey();
		ComponentInstance componentInstance = entry.getValue();
		componentInstance.setPosX(calculateCompositionPosition(xCenter, matrixPosition.getLeft(), componentInstance));
		componentInstance.setPosY(calculateCompositionPosition(yCenter, matrixPosition.getRight(), componentInstance));
	}

	private String calculateCompositionPosition(int center, double relativePosition, ComponentInstance componentInstance) {
		final double topLeftCanvasPosition = center + relativePosition * CompositionBusinessLogic.SPACE_BETWEEN_ELEMENTS;
		double offsetedCanvasPosition;
		switch (componentInstance.getOriginType()) {
		case CP:
			offsetedCanvasPosition = topLeftCanvasPosition - CompositionBusinessLogic.CP_CANVAS_ELEMENT_SIZE / 2;
			break;
		case VL:
			offsetedCanvasPosition = topLeftCanvasPosition - CompositionBusinessLogic.CP_CANVAS_ELEMENT_SIZE / 2;
			break;
		case VF:
			offsetedCanvasPosition = topLeftCanvasPosition - CompositionBusinessLogic.VFC_CANVAS_ELEMENT_SIZE / 2;
			break;
		case VFC:
			offsetedCanvasPosition = topLeftCanvasPosition - CompositionBusinessLogic.VFC_CANVAS_ELEMENT_SIZE / 2;
			break;
		case VFCMT:
			offsetedCanvasPosition = topLeftCanvasPosition - CompositionBusinessLogic.VFC_CANVAS_ELEMENT_SIZE / 2;
			break;
		default:
			offsetedCanvasPosition = topLeftCanvasPosition - CompositionBusinessLogic.VFC_CANVAS_ELEMENT_SIZE / 2;
			break;
		}
		return String.valueOf(offsetedCanvasPosition);
	}

	protected Map<ImmutablePair<Double, Double>, ComponentInstance> buildSpiralPatternPositioningForComponentInstances(Resource resource) {

		Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstanceLocations = new HashMap<>();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		componentInstances.addAll(resource.getComponentInstances());
		Map<ComponentInstance, List<ComponentInstance>> connectededCps = getCpsConnectedToVFC(componentInstances, resource);
		// Remove all cp that are connected from the list
		componentInstances.removeAll(connectededCps.values().stream().flatMap(e -> e.stream()).collect(Collectors.toList()));

		buildSpiralPatternForMajorComponents(componentInstanceLocations, componentInstances);
		buildCirclePatternForCps(componentInstanceLocations, connectededCps);

		return componentInstanceLocations;
	}

	protected void buildCirclePatternForCps(Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstLocations, Map<ComponentInstance, List<ComponentInstance>> connectedCps) {

		for (Entry<ComponentInstance, List<ComponentInstance>> vfcCpList : connectedCps.entrySet()) {
			Entry<ImmutablePair<Double, Double>, ComponentInstance> vfcOfTheCps = componentInstLocations.entrySet().stream().filter(p -> p.getValue().getUniqueId().equals(vfcCpList.getKey().getUniqueId())).findAny().get();
			buildCirclePatternForOneGroupOfCps(vfcOfTheCps.getKey(), vfcCpList.getValue(), componentInstLocations);
		}

	}

	private void buildCirclePatternForOneGroupOfCps(ImmutablePair<Double, Double> vfcLocation, List<ComponentInstance> cpsGroup, Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstLocations) {
		final int numberOfCps = cpsGroup.size();
		double angleBetweenCps = (!cpsGroup.isEmpty()) ? Math.toRadians(360) / numberOfCps : 0;
		double currentAngle = 0;
		Double xCenter = vfcLocation.getLeft();
		Double yCenter = vfcLocation.getRight();
		for (ComponentInstance currCp : cpsGroup) {
			double cpXposition = xCenter + CompositionBusinessLogic.CP_RADIUS_FACTOR * Math.cos(currentAngle);
			double cpYposition = yCenter + CompositionBusinessLogic.CP_RADIUS_FACTOR * Math.sin(currentAngle);
			componentInstLocations.put(new ImmutablePair<Double, Double>(cpXposition, cpYposition), currCp);
			currentAngle += angleBetweenCps;
		}

	}

	private void buildSpiralPatternForMajorComponents(Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstanceLocations, List<ComponentInstance> componentInstances) {
		int elementsCounter = 0;
		ImmutablePair<Double, Double> currPlacement;
		ImmutablePair<Double, Double> prevPlacement = null;
		RelativePosition relationToPrevElement = null;
		for (ComponentInstance curr : componentInstances) {
			elementsCounter++;
			if (elementsCounter == 1) {
				currPlacement = new ImmutablePair<Double, Double>(0D, 0D);
			} else if (elementsCounter == 2) {
				currPlacement = new ImmutablePair<Double, Double>(-1D, 0D);
				relationToPrevElement = RelativePosition.LEFT;
			} else {
				relationToPrevElement = getRelativePositionForCurrentElement(componentInstanceLocations, relationToPrevElement, prevPlacement);
				currPlacement = getRelativeElementLocation(prevPlacement, relationToPrevElement);

			}

			componentInstanceLocations.put(currPlacement, curr);
			prevPlacement = currPlacement;
		}
	}

	protected Map<ComponentInstance, List<ComponentInstance>> getCpsConnectedToVFC(List<ComponentInstance> allComponentInstances, Resource vf) {
		Map<ComponentInstance, List<ComponentInstance>> vfcWithItsCps = new HashMap<>();
		List<RequirementCapabilityRelDef> allRelations = vf.getComponentInstancesRelations();
		for (ComponentInstance curr : allComponentInstances) {
			// Filters Only CPs
			if (curr.getOriginType() == OriginTypeEnum.CP) {
				// List Of elements the CP is connected to
				List<RequirementCapabilityRelDef> connectedToList = allRelations.stream().filter(p -> p.getFromNode().equals(curr.getUniqueId()) || p.getToNode().equals(curr.getUniqueId())).collect(Collectors.toList());
				// Adds Only CPs Which are connected to VFC
				filterCpConnectedToVFC(allComponentInstances, vfcWithItsCps, curr, connectedToList);
			}
		}
		return vfcWithItsCps;
	}

	private void filterCpConnectedToVFC(List<ComponentInstance> allComponentInstances, Map<ComponentInstance, List<ComponentInstance>> vfcWithItsCps, ComponentInstance currCP, List<RequirementCapabilityRelDef> connectedToTheCPList) {
		if (!connectedToTheCPList.isEmpty()) {
			// Set Of Ids Of components Instances which are connected certain CP
			Set<String> mateIds = connectedToTheCPList.stream().map(cpRelation -> cpRelation.getFromNode().equals(currCP.getUniqueId()) ? cpRelation.getToNode() : cpRelation.getFromNode()).collect(Collectors.toSet());

			// Vfc Component instance Connected to the CP
			Optional<ComponentInstance> optionalVfcConnectedToCP = allComponentInstances.stream().
			// All instances connected to CP
					filter(p -> mateIds.contains(p.getUniqueId())).
					// Filter in only VFC connected to the CP
					filter(p -> p.getOriginType() == OriginTypeEnum.VFC).findAny();

			if (optionalVfcConnectedToCP.isPresent()) {
				final ComponentInstance vfcWithCps = optionalVfcConnectedToCP.get();
				if (vfcWithItsCps.containsKey(vfcWithCps)) {
					vfcWithItsCps.get(vfcWithCps).add(currCP);
				} else {
					List<ComponentInstance> cpsList = new ArrayList<>();
					cpsList.add(currCP);
					vfcWithItsCps.put(vfcWithCps, cpsList);
				}
			}
		}
	}

	private RelativePosition getRelativePositionForCurrentElement(Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstanceLocations, RelativePosition relationToPrevElement, ImmutablePair<Double, Double> prevPlacement) {
		switch (relationToPrevElement) {
		case LEFT: {
			boolean isOccupied = isAdjacentElementOccupied(prevPlacement, RelativePosition.UP, componentInstanceLocations);
			relationToPrevElement = isOccupied ? RelativePosition.LEFT : RelativePosition.UP;
			break;
		}
		case RIGHT: {
			boolean isOccupied = isAdjacentElementOccupied(prevPlacement, RelativePosition.DOWN, componentInstanceLocations);
			relationToPrevElement = isOccupied ? RelativePosition.RIGHT : RelativePosition.DOWN;
			break;
		}
		case UP: {
			boolean isOccupied = isAdjacentElementOccupied(prevPlacement, RelativePosition.RIGHT, componentInstanceLocations);
			relationToPrevElement = isOccupied ? RelativePosition.UP : RelativePosition.RIGHT;
			break;
		}
		case DOWN: {
			boolean isOccupied = isAdjacentElementOccupied(prevPlacement, RelativePosition.LEFT, componentInstanceLocations);
			relationToPrevElement = isOccupied ? RelativePosition.DOWN : RelativePosition.LEFT;
			break;
		}
		default: {
			throw new UnsupportedOperationException();
		}
		}
		return relationToPrevElement;
	}

	private boolean isAdjacentElementOccupied(ImmutablePair<Double, Double> currElement, RelativePosition adjacentElementRelationToCurrElement, Map<ImmutablePair<Double, Double>, ComponentInstance> allElements) {

		ImmutablePair<Double, Double> adjacentElementPosition = getRelativeElementLocation(currElement, adjacentElementRelationToCurrElement);
		return allElements.containsKey(adjacentElementPosition);
	}

	private ImmutablePair<Double, Double> getRelativeElementLocation(ImmutablePair<Double, Double> currElement, RelativePosition relativeLocation) {
		ImmutablePair<Double, Double> relativeElementPosition;
		switch (relativeLocation) {

		case LEFT: {
			relativeElementPosition = new ImmutablePair<Double, Double>(currElement.getLeft() - 1, currElement.getRight());
			break;
		}
		case RIGHT: {
			relativeElementPosition = new ImmutablePair<Double, Double>(currElement.getLeft() + 1, currElement.getRight());
			break;
		}
		case UP: {
			relativeElementPosition = new ImmutablePair<Double, Double>(currElement.getLeft(), currElement.getRight() + 1);
			break;
		}
		case DOWN: {
			relativeElementPosition = new ImmutablePair<Double, Double>(currElement.getLeft(), currElement.getRight() - 1);
			break;
		}
		default: {
			throw new UnsupportedOperationException();
		}
		}
		return relativeElementPosition;
	}

}
