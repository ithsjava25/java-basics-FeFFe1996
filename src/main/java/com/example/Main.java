package com.example;
import com.example.api.ElpriserAPI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();
        String zone = "";
        String[] validZones ={"SE1", "SE2", "SE3", "SE4"};
        String date = "";
        boolean zoneIsValid = false;
        boolean sorted = false;
        boolean chargingRequest = false;
        String charging;
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
        for (String validZone : validZones) {
            if (validZone.equals(zone)) {
                zoneIsValid = true;
                break;
            }
        }

        boolean validateDate;

        if (!zoneIsValid){
            System.out.println("zone required or invalid zone!! your input was " + zone);
        }else{
            if (date.isEmpty()){
                getElpriser(elpriserAPI, zone, hours, sorted, chargingRequest);
            }else {
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
            System.out.println("Invalid date. it should be YYYY-MM-DD");
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
        if (dataLength == 0){
            System.out.println("no data available for next day");
        }
        else{
            double[] priceArr = new double[dataLength];
            String[] timeStart =  new String[dataLength];
            String[] timeEnd =  new String[dataLength];
            double[] priceArrNext = new double[dataLengthNext];
            String[] timeStartNext =  new String[dataLengthNext];
            String[] timeEndNext =  new String[dataLengthNext];
            if(dataLengthNext > 0){
                GetPriceArrays(elpriserAPI, nextDay, zone, dataLengthNext, priceArrNext, timeStartNext, timeEndNext);
            }
            GetPriceArrays(elpriserAPI, date, zone, dataLength, priceArr, timeStart, timeEnd);
            CheckInputArgs(hours, sorted, chargingRequest, dataLength, priceArr, timeStart, timeEnd, priceArrNext, timeStartNext, timeEndNext);
        }
    }

    private static void GetPriceArrays(ElpriserAPI elpriserAPI, String date, String zone, int dataLength, double[] priceArr, String[] timeStart, String[] timeEnd) {
        for (int i = 0; i < dataLength; i++){
            priceArr[i] = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).sekPerKWh();
            timeStart[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeStart());
            timeEnd[i] = String.valueOf(elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(zone)).get(i).timeEnd());
        }
    }

    private static void CheckInputArgs(int hours, boolean sorted, boolean chargingRequest, int dataLength, double[] priceArr, String[] timeStart, String[] timeEnd, double[] priceArrNext, String[] timeStartNext, String[] timeEndNext) {
        if (sorted){
                sortedData(dataLength, priceArr, timeStart, timeEnd, priceArrNext, timeStartNext, timeEndNext);
        }
        if (chargingRequest){
                ChargeWindow(priceArr, timeStart, priceArrNext, timeStartNext, dataLength, hours);
            }
        printValues(priceArr, timeStart, timeEnd, dataLength);
    }

    private static void sortedData(int dataLength, double[] priceArr, String[] timeStart, String[] timeEnd, double[] priceArrNext, String[] timeStartNext, String[] timeEndNext) {
        /***
         * Sort data if next day is avaialble
         */
        if (priceArrNext.length > 0){
            int indexLength = priceArr.length+priceArrNext.length;
            String[] sortedArr = new String[indexLength];
            double[] tempArray = new double[indexLength];
            String[] tempTimeStart = new String[indexLength];
            String[] tempTimeEnd = new String[indexLength];
            int iterator = 0;
            for (int i = 0; i < (indexLength); i++){ //add arrays together for data of current and next day
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
            SortDataHighestToLowest(priceArr, timeStart, timeEnd, indexLength, sortedArr);

            for (int p = 0; p < indexLength; p++){
                System.out.println(sortedArr[p]);
            }
        }else{
            /***
             * sort data if next day is unavailable
             */
            String[] sortedArr = new String[dataLength];

            SortDataHighestToLowest(priceArr, timeStart, timeEnd, dataLength, sortedArr);

            for (int p = 0; p < dataLength; p++){
                System.out.println(sortedArr[p]);
            }
        }
    }

    private static void SortDataHighestToLowest(double[] priceArr, String[] timeStart, String[] timeEnd, int indexLength, String[] sortedArr) {
        String startTime;
        String endTime;
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
            OffsetDateTime timeGet = OffsetDateTime.parse(startTime);
            LocalTime Time = timeGet.toLocalTime();
            endTime = Time.plusHours(1).toString();
            startTime = Time.toString();
            startTime  = startTime .substring(0,2);
            endTime = endTime.substring(0,2);
            DecimalFormat df = new DecimalFormat("#0.00");
            String newValue = df.format(value);
            newValue = newValue.replace(".", ",");
            sortedArr[i] = startTime + "-" +  endTime + " " + (newValue) + " öre";
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
            for (int i = 0; i < 4; i++) { //count hourly prices by adding 4 quarters
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
                    OffsetDateTime timeGet = OffsetDateTime.parse(minTimeStart);
                    LocalTime Time = timeGet.toLocalTime();
                    minTimeEnd = Time.toString();
                    minTimeStart = Time.minusHours(1).toString();
                    minTimeStart = minTimeStart.substring(0,2);
                    minTimeEnd = minTimeEnd.substring(0,2);

                } if(maxValue > windowSum){
                    maxTimeStart = timeStart[j];
                    OffsetDateTime timeGet = OffsetDateTime.parse(maxTimeStart);
                    LocalTime Time = timeGet.toLocalTime();
                    maxTimeEnd = Time.plusHours(1).toString();
                    maxTimeStart = Time.toString();
                    maxTimeStart = maxTimeStart.substring(0,2);
                    maxTimeEnd = maxTimeEnd.substring(0,2);
                }

            }

        }else {for (int a = 0; a < dataLength; a++) { //for loop to add the values of pricesArray
            value += priceArr[a];
            if(minValue > priceArr[a]){
                minValue = priceArr[a];
                minTimeStart = timeStart[a];
                OffsetDateTime timeGet = OffsetDateTime.parse(minTimeStart);
                LocalTime Time = timeGet.toLocalTime();
                minTimeEnd = Time.plusHours(1).toString();
                minTimeStart = Time.toString();
                minTimeStart = minTimeStart.substring(0,2);
                minTimeEnd = minTimeEnd.substring(0,2);

            } if(maxValue < priceArr[a]){
                maxValue = priceArr[a];
                maxTimeStart = timeStart[a];
                OffsetDateTime timeGet = OffsetDateTime.parse(maxTimeStart);
                LocalTime Time = timeGet.toLocalTime();
                maxTimeEnd = Time.plusHours(1).toString();
                maxTimeStart = Time.toString();
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
        double sum = 0.0;
        String startTime = "";
        if(priceArr.length > 0){
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
            WindowValue(priceArr, timeStart, hour, sum, startTime, indexLength);

        } else {
            if (dataLength >= 96){
                hour *= 4;
            }
            WindowValue(priceArr, timeStart, hour, sum, startTime, dataLength);
        }
    }

    private static void WindowValue(double[] priceArr, String[] timeStart, int hour, double sum, String startTime, int indexLength) {
        double windowSum;
        double windowAvg;
        for (int i = 0; i < hour; i++) {
            sum += priceArr[i];
        }
        windowSum = sum/hour;
        for (int j = hour; j < indexLength; j++) {
            sum += priceArr[j];
            sum -= priceArr[j-hour];
            windowAvg = sum/hour;
            if(windowAvg < windowSum){
                windowSum = windowAvg;
                startTime = timeStart[j-(hour-1)];
            }
        }
        printWindow(windowSum, startTime);
    }

    private static void printWindow(double windowSum, String startTime) {

        double sum = windowSum * 100;
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
        System.out.println("usage:");
        System.out.println("--zone argument for the zone. Zone must be present to fetch the data");
        System.out.println("Available zones:");
        System.out.println("SE1");
        System.out.println("SE2");
        System.out.println("SE3");
        System.out.println("SE4");
        System.out.println("--date for the date in yyyy-MM-dd format.");
        System.out.println("--charging to check for the optimal charging hours. The hours can be 2, 4, 8");
        System.out.println("--sorted to sort prices from highest to lowest");
    }
}











