package com.stettler.scopa.scopaserver.cucumber;


import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasspathResource("features")
public class CucumberRunnerTest {
}
