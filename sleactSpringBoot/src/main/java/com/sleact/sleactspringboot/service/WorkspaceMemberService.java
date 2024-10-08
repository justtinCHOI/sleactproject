package com.unitekndt.mqnavigator.service;

import com.unitekndt.mqnavigator.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceMemberService {

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

}