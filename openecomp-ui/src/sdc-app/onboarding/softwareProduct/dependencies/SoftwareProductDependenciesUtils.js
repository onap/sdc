/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import DirectedGraph from 'nfvo-utils/DirectedGraph.js';

function findCycles(graph, node, id, visited = {}, visitedConnections = {}, recursionStack = {}, connectionsWithCycle = {}) {
	visited[node] = true;
	recursionStack[node] = true;
	if (id) {
		visitedConnections[id] = true;
	}
	for (let edge of graph.getEdges(node)) {
		if (!visited[edge.target]) {
			findCycles(graph, edge.target, edge.id, visited, visitedConnections, recursionStack, connectionsWithCycle);
		} else if (recursionStack[edge.target]) {
			visitedConnections[edge.id] = true;
			for (let connection in visitedConnections) {
				connectionsWithCycle[connection] = true;
			}
		}
	}
	recursionStack[node] = false;
	return {visitedNodes: visited, connectionsWithCycle: connectionsWithCycle};
}

export function checkCyclesAndMarkDependencies(dependenciesList) {
	let overallVisitedNodes = {};
	let overallConnectionsWithCycles = {};

	let g = new DirectedGraph();
	for (let dependency of dependenciesList) {
		if (dependency.sourceId !== null && dependency.targetId !== null) {
			g.addEdge(dependency.sourceId, dependency.targetId, {id: dependency.id});
		}
	}

	for (let node in g.nodes) {
		if (!overallVisitedNodes.node) {
			let {visitedNodes, connectionsWithCycle} = findCycles(g, node, undefined);
			overallVisitedNodes = {...overallVisitedNodes, ...visitedNodes};
			overallConnectionsWithCycles = {...overallConnectionsWithCycles, ...connectionsWithCycle};
		}
	}
	return dependenciesList.map(dependency => (
		{
			...dependency,
			 hasCycle: dependency.sourceId && dependency.targetId ? 
			 	overallConnectionsWithCycles.hasOwnProperty(dependency.id) 
				 : undefined
		}));
}
