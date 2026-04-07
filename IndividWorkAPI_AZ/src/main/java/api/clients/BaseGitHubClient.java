package api.clients;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseGitHubClient {

    protected RequestSpecification requestSpec;
    protected final String GITHUB_OWNER = System.getenv().getOrDefault("GITHUB_OWNER", "Zolank");

    public BaseGitHubClient() {
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Пожалуйста, установите переменную среды GITHUB_TOKEN");
        }

        RestAssured.baseURI = "https://api.github.com";

        requestSpec = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    public <T> T deserialize(Response response, Class<T> clazz) {
        return response.then().extract().as(clazz);
    }
}