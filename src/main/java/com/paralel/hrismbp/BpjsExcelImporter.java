package com.paralel.hrismbp;

import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class BpjsExcelImporter {

    public static List<EmployeeBpjs> importFromExcel(File excelFile) throws Exception {
        List<EmployeeBpjs> importedList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Membaca Sheet Pertama
            Sheet sheet = workbook.getSheetAt(0);

            // Deteksi baris header untuk menentukan indeks kolom secara fleksibel
            int headerRowIndex = -1;
            int colProject = -1, colLokasi = -1, colNama = -1, colJabatan = -1, colGapok = -1;

            for (int r = 0; r <= Math.min(10, sheet.getLastRowNum()); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                for (Cell cell : row) {
                    String val = getCellValueAsString(cell).toLowerCase().trim();
                    if (val.contains("nama") || val.contains("gapok") || val.contains("lokasi")) {
                        headerRowIndex = r;
                        break;
                    }
                }
                if (headerRowIndex != -1) break;
            }

            if (headerRowIndex == -1) {
                throw new IllegalArgumentException("Header tabel tidak ditemukan di file Excel!");
            }

            // Pemetaan Kolom berdasarkan nama Header
            Row headerRow = sheet.getRow(headerRowIndex);
            for (Cell cell : headerRow) {
                String headerName = getCellValueAsString(cell).toLowerCase().trim();
                int colIdx = cell.getColumnIndex();

                if (headerName.contains("project") || headerName.contains("unit")) {
                    colProject = colIdx;
                } else if (headerName.contains("lokasi")) {
                    colLokasi = colIdx;
                } else if (headerName.contains("nama")) {
                    colNama = colIdx;
                } else if (headerName.contains("jabatan")) {
                    colJabatan = colIdx;
                } else if (headerName.contains("gapok") || headerName.contains("upah") || headerName.contains("gaji")) {
                    colGapok = colIdx;
                }
            }

            // Jika Nama / Gapok tidak ditemukan di header, atur default index (fallback)
            if (colNama == -1) colNama = 3;   // Sesuai template BSD II
            if (colGapok == -1) colGapok = 4; // Sesuai template BSD II
            if (colLokasi == -1) colLokasi = 2;

            // Loop membaca baris data (Mulai setelah header)
            int autoNo = 1;
            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String nama = getCellValueAsString(row.getCell(colNama)).trim();

                // Hentikan pembacaan jika bertemu baris 'JUMLAH' / Total / Baris Kosong
                if (nama.isEmpty() || nama.equalsIgnoreCase("jumlah") || nama.equalsIgnoreCase("total")) {
                    continue;
                }

                String project = colProject != -1 ? getCellValueAsString(row.getCell(colProject)).trim() : "UNIT BSD II";
                String lokasi = colLokasi != -1 ? getCellValueAsString(row.getCell(colLokasi)).trim() : "-";
                String jabatan = colJabatan != -1 ? getCellValueAsString(row.getCell(colJabatan)).trim() : "Teknisi";

                double gapok = 0.0;
                try {
                    Cell gapokCell = row.getCell(colGapok);
                    if (gapokCell != null) {
                        if (gapokCell.getCellType() == CellType.NUMERIC) {
                            gapok = gapokCell.getNumericCellValue();
                        } else {
                            String gapokStr = getCellValueAsString(gapokCell).replaceAll("[^0-9.]", "");
                            if (!gapokStr.isEmpty()) gapok = Double.parseDouble(gapokStr);
                        }
                    }
                } catch (Exception ignored) {}

                // Default keikutsertaan JP = true
                importedList.add(new EmployeeBpjs(autoNo++, project, lokasi, nama, jabatan, gapok, true));
            }
        }

        return importedList;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }
}