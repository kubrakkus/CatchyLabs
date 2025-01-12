package com.catchylabs.steps;

import com.catchylabs.driver.DriverManager;
import com.catchylabs.implementations.ConfigFileReader;
import com.catchylabs.model.ElementInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j
public class BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(BaseSteps.class);
    private static BaseSteps baseSteps;
    private final ConfigFileReader config = ConfigFileReader.getInstance();
    public static JavascriptExecutor jsExecutor;
    protected static WebDriver webDriver;
    protected static WebDriverWait webWait;
    public Map<String, Object> elementsMap;
    public Map<String, String> dataStorage;
    private final int waitMillis = config.getInteger("WaitMilliseconds");
    private final int waitSeconds = config.getInteger("WaitSeconds");

    private BaseSteps() {
        dataStorage = new HashMap<>();
        if (elementsMap == null) initElementMap(getFileList());
    }

    public static BaseSteps getInstance() {
        DriverManager driverManager = DriverManager.getInstance();
        if (webDriver == null) webDriver = driverManager.getDriver();
        if (webWait == null) webWait = driverManager.getWebWait();
        if (jsExecutor == null) jsExecutor = driverManager.getJSExecutor();
        if (baseSteps == null) baseSteps = new BaseSteps();
        return baseSteps;
    }

    public By getByWithKey(String keyword) {
        try {
            ElementInfo elements = (ElementInfo) elementsMap.get(keyword);
            return getBy(elements);
        } catch (NullPointerException e) {
            String format = String.format("%s ile kayıtlı bir element bulunmamaktadır. Json dosyalarınızı kontrol ediniz.", keyword);
            log.error(format);
            return null;
        }
    }

    public String getLocatorValue(String keyword) {
        return ((ElementInfo) elementsMap.get(keyword)).getLocatorValue();
    }

    public String getLocatorType(String keyword) {
        return ((ElementInfo) elementsMap.get(keyword)).getLocatorType();
    }

    public By getBy(ElementInfo element) {
        String locatorValue = element.getLocatorValue();
        String locatorType = element.getLocatorType();
        switch (locatorType) {
            case "id":
                return By.id(locatorValue);
            case "css":
                return By.cssSelector(locatorValue);
            case "xpath":
                return By.xpath(locatorValue);
            case "class":
                return By.className(locatorValue);
            case "linktext":
                return By.linkText(locatorValue);
            case "name":
                return By.name(locatorValue);
            case "partial":
                return By.partialLinkText(locatorValue);
            case "tagName":
                return By.tagName(locatorValue);
            default:
                log.error("Desteklenen locator tipi girilmediği için test durduruldu.");
                return null;
        }
    }

    public void initElementMap(File[] fileList) {
        Type elementType = new TypeToken<List<ElementInfo>>() {
        }.getType();
        Gson gson = new Gson();
        List<ElementInfo> elementInfoList;
        elementsMap = new ConcurrentHashMap<>();
        for (File file : fileList) {
            try {
                elementInfoList = gson.fromJson(new FileReader(file), elementType);
                elementInfoList.parallelStream().forEach(elementInfo -> elementsMap.put(elementInfo.getKeyword(), elementInfo));
            } catch (FileNotFoundException e) {
                log.warn("{} not found " + e);
            }
        }
    }

    public File[] getFileList() {
        String elementsPath = config.getString("ElementsPath");
        try {
            return new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(elementsPath)).getFile()).listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".json"));
        } catch (Exception e) {
            String msg = "Belirtilen dosya bulunamadı. Dosya yolu = " + elementsPath;
            log.warn(msg);
            log.error(e.getMessage());
            return null;
        }
    }

    public List<WebElement> findElements(String keyword) {
        By by = getByWithKey(keyword);
        List<WebElement> elements = null;
        try {
            elements = webWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
        } catch (TimeoutException | NoSuchElementException e) {
            String format = String.format("Keyword %s locator %s olan element %s saniye boyunca her %s milisaniyede arandı bulunamadı.", keyword, by, waitSeconds, waitMillis);
            log.error(format);
        }
        return elements;
    }

    public List<WebElement> findElements(By by) {

        List<WebElement> elements = null;
        try {
            elements = webWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
        } catch (TimeoutException | NoSuchElementException e) {
            String format = String.format("Locator %s olan element %s saniye boyunca her %s milisaniyede arandı bulunamadı.", by, waitSeconds, waitMillis);
            log.error(format);
        }
        return elements;
    }

    public WebElement findElement(String keyword) {
        WebElement element = null;
        By by = getByWithKey(keyword);
        try {

            element = webWait.until(ExpectedConditions.presenceOfElementLocated(by));
            if (!element.isDisplayed()) moveToElementWithJS(element);
        } catch (TimeoutException | NoSuchElementException e) {
            String format = String.format("Keyword %s locator %s olan element %s saniye boyunca her %s milisaniyede arandı bulunamadı.", keyword, by, waitSeconds, waitMillis);
            log.error(format);
        } catch (InvalidElementStateException e) {
            waitSecondsWithoutLog(1);
            element = webWait.until(ExpectedConditions.presenceOfElementLocated(by));
        }
        return element;
    }

    public WebElement findElement(By by) {
        WebElement element = null;
        try {

            element = webWait.until(ExpectedConditions.presenceOfElementLocated(by));
            if (!element.isDisplayed()) moveToElementWithJS(element);
        } catch (TimeoutException | NoSuchElementException e) {
            String format = String.format("Locator %s olan element %s saniye boyunca her %s milisaniyede arandı bulunamadı.", by, waitSeconds, waitMillis);
            log.error(format);
        }
        return element;
    }

    public void moveToElementWithJS(WebElement element) {
        webWait.until(ExpectedConditions.visibilityOf(element));
        jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
        log.info(element.getText() + " elemente JS ile scroll edildi");
    }

    public void waitSeconds(long second) {
        if (second <= 0) second = 1;
        try {
            Thread.sleep(1000 * second);
            log.info(second + " saniye kadar beklendi.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitSecondsWithoutLog(long second) {
        if (second <= 0) second = 1;
        try {
            Thread.sleep(1000 * second);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isElementVisible(String keyword) {
        String format = String.format("%s elementi görünür", keyword);
        try {
            webDriver.findElement(getByWithKey(keyword));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitMilliseconds(long miliSeconds) {
        if (miliSeconds <= 0) miliSeconds = 100;
        try {
            Thread.sleep(miliSeconds);
            log.info(miliSeconds + " milisaniye kadar beklendi.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitMillisecondsWithoutLog(long miliSeconds) {
        try {
            Thread.sleep(miliSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void click(String keyword) {
        try {
            findElement(keyword).click();
            log.info(keyword + " elementine tıklandı.","cyan");
        } catch (ElementClickInterceptedException | InvalidSelectorException e ) {
            clickWithJS(findElement(keyword));
            log.info(keyword + " elemente JS ile tıklandı.","cyan");
        } catch (StaleElementReferenceException e) {
            findElement(keyword).click();
            log.info(keyword + "  elementine tıklandı.","cyan");
        } catch (ElementNotInteractableException e) {
            findElement(keyword).click();
            log.info(keyword + "  elementine tıklandı.","cyan");
        } catch (Exception e) {
            log.error(keyword + " elementine tıklanamadı." + e.getMessage());
            Assertions.fail(keyword + " elementine tıklanamadı." + e.getMessage());
        }
    }
    public void click(By by) {
        try {
            findElement(by).click();
            log.info(by.toString() + " elementine tıklandı.", "cyan");
        } catch (ElementClickInterceptedException e) {
            WebElement element = findElement(by);
            moveToElement(element);
            element.click();
            log.info(by.toString() + " elemente selenium ile tıklandı.", "cyan");
        } catch (StaleElementReferenceException e) {
            findElement(by).click();
            log.info(by.toString() + "  elementine tıklandı.", "cyan");
        } catch (ElementNotInteractableException e) {
            findElement(by).click();
            log.info(by.toString() + "  elementine tıklandı.", "cyan");
        } catch (InvalidSelectorException e) {
            clickWithJS(findElement(by));
            log.info(by.toString() + "  elementine JS ile tıklandı.", "cyan");
        } catch (Exception e) {
            log.error(by.toString() + " elementine tıklanamadı." + e.getMessage());
        }
    }

    public void click(WebElement element) {
        try {
            element.click();
            log.info(element.getAccessibleName() + " elementine tıklandı.", "cyan");
        } catch (ElementClickInterceptedException e) {
            moveToElementWithJS(element);
            element.click();
            log.info(element + " elementine JS ile tıklandı.", "cyan");
        } catch (StaleElementReferenceException | ElementNotInteractableException e) {
            clickWithJS(element);
            log.info(element + "  elementine tıklandı.", "cyan");
        } catch (InvalidSelectorException e) {
            clickWithJS(element);
            log.info(element + "  elementine JS ile tıklandı.", "cyan");
        } catch (Exception e) {
            log.error(element + " elementine tıklanamadı." + e.getMessage());
        }
    }

    public void clickWithJS(String keyword) {
        jsExecutor.executeScript("arguments[0].click();", findElement(keyword));
        log.info(keyword + " elementine JavaScript ile tıklandı.", "blue");
    }

    public void clickWithJS(WebElement element) {
        jsExecutor.executeScript("arguments[0].click();", element);
        log.info(element.getText() + " elementine JavaScript ile tıklandı.", "blue");

    }
    public void sendKeys(String keyword, String text) {
        String format = String.format("%s elementine %s text değeri girildi.", keyword, text);
        try {
            WebElement element = findElement(keyword);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            log.info(format);
        } catch (StaleElementReferenceException e) {
            WebElement element = findElement(keyword);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            log.info(format);
        } catch (InvalidElementStateException e) {
            waitMillisecondsWithoutLog(250);
            WebElement element = findElement(keyword);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            log.info(format);
        } catch (Exception e) {
            e.printStackTrace();
            format = String.format("%s elementine %s text değeri girilemedi.", keyword, text);
            log.error(format);
        }
    }
    public void sendKeys(By by, String text) {
        String format = String.format("%s elementine %s text değeri girildi.", by, text);
        try {
            WebElement element = findElement(by);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            log.info(format);
        } catch (StaleElementReferenceException e) {
            WebElement element = findElement(by);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            log.info(format);
        } catch (InvalidElementStateException e) {
            waitMillisecondsWithoutLog(250);
            WebElement element = findElement(by);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            log.info(format);
        } catch (Exception e) {
            e.printStackTrace();
            format = String.format("%s elementine %s text değeri girilemedi.", by, text);
            log.error(format);
        }
    }
    public void moveToElement(String key) {
        Actions action = new Actions(webDriver);
        WebElement webElement = findElement(key);
        action.moveToElement(webElement, 10, 10);
        action.perform();
        log.info(key+" li elemente scroll edildi.");
    }

    public void moveToElement(WebElement element) {
        Actions action = new Actions(webDriver);
        action.moveToElement(element, 10, 10);
        action.perform();
        log.info(element.getText()+" elemente scroll edildi.");
    }

    public void goToUrl(String url) {
        webDriver.get(url);
        log.info(url + " adresine gidildi.");
    }
    public  void selectByText( String keyword, String optionText) {
        try {
            Select select = new Select(findElement(getByWithKey(keyword)));
            select.selectByVisibleText(optionText);
        } catch (Exception e) {
            log.error("Dropdown seçiminde hata oluştu: " + e.getMessage());
            Assertions.fail("Dropdown seçiminde hata oluştu:" + e.getMessage());
        }
    }
}