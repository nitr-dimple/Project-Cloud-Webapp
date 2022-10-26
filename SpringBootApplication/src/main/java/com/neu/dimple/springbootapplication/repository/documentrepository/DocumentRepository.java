package com.neu.dimple.springbootapplication.repository.documentrepository;

import com.neu.dimple.springbootapplication.persistance.documentpersistance.DocumentPersistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentPersistance, Integer> {

    DocumentPersistance findById(UUID id);

    List<DocumentPersistance> findByUserId(UUID userId);


    DocumentPersistance deleteById(UUID id);
}
