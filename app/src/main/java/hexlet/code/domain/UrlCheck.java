package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {

    @Id
    private long id;
    private final int statusCode;
    private final String title;
    private final String h1;
    @Lob
    private final String description;
    @ManyToOne
    @NotNull
    private final Url url;
    @WhenCreated
    private Instant createdAt;


    public UrlCheck(int requestStatusCode, String responseTitle, String responseH1,
                    String responseDescription, Url checkedUrl) {
        this.statusCode = requestStatusCode;
        this.title = responseTitle;
        this.h1 = responseH1;
        this.description = responseDescription;
        this.url = checkedUrl;
    }

    public long getId() {
        return id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Url getUrl() {
        return url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}