package com.unitekndt.mqnavigator.service;

import com.unitekndt.mqnavigator.dto.IDM;
import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.entity.DM;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.entity.Workspace;
import com.unitekndt.mqnavigator.repository.DMRepository;
import com.unitekndt.mqnavigator.repository.UserRepository;
import com.unitekndt.mqnavigator.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DMService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DMRepository dmRepository;

    @Autowired
    private UserRepository userRepository;

    public IDM entityToDto(DM dm) {
        IUser senderDto = new IUser(
                dm.getSender().getId(),
                dm.getSender().getNickname(),
                dm.getSender().getEmail(),
                new ArrayList<>()
        );

        IUser receiverDto = new IUser(
                dm.getReceiver().getId(),
                dm.getReceiver().getNickname(),
                dm.getReceiver().getEmail(),
                new ArrayList<>()
        );

        return new IDM(
                dm.getId(),
                dm.getSender().getId(),
                senderDto,
                dm.getReceiver().getId(),
                receiverDto,
                dm.getContent(),
                dm.getCreatedAt()
//                // LocalDateTime을 Date로 변환
//                Date createdAtDate = Date.from(dm.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        );
    }

    // 현재 사용자와 특정 사용자가 나눈 DM 목록을 조회
    public List<IDM> getDMs(String workspaceUrl, Long otherUserId, Long currentUserId, int perPage, int page) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. Pageable 객체 생성 (페이지와 페이지 당 항목 수 설정)
        Pageable pageable = PageRequest.of(page - 1, perPage);  // 페이지 번호는 0부터 시작하므로 page - 1

        // 3. DM 목록 조회
        List<DM> dms = dmRepository.findDMsByUsers(workspace.getId(), otherUserId, currentUserId, pageable);

        // 4. DM을 IDM DTO로 변환
        return dms.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    // 읽지 않은 DM 메시지 개수 조회
    public Long countUnreadDMs(String workspaceUrl, Long senderId, Long receiverId, Long afterTimestamp) {
        // Long 타입의 타임스탬프를 LocalDateTime으로 변환
        LocalDateTime afterDateTime = convertTimestampToLocalDateTime(afterTimestamp);

        return dmRepository.countUnreadDMs(workspaceUrl, senderId, receiverId, afterDateTime);
    }

    // 타임스탬프를 LocalDateTime으로 변환하는 유틸리티 메서드
    private LocalDateTime convertTimestampToLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    // DM 저장 로직
    public DM saveDM(String workspaceUrl, Long senderId, Long receiverId, String content) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 송신자와 수신자 조회 및 예외 처리
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 송신자입니다."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 수신자입니다."));

        // 3. DM 메시지 생성 및 저장
        DM dm = new DM();
        dm.setSender(sender);       // 송신자 설정
        dm.setReceiver(receiver);   // 수신자 설정
        dm.setWorkspace(workspace); // 워크스페이스 설정
        dm.setContent(content);     // DM 내용 설정
        dm.setCreatedAt(LocalDateTime.now()); // 생성 시간 설정

        return dmRepository.save(dm); // 데이터베이스에 DM 저장
    }

}