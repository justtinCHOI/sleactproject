package com.unitekndt.mqnavigator.controller;

import com.unitekndt.mqnavigator.dto.ChatRequest;
import com.unitekndt.mqnavigator.dto.IChat;
import com.unitekndt.mqnavigator.entity.ChannelChat;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.service.ChannelChatService;
import com.unitekndt.mqnavigator.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/chats")
public class ChannelChatController {

    private final ChannelChatService channelChatService;
    private final ChannelService channelService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // Spring WebSocket을 사용한 메시지 전송

    @Autowired
    public ChannelChatController(ChannelChatService channelChatService, ChannelService channelService) {
        this.channelChatService = channelChatService;
        this.channelService = channelService;
    }

    // 채팅 메시지 저장 및 실시간 전송
    @PostMapping("/{workspaceUrl}/channels/{channelName}/chats")
    public ResponseEntity<String> postChat(
            @PathVariable String workspaceUrl,         // 워크스페이스 URL
            @PathVariable String channelName,          // 채널 이름
            @RequestBody ChatRequest request,          // 채팅 내용 (body: { content: string })
            @AuthenticationPrincipal User currentUser  // 현재 로그인한 사용자
    ) {
        // 서비스 계층을 통해 채팅 메시지 저장
        ChannelChat chat = channelChatService.saveChatMessage(workspaceUrl, channelName, request.getContent(), currentUser);

        // 저장된 채팅 메시지를 DTO로 변환하여 전송
        IChat chatDto = channelChatService.entityToDto(chat);

        // WebSocket을 통해 실시간으로 메시지를 전송
        String destination = String.format("/ws/%s/%s", workspaceUrl, channelName);  // WebSocket 주소
        messagingTemplate.convertAndSend(destination, chatDto);  // 채팅 전송

        return ResponseEntity.ok("ok");
    }

    // 이미지 파일 업로드 및 메시지 전송
    @PostMapping("/{workspaceUrl}/channels/{channelName}/images")
    public ResponseEntity<String> uploadImage(
            @PathVariable String workspaceUrl,       // 워크스페이스 URL
            @PathVariable String channelName,        // 채널 이름
            @RequestParam("image") MultipartFile file, // 이미지 파일
            @AuthenticationPrincipal User currentUser // 로그인된 사용자 정보
    ) throws IOException {
        // 이미지 파일을 저장하고 채팅 메시지를 저장
        ChannelChat chat = channelChatService.saveImageChat(workspaceUrl, channelName, file, currentUser);

        // 저장된 채팅 메시지를 DTO로 변환
        IChat chatDto = channelChatService.entityToDto(chat);

        // WebSocket을 통해 실시간으로 메시지를 전송
        String destination = String.format("/ws/%s/%s", workspaceUrl, channelName);
        messagingTemplate.convertAndSend(destination, chatDto);

        return ResponseEntity.ok("ok");
    }

}