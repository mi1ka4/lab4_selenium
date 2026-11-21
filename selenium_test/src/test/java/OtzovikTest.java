import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class OtzovikTest {

    WebDriver driver;
    final String URL = "https://otzovik.com";
    String testName = "";

    @RegisterExtension
    AfterTestExecutionCallback screenshotOnFail = context -> {
        if (context.getExecutionException().isPresent()) {
            screenshot("FAILED");
        }
    };

    @BeforeEach
    public void setup(TestInfo info) {
        testName = info.getTestMethod().get().getName();
        driver = new ChromeDriver();
    }

    @AfterEach
    public void quit() {
        if (driver != null) {
            driver.quit(); 
        }
    }

    private void screenshot(String name) {
        try {
            Path dir = Paths.get("screenshots");
            Files.createDirectories(dir);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), dir.resolve(testName + "_" + name + ".png"),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Скриншот не сохранён: " + e.getMessage());
        }
    }

    private void clickWhenReady(By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        wait.until(d -> {
            try {
                d.findElement(locator).click();
                return true;
            } catch (StaleElementReferenceException | ElementClickInterceptedException | NoSuchElementException e) {
                return false;
            }
        });
    }

    @Test
    public void TestOtzovikScenario1() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5)); 

        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("header-search-input")));
        searchInput.sendKeys("яндекс");

        By yandexMetroSuggestion = By.xpath("//div[contains(@class,'prod-popup')]//a[contains(@class,'popup-obj')][contains(.,'Yandex Metro')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(yandexMetroSuggestion));
        screenshot("01_suggestions_shown");
        clickWhenReady(yandexMetroSuggestion, 5);

        wait.until(ExpectedConditions.urlContains("yandex_metro"));
        
        WebElement reviewsCountElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.reviews.tab.active span.counter"))
        );
        
        String countText = reviewsCountElement.getText().replaceAll("\\D+", "");
        System.out.println("Найдено отзывов: " + countText);
        screenshot("02_product_page_reviews");
        
        assertFalse(countText.isEmpty(), "Количество отзывов не найдено");
        assertTrue(Integer.parseInt(countText) > 0, "Количество отзывов равно нулю");
    }

    @Test
    public void TestOtzovikScenario2() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement writeReviewBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.header-postreview-btn"))
        );
        writeReviewBtn.click();

        wait.until(ExpectedConditions.urlContains("postreview.php"));

        WebElement objectInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("tproduct"))
        );
        objectInput.sendKeys("Ботанический сад ВятГУ");

        WebElement botanicGarden = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'prod-popup')]//a[contains(@class,'popup-obj')][contains(.,'Ботанический сад ВятГУ')]")
                )
        );
        screenshot("01_suggestions_shown");
        botanicGarden.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content_title")));
        screenshot("02_review_form_opened");

        WebElement ratingElement = driver.findElement(By.id("rating0"));
        Select ratingSelect = new Select(ratingElement);
        ratingSelect.selectByValue("5"); 

        WebElement titleInput = driver.findElement(By.id("content_title"));
        titleInput.clear();
        titleInput.sendKeys("Отличное место для прогулок");


        WebElement prosInput = driver.findElement(By.name("content_pros"));
        prosInput.sendKeys("Красивая природа, свежий воздух");

        WebElement consInput = driver.findElement(By.name("content_cons"));
        consInput.sendKeys("Много комаров летом");

        screenshot("03_fields_filled");


        WebElement previewBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("previewbtn"))
        );
        previewBtn.click();

        WebElement errorDialog = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'diAlert')]")
                )
        );
        screenshot("04_errors_dialog");
        
        assertTrue(errorDialog.isDisplayed(), "Диалоговое окно с ошибками не отображается");

    }
}