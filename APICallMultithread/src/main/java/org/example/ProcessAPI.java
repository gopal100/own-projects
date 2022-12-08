package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessAPI {

    private static final String URL_TO_FETCH = "https://jsonmock.hackerrank.com/api/transactions/search?userId=";

    public static List<Integer> getUserTransaction(int uid, String txnType, String monthYear) {
        List<Integer> finalLs = new ArrayList<>();
        try {
            List<TransactionAPIResponse> ls = parsePages(uid);
            double averageSpending = 0;
            double totalSpending = 0.0;
            int totalRecords = 0;
            if (ls != null) {
                for (TransactionAPIResponse transactionAPIResponse : ls) {
                    for (TransactionData data : transactionAPIResponse.getData()) {
                        String monthYr = getMnthYr(data);
                        System.out.println(" : " + monthYr + "  : " + data.getTxnType());
                        if ("debit".equalsIgnoreCase(data.getTxnType()) && monthYr.equalsIgnoreCase(monthYear)) {
                            totalSpending += NumberFormat.getCurrencyInstance(Locale.US).parse(data.getAmount()).doubleValue();
                            totalRecords++;
                        }
                    }
                }
                averageSpending = totalSpending / totalRecords;
                for (TransactionAPIResponse transactionAPIResponse : ls) {
                    for (TransactionData data : transactionAPIResponse.getData()) {
                        String monthYr = getMnthYr(data);
                        if (txnType.equalsIgnoreCase(data.getTxnType()) && monthYr.equalsIgnoreCase(monthYear)) {
                            if (NumberFormat.getCurrencyInstance(Locale.US).parse(data.getAmount()).doubleValue() > averageSpending) {
                                finalLs.add(data.getId());
                            }
                        }
                    }
                }
            }
        } catch (ExecutionException | InterruptedException | ParseException e) {
            throw new RuntimeException(e);
        }

        return finalLs;
    }

    private static String getMnthYr(TransactionData e) {
        Instant instant = Instant.ofEpochMilli(e.getTimestamp());
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        String month = ldt.getMonthValue() <= 9 ? "0" + ldt.getMonthValue() : String.valueOf(ldt.getMonthValue());
        String yr = String.valueOf(ldt.getYear());
        return month + "-" + yr;
    }


    private static String processAPIResponse(String finalURL) throws Exception {
        //String getURL = URL_TO_FETCH+userId;
        URL url = new URL(finalURL);
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            StringBuilder response = new StringBuilder();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            return response.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new Exception("Error while calling the GET API");
        }

    }

    private static TransactionAPIResponse parseJson(String jsonResponse) {

        try {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(jsonResponse, TransactionAPIResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<TransactionAPIResponse> parsePages(int uid) throws ExecutionException, InterruptedException {

        String getAPIRes = null;
        List<Future<String>> responseList = new ArrayList<>();
        List<TransactionAPIResponse> finalList = new ArrayList<>();

        try {
            getAPIRes = processAPIResponse(URL_TO_FETCH + uid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TransactionAPIResponse firstTransactionData = parseJson(getAPIRes);
        if (firstTransactionData == null)
            return null;
        Integer totalPages = firstTransactionData.getTotalPages();
        finalList.add(firstTransactionData);

        ExecutorService ex = Executors.newFixedThreadPool(4);
        for (int idx = 2; idx <= totalPages; idx++) {
            try {
                int finalIdx = idx;
                Future<String> future = ex.submit(() -> processAPIResponse(URL_TO_FETCH + uid + "&page=" + finalIdx));
                responseList.add(future);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Future<String> future : responseList) {
            finalList.add(parseJson(future.get()));
        }
        ex.shutdown();
        return finalList;
    }
}
