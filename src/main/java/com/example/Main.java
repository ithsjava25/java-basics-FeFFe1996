package com.example;

import com.example.api.ElpriserAPI;

import java.text.DecimalFormat;

import java.text.DecimalFormatSymbols;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String zone = "";
        String date = "";
        if (args.length == 0){
            helpInfo();
        } else{
            for (int i = 0; i < args.length; i++){
                if (args[i].equals("--zone")){
                    zone = args[i+1];
                }
                else if (args[i].equals("--date")){
                    date = args[i+1];
                }
                else if (args[i].equals("--sorted")) {
                    System.out.println("shall return sorted soon");
                }
                else if (args[i].equals("--help")) {
                    helpInfo();
                }
        }


        }
        if (!date.isEmpty() && !zone.isEmpty()) {
            getElpriser(elpriserAPI, date, zone);
        }
        if (date.isEmpty() && !zone.isEmpty()) {
            getElpriser(elpriserAPI, zone);
        }


    }


    private static void getElpriser(ElpriserAPI elpriserAPI, String zone) {
        String date = LocalDate.now().toString();
        int dataLength = getDataLength(elpriserAPI, date, zone);
        String[] getData = new String[dataLength];
        Double[] splitArray = new Double[dataLength];
        String[] dataArray = new String[dataLength];
        DecimalFormat df = new DecimalFormat("##,00");
        for (int i = 0; i < dataLength; i++) {
            getData[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i));

        }

        double value = 0;
        double minValue = 1;
        double maxValue = 0;
        String minTimeStart = "";
        String minTimeEnd = "";
        String maxTimeStart = "";
        String maxTimeEnd = "";
        for (int a = 0; a < 24; a++) { //for loop to add the values of pricesArray
            value += getSekPerKWh(elpriserAPI, date, zone, a);
            if(minValue > getSekPerKWh(elpriserAPI, date, zone, a)){
                minValue = getSekPerKWh(elpriserAPI, date, zone, a);
                minTimeStart = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeStart());
                minTimeEnd = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeEnd());
                minTimeStart = minTimeStart.substring(minTimeStart.indexOf("T")+1, minTimeStart.indexOf("T")+3);
                minTimeEnd =  minTimeEnd.substring(minTimeEnd.indexOf("T")+1, minTimeEnd.indexOf("T")+3);
            } if(maxValue < getSekPerKWh(elpriserAPI, date, zone, a)){
                maxValue = getSekPerKWh(elpriserAPI, date, zone, a);
                maxTimeStart = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeStart());
                maxTimeEnd = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeEnd());
                maxTimeStart = maxTimeStart.substring(maxTimeStart.indexOf("T")+1, maxTimeStart.indexOf("T")+3);
                maxTimeEnd = maxTimeEnd.substring(maxTimeEnd.indexOf("T")+1, maxTimeEnd.indexOf("T")+3);
            }
        }
        value = value / dataLength;
        value = value*100;
        int mean = (int) value;
        minValue *= 100;
        maxValue *= 100;
        System.out.println(" ");
        System.out.println("medelpris " + mean);
        System.out.println("lägsta pris " + minValue + " " + minTimeStart + "-" + minTimeEnd);
        System.out.println("högsta pris " + maxValue + " " + maxTimeStart + "-" + maxTimeEnd);
    }

    private static void getElpriser(ElpriserAPI elpriserAPI, String date, String zone) {
        int dataLength = getDataLength(elpriserAPI, date, zone);
        String[] getData = new String[dataLength];
        Double[] splitArray = new Double[dataLength];
        String[] dataArray = new String[dataLength];
        DecimalFormat df = new DecimalFormat("##,00");
        for (int i = 0; i < dataLength; i++) {
            getData[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i));
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
            if(minValue > getSekPerKWh(elpriserAPI, date, zone, a)){
                minValue = getSekPerKWh(elpriserAPI, date, zone, a);
                minTimeStart = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeStart());
                minTimeEnd = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeEnd());
                minTimeStart = minTimeStart.substring(minTimeStart.indexOf("T")+1, minTimeStart.indexOf("T")+3);
                minTimeEnd =  minTimeEnd.substring(minTimeEnd.indexOf("T")+1, minTimeEnd.indexOf("T")+3);
            } if(maxValue < getSekPerKWh(elpriserAPI, date, zone, a)){
                maxValue = getSekPerKWh(elpriserAPI, date, zone, a);
                maxTimeStart = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeStart());
                maxTimeEnd = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).timeEnd());
                maxTimeStart = maxTimeStart.substring(maxTimeStart.indexOf("T")+1, maxTimeStart.indexOf("T")+3);
                maxTimeEnd = maxTimeEnd.substring(maxTimeEnd.indexOf("T")+1, maxTimeEnd.indexOf("T")+3);
            }
        }
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        value = value / dataLength;
        value = value*100;
        int mean = (int) value;
        minValue *= 100;
        maxValue *= 100;

        System.out.println(" ");
        System.out.println("medelpris " + mean);
        System.out.println("lägsta pris " + minValue + " " + minTimeStart + "-" + minTimeEnd);
        System.out.println("högsta pris " + maxValue + " " + maxTimeStart + "-" + maxTimeEnd);
    }

    private static int getDataLength(ElpriserAPI elpriserAPI, String date, String zone) {
        int dataLength = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size();
        return dataLength;
    }

    private static double getSekPerKWh(ElpriserAPI elpriserAPI, String date, String zone, int a) {
        return elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(a).sekPerKWh();
    }

    public static void helpInfo(){
        System.out.println("usage");
        System.out.println("--zone");
        System.out.println("--date");
        System.out.println("--charging");
        System.out.println("--sorted");
        System.out.println("Zones:");
        System.out.println("SE1");
        System.out.println("SE2");
        System.out.println("SE3");
        System.out.println("SE4");
    }

}











