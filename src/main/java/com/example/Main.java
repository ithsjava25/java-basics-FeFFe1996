package com.example;

import com.example.api.ElpriserAPI;

import java.text.DecimalFormat;

import java.time.LocalDate;



public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String date =  LocalDate.now().toString();
        String zone =  "SE3";
        int dataLength = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size();
        String[] dataArray = new String[dataLength];
        String[] splitArray = new String[dataLength];
        String[] timeArray = new String[dataLength];
        for(int i = 0; i < dataLength; i++){
            dataArray[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i));

            //splitArray[i] = dataArray[i].split("[,=:]")[1];


       }
//        for(int j = 0; j < dataLength; j++){
//
//            timeArray[j] = String.valueOf(splitArray[7]); //finds the start time of the array
//        }

        getMean(dataArray, splitArray, dataLength);
        //getMaxMin(pricesArray, timeArray);
    }


    public static void getCurrentDay(String zone){
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        System.out.println(elpriserAPI.getPriser(today, ElpriserAPI.Prisklass.valueOf(zone)).size());
    }

    private static void getMean(String[] dataArray, String[] splitArray, int dataLength) { //get the mean value of the day prices
        for (int i = 0; i < dataLength; i++) {
            splitArray[i] = dataArray[i].split("[:=,]")[1];
        }

        DecimalFormat df = new DecimalFormat("#,00");

        double value = 0;
        for (int a = 0; a < dataLength; a++){ //for loop to add the values of pricesArray
            value += Double.parseDouble(splitArray[a]);
        }
        double meanValue = (value / dataLength) * 100;
        String mean = String.valueOf(df.format(meanValue));
        System.out.println(" ");
        System.out.println("medelpris: " + mean + " öre");
    }

//    private static void getMaxMin(double[] pricesArray, String[] timeArray) { //get the max and min prices fpr the day.
//        double maxValue = 0.0;
//        double minValue = 0.0;
//        String maxValueTime = "";
//        String minValueTime = "";
//
//        for (int a = 0; a < 24; a++){
//
//            if (pricesArray[a] > maxValue){ //finds the max value and the time it is.
//                maxValue = pricesArray[a];
//                maxValueTime = timeArray[a];
//
//            }
//            else if (pricesArray[a] < minValue){ //same as max but for the lowest value of the day.
//                minValue = pricesArray[a];
//                minValueTime = timeArray[a];
//            }
//        }
//        printText(maxValueTime, minValueTime, maxValue, minValue);
//    }

    private static void printText(String maxValueTime, String minValueTime, double maxValue, double minValue) {
        maxValueTime = maxValueTime.replace("T", " ");
        minValueTime = minValueTime.replace("T", " ");
        System.out.println("Max: " + maxValue + " at: "+ maxValueTime);
        System.out.println("Min: " + minValue +  " at: "+ minValueTime);
    }
}











