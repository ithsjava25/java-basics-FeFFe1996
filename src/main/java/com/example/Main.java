package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        getValues(String.valueOf(LocalDate.now()), "SE1");
    }

    public static void getCurrentDay(String zone){
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        System.out.println(elpriserAPI.getPriser(today, ElpriserAPI.Prisklass.valueOf(zone)).size());
    }


    public static void getValues(String date, String zone){ //gets the values and puts them in an array
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String[] dataArray = new String[elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size()];
        double[] pricesArray = new double[elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size()];
        String[] timeArrayStart = new String[elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size()];
        String[] timeArrayEnd = new String[elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).size()];
        for (int i = 0; i < dataArray.length; i++) {
            dataArray[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i));
            for(int j = 0; j < dataArray.length; j++){
                String[] splitArray = dataArray[i].split("[=,]"); //splits the array to find values and to separate the data of each place in the array.
                pricesArray[i] = Double.parseDouble(splitArray[1]); //finds sekPerKWH
                timeArrayStart[i] = String.valueOf(splitArray[7]); //finds the start time of the array
                timeArrayEnd[i] = String.valueOf(splitArray[9]); //finds the start time of the array
            }
        }
        getMean(pricesArray);
        getMaxMin(pricesArray, timeArrayStart, timeArrayEnd);
    }

    private static void getMaxMin(double[] pricesArray, String[] timeArrayStart, String[] timeArrayEnd) { //get the max and min prices fpr the day.
        double maxValue = pricesArray[0];
        double minValue = pricesArray[0];
        String maxValueTimeStart = timeArrayStart[0];
        String minValueTimeStart = timeArrayStart[0];
        String maxValueTimeEnd = timeArrayStart[0];
        String minValueTimeEnd = timeArrayStart[0];

        for (int a = 0; a < 24; a++){

            if (pricesArray[a] > maxValue){ //finds the max value and the time it is.
                maxValue = pricesArray[a];
                maxValueTimeStart = timeArrayStart[a];
                maxValueTimeEnd = timeArrayEnd[a];
            }
            else if (pricesArray[a] < minValue){ //same as max but for the lowest value of the day.
                minValue = pricesArray[a];
                minValueTimeStart = timeArrayStart[a];
                minValueTimeEnd = timeArrayEnd[a];
            }
        }
        printText(maxValueTimeStart, minValueTimeStart, maxValueTimeEnd, minValueTimeEnd, maxValue, minValue);
    }

    private static void printText(String maxValueTimeStart, String minValueTimeStart, String maxValueTimeEnd, String minValueTimeEnd, double maxValue, double minValue) {
        maxValueTimeStart = maxValueTimeStart.replace("T", " ");
        minValueTimeStart = minValueTimeStart.replace("T", " ");
        maxValueTimeEnd = maxValueTimeEnd.replace("T", " ");
        maxValueTimeEnd = maxValueTimeEnd.replace("]", " ");
        minValueTimeEnd = minValueTimeEnd.replace("T", " ");
        minValueTimeEnd = minValueTimeEnd.replace("]", " ");
        System.out.println("Max: " + maxValue + " at: "+ maxValueTimeStart +" and ends at " + maxValueTimeEnd);
        System.out.println("Min: " + minValue +  " at: "+ minValueTimeStart +" and ends at " + minValueTimeEnd);
    }

    private static void getMean(double[] pricesArray) { //get the mean value of the day prices
        double value = 0;
        for (int a = 0; a < 24; a++){ //for loop to add the values of pricesArray
            value = value + pricesArray[a];
        }
        double meanValue = value / 24;
        System.out.println(" ");
        System.out.println("Mean: " + meanValue + " SekPerKWH");
    }
}


