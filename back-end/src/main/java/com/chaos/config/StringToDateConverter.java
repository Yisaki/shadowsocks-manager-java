package com.chaos.config;

import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateConverter implements Converter<String, Date> {
    private static final String dateFormat    = "yyyy-MM-dd HH:mm:ss";
    private static final String dateFormat2    = "yyyy-MM-dd";
    @Override
    public Date convert(String s) {
        Date date=null;
        SimpleDateFormat sdf=null;
        if(s.contains(":")){
            sdf=new SimpleDateFormat(dateFormat);
        }else{
            sdf=new SimpleDateFormat(dateFormat2);
        }
        try {
            date=sdf.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
