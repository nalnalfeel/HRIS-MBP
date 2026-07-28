package com.paralel.hrismbp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BpjsManagementController {

    private final BpjsConfig config = new BpjsConfig();

    // In-Memory Storage: Data hanya hidup selama aplikasi / tampilan aktif
    private final ObservableList<EmployeeBpjs> memoryEmployeeList = FXCollections.observableArrayList();
    private final ObservableList<String> unitList = FXCollections.observableArrayList();
    private FilteredList<EmployeeBpjs> filteredList;

    private final TableView<EmployeeBpjs> tableBpjs = new TableView<>();
    private final ComboBox<String> cbFilterProject = new ComboBox<>();
    private final TextField txtSearch = new TextField();

    private final Label lblTotalGapok = new Label("Rp 0");
    private final Label lblTotalIuran = new Label("Rp 0");

    public BorderPane createView(List<Employee> activeEmployees) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Inisialisasi awal list unit
        resetDataInMemory();

        // --- TOP BAR: TITLE & INDIKATOR TEMPORARY ---
        Label lblTitle = new Label("Kalkulator & Rekap BPJS TK (Temporary)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        Label lblBadge = new Label("⚠️ Mode In-Memory (Tanpa DB)");
        lblBadge.setStyle("-fx-background-color: #feebc8; -fx-text-fill: #744210; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 4px;");

        cbFilterProject.setItems(unitList);
        cbFilterProject.getSelectionModel().select("SEMUA PROJECT");
        cbFilterProject.setOnAction(e -> applyFilter());

        txtSearch.setPromptText("Cari Nama / Lokasi...");
        txtSearch.setPrefWidth(160);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        // --- TOMBOL-TOMBOL AKSI ---
        Button btnImport = new Button("📂 Import Excel");
        btnImport.setStyle("-fx-background-color: #319795; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnImport.setMinWidth(110);
        btnImport.setOnAction(e -> handleImportExcel());

        Button btnAdd = new Button("+ Baris Baru");
        btnAdd.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnAdd.setMinWidth(110);
        btnAdd.setOnAction(e -> showEmployeeDialog(null));

        Button btnEdit = new Button("✏️ Edit");
        btnEdit.setStyle("-fx-background-color: #d69e2e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnEdit.setMinWidth(80);
        btnEdit.setOnAction(e -> {
            EmployeeBpjs selected = tableBpjs.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEmployeeDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih baris yang ingin diedit!");
            }
        });

        Button btnDelete = new Button("🗑️ Hapus");
        btnDelete.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnDelete.setMinWidth(90);
        btnDelete.setOnAction(e -> handleDeleteSelected());

        Button btnReset = new Button("🔄 Reset / Clear");
        btnReset.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnReset.setMinWidth(110);
        btnReset.setOnAction(e -> handleResetAll());

        HBox titleBox = new HBox(8, lblTitle, lblBadge);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // --- MENGHAPUS TOMBOL EXPORT DARI TOP BAR ---
        HBox topBox = new HBox(8, titleBox, new Region(), cbFilterProject, txtSearch, btnImport, btnAdd, btnEdit, btnDelete, btnReset);
        topBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(topBox.getChildren().get(1), Priority.ALWAYS);
        topBox.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBox);

        // --- CENTER: TABLE VIEW ---
        setupColumns();
        tableBpjs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filteredList = new FilteredList<>(memoryEmployeeList, p -> true);
        tableBpjs.setItems(filteredList);
        root.setCenter(tableBpjs);

        // --- TOMBOL EXPORT (DIPINDAH KE BAWAH) ---
        Button btnExport = new Button("📥 Export Hasil (.xlsx)");
        btnExport.setStyle("-fx-background-color: #2f855a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnExport.setMinWidth(150);
        btnExport.setOnAction(e -> handleExportExcel());

        Button btnExportPdf = new Button("📄 Export PDF (.pdf)");
        btnExportPdf.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnExportPdf.setMinWidth(140);
        btnExportPdf.setOnAction(e -> handleExportPdf());

        // 1. Buat kotak untuk membungkus tombol Export
        HBox exportBox = new HBox(10, btnExport, btnExportPdf);
        exportBox.setAlignment(Pos.CENTER_LEFT);

        // 2. Kotak ringkasan (Summary)
        HBox summaryBox = new HBox(20,
                createSummaryCard("Total Gapok Diolah", lblTotalGapok, "#2d3748"),
                createSummaryCard("Total Transfer Iuran BPJS TK", lblTotalIuran, "#e53e3e")
        );
        summaryBox.setAlignment(Pos.CENTER_RIGHT);

        // 3. Buat ruang kosong (spacer) untuk mendorong Export ke kiri dan Summary ke kanan
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Gabungkan semuanya ke dalam satu Layout Bawah
        HBox bottomLayout = new HBox(exportBox, spacer, summaryBox);
        bottomLayout.setPadding(new Insets(15, 0, 0, 0));
        bottomLayout.setAlignment(Pos.CENTER);

        // 5. Pasang layout baru ini ke bagian bawah halaman
        root.setBottom(bottomLayout);

        updateSummary();
        return root;
    }

    private void handleImportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Excel BPJS Unit");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx, *.xls)", "*.xlsx", "*.xls"));

        File selectedFile = fileChooser.showOpenDialog(tableBpjs.getScene().getWindow());
        if (selectedFile == null) return;

        try {
            List<EmployeeBpjs> importedData = BpjsExcelImporter.importFromExcel(selectedFile);

            if (importedData.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Import Kosong", "Tidak ada data karyawan yang valid ditemukan dalam file.");
                return;
            }

            // Opsi untuk menambah atau mengganti buffer in-memory
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Import Data BPJS");
            alert.setHeaderText("Terbaca " + importedData.size() + " baris data.");
            alert.setContentText("Pilih mode pengolahan data:");

            ButtonType btnReplace = new ButtonType("Ganti Semua Data");
            ButtonType btnAppend = new ButtonType("Gabungkan");
            ButtonType btnCancel = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnReplace, btnAppend, btnCancel);

            alert.showAndWait().ifPresent(type -> {
                if (type == btnReplace) {
                    resetDataInMemory();
                }

                if (type == btnReplace || type == btnAppend) {
                    for (EmployeeBpjs emp : importedData) {
                        if (emp.getNamaProject() != null && !unitList.contains(emp.getNamaProject())) {
                            unitList.add(emp.getNamaProject());
                        }
                        emp.setNo(memoryEmployeeList.size() + 1);
                        memoryEmployeeList.add(emp);
                    }
                    tableBpjs.refresh();
                    updateSummary();
                }
            });

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error Import", "Gagal mengolah file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        ObservableList<EmployeeBpjs> selectedItems = tableBpjs.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Pilih Baris", "Silakan pilih baris yang ingin dihapus.");
            return;
        }

        memoryEmployeeList.removeAll(selectedItems);
        recalculateRowNumbers();
        tableBpjs.refresh();
        updateSummary();
    }

    private void handleResetAll() {
        if (memoryEmployeeList.isEmpty()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Data Temporary");
        confirm.setHeaderText("Bersihkan Seluruh Sesi Perhitungan?");
        confirm.setContentText("Semua data yang telah diimpor/diedit di layar ini akan dihapus dari memori.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                resetDataInMemory();
                tableBpjs.refresh();
                updateSummary();
            }
        });
    }

    private void resetDataInMemory() {
        memoryEmployeeList.clear();
        unitList.clear();
        unitList.add("SEMUA PROJECT");
        cbFilterProject.getSelectionModel().select("SEMUA PROJECT");
    }

    private void recalculateRowNumbers() {
        for (int i = 0; i < memoryEmployeeList.size(); i++) {
            memoryEmployeeList.get(i).setNo(i + 1);
        }
    }

    private void applyFilter() {
        String selectedProject = cbFilterProject.getValue();
        String searchText = txtSearch.getText() != null ? txtSearch.getText().toLowerCase().trim() : "";

        filteredList.setPredicate(emp -> {
            boolean matchesProject = selectedProject == null || selectedProject.equals("SEMUA PROJECT") ||
                    (emp.getNamaProject() != null && emp.getNamaProject().equalsIgnoreCase(selectedProject));

            boolean matchesSearch = searchText.isEmpty() ||
                    (emp.getNama() != null && emp.getNama().toLowerCase().contains(searchText)) ||
                    (emp.getLokasiProject() != null && emp.getLokasiProject().toLowerCase().contains(searchText));

            return matchesProject && matchesSearch;
        });

        updateSummary();
    }

    private void setupColumns() {
        TableColumn<EmployeeBpjs, Integer> colNo = new TableColumn<>("No.");
        colNo.setCellValueFactory(c -> c.getValue().noProperty().asObject());
        colNo.setPrefWidth(45);

        TableColumn<EmployeeBpjs, String> colProject = new TableColumn<>("PROJECT");
        colProject.setCellValueFactory(c -> c.getValue().namaProjectProperty());

        TableColumn<EmployeeBpjs, String> colLokasi = new TableColumn<>("LOKASI");
        colLokasi.setCellValueFactory(c -> c.getValue().lokasiProjectProperty());

        TableColumn<EmployeeBpjs, String> colNama = new TableColumn<>("NAMA KARYAWAN");
        colNama.setCellValueFactory(c -> c.getValue().namaProperty());
        colNama.setPrefWidth(160);

        TableColumn<EmployeeBpjs, Double> colGapok = createCurrencyCol("GAPOK", EmployeeBpjs::getGapok);
        TableColumn<EmployeeBpjs, Double> colJkk = createCurrencyCol("JKK (0.24%)", e -> BpjsDynamicCalculator.calcJkk(e, config));
        TableColumn<EmployeeBpjs, Double> colJkm = createCurrencyCol("JKM (0.30%)", e -> BpjsDynamicCalculator.calcJkm(e, config));

        TableColumn<EmployeeBpjs, String> colJhtGroup = new TableColumn<>("JHT");
        TableColumn<EmployeeBpjs, Double> colJhtPk = createCurrencyCol("PK (3.7%)", e -> BpjsDynamicCalculator.calcJhtCompany(e, config));
        TableColumn<EmployeeBpjs, Double> colJhtTk = createCurrencyCol("TK (2%)", e -> BpjsDynamicCalculator.calcJhtEmployee(e, config));
        colJhtGroup.getColumns().addAll(List.of(colJhtPk, colJhtTk));

        TableColumn<EmployeeBpjs, String> colJpGroup = new TableColumn<>("JP");
        TableColumn<EmployeeBpjs, Double> colJpPk = createCurrencyCol("PK (2%)", e -> BpjsDynamicCalculator.calcJpCompany(e, config));
        TableColumn<EmployeeBpjs, Double> colJpTk = createCurrencyCol("TK (1%)", e -> BpjsDynamicCalculator.calcJpEmployee(e, config));
        colJpGroup.getColumns().addAll(List.of(colJpPk, colJpTk));

        TableColumn<EmployeeBpjs, Double> colTotal = createCurrencyCol("Total Transfer (Rp)", e -> BpjsDynamicCalculator.calcTotalIuran(e, config));
        colTotal.setStyle("-fx-font-weight: bold;");

        tableBpjs.getColumns().clear();
        tableBpjs.getColumns().addAll(List.of(colNo, colProject, colLokasi, colNama, colGapok, colJkk, colJkm, colJhtGroup, colJpGroup, colTotal));
    }

    private TableColumn<EmployeeBpjs, Double> createCurrencyCol(String title, java.util.function.Function<EmployeeBpjs, Double> mapper) {
        TableColumn<EmployeeBpjs, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleObjectProperty<>(mapper.apply(c.getValue())));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null || val == 0.0) {
                    setText(empty || val == null ? null : "-");
                } else {
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    setText(fmt.format(val));
                }
            }
        });
        return col;
    }

    private void showEmployeeDialog(EmployeeBpjs targetEmp) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(targetEmp == null ? "Tambah Data Temporary" : "Edit Data Temporary");
        dialog.setHeaderText("Form Input/Edit Karyawan BPJS");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField txtNamaProject = new TextField(targetEmp != null && targetEmp.getNamaProject() != null ? targetEmp.getNamaProject() : "UNIT BSD II");
        TextField txtLokasiProject = new TextField(targetEmp != null && targetEmp.getLokasiProject() != null ? targetEmp.getLokasiProject() : "");
        TextField txtNama = new TextField(targetEmp != null && targetEmp.getNama() != null ? targetEmp.getNama() : "");
        TextField txtJabatan = new TextField(targetEmp != null && targetEmp.getJabatan() != null ? targetEmp.getJabatan() : "Teknisi");
        TextField txtGapok = new TextField(targetEmp != null ? String.valueOf((long) targetEmp.getGapok()) : "4551451");

        CheckBox chkJp = new CheckBox("Peserta Jaminan Pensiun (JP)");
        chkJp.setSelected(targetEmp == null || targetEmp.isJpActive());

        grid.add(new Label("Nama Unit/Project:"), 0, 0); grid.add(txtNamaProject, 1, 0);
        grid.add(new Label("Lokasi Project:"), 0, 1);    grid.add(txtLokasiProject, 1, 1);
        grid.add(new Label("Nama Karyawan:"), 0, 2);     grid.add(txtNama, 1, 2);
        grid.add(new Label("Jabatan:"), 0, 3);           grid.add(txtJabatan, 1, 3);
        grid.add(new Label("Gaji Pokok (Rp):"), 0, 4);   grid.add(txtGapok, 1, 4);
        grid.add(chkJp, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String proj = txtNamaProject.getText().trim();
                    String lok = txtLokasiProject.getText().trim();
                    String nama = txtNama.getText().trim();
                    String jab = txtJabatan.getText().trim();
                    double gapok = Double.parseDouble(txtGapok.getText().replaceAll("[^0-9.]", ""));

                    if (!unitList.contains(proj)) {
                        unitList.add(proj);
                    }

                    if (targetEmp == null) {
                        int newNo = memoryEmployeeList.size() + 1;
                        memoryEmployeeList.add(new EmployeeBpjs(newNo, proj, lok, nama, jab, gapok, chkJp.isSelected()));
                    } else {
                        targetEmp.setNamaProject(proj);
                        targetEmp.setLokasiProject(lok);
                        targetEmp.setNama(nama);
                        targetEmp.setJabatan(jab);
                        targetEmp.setGapok(gapok);
                        targetEmp.setJpActive(chkJp.isSelected());
                    }

                    tableBpjs.refresh();
                    updateSummary();

                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error Input", "Format Gaji Pokok tidak valid!");
                }
            }
        });
    }

    private VBox createSummaryCard(String title, Label lblVal, String colorHex) {
        Label lblT = new Label(title);
        lblT.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
        lblVal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        VBox card = new VBox(4, lblT, lblVal);
        card.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        card.setPadding(new Insets(10, 20, 10, 20));
        return card;
    }

    private void updateSummary() {
        double totalGapok = filteredList.stream().mapToDouble(EmployeeBpjs::getGapok).sum();
        double totalIuran = filteredList.stream().mapToDouble(e -> BpjsDynamicCalculator.calcTotalIuran(e, config)).sum();

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        lblTotalGapok.setText(fmt.format(totalGapok));
        lblTotalIuran.setText(fmt.format(totalIuran));
    }

    private void handleExportExcel() {
        if (filteredList == null || filteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada data untuk diexport!");
            return;
        }

        String selectedProject = cbFilterProject.getValue() != null ? cbFilterProject.getValue() : "SEMUA_PROJECT";
        String safeFileName = "REKAP_BPJS_TK_" + selectedProject.replaceAll("[^a-zA-Z0-9.-]", "_") + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Rekapitulasi BPJS");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName(safeFileName);

        File file = fileChooser.showSaveDialog(tableBpjs.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("BPJS TK");
            sheet.setDisplayGridlines(true);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));

            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("REKAPITULASI PERHITUNGAN BPJS KETENAGAKERJAAN - " + selectedProject);

            Row rHead = sheet.createRow(2);
            String[] headers = {"No.", "Project", "Lokasi", "Nama Karyawan", "Jabatan", "Gapok (Rp)", "JKK 0.24%", "JKM 0.30%", "JHT PK 3.7%", "JHT TK 2%", "JP PK 2%", "JP TK 1%", "Total Iuran (Rp)"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = rHead.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            for (EmployeeBpjs emp : filteredList) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(emp.getNo());
                r.createCell(1).setCellValue(emp.getNamaProject() != null ? emp.getNamaProject() : "");
                r.createCell(2).setCellValue(emp.getLokasiProject() != null ? emp.getLokasiProject() : "");
                r.createCell(3).setCellValue(emp.getNama() != null ? emp.getNama() : "");
                r.createCell(4).setCellValue(emp.getJabatan() != null ? emp.getJabatan() : "");

                createNumericCell(r, 5, emp.getGapok(), currencyStyle);
                createNumericCell(r, 6, BpjsDynamicCalculator.calcJkk(emp, config), currencyStyle);
                createNumericCell(r, 7, BpjsDynamicCalculator.calcJkm(emp, config), currencyStyle);
                createNumericCell(r, 8, BpjsDynamicCalculator.calcJhtCompany(emp, config), currencyStyle);
                createNumericCell(r, 9, BpjsDynamicCalculator.calcJhtEmployee(emp, config), currencyStyle);
                createNumericCell(r, 10, BpjsDynamicCalculator.calcJpCompany(emp, config), currencyStyle);
                createNumericCell(r, 11, BpjsDynamicCalculator.calcJpEmployee(emp, config), currencyStyle);
                createNumericCell(r, 12, BpjsDynamicCalculator.calcTotalIuran(emp, config), currencyStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            showAlert(Alert.AlertType.INFORMATION, "Sukses Export", "File rekap berhasil disimpan!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Export", "Gagal menyimpan file: " + e.getMessage());
        }
    }

    private void handleExportPdf() {
        if (filteredList == null || filteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada data untuk diexport ke PDF!");
            return;
        }

        String selectedProject = cbFilterProject.getValue() != null ? cbFilterProject.getValue() : "SEMUA_PROJECT";
        String safeFileName = "REKAP_BPJS_TK_" + selectedProject.replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Rekapitulasi BPJS (PDF)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName(safeFileName);

        File file = fileChooser.showSaveDialog(tableBpjs.getScene().getWindow());
        if (file == null) return;

        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("REKAPITULASI PERHITUNGAN BPJS KETENAGAKERJAAN - " + selectedProject, titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(13);
            table.setWidthPercentage(100);

            table.setWidths(new float[]{1, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2});

            String[] headers = {"No.", "Project", "Lokasi", "Nama Karyawan", "Jabatan", "Gapok (Rp)", "JKK 0.24%", "JKM 0.30%", "JHT PK 3.7%", "JHT TK 2%", "JP PK 2%", "JP TK 1%", "Total Iuran"};
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);

            for (String header : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);
            NumberFormat fmt = NumberFormat.getNumberInstance(new java.util.Locale("id", "ID"));

            for (EmployeeBpjs emp : filteredList) {
                table.addCell(new com.itextpdf.text.Phrase(String.valueOf(emp.getNo()), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(emp.getNamaProject() != null ? emp.getNamaProject() : "", dataFont));
                table.addCell(new com.itextpdf.text.Phrase(emp.getLokasiProject() != null ? emp.getLokasiProject() : "", dataFont));
                table.addCell(new com.itextpdf.text.Phrase(emp.getNama() != null ? emp.getNama() : "", dataFont));
                table.addCell(new com.itextpdf.text.Phrase(emp.getJabatan() != null ? emp.getJabatan() : "", dataFont));

                table.addCell(new com.itextpdf.text.Phrase(fmt.format(emp.getGapok()), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcJkk(emp, config)), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcJkm(emp, config)), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcJhtCompany(emp, config)), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcJhtEmployee(emp, config)), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcJpCompany(emp, config)), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcJpEmployee(emp, config)), dataFont));

                com.itextpdf.text.pdf.PdfPCell totalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(BpjsDynamicCalculator.calcTotalIuran(emp, config)), dataFont));
                totalCell.setBackgroundColor(new com.itextpdf.text.BaseColor(240, 240, 240));
                table.addCell(totalCell);
            }

            document.add(table);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Sukses Export", "File rekap PDF berhasil disimpan!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Export", "Gagal menyimpan file PDF: " + e.getMessage());
        }
    }

    private void createNumericCell(Row row, int colIndex, double value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}