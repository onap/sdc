/*
 * Copyright © 2019 iconectiv
 *
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
 */

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree structure of tests.   VTP does not provide an organized
 * tree of tests.  Here we define a tree node with tests and
 * child nodes to represent our tree.
 */
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data()
@EqualsAndHashCode(callSuper=true)
public class TestTreeNode extends VtpNameDescriptionPair {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<VtpTestCase> tests;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TestTreeNode> children;

  public TestTreeNode() {
    super();
  }

  public TestTreeNode(String name, String description) {
    super(name, description);
  }
}
