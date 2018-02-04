<br>
<h1>Welcome!</h1>
This is the documentation for using the BDD testing framework for SDC.<br>
The Modules on the left contains all steps for particalar aress and/or explanations of what they do.<br>
<br><br>
<h3>How to set the server</h3>
<li> Either update the config.json file and set the server
<li> Or set the SERVER environment variable and it will override the configuration file
<h3>How to run with Maven</h3>
<li>"mvn install" will install npm if needed, download all modules and create the documentation under the "docs" folder
<li>"mvn test-and-report" will run all  tests in the features folder and create an HTML report under the "reports" folder
<h3>How to develop tests</h3>
You can open the project in IntelliJ and Webstorm to run and develop scenarios.<br>
<li><b>You will need to install the Cucumber.Js plugin</b> In order to install, go to "Settings/Plugins". If cucumber.js in not on the list, go to "Browse repositories.." and install .
<li>First time only: Right click on feature file and try to run. Now go to "Run/edit configurations" and set the "executable path" to the "node_modules\.bin\cucumber-js.cmd" under your current project.
<li>Now you can run the feature files by right clicking on the file and selecting "Run" from IDEA.<br>
<li>Add to existing scenarios or create new files under the "features" directory for additional tests
<br>
<li>You can also run a specific test from the command line by running "npm run test -- [features/path to file]
<h3>More Information</h3>
<li> More on <a href="https://cucumber.io/docs/reference">Cucumber</a>
<li> More on <a herf="https://github.com/cucumber/cucumber/wiki/Gherkin">Gherkin</a>
<li> More on <a href="https://github.com/cucumber/cucumber-js">Cucumber-js</a>
