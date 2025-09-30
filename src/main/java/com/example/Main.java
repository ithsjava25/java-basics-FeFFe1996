package com.example;
import com.example.api.ElpriserAPI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String zone = "";
        String[] validZones ={"SE1", "SE2", "SE3", "SE4"};
        String date = "";
        boolean zoneIsValid = false;
        boolean sorted = false;
        boolean chargingRequest = false;
        String charging = "";
        int hours = 0;

        if (args.length == 0) {
            helpInfo();
//            Scanner sc = new Scanner(System.in);
//            System.out.println("Enter Zone Name:");
//            zone = sc.nextLine();
//            System.out.println("Enter Date or press enter for current date:" );
//            date = sc.nextLine();
        } else{
            for (int i = 0; i < args.length; i++){
                switch (args[i]) {
                    case "--zone" -> zone = args[i + 1];
                    case "--date" -> date = args[i + 1];
                    case "--sorted" -> sorted = true;
                    case "--charging" -> {
                        charging = args[i + 1];
                        charging = charging.replaceAll("[H, h]", "");
                        hours = Integer.parseInt(charging);
                        chargingRequest = true;
                    }
                    case "--help" -> helpInfo();
                }
        }
        }
        for (int i = 0; i < validZones.length; i++){
            if (validZones[i].equals(zone)){
                zoneIsValid = true;
            }
        }

        boolean validateDate = false;

        if (zoneIsValid == false){
            System.out.println("zone required or invalid zone");
            System.out.println("your input " + zone);
        }else{
            if (date.length() == 0){
                getElpriser(elpriserAPI, zone, hours, sorted, chargingRequest);
            }else if (date.length() > 0) {
                validateDate = validDate(date);
                if (validateDate) {
                    getElpriser(elpriserAPI, date, zone, hours, sorted, chargingRequest);
                }
            }
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

    private static void getElpriser(ElpriserAPI elpriserAPI, String zone, int hours, boolean sorted, boolean chargingRequest) {
        String date = LocalDate.now().toString();
        checkNextDayAvailable(elpriserAPI, date, zone, hours, sorted, chargingRequest);
    }

    private static void getElpriser(ElpriserAPI elpriserAPI, String date, String zone, int hours, boolean sorted, boolean chargingRequest) {
        checkNextDayAvailable(elpriserAPI, date, zone, hours, sorted, chargingRequest);
    }

    private static void checkNextDayAvailable(ElpriserAPI elpriserAPI, String date, String zone, int hours, boolean sorted, boolean chargingRequest) {
        String nextDay = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1).toString();
        int dataLength = getDataLength(elpriserAPI, date, zone);
        int dataLengthNext = getDataLength(elpriserAPI, nextDay, zone);
        if (dataLength==0){
            System.out.println("no data available fo next day");
        }
        else{
            double[] priceArr = new double[dataLength];
            String[] timeStart =  new String[dataLength];
            String[] timeEnd =  new String[dataLength];
            double[] priceArrNext = new double[dataLengthNext];
            String[] timeStartNext =  new String[dataLengthNext];
            String[] timeEndNext =  new String[dataLengthNext];
            if(dataLengthNext > 0){
                for (int i = 0; i < dataLengthNext; i++){
                    priceArrNext[i] = elpriserAPI.getPriser(nextDay, ElpriserAPI.Prisklass.valueOf(zone)).get(i).sekPerKWh();
                    timeStartNext[i] = String.valueOf(elpriserAPI.getPriser(nextDay, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeStart());
                    timeEndNext[i] = String.valueOf(elpriserAPI.getPriser(nextDay, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeEnd());
                }
            }
            for (int i = 0; i < dataLength; i++){
                priceArr[i] = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).sekPerKWh();
                timeStart[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeStart());
                timeEnd[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeEnd());
            }
            if (sorted){
                if(dataLengthNext > 0){
                    sortedData(dataLength, priceArr, timeStart, timeEnd, priceArrNext, timeStartNext, timeEndNext);
                }else{sortedData(dataLength, priceArr, timeStart, timeEnd);}
            }
            if (chargingRequest == true){
                if (dataLengthNext > 0){
                    ChargeWindow(priceArr, timeStart, priceArrNext,timeStartNext, dataLength, hours);
                }else {
                    ChargeWindow(priceArr, timeStart, dataLength, hours);
                }
            }
            printValues(priceArr, timeStart, timeEnd, dataLength);
        }
    }

    private static void sortedData(int dataLength, double[] priceArr, String[] timeStart, String[] timeEnd, double[] priceArrNext, String[] timeStartNext, String[] timeEndNext) {
        /***
         * Sort data if next day is avaialble
         */
        int indexLength = priceArr.length+priceArrNext.length;
        String[] sortedArr = new String[indexLength];
        String startTime = "";
        String endTime = "";
        double[] tempArray = new double[indexLength];
        String[] tempTimeStart = new String[indexLength];
        String[] tempTimeEnd = new String[indexLength];
        int iterator = 0;
        for (int i = 0; i < (indexLength); i++){
            if(i < dataLength){
                tempArray[i] = priceArr[i];
                tempTimeStart[i] = timeStart[i];
                tempTimeEnd[i] = timeEnd[i];
            }else {
                tempArray[i] = priceArrNext[iterator];
                tempTimeStart[i] = timeStartNext[iterator];
                tempTimeEnd[i] = timeEndNext[iterator];
                iterator++;
            }
        }
        priceArr = tempArray;
        timeStart = tempTimeStart;
        timeEnd = tempTimeEnd;

        for (int i = 1; i < indexLength; i++){ //find and sort arrays from highest to lowest with time following
            double currentprice = priceArr[i];
            String currentTimeStart = timeStart[i];
            String currentTimeEnd = timeEnd[i];
            int j = i-1;
            while(j >= 0 && priceArr[j] < currentprice){
                priceArr[j+1] = priceArr[j];
                timeStart[j+1] = timeStart[j];
                timeEnd[j+1] = timeEnd[j];
                j--;
            }
            priceArr[j+1] = currentprice;
            timeStart[j+1] = currentTimeStart;
            timeEnd[j+1] = currentTimeEnd;
        }

            for (int i = 0; i < indexLength; i++){
                double value = priceArr[i];
                value=value*100;
                startTime = timeStart[i];
                endTime = timeEnd[i];
                startTime = startTime.substring(startTime.indexOf("T")+1, startTime.indexOf("T")+3);
                endTime =  endTime.substring(endTime.indexOf("T")+1, endTime.indexOf("T")+3);
                DecimalFormat df = new DecimalFormat("#0.00");
                String newValue = df.format(value);
                newValue = newValue.replace(".", ",");
                sortedArr[i] = startTime + "-" +  endTime + " " + (newValue) + " öre";
            }

        for (int p = 0; p < indexLength; p++){
            System.out.println(sortedArr[p]);
        }
    }

    private static void sortedData(int dataLength, double[] priceArr, String[] timeStart, String[] timeEnd) {
        /***
         * sort data if next day is unavailable
         */

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

    private static void printValues(double[] priceArr, String[] timeStart, String[] timeEnd, int dataLength) {
        double value = 0;
        double minValue = 1;
        double maxValue = 0;
        double sum = Double.MIN_VALUE;
        double windowSum = 0.0;
        double windowAvg = 0.0;
        String minTimeStart = "";
        String minTimeEnd = "";
        String maxTimeStart = "";
        String maxTimeEnd = "";
        if (dataLength >= 96){
            for (int i = 0; i < dataLength; i++){
                value += priceArr[i];
            }
            for (int i = 0; i < 4; i++) {
                sum += priceArr[i];
            }
             //for loop to find highest and lowest
                windowSum = sum/4;
                for (int j = 4; j < dataLength; j++) {
                    sum += priceArr[j];
                    sum -= priceArr[j-4];
                    windowAvg = sum/4;
                    if(windowAvg < windowSum){
                        windowSum = windowAvg;
                    }else if(windowAvg > maxValue){
                        maxValue = windowAvg;
                    }

                if(minValue > windowSum){
                    minValue = windowSum;
                    minTimeStart = timeStart[j];
                    minTimeStart = minTimeStart.substring(minTimeStart.indexOf("T")+1, minTimeStart.indexOf("+"));
                    LocalTime Time = LocalTime.parse(minTimeStart);
                    minTimeStart = Time.minusHours(1).toString();
                    minTimeEnd = Time.toString();
                    minTimeStart = minTimeStart.substring(0,2);
                    minTimeEnd = minTimeEnd.substring(0,2);

                } if(maxValue > windowSum){
                    maxTimeStart = timeStart[j];
                    maxTimeStart = maxTimeStart.substring(maxTimeStart.indexOf("T")+1, maxTimeStart.indexOf("+"));
                    LocalTime Time = LocalTime.parse(maxTimeStart);
                    maxTimeEnd = Time.plusHours(1).toString();
                    maxTimeStart = maxTimeStart.substring(0,2);
                    maxTimeEnd = maxTimeEnd.substring(0,2);
                }

            }

        }else {for (int a = 0; a < dataLength; a++) { //for loop to add the values of pricesArray
            value += priceArr[a];
            if(minValue > priceArr[a]){
                minValue = priceArr[a];
                minTimeStart = timeStart[a];
                minTimeStart = minTimeStart.substring(minTimeStart.indexOf("T")+1, minTimeStart.indexOf("+"));
                LocalTime Time = LocalTime.parse(minTimeStart);
                minTimeEnd = Time.plusHours(1).toString();
                minTimeStart = minTimeStart.substring(0,2);
                minTimeEnd = minTimeEnd.substring(0,2);

            } if(maxValue < priceArr[a]){
                maxValue = priceArr[a];
                maxTimeStart = timeStart[a];
                maxTimeStart = maxTimeStart.substring(maxTimeStart.indexOf("T")+1, maxTimeStart.indexOf("+"));
                LocalTime Time = LocalTime.parse(maxTimeStart);
                maxTimeEnd = Time.plusHours(1).toString();
                maxTimeStart = maxTimeStart.substring(0,2);
                maxTimeEnd = maxTimeEnd.substring(0,2);
            }
        }
        }

        formatDataForPrint(dataLength, value, minValue, maxValue, minTimeStart, minTimeEnd, maxTimeStart, maxTimeEnd);
    }

    private static void formatDataForPrint(int dataLength, double value, double minValue, double maxValue, String minTimeStart, String minTimeEnd, String maxTimeStart, String maxTimeEnd) {
        DecimalFormat df = new DecimalFormat("#.00");
        value = value / dataLength;
        value = value *100;
        String mean = df.format(value);
        mean = mean.replace(".", ",");
        minValue *= 100;
        maxValue *= 100;
        String minPrice = df.format(minValue);
        minPrice = minPrice.replace(".", ",");
        String maxPrice = df.format(maxValue);
        maxPrice = maxPrice.replace(".", ",");
        System.out.println(" ");
        System.out.println("Medelpris: " + mean + " öre");
        System.out.println("lägsta pris " + minPrice + " " + minTimeStart + "-" + minTimeEnd);
        System.out.println("högsta pris " + maxPrice + " " + maxTimeStart + "-" + maxTimeEnd);
    }

    private static void ChargeWindow(double[] priceArr, String[] timeStart, double[]priceArrNext, String[] timeStartNext, int dataLength, int hour) {
        /***
         * Chargewindow method if data for next day is available!!
         ***/
        double sum = Double.MIN_VALUE;
        double windowSum = 0.0;
        double windowAvg = 0.0;
        String startTime = "";
        int indexLength = priceArr.length+priceArrNext.length;
        double[] tempArray = new double[indexLength];
        String[] tempTimeStart = new String[indexLength];
        int iterator = 0;
        for (int i = 0; i < (indexLength); i++){
            if(i <= priceArr.length-1){
                tempArray[i] = priceArr[i];
                tempTimeStart[i] = timeStart[i];
            }else {
                tempTimeStart[i] = timeStartNext[iterator];
                tempArray[i] = priceArrNext[iterator];
                iterator++;
            }
        }
        if (dataLength >= 96){
            hour *= 4;
        }
        priceArr = tempArray;
        timeStart = tempTimeStart;
        for (int i = 0; i < hour; i++) {
            sum += priceArr[i];
        }
        windowSum = sum/hour;
        for (int j = hour; j < (indexLength); j++) {
            sum += priceArr[j];
            sum -= priceArr[j-hour];
            windowAvg = sum/hour;
            if(windowAvg < windowSum){
                windowSum = windowAvg;
                startTime = timeStart[j-(hour-1)];
            }
        }
        printWindow(sum, windowSum, startTime);
    }

    private static void ChargeWindow(double[] priceArr, String[] timeStart, int dataLength, int hour) {
        /***
         * Chargewindow method if data for next day is unavailable!!
         ***/

        double sum = Double.MIN_VALUE;
        double windowSum = 0.0;
        double windowAvg = 0.0;
        String startTime = "";
        if (dataLength >= 96){
            hour *= 4;
        }
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

        printWindow(sum, windowSum, startTime);
    }

    private static void printWindow(double sum, double windowSum, String startTime) {

        sum = windowSum * 100;
        String valueSum = "";
        DecimalFormat df = new DecimalFormat("#0.00");
        valueSum = df.format(sum);
        valueSum = valueSum.replace(".", ",");

        startTime = startTime.substring(startTime.indexOf("T") + 1, (startTime.indexOf("+") + 1));
        startTime = startTime.replace("+", "");
        System.out.println(" ");
        System.out.println("Påbörja laddning kl " + startTime + " Medelpris för fönster: " + valueSum + " öre");
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











