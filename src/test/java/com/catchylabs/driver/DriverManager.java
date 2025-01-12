package com.catchylabs.driver;

import com.catchylabs.implementations.ConfigFileReader;
import com.thoughtworks.gauge.ExecutionContext;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.log4j.Log4j;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j
public class DriverManager {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);
    private static DriverManager driverManager;
    private final boolean IS_MAC = (OS.contains("mac"));
    private final boolean IS_UNIX = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    public boolean IS_WINDOWS = (OS.contains("win"));
    protected WebDriver webDriver;
    protected WebDriverWait webWait;
    protected ConfigFileReader configFileReader;
    private boolean IS_CHROME = false;
    private boolean IS_EDGE = false;
    private boolean IS_FIREFOX = false;
    private List<String> tags;


    private DriverManager() {
        configFileReader = ConfigFileReader.getInstance();
    }

    public static DriverManager getInstance() {
        if (driverManager == null) {
            driverManager = new DriverManager();
        }
        log.info("");
        return driverManager;
    }

    private static String getScenarioStatusColor(ExecutionContext context) {
        if (context.getCurrentScenario().getIsFailing()) return "red";
        return "green";
    }

    public WebDriver getDriver() {
        return webDriver;
    }

    public WebDriverWait getWebWait() {
        return webWait;
    }

    public JavascriptExecutor getJSExecutor() {
        return (JavascriptExecutor) webDriver;
    }

    protected void initializeDriver(ExecutionContext context) {
        setDriverEnvironment(context);
        setDriver();
        beforeScenarioLog(context);
    }

    private void setDriver() {
        try {
            if (IS_CHROME) setChromeDriver();
            else if (IS_FIREFOX) setFirefoxDriver();
            else if (IS_EDGE) setEdgeDriver();
            setWebWaitAndDriver();
        } catch (Exception e) {
            Assertions.fail("Testi başlatılamadı." + e.getMessage());
        }

    }

    private void setWebWaitAndDriver() {
        int waitSeconds = configFileReader.getInteger("WaitSeconds");
        int waitMilis = configFileReader.getInteger("WaitMilliseconds");
        int webWaitSeconds = configFileReader.getInteger("WebWaitSeconds");
        String url = configFileReader.getString("url");
        int scriptLoadTime = configFileReader.getInteger("ScriptLoadMinutes");
        int pageLoadTime = configFileReader.getInteger("PageLoadMinutes");
        webDriver.manage().timeouts().scriptTimeout(Duration.ofMinutes(scriptLoadTime));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(pageLoadTime));
        webWait = new WebDriverWait(webDriver, Duration.ofSeconds(webWaitSeconds));
        webWait.withTimeout(Duration.ofSeconds(waitSeconds))
                .pollingEvery(Duration.ofMillis(waitMilis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        webDriver.manage().window().maximize();
        webDriver.get(url);
    }

    public void beforeScenarioLog(ExecutionContext context) {

        log.info("########################################################################");
        log.info("Specification tags => " + tags);
        String scenarioName = context.getCurrentScenario().getName();
        log.info("Scenario name => " + scenarioName);
        log.info("########################################################################");
    }

    public List<String> getTags(ExecutionContext context) {
        return Stream.concat(
                        context.getCurrentScenario().getTags().stream(),
                        context.getCurrentSpecification().getTags().stream())
                .collect(Collectors.toList())
                .stream().map(String::toLowerCase).collect(Collectors.toList()
                );
    }

    public void setDriverEnvironment(ExecutionContext context) {
        tags = getTags(context);
        setDriverType();
    }

    public void setDriverType() {
        if (tags.contains("firefox")) IS_FIREFOX = true;
        else if (tags.contains("edge")) IS_EDGE = true;
        else IS_CHROME = true;
    }


    private void setChromeDriver() {
        log.info(" Driver chrome olarak seçilmiştir.Driver indiriliyor.");
        if (IS_WINDOWS) WebDriverManager.chromedriver().win().setup();
        else if (IS_MAC) WebDriverManager.chromedriver().mac().setup();
        else if (IS_UNIX) WebDriverManager.chromedriver().linux().setup();
        webDriver = new ChromeDriver(getChromeOptions());
    }

    public ChromeOptions getChromeOptions() {
        log.info("Chrome driver ayarlamaları yapılıyor.");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-ssl-errors=yes");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-translate");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        return options;
    }

    private void setFirefoxDriver() {
        log.info(" Driver firefox olarak seçilmiştir.");
        if (IS_WINDOWS) WebDriverManager.firefoxdriver().win().setup();
        else if (IS_MAC) WebDriverManager.firefoxdriver().mac().setup();
        else if (IS_UNIX) WebDriverManager.firefoxdriver().linux().setup();
        webDriver = new FirefoxDriver(getFirefoxOptions());
    }

    public FirefoxOptions getFirefoxOptions() {
        FirefoxProfile profile = new FirefoxProfile();
        FirefoxOptions options = new FirefoxOptions();
        profile.setPreference("browser.download.folderList", 1);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.download.manager.focusWhenStarting", false);
        profile.setPreference("browser.download.useDownloadDir", true);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
        profile.setPreference("browser.download.manager.closeWhenDone", true);
        profile.setPreference("browser.download.manager.showAlertOnComplete", false);
        profile.setPreference("browser.download.manager.useWindow", false);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");
        options.setProfile(profile);
        return options;
    }

    private void setEdgeDriver() {
        if (IS_WINDOWS) WebDriverManager.edgedriver().win().setup();
        else if (IS_MAC) WebDriverManager.edgedriver().mac().setup();
        else if (IS_UNIX) WebDriverManager.edgedriver().linux().setup();
        webDriver = new EdgeDriver(getEdgeOptions());
    }

    public EdgeOptions getEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.setCapability("acceptSslCerts", true);
        options.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, "eager");
        return options;
    }

    public void afterScenario() {
        closeDriver();
    }

    public void closeDriver() {
        if (webDriver != null) webDriver.quit();
    }

    public void beforeEachStep(ExecutionContext context) {
        log.info("Step Name => " + context.getCurrentStep().getText());

    }
}