package com.neu.dimple.springbootapplication.repository.accountrepository;

import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountPersistance, Integer>{
    AccountPersistance findById(UUID uuid);

    AccountPersistance findByUsername(String username);
}
