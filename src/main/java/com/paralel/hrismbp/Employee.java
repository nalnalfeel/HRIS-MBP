package com.paralel.hrismbp;

public record Employee(
        int id,
        String employeeId,
        String nik,
        String familyCardNumber,
        String fullName,
        String projectName,
        String birthPlace,       // [BARU] Tambahkan baris ini untuk Tempat Lahir
        String birthDate,
        String address,
        String phoneNumber,
        String gender,
        String education,
        String bankAccount,
        String bpjsTk,
        String bpjsKesehatan,
        double salary
) {}

/*public record Employee(
        int id,
        String nik,
        String fullName,
        String projectName,
        String birthDate,
        String address,
        String phoneNumber,
        String gender,
        String education,
        String bankAccount,
        String bpjsTk,
        String bpjsKesehatan,
        double salary
) {}*/
