package com.stettler.scopa.scopaserver.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features="src/test/resources/features/scopa.feature", plugin = {"pretty" ,
        "json:Folder_Name/cucumber.json"})
public class CucumberRunnerTest {
}
