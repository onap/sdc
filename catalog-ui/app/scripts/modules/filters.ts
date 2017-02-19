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
/// <reference path="../references"/>
module Sdc {

  let moduleName: string = 'Sdc.Filters';
  let filterModule: ng.IModule = angular.module(moduleName, []);
  filterModule.filter("resourceName", Sdc.Filters.ResourceNameFilter);
  filterModule.filter("graphResourceName", Sdc.Filters.GraphResourceNameFilter);
  filterModule.filter("categoryNameFilter", Sdc.Filters.CategoryNameFilter);
  filterModule.filter("entityFilter", Sdc.Filters.EntityFilter);
  filterModule.filter("truncate", Sdc.Filters.TruncateFilter);
  filterModule.filter("catalogStatusFilter", Sdc.Filters.CatalogStatusFilter);
  filterModule.filter("categoryTypeFilter", Sdc.Filters.CategoryTypeFilter);
  filterModule.filter("stringToDateFilter", Sdc.Filters.StringToDateFilter);
  filterModule.filter("categoryIcon", Sdc.Filters.CategoryIconFilter);
  filterModule.filter("capitalizeFilter", Sdc.Filters.CapitalizeFilter);
  filterModule.filter("underscoreLessFilter", Sdc.Filters.UnderscoreLessFilter);
  filterModule.filter("resourceTypeName", Sdc.Filters.ResourceTypeFilter);
  filterModule.filter("relationName", Sdc.Filters.RelationNameFilter);
  filterModule.filter("trim", Sdc.Filters.TrimFilter);
  filterModule.filter("clearWhiteSpaces", Sdc.Filters.ClearWhiteSpacesFilter);
  filterModule.filter('testsId', Sdc.Filters.TestsIdFilter);

  //Added by Ikram
  filterModule.filter("productCategoryNameFilter", Sdc.Filters.ProductCategoryNameFilter);
}
