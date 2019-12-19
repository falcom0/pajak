package usu.pajak.fariz.service;

import java.math.BigDecimal;

public enum StaticValue {
    getInstance();
    public static final BigDecimal persenBiayaJabatan = new BigDecimal("0.05");
    public static final BigDecimal limitBiayaJabatan = new BigDecimal("6000000.00");
    public static final BigDecimal persenPotPensiun = new BigDecimal("0.0475");
    public static final BigDecimal jkk = new BigDecimal("6597.78");
    public static final BigDecimal jkm = new BigDecimal("68247.22");
    public static final BigDecimal bpjs_kesehatan = new BigDecimal("137454.00");
}
