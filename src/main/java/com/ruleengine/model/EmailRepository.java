package com.ruleengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailRecord, Long> {

    Optional<EmailRecord> findByMessageId(String messageId);

    List<EmailRecord> findByAiCategory(String aiCategory);

    List<EmailRecord> findByStatus(String status);
}
