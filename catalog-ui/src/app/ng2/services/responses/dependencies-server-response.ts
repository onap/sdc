import { state } from '@angular/core';
/**
 * Created by ob0695 on 4/23/2018.
 */
export interface IDependenciesServerResponse {
    icon: string;
    name: string;
    type: string;
    uniqueId: string;
    version: string;
    state: string;
    dependencies: Array<IDependenciesServerResponse>;
    instanceNames: Array<string>;
}