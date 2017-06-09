import {TestsIdFilter} from "../filters/tests-id-filter";
import {TrimFilter} from "../filters/trim-filter";
import {ResourceTypeFilter} from "../filters/resource-type-filter";
import {StringToDateFilter} from "../filters/string-to-date-filter";
import {CategoryTypeFilter} from "../filters/category-type-filter";
import {CatalogStatusFilter} from "../filters/catalog-status-filter";
import {TruncateFilter} from "../filters/truncate-filter";
import {EntityFilter} from "../filters/entity-filter";
import {GraphResourceNameFilter} from "../filters/graph-resource-name-filter";
import {ResourceNameFilter} from "../filters/resource-name-filter";
import {ClearWhiteSpacesFilter} from "../filters/clear-whitespaces-filter";

let moduleName:string = 'Sdc.Filters';
let filterModule:ng.IModule = angular.module(moduleName, []);

filterModule.filter("resourceName", ResourceNameFilter);
filterModule.filter("graphResourceName", GraphResourceNameFilter);
filterModule.filter("entityFilter", EntityFilter);
filterModule.filter("truncate", TruncateFilter);
filterModule.filter("catalogStatusFilter", CatalogStatusFilter);
filterModule.filter("categoryTypeFilter", CategoryTypeFilter);
filterModule.filter("stringToDateFilter", StringToDateFilter);
filterModule.filter("resourceTypeName", ResourceTypeFilter);
filterModule.filter("trim", TrimFilter);
filterModule.filter("clearWhiteSpaces", ClearWhiteSpacesFilter);
filterModule.filter('testsId', TestsIdFilter);
