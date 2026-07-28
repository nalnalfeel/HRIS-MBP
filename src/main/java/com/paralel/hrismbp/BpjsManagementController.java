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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BpjsManagementController {

    private final BpjsConfig config = new BpjsConfig();
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

        resetDataInMemory();

        Label lblTitle = new Label("Kalkulator & Rekap BPJS TK (Temporary)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        Label lblBadge = new Label("  Mode In-Memory (Tanpa DB)");
        lblBadge.setStyle("-fx-background-color: #feebc8; -fx-text-fill: #744210; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 4px;");

        cbFilterProject.setItems(unitList);
        cbFilterProject.getSelectionModel().select("SEMUA PROJECT");
        cbFilterProject.setOnAction(e -> applyFilter());

        txtSearch.setPromptText("Cari Nama / Lokasi...");
        txtSearch.setPrefWidth(160);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        Button btnImport = new Button("  Import Excel");
        btnImport.setStyle("-fx-background-color: #319795; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnImport.setMinWidth(110);
        btnImport.setOnAction(e -> handleImportExcel());

        Button btnAdd = new Button("+ Baris Baru");
        btnAdd.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnAdd.setMinWidth(110);
        btnAdd.setOnAction(e -> showEmployeeDialog(null));

        Button btnEdit = new Button("  Edit");
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

        Button btnDelete = new Button("  Hapus");
        btnDelete.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnDelete.setMinWidth(90);
        btnDelete.setOnAction(e -> handleDeleteSelected());

        Button btnReset = new Button("  Reset / Clear");
        btnReset.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnReset.setMinWidth(110);
        btnReset.setOnAction(e -> handleResetAll());

        HBox titleBox = new HBox(8, lblTitle, lblBadge);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        HBox topBox = new HBox(8, titleBox, new Region(), cbFilterProject, txtSearch, btnImport, btnAdd, btnEdit, btnDelete, btnReset);
        topBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(topBox.getChildren().get(1), Priority.ALWAYS);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        root.setTop(topBox);

        setupColumns();
        tableBpjs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filteredList = new FilteredList<>(memoryEmployeeList, p -> true);
        tableBpjs.setItems(filteredList);

        root.setCenter(tableBpjs);

        Button btnExport = new Button("  Export Hasil (.xlsx)");
        btnExport.setStyle("-fx-background-color: #2f855a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnExport.setMinWidth(150);
        btnExport.setOnAction(e -> showExportDialog(true));

        Button btnExportPdf = new Button("  Export PDF (.pdf)");
        btnExportPdf.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnExportPdf.setMinWidth(140);
        btnExportPdf.setOnAction(e -> showExportDialog(false));

        HBox exportBox = new HBox(10, btnExport, btnExportPdf);
        exportBox.setAlignment(Pos.CENTER_LEFT);

        HBox summaryBox = new HBox(20,
                createSummaryCard("Total Gapok Diolah", lblTotalGapok, "#2d3748"),
                createSummaryCard("Total Transfer Iuran BPJS TK", lblTotalIuran, "#e53e3e")
        );
        summaryBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomLayout = new HBox(exportBox, spacer, summaryBox);
        bottomLayout.setPadding(new Insets(15, 0, 0, 0));
        bottomLayout.setAlignment(Pos.CENTER);

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

    private VBox createSummaryCard(String title, Label lblVal, String accentColorHex) {
        Label lblT = new Label(title);
        lblT.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblVal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Indicator Line Accent
        Region indicator = new Region();
        indicator.setPrefHeight(3);
        indicator.setStyle("-fx-background-color: " + accentColorHex + "; -fx-background-radius: 2px;");

        VBox card = new VBox(6, lblT, lblVal, indicator);
        card.setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 4);");
        card.setPadding(new Insets(12, 18, 12, 18));
        card.setMinWidth(180);
        return card;
    }

    private void setupTableEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);

        Label lblEmpty = new Label("Belum ada data BPJS yang dimuat");
        lblEmpty.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        Label lblSub = new Label("Klik '+ Baris Baru' atau 'Import Excel' untuk mulai mengolah data.");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        emptyState.getChildren().addAll(lblEmpty, lblSub);
        tableBpjs.setPlaceholder(emptyState);
    }

    private void updateSummary() {
        double totalGapok = filteredList.stream().mapToDouble(EmployeeBpjs::getGapok).sum();
        double totalIuran = filteredList.stream().mapToDouble(e -> BpjsDynamicCalculator.calcTotalIuran(e, config)).sum();

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        lblTotalGapok.setText(fmt.format(totalGapok));
        lblTotalIuran.setText(fmt.format(totalIuran));
    }

    private void showExportDialog(boolean isExcel) {
        if (filteredList == null || filteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada data untuk diexport!");
            return;
        }

        String selectedProject = cbFilterProject.getValue() != null && !cbFilterProject.getValue().equals("SEMUA PROJECT")
                ? cbFilterProject.getValue() : "JAKARTA";

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isExcel ? "Kustomisasi Header Export Excel" : "Kustomisasi Header Export PDF");
        dialog.setHeaderText("Sesuaikan Judul & Periode Laporan Bulanan (Dinamis)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField txtCompany = new TextField("PT MULTI BINA PRAKARSA");
        TextField txtUnit = new TextField(selectedProject);
        TextField txtBulan = new TextField(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"))).toUpperCase());
        TextField txtNpp = new TextField("JJ043691");

        grid.add(new Label("Nama Perusahaan:"), 0, 0); grid.add(txtCompany, 1, 0);
        grid.add(new Label("Unit / Project:"), 0, 1);    grid.add(txtUnit, 1, 1);
        grid.add(new Label("Bulan / Periode:"), 0, 2);   grid.add(txtBulan, 1, 2);
        grid.add(new Label("No NPP / Ref:"), 0, 3);      grid.add(txtNpp, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String company = txtCompany.getText().trim();
                String unit = txtUnit.getText().trim();
                String bulan = txtBulan.getText().trim();
                String npp = txtNpp.getText().trim();

                if (isExcel) {
                    handleExportExcelCustom(company, unit, bulan, npp);
                } else {
                    handleExportPdfCustom(company, unit, bulan, npp);
                }
            }
        });
    }



    private void handleExportExcelCustom(String company, String unit, String bulan, String npp) {
        String safeFileName = "PERHITUNGAN_BPJSTK_" + unit.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + bulan.replaceAll(" ", "_") + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Rekapitulasi BPJS (Excel)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName(safeFileName);
        File file = fileChooser.showSaveDialog(tableBpjs.getScene().getWindow());

        // Dialog Loading
        Dialog<Void> loadingDialog = new Dialog<>();
        loadingDialog.setTitle("Memproses Export");

        ProgressIndicator progress = new ProgressIndicator();
        Label lblStatus = new Label("Sedang membuat dokumen Excel...");

        VBox content = new VBox(15, progress, lblStatus);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(25));

        loadingDialog.getDialogPane().setContent(content);
        loadingDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Task Background
        /*javafx.concurrent.Task<Void> exportTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                // Panggil logika export excel kustom Anda di sini
                handleExportExcelCustomLogic(company, unit, bulan, npp, saveFile);
                return null;
            }
        };
*/
        /*exportTask.setOnFailed(e -> {
            loadingDialog.close();
            showAlert(Alert.AlertType.ERROR, "Error Export", "Gagal menyimpan file: " + exportTask.getException().getMessage());
        });

        // Jalankan di thread terpisah
        Thread thread = new Thread(exportTask);
        thread.setDaemon(true);
        thread.start();

        loadingDialog.showAndWait();*/

        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(bulan);
            sheet.setDisplayGridlines(true);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);

            Row r1 = sheet.createRow(1);
            Cell c1 = r1.createCell(1);
            c1.setCellValue(company + " UNIT " + unit);
            c1.setCellStyle(titleStyle);

            Row r2 = sheet.createRow(2);
            Cell c2 = r2.createCell(1);
            c2.setCellValue("BPJS KETENAGAKERJAAN");
            c2.setCellStyle(titleStyle);

            Row r3 = sheet.createRow(3);
            Cell c3 = r3.createCell(1);
            c3.setCellValue("BULAN " + bulan);
            c3.setCellStyle(titleStyle);

            Row r4 = sheet.createRow(4);
            Cell c4 = r4.createCell(11); // Digeser karena total kolom tinggal 11
            c4.setCellValue("NPP: " + npp);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setWrapText(true);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);



            Row r5 = sheet.createRow(5);
            Row r6 = sheet.createRow(6);
            Row r7 = sheet.createRow(7);

            // Total kolom sekarang menjadi 11
            for (int r = 5; r <= 7; r++) {
                Row row = sheet.getRow(r);
                for (int c = 1; c <= 11; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellStyle(headerStyle);
                }
            }

            r5.getCell(1).setCellValue("NO");
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 1, 1));

            // KTP, KPJ, BPJS Kes Dihapus, digeser langsung ke CABANG dkk
            r5.getCell(2).setCellValue("CABANG");
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 2, 2));

            r5.getCell(3).setCellValue("NAMA");
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 3, 3));

            r5.getCell(4).setCellValue("GAPOK " + LocalDate.now().getYear());
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 4, 4));

            r5.getCell(5).setCellValue("Iuran JKK 0,24% (Rp)");
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 5, 5));

            r5.getCell(6).setCellValue("Iuran JKM 0,3% (Rp)");
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 6, 6));

            r5.getCell(7).setCellValue("Iuran JHT TK");
            sheet.addMergedRegion(new CellRangeAddress(5, 6, 7, 8));
            r7.getCell(7).setCellValue("Pemberi Kerja 3,7 % (Rp)");
            r7.getCell(8).setCellValue("Tenaga Kerja 2% (Rp)");

            r5.getCell(9).setCellValue("Iuran JP TK");
            sheet.addMergedRegion(new CellRangeAddress(5, 6, 9, 10));
            r7.getCell(9).setCellValue("Pemberi Kerja 2% (Rp)");
            r7.getCell(10).setCellValue("Tenaga Kerja 1% (Rp)");

            r5.getCell(11).setCellValue("Total Iuran (Rp)");
            sheet.addMergedRegion(new CellRangeAddress(5, 7, 11, 11));

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setBorderTop(BorderStyle.THIN);
            currencyStyle.setBorderBottom(BorderStyle.THIN);
            currencyStyle.setBorderLeft(BorderStyle.THIN);
            currencyStyle.setBorderRight(BorderStyle.THIN);
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0.00"));

            int rowIdx = 8;
            double sumGapok = 0, sumJkk = 0, sumJkm = 0, sumJhtPk = 0, sumJhtTk = 0, sumJpPk = 0, sumJpTk = 0, grandTotal = 0;

            for (EmployeeBpjs emp : filteredList) {
                Row r = sheet.createRow(rowIdx++);
                for (int c = 1; c <= 11; c++) {
                    r.createCell(c).setCellStyle(dataStyle);
                }

                r.getCell(1).setCellValue(emp.getNo());
                r.getCell(2).setCellValue(emp.getLokasiProject() != null ? emp.getLokasiProject() : "Jakarta");
                r.getCell(3).setCellValue(emp.getNama() != null ? emp.getNama().toUpperCase() : "");

                r.getCell(4).setCellValue(emp.getGapok());
                r.getCell(4).setCellStyle(currencyStyle);
                sumGapok += emp.getGapok();

                double jkk = BpjsDynamicCalculator.calcJkk(emp, config);
                r.getCell(5).setCellValue(jkk);
                r.getCell(5).setCellStyle(currencyStyle);
                sumJkk += jkk;

                double jkm = BpjsDynamicCalculator.calcJkm(emp, config);
                r.getCell(6).setCellValue(jkm);
                r.getCell(6).setCellStyle(currencyStyle);
                sumJkm += jkm;

                double jhtPk = BpjsDynamicCalculator.calcJhtCompany(emp, config);
                r.getCell(7).setCellValue(jhtPk);
                r.getCell(7).setCellStyle(currencyStyle);
                sumJhtPk += jhtPk;

                double jhtTk = BpjsDynamicCalculator.calcJhtEmployee(emp, config);
                r.getCell(8).setCellValue(jhtTk);
                r.getCell(8).setCellStyle(currencyStyle);
                sumJhtTk += jhtTk;

                double jpPk = BpjsDynamicCalculator.calcJpCompany(emp, config);
                r.getCell(9).setCellValue(jpPk);
                r.getCell(9).setCellStyle(currencyStyle);
                sumJpPk += jpPk;

                double jpTk = BpjsDynamicCalculator.calcJpEmployee(emp, config);
                r.getCell(10).setCellValue(jpTk);
                r.getCell(10).setCellStyle(currencyStyle);
                sumJpTk += jpTk;

                double totalIuran = BpjsDynamicCalculator.calcTotalIuran(emp, config);
                r.getCell(11).setCellValue(totalIuran);
                r.getCell(11).setCellStyle(currencyStyle);
                grandTotal += totalIuran;
            }

            Row rTotal = sheet.createRow(rowIdx);
            for (int c = 1; c <= 11; c++) {
                Cell cell = rTotal.createCell(c);
                cell.setCellStyle(headerStyle);
            }
            rTotal.getCell(1).setCellValue("JUMLAH");
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 1, 3)); // Merge sampai nama

            rTotal.getCell(4).setCellValue(sumGapok); rTotal.getCell(4).setCellStyle(currencyStyle);
            rTotal.getCell(5).setCellValue(sumJkk); rTotal.getCell(5).setCellStyle(currencyStyle);
            rTotal.getCell(6).setCellValue(sumJkm); rTotal.getCell(6).setCellStyle(currencyStyle);
            rTotal.getCell(7).setCellValue(sumJhtPk); rTotal.getCell(7).setCellStyle(currencyStyle);
            rTotal.getCell(8).setCellValue(sumJhtTk); rTotal.getCell(8).setCellStyle(currencyStyle);
            rTotal.getCell(9).setCellValue(sumJpPk); rTotal.getCell(9).setCellStyle(currencyStyle);
            rTotal.getCell(10).setCellValue(sumJpTk); rTotal.getCell(10).setCellStyle(currencyStyle);
            rTotal.getCell(11).setCellValue(grandTotal); rTotal.getCell(11).setCellStyle(currencyStyle);

            for (int i = 1; i <= 11; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            showAlert(Alert.AlertType.INFORMATION, "Sukses Export", "File Excel berhasil disimpan dengan header kustom!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Export", "Gagal menyimpan file Excel: " + e.getMessage());
        }
    }

    private void handleExportPdfCustom(String company, String unit, String bulan, String npp) {
        String safeFileName = "PERHITUNGAN_BPJSTK_" + unit.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + bulan.replaceAll(" ", "_") + ".pdf";

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

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);

            com.itextpdf.text.Paragraph p1 = new com.itextpdf.text.Paragraph(company + " UNIT " + unit, titleFont);
            com.itextpdf.text.Paragraph p2 = new com.itextpdf.text.Paragraph("BPJS KETENAGAKERJAAN", titleFont);
            com.itextpdf.text.Paragraph p3 = new com.itextpdf.text.Paragraph("BULAN " + bulan, titleFont);
            com.itextpdf.text.Paragraph p4 = new com.itextpdf.text.Paragraph("NPP: " + npp, new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL));

            p4.setSpacingAfter(10);
            document.add(p1);
            document.add(p2);
            document.add(p3);
            document.add(p4);

            // Ubah menjadi 11 Kolom
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(11);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 3, 2.5f, 2, 2, 2, 2, 2, 2, 2.5f});

            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 7, com.itextpdf.text.Font.NORMAL);

            String[] headers = {
                    "NO", "CABANG", "NAMA", "GAPOK " + LocalDate.now().getYear(),
                    "Iuran JKK 0,24%", "Iuran JKM 0,3%", "JHT PK 3,7%", "JHT TK 2%", "JP PK 2%", "JP TK 1%", "Total Iuran"
            };

            for (String header : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);

                // UBAH BARIS INI (Gunakan kode RGB 144, 238, 144 untuk Hijau Muda):
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(144, 238, 144));

                cell.setPadding(4);
                table.addCell(cell);
            }

            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            fmt.setMinimumFractionDigits(2);
            fmt.setMaximumFractionDigits(2);

            double sumGapok = 0, sumJkk = 0, sumJkm = 0, sumJhtPk = 0, sumJhtTk = 0, sumJpPk = 0, sumJpTk = 0, grandTotal = 0;

            for (EmployeeBpjs emp : filteredList) {
                table.addCell(new com.itextpdf.text.Phrase(String.valueOf(emp.getNo()), dataFont));
                table.addCell(new com.itextpdf.text.Phrase(emp.getLokasiProject() != null ? emp.getLokasiProject() : "Jakarta", dataFont));
                table.addCell(new com.itextpdf.text.Phrase(emp.getNama() != null ? emp.getNama().toUpperCase() : "", dataFont));

                table.addCell(new com.itextpdf.text.Phrase(fmt.format(emp.getGapok()), dataFont));
                sumGapok += emp.getGapok();

                double jkk = BpjsDynamicCalculator.calcJkk(emp, config);
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(jkk), dataFont));
                sumJkk += jkk;

                double jkm = BpjsDynamicCalculator.calcJkm(emp, config);
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(jkm), dataFont));
                sumJkm += jkm;

                double jhtPk = BpjsDynamicCalculator.calcJhtCompany(emp, config);
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(jhtPk), dataFont));
                sumJhtPk += jhtPk;

                double jhtTk = BpjsDynamicCalculator.calcJhtEmployee(emp, config);
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(jhtTk), dataFont));
                sumJhtTk += jhtTk;

                double jpPk = BpjsDynamicCalculator.calcJpCompany(emp, config);
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(jpPk), dataFont));
                sumJpPk += jpPk;

                double jpTk = BpjsDynamicCalculator.calcJpEmployee(emp, config);
                table.addCell(new com.itextpdf.text.Phrase(fmt.format(jpTk), dataFont));
                sumJpTk += jpTk;

                double totalIuran = BpjsDynamicCalculator.calcTotalIuran(emp, config);
                com.itextpdf.text.pdf.PdfPCell totalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(totalIuran), dataFont));
                totalCell.setBackgroundColor(new com.itextpdf.text.BaseColor(240, 240, 240));
                table.addCell(totalCell);
                grandTotal += totalIuran;
            }

            // Total Row
            com.itextpdf.text.pdf.PdfPCell footerLabelCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("JUMLAH", headerFont));
            footerLabelCell.setColspan(3); // Merge untuk kolom NO, CABANG, dan NAMA
            footerLabelCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            footerLabelCell.setBackgroundColor(new com.itextpdf.text.BaseColor(144, 238, 144));
            table.addCell(footerLabelCell);

            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumGapok), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumJkk), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumJkm), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumJhtPk), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumJhtTk), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumJpPk), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(sumJpTk), headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(fmt.format(grandTotal), headerFont)));

            document.add(table);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Sukses Export", "File rekap PDF berhasil disimpan dengan header kustom!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Export", "Gagal menyimpan file PDF: " + e.getMessage());
        }
    }

    // Function pendukung tambahan tetap ada
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