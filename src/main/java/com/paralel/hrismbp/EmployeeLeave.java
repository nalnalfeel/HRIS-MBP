package com.paralel.hrismbp;

public record EmployeeLeave(
        int id,
        String employeeId,
        String employeeName,
        String leaveType,
        String startDate,
        String endDate,
        int totalDays,
        String notes,
        String status
) {}
