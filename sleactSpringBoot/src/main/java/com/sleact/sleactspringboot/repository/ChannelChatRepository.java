package com.unitekndt.mqnavigator.repository;

import com.unitekndt.mqnavigator.entity.Channel;
import com.unitekndt.mqnavigator.entity.ChannelChat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChannelChatRepository extends JpaRepository<ChannelChat, Long> {
    List<ChannelChat> findByChannel(Channel channel);
    // 특정 채널에 속한 채팅 메시지들을 페이지네이션하여 가져옴
    List<ChannelChat> findByChannel(Channel channel, Pageable pageable);

    // 특정 채널에서 afterDateTime 이후에 생성된 채팅 메시지의 개수 조회
    Long countByChannelAndCreatedAtAfter(Channel channel, LocalDateTime afterDateTime);
}
