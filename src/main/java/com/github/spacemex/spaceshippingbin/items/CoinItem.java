package com.github.spacemex.spaceshippingbin.items;

public class CoinItem extends AbstractDepositableItem{
    private final double value;
    public CoinItem(Properties pProperties, double value) {
        super(pProperties);
        this.value = value;
    }

    @Override
    public double getDepositValue() {
        return value;
    }
}
