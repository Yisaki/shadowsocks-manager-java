package com.chaos.util;

import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.lang.Math;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CommonUtil {

    public static  String urlEncodeWithUtf8(String src){
        String result=null;
        try {
            result= URLEncoder.encode(src,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param begin 开始日期
     * @param end 结束日期
     * @return
     */
    public static int getSubDay(Date begin,Date end){
        long gapTime = end.getTime() - begin.getTime();
        int gapDay = Math.round(gapTime / 1000 / 60 / 60 / 24);
        return gapDay;
    }

    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formateDate(Date date){
        return sdf.format(date);
    }

    public static String getMd5(String src) {
        String md5 = DigestUtils.md5DigestAsHex(src.getBytes());
        return md5;
    }


    public static String generateToken(){
        UUID uuid=UUID.randomUUID();
        return uuid.toString().replace("-","");
    }

}
