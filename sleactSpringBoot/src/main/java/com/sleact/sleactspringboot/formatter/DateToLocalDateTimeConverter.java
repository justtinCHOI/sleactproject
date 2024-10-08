package com.unitekndt.mqnavigator.formatter;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {

    @Override
    public LocalDateTime convert(Date source) {
        // Date를 LocalDateTime으로 변환
        return LocalDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
    }
}
