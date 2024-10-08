package com.unitekndt.mqnavigator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class IChat {
    private Long id;
    private Long userId;
    private IUser user;
    private String content;
    private LocalDateTime createdAt; //formatter 자동 date 변환
    private Long channelId;
    private IChannel channel;
}
