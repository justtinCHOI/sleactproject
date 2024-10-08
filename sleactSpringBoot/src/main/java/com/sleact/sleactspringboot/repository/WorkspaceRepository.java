package com.unitekndt.mqnavigator.repository;

import com.unitekndt.mqnavigator.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByUrl(String url);

    // 특정 사용자가 속한 워크스페이스 목록을 조회하는 쿼리
    List<Workspace> findByMembersUserId(Long userId);

    // 사용자가 멤버로 속해있는 워크스페이스 리스트 조회
    List<Workspace> findAllByMembersId(Long userId);

}