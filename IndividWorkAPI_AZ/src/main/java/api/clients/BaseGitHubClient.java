package api.clients;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class BaseGitHubClient {

    protected RequestSpecification requestSpec;
    protected final String GITHUB_OWNER = System.getenv().getOrDefault("GITHUB_OWNER", "Zolank");

    // Список для удаления перенесли в BaseClass
    protected List<String> createdRepos = new ArrayList<>();

    @BeforeClass
    public void setup() {
        // Чтение конфигурации из файла (Пункт чек-листа: Configuration file)
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Установите переменную среды GITHUB_TOKEN");
        }

        RestAssured.baseURI = props.getProperty("base.url", "https://api.github.com");

        requestSpec = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    // Очистка перенесена в BaseClass (Пункт чек-листа: Common annotations in BaseClass)
    @AfterClass
    public void tearDown() {
        for (String repoName : createdRepos) {
            System.out.println("Очистка: удаление репозитория " + repoName);
            given()
                    .spec(requestSpec)
                    .when()
                    .delete("/repos/" + GITHUB_OWNER + "/" + repoName);
        }
        createdRepos.clear();
    }

    public <T> T deserialize(Response response, Class<T> clazz) {
        return response.then().extract().as(clazz);
    }
}