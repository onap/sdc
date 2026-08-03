/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import {LinksFactory} from "../models/graph/graph-links/links-factory";
import {NodesFactory} from "../models/graph/nodes/nodes-factory";

/**
 * The 42 `downgradeComponent` registrations that used to live here are gone with ui-router: every
 * one existed so an AngularJS-compiled template (a `.state()` `template:` string, or a view-model
 * HTML file) could host an Angular component. Nothing in the tree compiles AngularJS DOM any more —
 * `index.html` contains only `<app-root>`, and the 171 remaining `.html` files are all Angular
 * component templates resolving Angular selectors directly.
 *
 * The two services below stay: they are injected by string from AngularJS-DI'd classes.
 */

const moduleName: string = 'Sdc.Directives';
const directiveModule: ng.IModule = angular.module(moduleName, []);

directiveModule.service('NodesFactory', NodesFactory);
directiveModule.service('LinksFactory', LinksFactory);
