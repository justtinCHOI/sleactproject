package com.unitekndt.mqnavigator.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final MemberRepository memberRepository;
    private final MemberServiceImpl memberServiceImpl;

    //로그인할 떄 작동되는 메서드
    @Override //  username 은 우리에게 id 에 해당한다. UserDetails -> User -> Member
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.getWithRoles(username); // 이메일 을 통해서 Member 정보를 꺼낸다.

        if(member == null){
            throw new UsernameNotFoundException("Not Found");
        }
        return memberServiceImpl.entityToDTO(member);

    }
}
