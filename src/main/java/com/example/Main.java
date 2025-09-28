package com.example;
import com.example.api.ElpriserAPI;


import java.text.DecimalFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String zone = "";
        String[] validZones ={"SE1", "SE2", "SE3", "SE4"};
        String date = "";
        boolean zoneIsValid = false;
        boolean sorted = false;
        String charging = "";
        int hours = 0;
        if (args.length == 0){
            helpInfo();
        } else{
            for (int i = 0; i < args.length; i++){
                if (args[i].equals("--zone")){
                    zone = args[i+1];
                    //todo fix if statement
                }  else if (args[i].equals("--date")){
                    date = args[i+1];
                }
                else if (args[i].equals("--sorted")) {
                    sorted = true;

                }else if (args[i].equals("--charging")) {
                    charging = args[i+1];
                    hours = Integer.parseInt(charging.substring(0, 1));
                }
                else if (args[i].equals("--help")) {
                    helpInfo();
                }
        }
            for (int i = 0; i < validZones.length; i++){
                if (validZones[i].equals(zone)){
                    zoneIsValid = true;
                }
            }
        }
        boolean validateDate = false;

        if (zoneIsValid == false){
            System.out.println("zone required or invalid zone");
            System.out.println("your input " + zone);
        }else{
            if (date.length() == 0){
                getElpriser(elpriserAPI, zone, hours, sorted);
            }else if (date.length() > 0) {
                validateDate = validDate(date);
                if (validateDate) {
                    getElpriser(elpriserAPI, date, zone, hours, sorted);
                }
            }
//            if (!date.isEmpty() && !zone.isEmpty()) {
//                getElpriser(elpriserAPI, date, zone, hours, sorted);
//            }
//            if (date.isEmpty() && !zone.isEmpty()) {
//                getElpriser(elpriserAPI, zone, hours, sorted);
//            }
        }


    }
    private static boolean validDate(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            formatter.parse(date);
            return true;
        }
        catch (DateTimeParseException e){
            System.out.println("Invalid date");
            return false;
        }
    }

    private static void getElpriser(ElpriserAPI elpriserAPI, String zone, int hours, boolean sorted) {
        String date = LocalDate.now().toString();
        int dataLength = getDataLength(elpriserAPI, date, zone);
        if (dataLength==0){
            System.out.println("no data");
        }else{
        double[] priceArr = new double[dataLength];
        String[] timeStart =  new String[dataLength];
        String[] timeEnd =  new String[dataLength];
        for (int i = 0; i < dataLength; i++){
            priceArr[i] = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).sekPerKWh();
            timeStart[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeStart());
            timeEnd[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeEnd());
        }
        if (sorted){
            sortedData(dataLength, priceArr, timeStart, timeEnd);
        }
        printValues(priceArr, timeStart, timeEnd, dataLength, hours);
        ChargeWindow(priceArr, timeStart, dataLength, hours);
        }
    }

    private static void getElpriser(ElpriserAPI elpriserAPI, String date, String zone, int hours, boolean sorted) {
        int dataLength = getDataLength(elpriserAPI, date, zone);
        if (dataLength==0){
            System.out.println("no data");
        }else{
            double[] priceArr = new double[dataLength];
            String[] timeStart =  new String[dataLength];
            String[] timeEnd =  new String[dataLength];
            for (int i = 0; i < dataLength; i++){
                priceArr[i] = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).sekPerKWh();
                timeStart[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeStart());
                timeEnd[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeEnd());
            }
            if (sorted){
                sortedData(dataLength, priceArr, timeStart, timeEnd);
            }
            printValues(priceArr, timeStart, timeEnd, dataLength, hours);
            ChargeWindow(priceArr, timeStart, dataLength, hours);
        }
    }

    private static void sortedData(int dataLength, double[] priceArr, String[] timeStart, String[] timeEnd) {
        String[] sortedArr = new String[dataLength];
        String[] revArr = new String[dataLength];
        double[] checkValue = new double[dataLength];
        for (int i = 0; i < dataLength; i++){
            checkValue[i] = priceArr[i];
        }
        Arrays.sort(priceArr);
        int index = 0;
        String startTime = "";
        String endTime = "";
        for (int i = 0; i < dataLength; i++){
            for (int j = 0; j < dataLength; j++){
                if (priceArr[i] == checkValue[j]) {
                    index = j;
                }
                startTime = timeStart[index];
                endTime = timeEnd[index];
                startTime = startTime.substring(startTime.indexOf("T")+1, startTime.indexOf("T")+3);
                endTime =  endTime.substring(endTime.indexOf("T")+1, endTime.indexOf("T")+3);
            }
            double value = priceArr[i];
            value=value*100;
            DecimalFormat df = new DecimalFormat("#0.00");
            String newValue = df.format(value);
            newValue = newValue.replace(".", ",");
            revArr[i] = startTime + "-" +  endTime + " " + (newValue) + " öre";
        }

        int iterator = 0;
        for (int i = revArr.length; i > 0; i--){
            sortedArr[iterator] = revArr[i-1];
            iterator++;
        }

        for (int p = 0; p < dataLength; p++){
            System.out.println(sortedArr[p]);
        }
    }

    private static void printValues(double[] priceArr, String[] timeStart, String[] timeEnd, int dataLength, int hours) {
        double value = 0;
        double minValue = 1;
        double maxValue = 0;
        String minTimeStart = "";
        String minTimeEnd = "";
        String maxTimeStart = "";
        String maxTimeEnd = "";
        for (int a = 0; a < dataLength; a++) { //for loop to add the values of pricesArray
            value += priceArr[a];
            if(minValue > priceArr[a]){
                minValue = priceArr[a];
                minTimeStart = timeStart[a];
                minTimeEnd = timeEnd[a];
                minTimeStart = minTimeStart.substring(minTimeStart.indexOf("T")+1, minTimeStart.indexOf("T")+3);
                minTimeEnd =  minTimeEnd.substring(minTimeEnd.indexOf("T")+1, minTimeEnd.indexOf("T")+3);
            } if(maxValue < priceArr[a]){
                maxValue = priceArr[a];
                maxTimeStart = timeStart[a];;
                maxTimeEnd = timeEnd[a];
                maxTimeStart = maxTimeStart.substring(maxTimeStart.indexOf("T")+1, maxTimeStart.indexOf("T")+3);
                maxTimeEnd = maxTimeEnd.substring(maxTimeEnd.indexOf("T")+1, maxTimeEnd.indexOf("T")+3);
            }

        }
        DecimalFormat df = new DecimalFormat("#.00");
        value = value / dataLength;
        value = value*100;
        int mean = (int) value;
        minValue *= 100;
        maxValue *= 100;
        String minPrice = df.format(minValue);
        minPrice = minPrice.replace(".", ",");
        String maxPrice = df.format(maxValue);
        maxPrice = maxPrice.replace(".", ",");
        System.out.println(" ");
        System.out.println("medelpris " + mean);
        System.out.println("lägsta pris " + minPrice + " " + minTimeStart + "-" + minTimeEnd);
        System.out.println("högsta pris " + maxPrice + " " + maxTimeStart + "-" + maxTimeEnd);
        ChargeWindow(priceArr, timeStart, dataLength, hours);
    }

    //todo fix charging window
    private static void ChargeWindow(double[] priceArr, String[] timeStart, int dataLength, int hour) {
        double sum = Double.MIN_VALUE;
        double windowSum = 0.0;
        double windowAvg = 0.0;
        String startTime = "";
        for (int i = 0; i < hour; i++) {
            sum += priceArr[i];
        }
        windowSum = sum/hour;
        for (int j = hour; j < dataLength; j++) {
            sum += priceArr[j];
            sum -= priceArr[j-hour];
            windowAvg = sum/hour;
            if(windowAvg < windowSum){
                windowSum = windowAvg;
                startTime = timeStart[j-(hour-1)];
            }
        }

        sum = windowSum*100;
        String valueSum= "";
        DecimalFormat df = new DecimalFormat("#0.00");
        valueSum = df.format(sum);
        valueSum = valueSum.replace(".", ",");

        startTime = startTime.substring(startTime.indexOf("T")+1, (startTime.indexOf("+")+1));
        startTime = startTime.replace("+", "");
        System.out.println("påbörja laddning kl "+ startTime);
        System.out.println("Medelpris för fönster: "+ valueSum + " öre");
    }

    private static int getDataLength(ElpriserAPI elpriserAPI, String date, String zone) {
        int dataLength = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size();
        return dataLength;
    }

    public static void helpInfo(){
        System.out.println("usage");
        System.out.println("--zone for the zone");
        System.out.println("--date for the date in yyyy-MM-dd");
        System.out.println("--charging for the charging hours 2, 4, 8");
        System.out.println("--sorted for a sorted list of prices");
        System.out.println("Zones:");
        System.out.println("SE1");
        System.out.println("SE2");
        System.out.println("SE3");
        System.out.println("SE4");
    }

}











