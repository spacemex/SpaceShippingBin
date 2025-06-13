package com.github.spacemex.spaceshippingbin.packet;

public class ClientCurrencyData {
    private static double clientBalance = 0;
    public static void setBalance(double balance) { clientBalance = balance;}
    public static double getBalance() {return clientBalance;}
}
