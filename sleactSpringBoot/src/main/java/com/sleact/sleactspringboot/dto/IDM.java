package com.unitekndt.mqnavigator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class IDM {
    private Long id;
    private Long senderId;
    private IUser sender;
    private Long receiverId;
    private IUser receiver;
    private String content;
    private LocalDateTime createdAt;//formatter 자동 date 변환
}
