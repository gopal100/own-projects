package org.example;

public class APICall {
    public static void main(String[] args) {

        System.out.println("Hello world!");
        ProcessAPI.getUserTransaction(4, "debit", "02-2019");
    }
}