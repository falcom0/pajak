package usu.pajak.fariz.model;

import com.google.gson.JsonElement;

import java.math.BigInteger;

public class SalaryDetail{
    private BigInteger id;
    private User user;
    private Unit unit;
    private JsonElement payment;

    public BigInteger getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Unit getUnit() {
        return unit;
    }

    public JsonElement getPayment() {
        return payment;
    }
}
