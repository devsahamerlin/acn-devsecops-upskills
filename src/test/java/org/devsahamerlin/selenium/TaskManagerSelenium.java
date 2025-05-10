package org.devsahamerlin.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerSelenium {

    private static final Logger LOGGER = Logger.getLogger(TaskManagerSelenium.class.getName());
    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = System.getProperty("selenium.url", "http://129.151.250.111:8083");
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String SCREENSHOTS_DIR = "screenshots";

    @BeforeEach
    void setUp() {
        try {
            LOGGER.log(Level.INFO,"Attempting Firefox setup...");
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            driver = new FirefoxDriver(options);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Firefox failed: " + e.getMessage());

            try {
                LOGGER.log(Level.INFO,"Attempting manual geckodriver setup...");
                System.setProperty("webdriver.gecko.driver", "/home/ubuntu/geckodriver");
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                driver = new FirefoxDriver(options);
            } catch (Exception e2) {
                LOGGER.log(Level.SEVERE,"Manual geckodriver failed: " + e2.getMessage());
            }
        }

        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        driver.manage().window().maximize();

        LOGGER.log(Level.INFO,"Running tests against: " + BASE_URL);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            try {
                captureScreenshot();
            } finally {
                driver.quit();
                LOGGER.info("WebDriver successfully closed");
            }
        }
    }


    @Test
    void testAddTask() {
        driver.get(BASE_URL);
        LOGGER.info("Navigated to task manager application");

        String taskTitle = "Test Task " + UUID.randomUUID().toString().substring(0, 8);
        String taskDescription = "This is a test task created by Selenium";

        WebElement titleInput = findElementSafely(By.id("title"));
        WebElement descriptionInput = findElementSafely(By.id("description"));
        WebElement submitButton = findElementSafely(By.cssSelector("button[type='submit']"));

        titleInput.sendKeys(taskTitle);
        descriptionInput.sendKeys(taskDescription);
        submitButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-header")));
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);

        List<WebElement> taskRows = driver.findElements(By.cssSelector("table tbody tr"));
        boolean taskFound = false;

        for (WebElement row : taskRows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 3) {
                String title = cells.get(1).getText();
                String description = cells.get(2).getText();

                if (title.equals(taskTitle) && description.equals(taskDescription)) {
                    taskFound = true;
                    break;
                }
            }
        }

        assertTrue(taskFound, "Added task was not found in the task list");
        LOGGER.info("Task successfully added and verified");
    }

    private WebElement findElementSafely(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void captureScreenshot() {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String filename = "screenshot-" + System.currentTimeMillis() + ".png";
            Path dest = Path.of(SCREENSHOTS_DIR, filename);
            Files.createDirectories(dest.getParent());
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.log(Level.INFO,"Screenshot saved to: " + dest);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,"Failed to save screenshot: " + e.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,"Screenshot failed: " + ex.getMessage());
        }
    }

}
