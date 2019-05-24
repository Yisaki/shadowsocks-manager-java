package com.chaos.util;

public class Math {

    public static  float weightedMean(int[] data,float[] weight){
        float result=0F;
        int dataLength=data.length;
        if(dataLength!=weight.length){
            return 0F;
        }

        float up=0F;
        int down=0;
        for(int i=0;i<dataLength;i++){
            up+=data[i]*weight[i];
            down+=data[i];
        }

        result=up/down;
        return result;
    }


    public static void main(String[] args){
        float result=weightedMean(new int[]{1,1,1},new float[]{0.1F,0.2F,0.7F});
        System.out.println(result);
    }
}
