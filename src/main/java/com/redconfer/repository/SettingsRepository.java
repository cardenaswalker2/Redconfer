package com.redconfer.repository;

import com.redconfer.model.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends MongoRepository<Settings, String> {
    Optional<Settings> findFirstByOrderByIdAsc();
}
