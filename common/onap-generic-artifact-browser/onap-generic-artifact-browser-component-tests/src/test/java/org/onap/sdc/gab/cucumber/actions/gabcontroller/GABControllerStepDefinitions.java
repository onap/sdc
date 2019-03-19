package org.onap.sdc.gab.cucumber.actions.gabcontroller;

import static org.junit.Assert.assertEquals;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.onap.sdc.gab.GABService;
import org.onap.sdc.gab.GABServiceImpl;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABQuery.GABQueryType;
import org.onap.sdc.gab.model.GABResults;

public class GABControllerStepDefinitions {

    private GABService gabService;
    private String yaml;
    private List<String> headers;
    private GABResults actualAnswer;

    public GABControllerStepDefinitions() {
        headers = new ArrayList<>();
    }

    @Given("^new GAB service$")
    public void createNewGABService() {
        gabService = new GABServiceImpl();
    }

    @Given("^yaml document path \"([^\"]*)\"$")
    public void prepareYamlFile(String yamlPath) {
        this.yaml = yamlPath;
    }

    @Given("^yaml document content \"([^\"]*)\"$")
    public void prepareYamlContent(String yamlContent) {
        this.yaml = yamlContent;
    }

    @Given("^header to search \"([^\"]*)\"$")
    public void prepareHeader(String header) {
        this.headers.add(header);
    }

    @When("^I ask service for results providing file$")
    public void executeSearchQueryForPath() throws IOException {
        GABQuery gabQuery = new GABQuery(headers, yaml, GABQueryType.PATH);
        actualAnswer = gabService.searchFor(gabQuery);
    }

    @When("^I ask service for results providing content")
    public void executeSearchQueryForContent() throws IOException {
        GABQuery gabQuery = new GABQuery(headers, yaml, GABQueryType.CONTENT);
        actualAnswer = gabService.searchFor(gabQuery);
    }

    @Then("^Service should find (\\d+) results$")
    public void iShouldBeTold(int size) {
        assertEquals(actualAnswer.getRows().size(), size);
    }

}