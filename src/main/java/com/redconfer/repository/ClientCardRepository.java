package com.redconfer.repository;

import com.redconfer.model.ClientCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientCardRepository extends MongoRepository<ClientCard, String> {
    List<ClientCard> findByTagsContaining(String tag);
}
