import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class OldVyatsuTest {

    WebDriver driver;
    final String URL = "https:/old.vyatsu.ru";
    String testName = "";

    @RegisterExtension
    AfterTestExecutionCallback screenshotOnFail = context -> {
        if (context.getExecutionException().isPresent()) {
            screenshot("FAILED");
        }
    };
    
    @BeforeEach
    public void setup(TestInfo info){
        testName = info.getTestMethod().get().getName();
        driver = new ChromeDriver();
    }

    private void screenshot(String name) {
        try {
            Path dir = Paths.get("screenshots");
            Files.createDirectories(dir);
            File src;
            try {
                src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            } catch (UnhandledAlertException e) {
                // открытый alert блокирует скриншот; драйвер его закрывает, пробуем ещё раз
                src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            }
            Files.copy(src.toPath(), dir.resolve(testName + "_" + name + ".png"),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Скриншот не сохранён: " + e.getMessage());
        }
    }

    @Test
    public void titleTest(){
        driver.get(URL);
        screenshot("01_page_opened");
        assertEquals(driver.getTitle(), "Официальный сайт ВятГУ");
    }

    @Test
    public void oldvyatsuScenario1(){
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement firstLi = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='jsddm']/li[1]")));

        Actions action = new Actions(driver);
        action.moveToElement(firstLi).perform();
        WebElement targetMenuItem = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='jsddm']/li[1]//ul//li//a[contains(normalize-space(.), 'Образовательные стандарты')]")
            )
        );
        screenshot("01_menu_opened");
        targetMenuItem.click();

        System.out.println("URL = " + driver.getCurrentUrl());
        System.out.println("TITLE = " + driver.getTitle());

        wait.until(ExpectedConditions.titleContains("Образовательные стандарты"));
        screenshot("02_standards_page");
    }

    @Test
    public void oldvyatsuScenario2Search() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));


        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("form.form-line input[name='query']")));
        searchInput.clear();
        searchInput.sendKeys("отпуск");
        screenshot("01_query_typed");

        searchInput.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.urlContains("/search"));

        By resultItems = By.cssSelector("div.column-center > div a");
        wait.until(ExpectedConditions.presenceOfElementLocated(resultItems));

        List<WebElement> results = driver.findElements(resultItems);
        System.out.println("Найдено результатов: " + results.size());
        screenshot("02_search_results");

        assertFalse( results.isEmpty(),"Результаты поиска пусты");
    }

    @Test
    public void oldvyatsuScenario3MailInvalid() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement targetMenuItem = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[.//h3[text()='Студенту']]")
            )
        );
        targetMenuItem.click();
        WebElement mailInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='email-input']")
            )
        );
        screenshot("01_student_page");
        mailInput.clear();
        mailInput.sendKeys("@vgmale.com");
        screenshot("02_email_entered");

        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Подписаться на новости']")
            )
        );
        submitButton.click();

        wait.until(ExpectedConditions.alertIsPresent());

        Alert alert = driver.switchTo().alert();

        String alertText = alert.getText();
        System.out.println("Текст алерта: " + alertText);

        assertTrue(alertText.contains("Неправильно введен e-mail"), 
            "Текст ошибки не совпадает, текст: " + alertText);

        alert.accept(); 
        screenshot("03_after_alert");
    }

    @Test
    public void oldvyatsuScenario3MailValid() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement targetMenuItem = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[.//h3[text()='Студенту']]")
            )
        );
        targetMenuItem.click();
        WebElement mailInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='email-input']")
            )
        );
        screenshot("01_student_page");
        mailInput.clear();
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        mailInput.sendKeys(String.valueOf(randomNum)+"@vgmale.com");
        screenshot("02_email_entered");

        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Подписаться на новости']")
            )
        );
        submitButton.click();

        wait.until(ExpectedConditions.alertIsPresent());

        Alert alert = driver.switchTo().alert();

        String alertText = alert.getText();
        System.out.println("Текст алерта: " + alertText);

        assertTrue(alertText.contains("успешно добавлен в список рассылки новостей"), 
            "Текст ошибки не совпадает, текст: " + alertText);

        alert.accept(); 
        screenshot("03_after_alert");
    }

    @AfterEach 
    public void quit(){
        driver.close();
    }

}