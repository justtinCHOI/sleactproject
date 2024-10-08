package com.unitekndt.mqnavigator.service;

import com.unitekndt.mqnavigator.dto.IChannel;
import com.unitekndt.mqnavigator.dto.IChat;
import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.entity.Channel;
import com.unitekndt.mqnavigator.entity.ChannelChat;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.entity.Workspace;
import com.unitekndt.mqnavigator.repository.ChannelChatRepository;
import com.unitekndt.mqnavigator.repository.WorkspaceRepository;
import com.unitekndt.mqnavigator.util.CustomFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelChatService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ChannelChatRepository channelChatRepository;

    @Autowired
    private CustomFileUtil customFileUtil;

    public IChat entityToDto(ChannelChat chat) {
        // User 엔티티를 IUser DTO로 변환
        IUser userDto = new IUser(
                chat.getUser().getId(),
                chat.getUser().getNickname(),
                chat.getUser().getEmail(), // 필요에 따라 더 많은 정보 포함 가능
                new ArrayList<>() // Workspaces 필드가 필요하면 채워넣을 수 있음
        );

        // Channel 엔티티를 IChannel DTO로 변환
        IChannel channelDto = new IChannel(
                chat.getChannel().getId(),
                chat.getChannel().getName(),
                chat.getChannel().getIsPrivate(), // 비공개 여부 포함
                chat.getChannel().getWorkspace().getId()
        );

        // 최종적으로 IChat DTO로 변환하여 반환
        return new IChat(
                chat.getId(),
                chat.getUser().getId(),
                userDto, // User 정보 포함
                chat.getContent(),
                chat.getCreatedAt(), // 생성 시간은 LocalDateTime으로 전달
                chat.getChannel().getId(),
                channelDto // Channel 정보 포함
        );
    }

    // 채팅 메시지 저장
    public ChannelChat saveChatMessage(String workspaceUrl, String channelName, String content, User currentUser) {
        // 워크스페이스와 채널 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        Channel channel = workspace.getChannels().stream()
                .filter(c -> c.getName().equals(channelName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 새로운 채팅 메시지 생성 및 저장
        ChannelChat chat = new ChannelChat();
        chat.setUser(currentUser);
        chat.setChannel(channel);
        chat.setContent(content);
        chat.setCreatedAt(LocalDateTime.now());  // 채팅 생성 시간 설정

        return channelChatRepository.save(chat);
    }

    // 이미지 파일 저장 및 채팅 메시지 생성
    public ChannelChat saveImageChat(String workspaceUrl, String channelName, MultipartFile file, User currentUser) throws IOException {
        // 1. 워크스페이스 및 채널 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        Channel channel = workspace.getChannels().stream()
                .filter(c -> c.getName().equals(channelName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 2. CustomFileUtil을 이용하여 파일 저장
        List<MultipartFile> files = new ArrayList<>();
        files.add(file); // 단일 파일이므로 리스트에 추가
        List<String> savedFileNames = customFileUtil.saveFiles(files);

        if (savedFileNames == null || savedFileNames.isEmpty()) {
            throw new RuntimeException("파일 저장 실패");
        }

        String savedFilePath = savedFileNames.get(0); // 저장된 파일 경로

        // 3. 채팅 메시지 생성
        ChannelChat chat = new ChannelChat();
        chat.setUser(currentUser);
        chat.setChannel(channel);
        chat.setContent(savedFilePath); // 이미지 파일 경로를 content로 저장
        chat.setCreatedAt(LocalDateTime.now());

        return channelChatRepository.save(chat); // DB에 채팅 메시지 저장
    }

}
