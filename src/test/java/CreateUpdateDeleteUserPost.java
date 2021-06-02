import com.mongodb.client.MongoIterable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CreateUpdateDeleteUserPost {

    @BeforeTest
    public static void setup() {
        RestAssured.baseURI = "https://gorest.co.in/";
    }

    @Test
    public void getPostDetailsAndSoreInDb() {
        Response res = Functions.getRequest("posts");
        Assertions.assertEquals(res.body().jsonPath().get("code").toString(), "200");
        Functions.storeResponseDataInDb("data[0]", "posts");
    }

    @Test
    public void createPost() {
        //        create user
        Response resUser = Functions.postRequest(Functions.generateUserData(), "users");
        String id = resUser.body().jsonPath().get("data.id").toString();
        Response resPost = Functions.postRequest(Functions.generatePostData(), "users/" + id + "/posts");

        Assertions.assertEquals("201", resPost.body().jsonPath().get("code").toString());
        Assertions.assertEquals(Functions.reqBody.get("title"), resPost.body().jsonPath().get("data.title"));
        Assertions.assertEquals(Functions.reqBody.get("body"), resPost.body().jsonPath().get("data.body"));
    }

    @Test
    public void updatePost() {
        //        create user
        Functions.postRequest(Functions.generateUserData(), "users");
        Document data = Functions.storeResponseDataInDb("data", "users");
        String id = data.get("id").toString();
        //        create post
        JSONObject reqBody = Functions.generatePostData();
        Response resPost = Functions.postRequest(reqBody, "users/" + id + "/posts");
        String postId = resPost.body().jsonPath().get("data.id").toString();
        //        update post
        reqBody = Functions.generatePostData();
        Response resPut = Functions.putRequest(reqBody, "posts/" + postId);

        Assertions.assertEquals("200", resPut.body().jsonPath().get("code").toString());
        Assertions.assertNotEquals((String) resPut.body().jsonPath().get("data.title"),
                resPost.body().jsonPath().get("data.title"));
    }

    @Test
    public void deletePost() {
        //        create new user
        Response resUser = Functions.postRequest(Functions.generateUserData(), "users");
        String id = resUser.body().jsonPath().get("data.id").toString();
        JSONObject reqBody = Functions.generatePostData();
        //        create new post
        Response resPost = Functions.postRequest(reqBody, "users/" + id + "/posts");
        String postId = resPost.body().jsonPath().get("data.id").toString();
        Response resDel = Functions.delete("posts/" + postId);
        Assertions.assertEquals("204", resDel.body().jsonPath().get("code").toString());
    }

    @AfterTest
    public static void tearDown() {
        //        drop db
        Functions.database.drop();
        System.out.println("List of databases:");
        MongoIterable<String> list = Functions.mongoClient.listDatabaseNames();
        for (String name : list) {
            System.out.println(name);
        }
    }

}
