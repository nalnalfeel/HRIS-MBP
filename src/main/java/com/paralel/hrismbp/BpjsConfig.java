package com.paralel.hrismbp;

public class BpjsConfig {
    private double jkkRate;       // Standar: 0.24% (0.0024)
    private double jkmRate;       // Standar: 0.30% (0.0030)
    private double jhtCompanyRate;// Standar: 3.70% (0.0370)
    private double jhtEmployeeRate;// Standar: 2.00% (0.0200)
    private double jpCompanyRate; // Standar: 2.00% (0.0200)
    private double jpEmployeeRate;// Standar: 1.00% (0.0100)
    private double maxCapJp;      // Batas atas gaji JP (misal: 10.042.300)

    public BpjsConfig() {
        // Nilai Default Standar BPJS TK
        this.jkkRate = 0.24;
        this.jkmRate = 0.30;
        this.jhtCompanyRate = 3.70;
        this.jhtEmployeeRate = 2.00;
        this.jpCompanyRate = 2.00;
        this.jpEmployeeRate = 1.00;
        this.maxCapJp = 10042300.0;
    }

    // Getters & Setters dalam skala Persen (misal 0.24 untuk 0.24%)
    public double getJkkRate() { return jkkRate; }
    public void setJkkRate(double jkkRate) { this.jkkRate = jkkRate; }

    public double getJkmRate() { return jkmRate; }
    public void setJkmRate(double jkmRate) { this.jkmRate = jkmRate; }

    public double getJhtCompanyRate() { return jhtCompanyRate; }
    public void setJhtCompanyRate(double jhtCompanyRate) { this.jhtCompanyRate = jhtCompanyRate; }

    public double getJhtEmployeeRate() { return jhtEmployeeRate; }
    public void setJhtEmployeeRate(double jhtEmployeeRate) { this.jhtEmployeeRate = jhtEmployeeRate; }

    public double getJpCompanyRate() { return jpCompanyRate; }
    public void setJpCompanyRate(double jpCompanyRate) { this.jpCompanyRate = jpCompanyRate; }

    public double getJpEmployeeRate() { return jpEmployeeRate; }
    public void setJpEmployeeRate(double jpEmployeeRate) { this.jpEmployeeRate = jpEmployeeRate; }

    public double getMaxCapJp() { return maxCapJp; }
    public void setMaxCapJp(double maxCapJp) { this.maxCapJp = maxCapJp; }
}