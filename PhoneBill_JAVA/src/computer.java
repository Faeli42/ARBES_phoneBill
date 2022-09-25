import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.temporal.ChronoUnit;

/*
created by: Oldřich Mahdal
date: 25.9.2022
purpose: entry task for ARBES Technologies
 */

public class computer {

    public static void main (String[] args){

        List<String> recordsRaw = new ArrayList<>();

        // načtení a formátování dat
        // TODO : lepsi adresa souboru, dynamicky
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\oldam\\IdeaProjects\\PhoneBill_JAVA\\src\\csv_arbes_test_v2.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                recordsRaw.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<List<String>> records = new ArrayList<>();

        for (int i = 0; i < recordsRaw.size(); i++) {
            String[] values = recordsRaw.get(i).split(",");
            records.add(Arrays.asList(values));

        }

        List<String> phoneNumbers = new ArrayList();
        for (int i = 0; i < records.size(); i++) {
            phoneNumbers.add(records.get(i).get(0));
        }

        // hledáme nejfrekventovanějšího volajícího
        List<String> mostCalled = new ArrayList();
        mostCalled.add(phoneNumbers.get(0));

        int currentCount = 1;
        int maxCount = 1;
        List<String> phoneNumbersSorted = new ArrayList(phoneNumbers);
        Collections.sort(phoneNumbersSorted);

        for (int i = 1; i < phoneNumbersSorted.size(); i++) {

            if (phoneNumbersSorted.get(i).equals(phoneNumbersSorted.get(i-1))){
                currentCount++;
                if (currentCount==maxCount){
                    mostCalled.add(phoneNumbersSorted.get(i));
                }
                if (currentCount>maxCount){
                    mostCalled.clear();
                    mostCalled.add(phoneNumbersSorted.get(i));
                    maxCount=currentCount;
                }
            } else{
                currentCount = 1;

            }
        }

        //TODO vymazat cisla krome >číslo s aritmeticky nejvyšší hodnotou<
/*       if (mostCalled.size()>1){

            List<Integer> avgs = new ArrayList();
            for (int i = 0; i < mostCalled.size(); i++) {
                String[] split = mostCalled.get(i).split("");
                int avg = 0;
                for (int j = 0; j < split.length; j++) {
                    avg += Integer.parseInt(split[j]);

                }
                avg = avg / split.length;
                avgs.add(avg);
            }
            int maxAvg = 0;
            int index = 0;
            for (int i = 0; i < avgs.size(); i++) {
                if (avgs.get(i)>maxAvg){
                    maxAvg = avgs.get(i);
                    index = i;
                }
            }
            System.out.println("mostCalled: "+phoneNumbers.get(index) );
            mostCalled = new ArrayList(Integer.parseInt(phoneNumbers.get(index)));
        }
*/

/*      //kontrola
        for (int i = 0; i < mostCalled.size(); i++) {
            System.out.println("mostCalled: "+mostCalled.get(i)+" "+ maxCount + " times" );
        }

*/

        // hlavní cyklus pro výpočet
        MyCalculator newCalc = new MyCalculator();
        BigDecimal finalPrice = BigDecimal.valueOf(0);

        for (int i = 0; i < phoneNumbers.size(); i++) {
            if (!phoneNumbers.get(i).equals(mostCalled.get(0))){
                finalPrice = finalPrice.add(newCalc.calculate(recordsRaw.get(i)));
                //System.out.println("new price is "+finalPrice);
            };
        }

        // finální cena za všechny hovory
        System.out.println("Final price for this input CSV file is: "+finalPrice+" Kč");
    }

    public static class MyCalculator implements TelephoneBillCalculator {


        @Override
        public BigDecimal calculate(String phoneLog) {

            //String phoneLogChopped = phoneLog.substring(0, phoneLog.length() - 1);
            String[] splitLog = phoneLog.split(",");
            String callNumber = splitLog[0];
            String callStart = splitLog[1];
            String callEnd = splitLog[2];
            String[] splitStart = callStart.split(" ");
            String callStartTime = splitStart[1];

            String[] splitStartTime = callStartTime.split(":");
            int callStartHours = Integer.parseInt(splitStartTime[0]);
            int callStartMinutes = Integer.parseInt(splitStartTime[1]);
            int callStartSeconds = Integer.parseInt(splitStartTime[2]);

            int callStartStartingSecond = 3600*callStartHours + 60*callStartMinutes + callStartSeconds;

            //System.out.println("------- ");

            //System.out.println("number "+callNumber);

            LocalDateTime dateTimeStart = LocalDateTime.parse(callStart, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            //System.out.println("start "+dateTimeStart);

            LocalDateTime dateTimeEnd = LocalDateTime.parse(callEnd, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            //System.out.println("end "+dateTimeEnd);


            //výpočet počtu provolaných sekund
            int diff = (int) ChronoUnit.SECONDS.between(dateTimeStart, dateTimeEnd);

            // TODO: diff minus?? error
            //System.out.println("seconds "+diff);

            // zde se počítá finální částka za tento hovor
            BigDecimal returnPrice = BigDecimal.valueOf(0);
            int currentTime = callStartStartingSecond;
            if (diff <= 300 && diff >= 60){
                for (int i = 0; i < diff/60; i++) {
                    if (diff >= 60){
                        //ošetření dražšího tarifu mezi 8 a 16h
                        if (currentTime > 28800 && currentTime < 57600){
                            diff -= 60;
                            currentTime +=60;
                            returnPrice = returnPrice.add(BigDecimal.valueOf(1));
                        }
                        else{
                            diff -= 60;
                            currentTime +=60;
                            returnPrice = returnPrice.add(BigDecimal.valueOf(0.5));
                        }
                    }
                }
                if (diff != 0){
                    if (currentTime > 28800 && currentTime < 57600){
                        returnPrice = returnPrice.add(BigDecimal.valueOf(1));
                    }
                    else{
                        returnPrice = returnPrice.add(BigDecimal.valueOf(0.5));
                    }
                }
            } else if (diff > 300) {
                for (int i = 0; i < 5; i++) {
                    if (diff >= 60){
                        if (currentTime > 28800 && currentTime < 57600){
                            diff -= 60;
                            currentTime +=60;
                            returnPrice = returnPrice.add(BigDecimal.valueOf(1));
                        }
                        else{
                            diff -= 60;
                            currentTime +=60;
                            returnPrice = returnPrice.add(BigDecimal.valueOf(0.5));
                        }
                    }
                }
                // dlouhé hovory stojí již jen 0,2 Kč za minutu
                for (int i = 0; i < diff/60; i++) {
                    if (diff >= 60){
                            diff -= 60;
                            currentTime +=60;
                            returnPrice = returnPrice.add(BigDecimal.valueOf(0.2));
                    }
                }
                if (diff != 0){
                    returnPrice = returnPrice.add(BigDecimal.valueOf(0.2));
                }
            } else if (diff < 60){
                if (diff != 0){
                    if (currentTime > 28800 && currentTime < 57600){
                        returnPrice = returnPrice.add(BigDecimal.valueOf(1));
                    }
                    else{
                        returnPrice = returnPrice.add(BigDecimal.valueOf(0.5));
                    }
                }
            }
            //System.out.println("price of this call "+returnPrice);
            return returnPrice;
        }
    }
}
