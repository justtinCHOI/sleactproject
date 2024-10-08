package com.unitekndt.mqnavigator.config;

import com.unitekndt.mqnavigator.formatter.DateToLocalDateTimeConverter;
import com.unitekndt.mqnavigator.formatter.LocalDateFormatter;
import com.unitekndt.mqnavigator.formatter.LocalDateTimeToDateConverter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Log4j2
public class CustomServletConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new LocalDateFormatter());
        // LocalDateTime <-> Date 변환기 추가
        registry.addConverter(new LocalDateTimeToDateConverter());
        registry.addConverter(new DateToLocalDateTimeConverter());
    }

}