package com.example;

import com.example.api.ElpriserAPI;

import java.text.DecimalFormat;

import java.time.LocalDate;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String date = LocalDate.now().toString();
        String zone = "SE3";

        int dataLength = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size();
        Double[] splitArray = new Double[dataLength];
        String[] dataArray = new String[dataLength];
        System.out.println(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)));
        DecimalFormat df = new DecimalFormat("##,00");
        for (int i = 0; i < dataLength; i++) {
            System.out.println(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i));
        }
        double value = 0;
        double minValue = 1;
        double maxValue = 0;
        String minTimeStart = "";
        String minTimeEnd = "";
        String maxTimeStart = "";
        String maxTimeEnd = "";
        for (int a = 0; a < dataLength; a++) { //for loop to add the values of pricesArray
            value += elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).sekPerKWh();
            if(minValue > elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).sekPerKWh()){
                minValue = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).sekPerKWh();
                minTimeStart = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeStart());
                minTimeEnd = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeEnd());
                minTimeStart = minTimeStart.substring(minTimeStart.indexOf("T")+1, minTimeStart.indexOf("T")+3);
                minTimeEnd =  minTimeEnd.substring(minTimeEnd.indexOf("T")+1, minTimeEnd.indexOf("T")+3);
            } else if(maxValue < elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).sekPerKWh()){
                maxValue = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).sekPerKWh();
                maxTimeStart = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeStart());
                maxTimeEnd = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeEnd());
                maxTimeStart = maxTimeStart.substring(maxTimeStart.indexOf("T")+1, maxTimeStart.indexOf("T")+3);
                maxTimeEnd = maxTimeEnd.substring(maxTimeEnd.indexOf("T")+1, maxTimeEnd.indexOf("T")+3);
            }
        }
        value = value / dataLength;
        double mean = value*100;
        int meanPrice = (int) mean;
        minValue *= 100;
        maxValue *= 100;
        System.out.println(" ");
        System.out.println("medelpris " + meanPrice);
        System.out.println("lägsta pris " + minValue + " " + minTimeStart + "-" + minTimeEnd);
        System.out.println("högsta pris " + maxValue + " " + maxTimeStart + "-" + maxTimeEnd);
    }


//    public static void getCurrentDay(String zone) {
//        ElpriserAPI elpriserAPI = new ElpriserAPI();
//        LocalDate today = LocalDate.now();
//        LocalDate tomorrow = today.plusDays(1);
//        System.out.println(elpriserAPI.getPriser(today, ElpriserAPI.Prisklass.valueOf(zone)));
//    }
}











