package com.github.spacemex.spaceshippingbin.util.currency;

public class Currency  implements ICurrency {
    private double balance = 0;
    private boolean initialized = false;

    @Override
    public double get() {
        return balance;
    }

    @Override
    public void set(double value) {
        this.balance = value;
        this.initialized = true;
    }

    @Override
    public void add(double value) {
        this.balance += value;
    }

    @Override
    public void remove(double value) {
        if (hasFunds(value)){
            this.balance -= value;
        }
    }

    @Override
    public boolean hasFunds(double amount) {
        return balance >= amount;
    }

    public boolean isInitialized(){
        return initialized;
    }
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
