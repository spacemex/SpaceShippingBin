package com.github.spacemex.spaceshippingbin.util.currency;

public interface ICurrency {
    double get();
    void set(double amount);
    void add(double amount);
    void remove(double amount);
    boolean hasFunds(double amount);
}
