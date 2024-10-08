package com.unitekndt.mqnavigator.controller;

import com.unitekndt.mqnavigator.dto.ChannelCreationRequest;
import com.unitekndt.mqnavigator.dto.IChannel;
import com.unitekndt.mqnavigator.dto.IChat;
import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.service.ChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    // 워크스페이스 내에서 특정 채널 정보를 가져오는 메서드
    @GetMapping("/{workspaceUrl}/channels/{channelName}")
    public ResponseEntity<IChannel> getChannelInfo(
            @PathVariable String workspaceUrl,         // URL에서 워크스페이스 정보 추출
            @PathVariable String channelName           // URL에서 채널 정보 추출
    ) {
        // 서비스 계층에서 해당 채널 정보 가져오기
        IChannel channelDto = channelService.getChannelInWorkspace(workspaceUrl, channelName);

        return ResponseEntity.ok(channelDto);
    }

    // 특정 워크스페이스의 특정 채널에 속한 채팅 목록을 가져오는 메서드
    @GetMapping("/{workspaceUrl}/channels/{channelName}/chats")
    public ResponseEntity<List<IChat>> getChannelChats(
            @PathVariable String workspaceUrl,      // 워크스페이스 URL
            @PathVariable String channelName,       // 채널 이름
            @RequestParam int perPage,              // 한 페이지당 메시지 개수
            @RequestParam int page                  // 페이지 번호
    ) {
        // 서비스 계층에서 채팅 데이터를 가져와 DTO로 변환
        List<IChat> chats = channelService.getChatsInChannel(workspaceUrl, channelName, perPage, page);

        return ResponseEntity.ok(chats);  // DTO 변환된 채팅 목록을 반환
    }

    // 특정 워크스페이스와 채널에서 안 읽은 채팅 메시지 수를 가져오는 메서드
    @GetMapping("/{workspaceUrl}/channels/{channelName}/unreads")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable String workspaceUrl,       // 워크스페이스 URL
            @PathVariable String channelName,        // 채널 이름
            @RequestParam("after") Long after        // after 파라미터 (Timestamp)
    ) {
        // 서비스 계층에서 안 읽은 채팅 메시지 개수 조회
        Long unreadCount = channelService.getUnreadCount(workspaceUrl, channelName, after);

        return ResponseEntity.ok(unreadCount);
    }

    // 워크스페이스 내부에 채널을 생성하는 메서드
    @PostMapping("/{workspaceUrl}/channels")
    @Transactional
    public ResponseEntity<IChannel> createChannel(
            @PathVariable String workspaceUrl,
            @RequestBody ChannelCreationRequest request,  // 요청에서 채널 이름을 받음
            @AuthenticationPrincipal User currentUser) {  // 현재 로그인된 사용자 정보 주입

        // 서비스 계층에서 워크스페이스 URL과 사용자 ID로 채널을 생성
        IChannel createdChannel = channelService.createChannelInWorkspace(workspaceUrl, request.getName(), currentUser);

        return ResponseEntity.ok(createdChannel);
    }

    // 특정 채널 멤버 목록 조회
    @GetMapping("/{workspaceUrl}/channels/{channelName}/members")
    public ResponseEntity<List<IUser>> getChannelMembers(
            @PathVariable String workspaceUrl,  // 워크스페이스 URL
            @PathVariable String channelName    // 채널 이름
    ) {
        List<IUser> members = channelService.getChannelMembers(workspaceUrl, channelName);

        if (members != null) {
            return ResponseEntity.ok(members);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    // 특정 채널로 멤버 초대
    @PostMapping("/{workspaceUrl}/channels/{channelName}/members")
    public ResponseEntity<String> inviteMemberToChannel(
            @PathVariable String workspaceUrl,      // 워크스페이스 URL
            @PathVariable String channelName,       // 채널 이름
            @RequestBody Map<String, String> requestBody // 요청 본문에서 이메일 가져오기
    ) {
        String email = requestBody.get("email"); // 이메일 추출

        // 채널에 멤버를 초대하는 서비스 호출
        boolean isMemberAdded = channelService.inviteMemberToChannel(workspaceUrl, channelName, email);

        if (isMemberAdded) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("워크스페이스, 채널 또는 사용자가 존재하지 않습니다.");
        }
    }

    // 특정 채널에서 멤버 제거
    @DeleteMapping("/{workspaceUrl}/channels/{channelName}/members/{memberId}")
    public ResponseEntity<String> removeMemberFromChannel(
            @PathVariable String workspaceUrl,     // 워크스페이스 URL
            @PathVariable String channelName,      // 채널 이름
            @PathVariable Long memberId            // 멤버 ID
    ) {
        boolean isMemberRemoved = channelService.removeMemberFromChannel(workspaceUrl, channelName, memberId);

        if (isMemberRemoved) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("워크스페이스, 채널 또는 멤버가 존재하지 않습니다.");
        }
    }

}
