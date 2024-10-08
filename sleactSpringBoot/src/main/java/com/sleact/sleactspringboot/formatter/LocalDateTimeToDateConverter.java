package com.unitekndt.mqnavigator.formatter;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {

    @Override
    public Date convert(LocalDateTime source) {
        // LocalDateTime을 Date로 변환
        return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
    }
}