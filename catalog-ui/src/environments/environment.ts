/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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

// Swapped for environment.prod.ts by AngularCompilerPlugin's hostReplacementPaths — see
// webpack.common.js for why the replacement has to happen in the TypeScript compiler host and
// not via NormalModuleReplacementPlugin.
//
// This must be a real exported const rather than the `__ENV__` DefinePlugin global that
// app.module.ts used before. AOT evaluates @NgModule metadata off the TypeScript AST, which
// happens before DefinePlugin's textual substitution, so `declare const __ENV__` is an
// unresolvable symbol there and the comparison folds to the wrong value with no diagnostic.
// (A `declare const` is still fine inside a function body — only decorator metadata is affected.)
export const environment = {
  production: false
};
