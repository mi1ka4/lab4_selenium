import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import java.util.List;


public class CdsVyatkaTest {

    WebDriver driver;
    final String URL = "https://m.cdsvyatka.com/";
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
        driver.manage().window().maximize();
    }

    private void chooseCityAndRoute(WebDriverWait wait) {
        Select city = new Select(wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("#id_CitySelect"))));
        city.selectByValue("1");

        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#id_MarshSelect option[value='1003']")));
        Select route = new Select(driver.findElement(By.cssSelector("#id_MarshSelect")));
        route.selectByValue("1003");
    }

    private void screenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dir = Paths.get("screenshots");
            Files.createDirectories(dir);
            Files.copy(src.toPath(), dir.resolve(testName + "_" + name + ".png"),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Скриншот не сохранён: " + e.getMessage());
        }
    }

    @Test
    public void cdsvyatkaScenario1BusInfo() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        chooseCityAndRoute(wait);
        screenshot("01_city_and_route_selected");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#id_buttonMap"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".leaflet-container")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("img.leaflet-marker-icon")));
        List<WebElement> stops = driver.findElements(By.cssSelector("img.leaflet-marker-icon"));
        System.out.println("Остановок на карте: " + stops.size());
        screenshot("02_route_on_map");
        assertFalse(stops.isEmpty(), "Маршрут не отображается на карте");

        WebElement bus = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("canvas.leaflet-marker-icon")));
        bus.click();

        WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".leaflet-popup-content")));
        String popupText = popup.getText();
        System.out.println("Характеристики автобуса:\n" + popupText);
        screenshot("03_bus_info");

        assertTrue(popupText.contains("Автобус по маршруту"),
            "Нет заголовка с характеристиками автобуса, текст: " + popupText);
        assertTrue(popupText.contains("№: 3"),
            "Характеристики не для выбранного маршрута, текст: " + popupText);
        assertTrue(popupText.contains("Гос. номер"),
            "Нет гос. номера автобуса, текст: " + popupText);
    }

    @Test
    public void cdsvyatkaScenario2Prediction() {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        chooseCityAndRoute(wait);

        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#id_StopSelect option[value='457']")));
        Select stop = new Select(driver.findElement(By.cssSelector("#id_StopSelect")));
        stop.selectByValue("457");
        screenshot("01_city_route_stop_selected");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#id_buttonPrediction"))).click();

        wait.until(ExpectedConditions.titleContains("Прогноз по остановке"));
        String stopTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div.redTitle"))).getText();
        System.out.println("Заголовок: " + stopTitle);
        assertTrue(stopTitle.contains("457"), "Открылась не та остановка, заголовок: " + stopTitle);

        String minutes = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("table tr:nth-child(2) td.minutes"))).getText().replace('\u00A0', ' ').trim();
        String route = driver.findElement(By.cssSelector("table tr:nth-child(2) td:nth-child(1)"))
            .getText().replace('\u00A0', ' ').trim();
        String busNumber = driver.findElement(By.cssSelector("table tr:nth-child(2) td:nth-child(4)"))
            .getText().replace('\u00A0', ' ').trim();
        System.out.println("Маршрут: " + route);
        System.out.println("Через: " + minutes);
        System.out.println("Гос. номер автобуса: " + busNumber);
        screenshot("02_prediction");

        assertTrue(minutes.matches(".*\\d+.*"), "Не определено время ожидания, значение: '" + minutes + "'");
        assertFalse(busNumber.isEmpty(), "Не определён номер автобуса");
    }

    @AfterEach
    public void quit(){
        driver.quit();
    }

}