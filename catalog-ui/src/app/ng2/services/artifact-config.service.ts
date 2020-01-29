import {Injectable} from '@angular/core';
import {CacheService} from './cache.service';
import {ArtifactType} from "../../utils/constants";

@Injectable()
export class ArtifactConfigService {

  artifactConfigList:Array<object>;

  constructor(private cacheService: CacheService) {
    const uiConfiguration = cacheService.get('UIConfiguration');
    this.artifactConfigList = uiConfiguration.artifact;
  }

  public getConfig() {
    return this.artifactConfigList;
  }

  public findAllBy(artifactType?:ArtifactType, componentType?:string, resourceType?:string):Array<object> {
    return this.artifactConfigList.filter((artifactConfig:any) => {
      let hasCategory = true;
      if (artifactType) {
        hasCategory = artifactConfig.categories && artifactConfig.categories.some(value => value == artifactType);
      }
      let hasComponentType = true;
      if (componentType) {
        hasComponentType = artifactConfig.componentTypes && artifactConfig.componentTypes.some(value => value == componentType);
      }
      let hasResourceType = true;
      //resourceTypes are not restrictive, if it was not configured all resources are accepted.
      if (resourceType && artifactConfig.resourceTypes) {
        hasResourceType = artifactConfig.resourceTypes.some(value => value == resourceType);
      }
      return hasCategory && hasComponentType && hasResourceType;
    });
  }


  public findAllTypeBy(artifactType?:ArtifactType, componentType?:string, resourceType?:string):Array<string> {
    const artifactConfigList = this.findAllBy(artifactType, componentType, resourceType);
    if (artifactConfigList) {
      return artifactConfigList.map((element: any) => {
        return element.type;
      });
    }

    return [];
  }

}
