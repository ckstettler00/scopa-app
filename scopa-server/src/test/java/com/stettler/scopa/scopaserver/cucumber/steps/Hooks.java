package com.stettler.scopa.scopaserver.cucumber.steps;

import com.stettler.scopa.scopaserver.cucumber.util.TestContext;
import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void setup() {
        TestContext.context().clear();
    }
}
