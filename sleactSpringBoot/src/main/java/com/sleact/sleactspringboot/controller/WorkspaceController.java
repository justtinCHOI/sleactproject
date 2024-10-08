package com.unitekndt.mqnavigator.controller;

import com.unitekndt.mqnavigator.dto.*;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.service.ChannelChatService;
import com.unitekndt.mqnavigator.service.ChannelService;
import com.unitekndt.mqnavigator.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ChannelChatService channelChatService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // Spring WebSocket을 사용한 메시지 전송

    // 현재 로그인한 사용자가 속한 워크스페이스 목록을 조회
    @GetMapping
    public ResponseEntity<List<IWorkspace>> getWorkspacesForUser(@AuthenticationPrincipal User currentUser) {
        // 서비스 계층에서 DTO로 변환된 데이터를 바로 받아옴
        List<IWorkspace> workspaces = workspaceService.getWorkspacesByUserDto(currentUser.getId());
        return ResponseEntity.ok(workspaces);
    }

    // 워크스페이스 생성
    @PostMapping
    @Transactional
    public ResponseEntity<IWorkspace> createWorkspace(
            @RequestBody WorkspaceCreationRequest request,
            @AuthenticationPrincipal User currentUser) {

        // 서비스 계층에서 워크스페이스를 생성하고, DTO로 변환하여 반환
        IWorkspace createdWorkspace = workspaceService.createWorkspaceDto(request, currentUser);
        return ResponseEntity.ok(createdWorkspace);
    }

    // 현재 로그인한 사용자가 속한 워크스페이스의 채널 목록 조회
    @GetMapping("/{workspaceUrl}/channels")
    public ResponseEntity<List<IChannel>> getUserChannels(
            @PathVariable String workspaceUrl,
            @AuthenticationPrincipal User currentUser) {

        // 서비스 계층에서 DTO로 변환된 채널 리스트를 받아옴
        List<IChannel> userChannels = workspaceService.getUserChannelsDto(workspaceUrl, currentUser.getId());
        return ResponseEntity.ok(userChannels);
    }

    // 워크스페이스 멤버 목록 조회
    @GetMapping("/{workspaceUrl}/members")
    public ResponseEntity<List<IUser>> getWorkspaceMembers(
            @PathVariable String workspaceUrl // 워크스페이스 URL을 받아옴
    ) {
        // 서비스 계층에서 멤버 목록 조회
        List<IUser> members = workspaceService.getWorkspaceMembers(workspaceUrl);

        // 멤버 목록을 반환
        return ResponseEntity.ok(members);
    }

    // 워크스페이스 멤버 초대
    @PostMapping("/{workspaceUrl}/members")
    public ResponseEntity<String> inviteMemberToWorkspace(
            @PathVariable String workspaceUrl,         // 워크스페이스 URL
            @RequestBody Map<String, String> requestBody // 요청 본문에서 이메일 가져오기
    ) {
        String email = requestBody.get("email"); // 이메일 추출

        // 워크스페이스에 멤버 초대하는 서비스 호출
        boolean isMemberAdded = workspaceService.inviteMemberToWorkspace(workspaceUrl, email);

        if (isMemberAdded) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("워크스페이스 또는 사용자가 존재하지 않습니다.");
        }
    }

    // 워크스페이스 멤버 제거
    @DeleteMapping("/{workspaceUrl}/members/{memberId}")
    public ResponseEntity<String> removeMemberFromWorkspace(
            @PathVariable String workspaceUrl,  // 워크스페이스 URL을 받아옴
            @PathVariable Long memberId         // 제거할 멤버 ID를 받아옴
    ) {
        boolean isMemberRemoved = workspaceService.removeMemberFromWorkspace(workspaceUrl, memberId);

        if (isMemberRemoved) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("워크스페이스 또는 멤버가 존재하지 않습니다.");
        }
    }

}