import com.codeborne.selenide.WebDriverRunner;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

public class DemoWebShopTest {

    private final String userLogin = "userLogin@gmail.com";
    private final String userPassword = "123456";
    Map<String, String> authorizationCookie;

    String webUrl = "http://demowebshop.tricentis.com";
    Response response;
    String goods;

    @Test
    void demoShopTest(){

        step("get Cookies", () -> {
            authorizationCookie =
                    given()
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .formParam("Email", userLogin)
                            .formParam("Password", userPassword)
                            .when()
                            .post(String.format("%s/login",webUrl))
                            .then()
                            .statusCode(302)
                            .extract()
                            .cookies();
        });

        step("Добавляем товар в корзину и сохраняем количество товара", () -> {
            response =
                    given()
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .cookies(authorizationCookie)
                            .when()
                            .post(String.format("%s/addproducttocart/catalog/31/1/1",webUrl))
                            .then()
                            .statusCode(200)
                            .body("success", is(true))
                            .body("message", is("The product has been added to your <a href=\"/cart\">shopping cart</a>"))
                            .extract().response();

            goods = response.path("updatetopcartsectionhtml");
        });

        step("Check number of goods", () -> {
            open(String.format("%s/Themes/DefaultClean/Content/images/logo.png", webUrl));

            HashMap<String, String> coockies = new HashMap<String, String>(authorizationCookie);
            for (Map.Entry<String, String> entry : coockies.entrySet()) {
                WebDriverRunner.getWebDriver().manage().addCookie(new Cookie(entry.getKey(), entry.getValue()));
            }

            open(webUrl);
            assertThat($(".cart-qty").getText()).isEqualTo(goods);
        });

    }
}
