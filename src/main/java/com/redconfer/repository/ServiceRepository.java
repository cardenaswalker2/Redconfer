package com.redconfer.repository;

import com.redconfer.model.Service;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends MongoRepository<Service, String> {
    Optional<Service> findBySlug(String slug);
    List<Service> findByCategory(String category);
    List<Service> findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc();
    List<Service> findByActiveTrueOrderByDisplayOrderAsc();
    List<Service> findAllByOrderByDisplayOrderAsc();
}
