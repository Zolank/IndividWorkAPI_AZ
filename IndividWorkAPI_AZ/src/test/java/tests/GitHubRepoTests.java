package tests;

import api.clients.BaseGitHubClient;
import api.models.RepositoryDTO;
import api.utils.HeaderUtils;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class GitHubRepoTests extends BaseGitHubClient {


    @DataProvider(name = "repoData")
    public Object[][] repoDataProvider() {
        return new Object[][]{
                {"test-repo-lab-auto-1", "First auto generated repo"},
                {"test-repo-lab-auto-2", "Second auto generated repo"}
        };
    }

    @Test(dataProvider = "repoData", priority = 1)
    public void testCreateRepository(String repoName, String description) {
        RepositoryDTO newRepo = RepositoryDTO.builder()
                .name(repoName)
                .description(description)
                .isPrivate(false)
                .build();

        Response response = given()
                .spec(requestSpec)
                .body(newRepo)
                .when()
                .post("/user/repos");

        response.then().statusCode(201);

        RepositoryDTO createdRepoDto = deserialize(response, RepositoryDTO.class);

        Assert.assertEquals(createdRepoDto.getName(), repoName);
        Assert.assertEquals(createdRepoDto.getDescription(), description);

        createdRepos.add(repoName);
    }

    @Test(priority = 2, dependsOnMethods = "testCreateRepository")
    public void testGetRepository() {
        String repoName = createdRepos.get(0); // Берем первый созданный

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/repos/" + GITHUB_OWNER + "/" + repoName);

        response.then().statusCode(200);

        HeaderUtils.validateGitHubHeaders(response);
    }

    @Test(priority = 3, dependsOnMethods = "testCreateRepository")
    public void testUpdateRepository() {
        String repoName = createdRepos.get(0);
        String updatedDescription = "Updated description via Java Automated Test";

        RepositoryDTO updateData = new RepositoryDTO();
        updateData.setDescription(updatedDescription);

        Response response = given()
                .spec(requestSpec)
                .body(updateData)
                .when()
                .patch("/repos/" + GITHUB_OWNER + "/" + repoName);

        response.then().statusCode(200);
        RepositoryDTO updatedRepoDto = deserialize(response, RepositoryDTO.class);
        Assert.assertEquals(updatedRepoDto.getDescription(), updatedDescription);
    }


    @Test(priority = 4, dependsOnMethods = "testCreateRepository")
    public void testCreateDuplicateRepository() {
        String existingRepoName = createdRepos.get(0);
        RepositoryDTO duplicateRepo = new RepositoryDTO();
        duplicateRepo.setName(existingRepoName);

        given()
                .spec(requestSpec)
                .body(duplicateRepo)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(422); // Ожидаем ошибку валидации от GitHub
    }


    @Test(priority = 5, dependsOnMethods = "testCreateRepository")
    public void testDeleteRepository() {
        String repoName = createdRepos.get(0);

        given()
                .spec(requestSpec)
                .when()
                .delete("/repos/" + GITHUB_OWNER + "/" + repoName)
                .then()
                .statusCode(204);

        createdRepos.remove(repoName);
    }

    // Пункт 7.1.5: Негативный тест (доступ к удаленному)
    @Test(priority = 6, dependsOnMethods = "testDeleteRepository")
    public void testGetDeletedRepository() {
        String deletedRepo = "test-repo-lab-auto-1";

        given()
                .spec(requestSpec)
                .when()
                .get("/repos/" + GITHUB_OWNER + "/" + deletedRepo)
                .then()
                .statusCode(404); // Валидация 404 Not Found
    }


    @Test(priority = 7)
    public void testUnauthorizedAccess() {
        given()
                // Передаем специально испорченный токен
                .header("Authorization", "Bearer bad_invalid_token_12345")
                .when()
                .get("/user")
                .then()
                .statusCode(401); // 401 Unauthorized - доступ запрещен
    }

    @Test(priority = 8)
    public void testCreateRepoWithoutName() {
        RepositoryDTO badRepo = new RepositoryDTO();
        badRepo.setDescription("Repository without a name");
        // Заметьте, мы НЕ делаем badRepo.setName(...)

        given()
                .spec(requestSpec)
                .body(badRepo)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(422); // Ошибка: отсутствует обязательное поле
    }

    @Test(priority = 8)
    public void testOptionsMethod() {
        given()
                .spec(requestSpec)
                .when()
                .options("/user/repos")
                .then()
                .statusCode(204); // GitHub API возвращает 204 для метода OPTIONS
    }
}