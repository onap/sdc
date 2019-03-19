/*
 * ============LICENSE_START=======================================================
 * GAB
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.onap.sdc.gab.cucumber.actions.gabcontroller;

import static org.junit.Assert.assertEquals;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.onap.sdc.gab.GABService;
import org.onap.sdc.gab.GABServiceImpl;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABQuery.GABQueryType;
import org.onap.sdc.gab.model.GABResults;

public class GABControllerStepDefinitions {

    private GABService gabService;
    private String yaml;
    private GABQueryType gabQueryType;
    private Set<String> headers;
    private GABResults actualAnswer;

    public GABControllerStepDefinitions() {
        headers = new HashSet<>();
    }

    @Before
    public void createNewGABService() {
        gabService = new GABServiceImpl();
    }

    @Given("^yaml document \"([^\"]*)\" of type \"([^\"]*)\"$")
    public void prepareYamlFile(String yaml, String type) {
        this.yaml = yaml;
        this.gabQueryType = GABQueryType.valueOf(type);
    }

    @Given("^header to search \"([^\"]*)\"$")
    public void prepareHeader(String header) {
        this.headers.add(header);
    }

    @When("^I ask service for results$")
    public void executeSearchQuery() throws IOException {
        GABQuery gabQuery = new GABQuery(headers, yaml, gabQueryType);
        actualAnswer = gabService.searchFor(gabQuery);
    }

    @Then("^Service should find (\\d+) results$")
    public void checkSizeOfTheAnswerEquals(int size) {
        assertEquals(actualAnswer.getRows().size(), size);
    }

}