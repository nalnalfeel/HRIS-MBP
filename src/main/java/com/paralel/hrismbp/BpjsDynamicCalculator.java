package com.paralel.hrismbp;

public class BpjsDynamicCalculator {

    public static double calcJkk(EmployeeBpjs emp, BpjsConfig config) {
        return emp.getGapok() * (config.getJkkRate() / 100.0);
    }

    public static double calcJkm(EmployeeBpjs emp, BpjsConfig config) {
        return emp.getGapok() * (config.getJkmRate() / 100.0);
    }

    public static double calcJhtCompany(EmployeeBpjs emp, BpjsConfig config) {
        return emp.getGapok() * (config.getJhtCompanyRate() / 100.0);
    }

    public static double calcJhtEmployee(EmployeeBpjs emp, BpjsConfig config) {
        return emp.getGapok() * (config.getJhtEmployeeRate() / 100.0);
    }

    public static double calcJpCompany(EmployeeBpjs emp, BpjsConfig config) {
        if (!emp.isJpActive()) return 0.0;
        double baseSalary = Math.min(emp.getGapok(), config.getMaxCapJp());
        return baseSalary * (config.getJpCompanyRate() / 100.0);
    }

    public static double calcJpEmployee(EmployeeBpjs emp, BpjsConfig config) {
        if (!emp.isJpActive()) return 0.0;
        double baseSalary = Math.min(emp.getGapok(), config.getMaxCapJp());
        return baseSalary * (config.getJpEmployeeRate() / 100.0);
    }

    public static double calcTotalIuran(EmployeeBpjs emp, BpjsConfig config) {
        return calcJkk(emp, config) + calcJkm(emp, config) +
                calcJhtCompany(emp, config) + calcJhtEmployee(emp, config) +
                calcJpCompany(emp, config) + calcJpEmployee(emp, config);
    }
}