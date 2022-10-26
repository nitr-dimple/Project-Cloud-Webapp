package com.neu.dimple.springbootapplication.persistance.documentpersistance;

import com.neu.dimple.springbootapplication.annotation.ReadOnly;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Entity
@Table(name = "documents")
public class DocumentPersistance {

    @ReadOnly
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name="date_created", nullable = false, updatable = false)
    private Date date_created;

    @Column(name = "s3_bucket_path", nullable = false)
    private String s3_bucket_path;

    public DocumentPersistance() {
    }

    public DocumentPersistance(UUID id, UUID userId, String name, Date date_created, String s3_bucket_path) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.date_created = date_created;
        this.s3_bucket_path = s3_bucket_path;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }
}
