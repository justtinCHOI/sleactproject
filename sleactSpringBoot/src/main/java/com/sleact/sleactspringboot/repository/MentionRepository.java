package com.unitekndt.mqnavigator.repository;

import com.unitekndt.mqnavigator.entity.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentionRepository extends JpaRepository<Mention, Long> {
}