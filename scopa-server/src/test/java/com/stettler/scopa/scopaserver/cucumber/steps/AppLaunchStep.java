package com.stettler.scopa.scopaserver.cucumber.steps;

import com.stettler.scopa.scopaserver.ScopaServerApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@ActiveProfiles("testhelper")
@SpringBootTest(classes = {ScopaServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AppLaunchStep {
}
