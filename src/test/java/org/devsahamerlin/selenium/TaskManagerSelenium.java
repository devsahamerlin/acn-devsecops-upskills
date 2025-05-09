package org.devsahamerlin.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
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

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerSelenium {

    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = System.getProperty("selenium.url", "http://129.151.250.111:8083");

    @BeforeEach
    void setUp() {
        try {
            System.out.println("Attempting Firefox setup...");
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            driver = new FirefoxDriver(options);
        } catch (Exception e) {
            System.out.println("Firefox failed: " + e.getMessage());

            try {
                System.out.println("Attempting manual geckodriver setup...");
                System.setProperty("webdriver.gecko.driver", "/home/ubuntu/geckodriver");
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                driver = new FirefoxDriver(options);
            } catch (Exception e2) {
                System.out.println("Manual geckodriver failed: " + e2.getMessage());

                try {
                    System.out.println("Attempting remote webdriver...");
                    DesiredCapabilities capabilities = new DesiredCapabilities();
                    capabilities.setBrowserName("firefox");
                    driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities);
                } catch (Exception e3) {
                    System.out.println("All browser attempts failed");
                    throw new RuntimeException("Unable to initialize any browser", e3);
                }
            }
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        System.out.println("Running tests against: " + baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            captureScreenshot();
            driver.quit();
        }
    }


    @Test
    void testAddTask() {
        driver.get(baseUrl);

        String taskTitle = "Test Task " + UUID.randomUUID().toString().substring(0, 8);
        String taskDescription = "This is a test task created by Selenium";

        WebElement titleInput = findElementSafely(By.id("title"));
        WebElement descriptionInput = findElementSafely(By.id("description"));
        WebElement submitButton = findElementSafely(By.cssSelector("button[type='submit']"));

        titleInput.sendKeys(taskTitle);
        descriptionInput.sendKeys(taskDescription);
        submitButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-header")));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

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
    }

    private WebElement findElementSafely(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void captureScreenshot() {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String filename = "screenshot-" + System.currentTimeMillis() + ".png";
            Path dest = Path.of("screenshots", filename);
            Files.createDirectories(dest.getParent());
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved to: " + dest);
        } catch (IOException e) {
            System.out.println("Failed to save screenshot: " + e.getMessage());
        } catch (Exception ex) {
            System.out.println("Screenshot failed: " + ex.getMessage());
        }
    }

}
