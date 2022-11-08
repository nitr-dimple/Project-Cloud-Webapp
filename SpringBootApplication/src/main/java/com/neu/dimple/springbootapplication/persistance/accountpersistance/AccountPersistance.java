package com.neu.dimple.springbootapplication.persistance.accountpersistance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neu.dimple.springbootapplication.annotation.DateReadOnly;
import com.neu.dimple.springbootapplication.annotation.ReadOnly;
import com.neu.dimple.springbootapplication.annotation.UniqueEmail;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Entity
@Table(name = "account")
public class AccountPersistance {

    @ReadOnly
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    @NotNull(message = "First name shouldn't be null")
    @NotEmpty(message = "First name shouldn't be empty")
    @Column(name = "first_name", nullable = false)
    private String firstname;

    @NotNull(message = "Last name shouldn't be null")
    @NotEmpty(message = "Last name shouldn't be empty")
    @Column(name= "last_name", nullable = false)
    private String lastname;

    @Email
    @NotNull(message = "Username shouldn't be null")
    @NotEmpty(message = "Username shouldn't be empty")
    @Column(name = "username", nullable = false)
    private String username;

    @NotNull(message = "Password shouldn't be null")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name="password", nullable = false)
    private String password;

    @DateReadOnly
    @CreationTimestamp
    @Column(name="account_created", nullable = false, updatable = false)
    private Date account_created;

    @DateReadOnly
    @UpdateTimestamp
    @Column(name="account_update")
    private Date account_updated;
    public AccountPersistance() {
    }

    public AccountPersistance(UUID id, String firstname, String lastname, String username, String password, Date account_created, Date account_updated) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.account_created = account_created;
        this.account_updated = account_updated;
    }

    public AccountPersistance(String firstname, String lastname, String username, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getAccount_created() {
        return account_created;
    }

    public void setAccount_created(Date account_created) {
        this.account_created = account_created;
    }

    public Date getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(Date account_updated) {
        this.account_updated = account_updated;
    }

    @Override
    public String toString(){
        String obj = "{ id: " + this.getId() +
                "\n firstname: " + this.getFirstname() +
                "\n lastname: " + this.getLastname() +
                "\n username: " + this.getUsername() +
                "\n account_created: " + this.getAccount_created() +
                "\n account_updated: " + this.getAccount_updated() + "\n }";
        return obj;
    }
}
