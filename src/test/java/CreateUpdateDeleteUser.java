import com.mongodb.client.MongoIterable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CreateUpdateDeleteUser {

    @BeforeTest
    public static void setup() {
        RestAssured.baseURI = "https://gorest.co.in/";
    }

    @Test
    public void getUserDetailsAndStoreInDb() {
        Response res = Functions.getRequest("users");
        Assertions.assertEquals(res.body().jsonPath().get("code").toString(), "200");
        Functions.storeResponseDataInDb("data[0]", "users");
    }

    @Test
    public void createNewUser() {
        Response res = Functions.postRequest(Functions.generateUserData(), "users");

        Assertions.assertEquals("201", res.body().jsonPath().get("code").toString());
        Assertions.assertEquals(Functions.reqBody.get("email"), res.body().jsonPath().get("data.email"));
        Assertions.assertEquals(Functions.reqBody.get("name"), res.body().jsonPath().get("data.name"));
    }

    @Test
    public void updateUserDetails() {
        //        create user
        JSONObject reqBody = Functions.generateUserData();
        Functions.postRequest(reqBody, "users");
        Document data = Functions.storeResponseDataInDb("data", "users");
        String id = data.get("id").toString();
        //        put request
        reqBody = Functions.generateUserData();
        Response resPut = Functions.putRequest(reqBody, "users/" + id);

        Assertions.assertEquals("200", resPut.body().jsonPath().get("code").toString());
        Assertions.assertEquals(id, resPut.getBody().jsonPath().get("data.id").toString());
        Assertions.assertNotEquals(resPut.body().jsonPath().get("data.email"), data.get("email"));

        //        patch request
        reqBody = Functions.generateUserData();
        Response resPatch = Functions.patchRequest(reqBody, "users/" + id);

        Assertions.assertEquals("200", resPatch.getBody().jsonPath().get("code").toString());
        Assertions.assertNotEquals((String) resPatch.body().jsonPath().get("data.email"),
                resPut.body().jsonPath().get("data.email"));
    }

    @Test
    public void deleteUser() {
        //        create new user
        Functions.postRequest(Functions.generateUserData(), "users");
        Document data = Functions.storeResponseDataInDb("data", "users");
        String id = data.get("id").toString();
        //        delete user details
        Response resDel = Functions.delete("users/" + id);
        Assertions.assertEquals("204", resDel.body().jsonPath().get("code").toString());
        //        verify that user is deleted
        Response resGet = Functions.getRequest("users/" + id);
        Assertions.assertEquals(resGet.body().jsonPath().get("code").toString(), "404");
        Assertions.assertEquals(resGet.body().jsonPath()
                .get("data.message").toString(), "Resource not found");
    }

    @Test
    public void postRequestsWithInvalidData(){
        //        with invalid authorization
        RequestSpecification request = RestAssured.given();
        request.header("Accept", "application/json");
        request.header("Content-Type", "application/json");
        request.header("Authorization", "Bearer ");
        request.body(Functions.generateUserData());
        Response res = request.post("public-api/users");
        res.then().log().body();
        Assertions.assertEquals("401", res.body().jsonPath().get("code").toString());
        Assertions.assertEquals("Authentication failed", res.body().jsonPath().get("data.message"));

        //        with invalid body
        JSONObject reqBody = Functions.generateUserData();
        reqBody.remove("email");
        Response res1 = Functions.postRequest(reqBody, "users");
        Assertions.assertEquals("422", res1.body().jsonPath().get("code").toString());
        Assertions.assertEquals("{field=email, message=can't be blank}",
                res1.body().jsonPath().get("data[0]").toString());
    }

    @AfterTest
    public static void tearDown() {
        //       drop db
        Functions.database.drop();
        System.out.println("List of databases:");
        MongoIterable<String> list = Functions.mongoClient.listDatabaseNames();
        for (String name : list) {
            System.out.println(name);
        }
    }

}
