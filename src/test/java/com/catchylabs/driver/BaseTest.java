package com.catchylabs.driver;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.BeforeStep;
import com.thoughtworks.gauge.ExecutionContext;
import lombok.extern.log4j.Log4j;


@Log4j
public class BaseTest {
    private final DriverManager driverManager = DriverManager.getInstance();

    @BeforeScenario
    public void setup(ExecutionContext context) {
        driverManager.initializeDriver(context);
    }

    @AfterScenario
    public void tearDown(ExecutionContext context) {
        driverManager.afterScenario();
    }

    @BeforeStep
    public void beforeStep(ExecutionContext context) {
        driverManager.beforeEachStep(context);
    }

}