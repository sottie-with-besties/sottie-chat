package com.sottie.sottiechat.repository;

import com.sottie.sottiechat.domain.LastReadStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastReadStatusRepository extends MongoRepository<LastReadStatus, String> {
}
