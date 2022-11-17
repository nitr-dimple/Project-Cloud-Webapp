package com.neu.dimple.springbootapplication.repository.dynamodbrepository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.neu.dimple.springbootapplication.persistance.dynamodbpersistance.UserEmailToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Dimpleben Kanjibhai Patel
 */
@Repository
public class UserEmailTokenRepository {
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    public UserEmailTokenRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public UserEmailToken getOneTimeToken(String token) {
        return dynamoDBMapper.load(UserEmailToken.class, token);
    }

    public UserEmailToken createOneTimeToken(UserEmailToken oneTimeToken) {
        dynamoDBMapper.save(oneTimeToken);
        return oneTimeToken;
    }
}
