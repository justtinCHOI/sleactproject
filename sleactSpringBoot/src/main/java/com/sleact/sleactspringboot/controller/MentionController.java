package com.unitekndt.mqnavigator.controller;

import com.unitekndt.mqnavigator.service.MentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentions")
public class MentionController {

    private final MentionService mentionService;

    @Autowired
    public MentionController(MentionService mentionService) {
        this.mentionService = mentionService;
    }

}