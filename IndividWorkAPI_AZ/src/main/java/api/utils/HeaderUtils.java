package api.utils;

import io.restassured.response.Response;
import org.testng.Assert;

public class HeaderUtils {

    public static void validateGitHubHeaders(Response response) {
        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType.contains("application/json"), "Content-Type не является application/json");

        String rateLimit = response.getHeader("X-RateLimit-Limit");
        Assert.assertNotNull(rateLimit, "Отсутствует заголовок X-RateLimit-Limit");
    }
}