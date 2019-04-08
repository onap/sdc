import {PathsAndNamesDefinition} from "./paths-and-names";

export class GabConfig {
  constructor(public artifactType: string, public pathsAndNamesDefinitions: PathsAndNamesDefinition[]) {

  }
}