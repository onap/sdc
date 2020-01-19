import {Requirement} from "../../app/models/requirement";

export const requirementMock = [
    {"name": "dependency", "capability": "tosca.capabilities.Node", "node": "tosca.nodes.Root", "relationship": "tosca.relationships.DependsOn", "minOccurrences": "0", "maxOccurrences": "UNBOUNDED"},
    {"name": "binding", "capability": "tosca.capabilities.network.Bindable", "node": null, "relationship": "tosca.relationships.network.BindsTo", "minOccurrences": "1", "maxOccurrences": "UNBOUNDED"},
    {"name": "link", "capability": "tosca.capabilities.network.Linkable", "node": null, "relationship": "tosca.relationships.network.LinksTo", "minOccurrences": "1", "maxOccurrences": "UNBOUNDED"}
];

export const capabilitiesMock = [
    {"name": "attachment", "type": "tosca.capabilities.Attachment", "validSourceTypes": ["firstSource", "secondSource"], "minOccurrences": "0", "maxOccurrences": "UNBOUNDED"},
    {"name": "binding", "type": "tosca.capabilities.Node", "validSourceTypes": ["1source"], "minOccurrences": "1", "maxOccurrences": "UNBOUNDED"}
];

const requirement1: Requirement = new Requirement();
requirement1.name = "dependency";
requirement1.capability = "tosca.capabilities.Node";
requirement1.node = "tosca.nodes.Root";
requirement1.relationship = "tosca.relationships.DependsOn";
requirement1.minOccurrences = "0";
requirement1.maxOccurrences = "UNBOUNDED";
const requirement2: Requirement = new Requirement();
requirement2.name = "binding";
requirement2.capability = "tosca.capabilities.network.Bindable";
requirement2.node = null;
requirement2.relationship = "tosca.relationships.network.BindsTo";
requirement2.minOccurrences = "1";
requirement2.maxOccurrences = "UNBOUNDED";
export const filterRequirmentsMock: Array<Requirement> = [requirement1, requirement2];


