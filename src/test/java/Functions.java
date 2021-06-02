import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.bson.Document;
import org.jfairy.Fairy;
import org.jfairy.producer.person.Person;
import org.jfairy.producer.text.TextProducer;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Functions {
    public static MongoClient mongoClient = new MongoClient("localhost", 27017);
    public static MongoDatabase database = mongoClient.getDatabase("testDb");
    public static Response res;
    public static String accessToken = "fc8f634ccb512397fa305f72bdddbc6cdde71f04ac4f70781330fe827c46cd69";
    public static JSONObject reqBody;


    static Response getRequest(String endpoint) {
        res = RestAssured.get("public-api/" + endpoint);
        System.out.println("Response body:");
        res.then().log().body();
        return res;
    }

    static Document storeResponseDataInDb(String data, String collName) {
        database.getCollection(collName);
        MongoCollection<Document> collection = database.getCollection(collName);
        HashMap<String, Object> list = res.getBody().jsonPath().get(data);
        Document doc = new Document(list);
        collection.insertOne(doc);
        System.out.println("Collection data:");
        FindIterable<Document> cursor = collection.find();
        for (Document document : cursor) {
            System.out.println(document);
        }
        Document iterDoc = collection.find().first();
        return iterDoc;
    }

    static JSONObject generateUserData () {
        Fairy fairy = Fairy.create();
        Person person = fairy.person();
        String str = person.sex().toString().toLowerCase();
        String gender = str.substring(0, 1).toUpperCase() + str.substring(1);
        List<String> list = new ArrayList<>();
        list.add("Active");
        list.add("Inactive");
        Random rand = new Random();
        int status = rand.nextInt(list.size());
        String email = person.email();
        String name = person.fullName();
        reqBody = new JSONObject();
        reqBody.put("name", name);
        reqBody.put("email", email);
        reqBody.put("gender", gender);
        reqBody.put("status", list.get(status));
        return reqBody;
    }

    static JSONObject generatePostData() {
        Fairy fairy = Fairy.create();
        TextProducer text = fairy.textProducer();
        String title = text.word();
        String body = text.sentence();
        reqBody = new JSONObject();
        reqBody.put("title", title);
        reqBody.put("body", body);
        return reqBody;
    }

    static Response delete(String endpoint) {
        RequestSpecification request = RestAssured.given();
        request.header("Accept", "application/json");
        request.header("Content-Type", "application/json");
        request.header("Authorization", "Bearer " + accessToken);
        res = request.delete("public-api/" + endpoint);
        System.out.println("Response body:");
        res.then().log().body();
        return res;
    }

    static Response postRequest(JSONObject body, String endpoint) {
        RequestSpecification request = RestAssured.given();
        request.header("Accept", "application/json");
        request.header("Content-Type", "application/json");
        request.header("Authorization", "Bearer " + accessToken);
        request.body(body);
        res = request.post("public-api/" + endpoint);
        System.out.println("Response body:");
        res.then().log().body();
        return res;
    }

    static Response putRequest(JSONObject body, String endpoint) {
        RequestSpecification request = RestAssured.given();
        request.header("Accept", "application/json");
        request.header("Content-Type", "application/json");
        request.header("Authorization", "Bearer " + accessToken);
        request.body(body);
        res = request.put("/public-api/" + endpoint);
        System.out.println("Response body:");
        res.then().log().body();
        return res;
    }

    static Response patchRequest(JSONObject body, String endpoint) {
        RequestSpecification request = RestAssured.given();
        request.header("Accept", "application/json");
        request.header("Content-Type", "application/json");
        request.header("Authorization", "Bearer " + accessToken);
        request.body(body);
        res = request.patch("/public-api/" + endpoint);
        System.out.println("Response body:");
        res.then().log().body();
        return res;
    }
}
