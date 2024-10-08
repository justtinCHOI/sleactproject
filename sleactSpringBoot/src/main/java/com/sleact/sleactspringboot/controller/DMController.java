package com.unitekndt.mqnavigator.controller;

import com.unitekndt.mqnavigator.dto.IDM;
import com.unitekndt.mqnavigator.entity.DM;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.service.DMService;
import com.unitekndt.mqnavigator.util.CustomFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dms")
public class DMController {

    @Autowired
    private DMService dmService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // WebSocket 사용

    @Autowired
    private CustomFileUtil customFileUtil;  // 파일 업로드 유틸리티

    // 특정 워크스페이스에서 현재 사용자와 다른 사용자(ID)가 나눈 DM 목록 조회
    @GetMapping("/{workspaceUrl}/dms/{id}/chats")
    public ResponseEntity<List<IDM>> getDMs(
            @PathVariable String workspaceUrl,       // 워크스페이스 URL
            @PathVariable Long id,                   // DM 상대방의 사용자 ID
            @RequestParam int perPage,               // 한 페이지에 몇 개의 메시지를 가져올지
            @RequestParam int page,                  // 페이지 번호
            @AuthenticationPrincipal User currentUser // 현재 로그인한 사용자 정보
    ) {
        // DMService를 통해 DM 목록을 가져옴
        List<IDM> dmList = dmService.getDMs(workspaceUrl, id, currentUser.getId(), perPage, page);

        return ResponseEntity.ok(dmList);  // DM 목록을 JSON으로 반환
    }

    // 읽지 않은 DM 메시지 개수를 조회
    @GetMapping("/{workspaceUrl}/dms/{id}/unreads")
    public ResponseEntity<Long> getUnreadDMCount(
            @PathVariable String workspaceUrl,          // 워크스페이스 URL
            @PathVariable Long id,                      // 상대방 사용자 ID
            @RequestParam("after") Long afterTimestamp, // 기준 시간 (Timestamp)
            @AuthenticationPrincipal User currentUser   // 현재 로그인한 사용자 정보
    ) {
        // DMService를 통해 읽지 않은 DM 메시지 개수를 조회
        Long unreadCount = dmService.countUnreadDMs(workspaceUrl, id, currentUser.getId(), afterTimestamp);

        return ResponseEntity.ok(unreadCount);  // 읽지 않은 메시지 개수 반환
    }

    // DM 저장 및 WebSocket 이벤트 emit
    @PostMapping("/{workspaceUrl}/dms/{id}/chats")
    public ResponseEntity<String> saveDM(
            @PathVariable String workspaceUrl,         // 워크스페이스 URL
            @PathVariable Long id,                     // 상대방 사용자 ID
            @RequestBody Map<String, String> body,     // 요청의 DM 내용
            @AuthenticationPrincipal User currentUser  // 현재 로그인된 사용자 정보
    ) {
        // 1. DM 저장
        DM savedDM = dmService.saveDM(workspaceUrl, currentUser.getId(), id, body.get("content"));

        // 2. 저장된 DM을 DTO로 변환
        IDM dmDto = dmService.entityToDto(savedDM);

        // 3. WebSocket을 통해 DM 메시지를 상대방에게 전송
        String destination = String.format("/ws/%s/dms/%s", workspaceUrl, id);
        messagingTemplate.convertAndSend(destination, dmDto);  // 소켓 이벤트 전송

        return ResponseEntity.ok("ok");
    }

    // 이미지 DM 저장 및 WebSocket 이벤트 emit
    @PostMapping("/{workspaceUrl}/dms/{id}/images")
    public ResponseEntity<String> saveImageDM(
            @PathVariable String workspaceUrl,         // 워크스페이스 URL
            @PathVariable Long id,                     // DM 상대방 사용자 ID
            @RequestParam("image") List<MultipartFile> images, // 업로드된 이미지 파일들
            @AuthenticationPrincipal User currentUser  // 현재 로그인된 사용자 정보
    ) {
        // 1. 이미지 파일 저장
        List<String> savedFilePaths = customFileUtil.saveFiles(images);

        // 2. 각 이미지 파일 경로를 사용하여 DM 생성 및 저장
        for (String filePath : savedFilePaths) {
            DM savedDM = dmService.saveDM(workspaceUrl, currentUser.getId(), id, filePath);

            // 저장된 DM을 DTO로 변환
            IDM dmDto = dmService.entityToDto(savedDM);

            // WebSocket을 통해 상대방에게 이미지 메시지 전송
            String destination = String.format("/ws/%s/dms/%s", workspaceUrl, id);
            messagingTemplate.convertAndSend(destination, dmDto);  // 소켓 이벤트 전송
        }

        return ResponseEntity.ok("ok");
    }

}
