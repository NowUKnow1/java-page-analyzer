package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static hexlet.code.App.getApp;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = getApp();
        app.start();
        baseUrl = "http://localhost:" + app.port();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testResponse() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testHomePage() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Бесплатно проверяйте сайты на SEO пригодность");

    }

    @Test
    void testAddUrl() {

        String testUrl = "https://en.wikipedia.org";

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", testUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");


        //check DB
        Url postedUrl = new QUrl()
                .name.equalTo(testUrl)
                .findOne();

        assertThat(postedUrl).isNotNull();
        assertThat(postedUrl.getName()).isEqualTo(testUrl);

        //Check the URL on the page
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains(testUrl);
        assertThat(content).doesNotContain("East of Eden");

    }


    @Test
    void testNormalizeUrl() {

        String testUrl = "https://ebean.io";
        String endpoint = "/here/we/go/";

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", testUrl + endpoint)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        //check DB
        Url postedUrl = new QUrl()
                .name.equalTo(testUrl)
                .findOne();

        assertThat(postedUrl).isNotNull();
        assertThat(postedUrl.getName()).isEqualTo(testUrl);
    }


    @Test
    void testDuplicationUrl() {

        String testUrl = "https://www.example.com";

        Unirest.post(baseUrl + "/urls")
                .field("url", testUrl)
                .asString();

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", testUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        //check DB
        int count = new QUrl()
                .name.icontains(testUrl)
                .findCount();

        assertThat(count).isEqualTo(1);

    }

    @Test
    void testAddIncorrectUrl() {

        String testUrl = "htttttps:///incorrectUrl";

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", testUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        Url postedUrl = new QUrl()
                .name.equalTo(testUrl)
                .findOne();

        assertThat(postedUrl).isNull();

    }


    @Test
    void testCheckUrl() throws IOException {

        String fakeContent = Files.readString(Paths.get("src/test/resources/mock/mock.html"),
                StandardCharsets.US_ASCII);


        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        MockWebServer server = new MockWebServer();

        String fakePageUrl = server.url("/").toString();

        // Schedule some responses.
        server.enqueue(new MockResponse().setBody(fakeContent));


        HttpResponse<?> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", fakePageUrl)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).contains("/urls");

        Url postedUrl = new QUrl()
                .name.equalTo(fakePageUrl.substring(0, (fakePageUrl.length() - 1)))
                .findOne();

        assertThat(postedUrl).isNotNull();


        HttpResponse checkResponse = Unirest
                .post(baseUrl + "/urls/" + postedUrl.getId() + "/checks")
                .asEmpty();

        assertThat(checkResponse.getStatus()).isEqualTo(302);

        HttpResponse<String> showUrlResponse = Unirest
                .get(baseUrl + "/urls/" + postedUrl.getId())
                .asString();

        assertThat(showUrlResponse.getStatus()).isEqualTo(200);

        UrlCheck check = new QUrlCheck()
                .findList().get(0);

        assertThat(check).isNotNull();
        assertThat(check.getUrl().getId()).isEqualTo(postedUrl.getId());

        assertThat(showUrlResponse.getBody()).contains("Title");
        assertThat(showUrlResponse.getBody()).contains("Description");
        assertThat(showUrlResponse.getBody()).contains("Header");


        // Shut down the server. Instances cannot be reused.
        server.shutdown();

    }



}
