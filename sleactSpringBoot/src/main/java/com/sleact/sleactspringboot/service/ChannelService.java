package com.unitekndt.mqnavigator.service;

import com.unitekndt.mqnavigator.dto.IChannel;
import com.unitekndt.mqnavigator.dto.IChat;
import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.entity.Channel;
import com.unitekndt.mqnavigator.entity.ChannelChat;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.entity.Workspace;
import com.unitekndt.mqnavigator.repository.ChannelChatRepository;
import com.unitekndt.mqnavigator.repository.ChannelRepository;
import com.unitekndt.mqnavigator.repository.UserRepository;
import com.unitekndt.mqnavigator.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelService {
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ChannelChatService channelChatService;
    @Autowired
    private ChannelChatRepository channelChatRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    public IChannel entityToDto(Channel channel) {
        return new IChannel(
                channel.getId(),
                channel.getName(),
                channel.getIsPrivate(),
                channel.getWorkspace().getId()
        );
    }

    // 채널 생성
    public Channel createChannel(Channel channel) {
        return channelRepository.save(channel);
    }

    @Transactional
    public IChannel createChannelInWorkspace(String workspaceUrl, String channelName, User currentUser) {

        // 워크스페이스를 URL로 검색
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 동일한 채널 이름이 존재하는지 확인
        if (workspace.getChannels().stream().anyMatch(c -> c.getName().equals(channelName))) {
            throw new RuntimeException("이미 존재하는 채널 이름입니다.");
        }

        // 채널 생성
        Channel newChannel = new Channel();
        newChannel.setName(channelName);
        newChannel.setWorkspace(workspace);
        newChannel.getMembers().add(currentUser); // 생성된 채널에 현재 사용자 추가

        // 채널 저장
        Channel savedChannel = channelRepository.save(newChannel);

        // DTO로 변환하여 반환
        return entityToDto(savedChannel);
    }

    // 특정 워크스페이스 내의 특정 채널 정보를 가져오는 메서드
    public IChannel getChannelInWorkspace(String workspaceUrl, String channelName) {

        // 워크스페이스 URL을 기반으로 워크스페이스를 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 워크스페이스 내에서 해당 이름을 가진 채널을 찾기
        Channel channel = workspace.getChannels().stream()
                .filter(c -> c.getName().equals(channelName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 채널 엔티티를 IChannel DTO로 변환하여 반환
        return entityToDto(channel);
    }

    // 특정 워크스페이스의 특정 채널에 속한 채팅 메시지 가져오기
    public List<IChat> getChatsInChannel(String workspaceUrl, String channelName, int perPage, int page) {
        // 워크스페이스 URL을 기준으로 워크스페이스를 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 해당 워크스페이스 내에서 채널 이름을 기반으로 채널을 조회
        Channel channel = workspace.getChannels().stream()
                .filter(c -> c.getName().equals(channelName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 채널에 속한 채팅 메시지들을 페이지네이션하여 가져옴
        Pageable pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending());
        List<ChannelChat> chats = channelChatRepository.findByChannel(channel, pageable);

        // 채팅 메시지들을 IChat DTO로 변환
        return chats.stream()
                .map((chat) -> channelChatService.entityToDto(chat))
                .collect(Collectors.toList());
    }

    // 특정 워크스페이스와 채널에서 안 읽은 채팅 메시지 개수 가져오기
    public Long getUnreadCount(String workspaceUrl, String channelName, Long afterTimestamp) {
        // 워크스페이스 URL을 기준으로 워크스페이스를 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 워크스페이스 내에서 채널 이름을 기준으로 채널 조회
        Channel channel = workspace.getChannels().stream()
                .filter(c -> c.getName().equals(channelName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // afterTimestamp를 LocalDateTime으로 변환
        LocalDateTime afterDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(afterTimestamp), ZoneId.systemDefault());

        // 채널 내에서 afterTimestamp 이후에 생성된 채팅 메시지 개수 계산
        return channelChatRepository.countByChannelAndCreatedAtAfter(channel, afterDateTime);
    }

    // 특정 채널 멤버 목록 조회 메서드
    public List<IUser> getChannelMembers(String workspaceUrl, String channelName) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 채널 조회
        Channel channel = channelRepository.findByNameAndWorkspace(channelName, workspace)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 3. 채널 멤버 엔티티를 IUser DTO로 변환하여 반환
        return channel.getMembers().stream()
                .map(userService::entityToDto)
                .collect(Collectors.toList());
    }

    // 특정 채널로 멤버 초대 메서드
    public boolean inviteMemberToChannel(String workspaceUrl, String channelName, String email) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 채널 조회
        Channel channel = channelRepository.findByNameAndWorkspace(channelName, workspace)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 3. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 4. 채널에 사용자 추가
        if (!channel.getMembers().contains(user)) {
            channel.getMembers().add(user);
            channelRepository.save(channel); // 변경 사항 저장
            return true;
        }
        return false;
    }

    // 특정 채널에서 멤버 제거 메서드
    public boolean removeMemberFromChannel(String workspaceUrl, String channelName, Long memberId) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 채널 조회
        Channel channel = channelRepository.findByNameAndWorkspace(channelName, workspace)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채널입니다."));

        // 3. 사용자 조회
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 멤버입니다."));

        // 4. 채널에서 멤버 제거
        if (channel.getMembers().contains(member)) {
            channel.getMembers().remove(member);
            channelRepository.save(channel); // 변경 사항 저장
            return true;
        }

        return false;
    }

}
