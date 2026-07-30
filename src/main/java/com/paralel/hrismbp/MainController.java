package com.paralel.hrismbp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.apache.poi.ss.util.CellUtil.createCell;

public class MainController {

    @FXML private TextField txtNik, txtFullName, txtPhone, txtBankAccount, txtBpjsTk, txtBpjsKesehatan, txtSalary, txtSearch, txtBirthPlace, txtBirthDate;
    @FXML private TextArea txtAddress;
    @FXML private DatePicker dpBirthDate;
    @FXML private ComboBox<String> cbGender, cbEducation, cbProjectName, cbLanguage;
    @FXML private ComboBox<Integer> cbBirthYear;

    @FXML private TableView<Employee> tableEmployees;
    @FXML private TableColumn<Employee, String> colNik, colName, colBirthDate, colProject, colGender, colPhone, colEducation, colBpjsTk, colBpjsKes, colAddress, colEmployeeId, colFamilyCard;
    @FXML private TableColumn<Employee, Double> colSalary;

    @FXML private TextField txtEmployeeId, txtFamilyCardNumber;

    @FXML private Label lblLanguage, lblEmployeeId, lblNik, lblFamilyCard, lblFullName, lblAddress, lblProject, lblSalary;
    @FXML private Button btnSave, btnUpdate, btnDelete, btnClear, btnExport, btnImport, btnReminders, btnLogout;

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredData;
    private Employee selectedEmployee = null;
    private ResourceBundle bundle;
    private List<Employee> employeeLists = new ArrayList<>();

    @FXML
    private void handleOpenBpjsMultiUnit() {
        try {
            Stage bpjsStage = new Stage();
            bpjsStage.setTitle("Sistem Perhitungan BPJS Ketenagakerjaan Multi-Unit");

            // Inisialisasi controller BPJS Management
            BpjsManagementController controller = new BpjsManagementController();

            // Mengambil daftar karyawan aktif dari aplikasi utama
            List<Employee> activeEmployees = getEmployeeListFromDatabase();
            BorderPane bpjsView = controller.createView(activeEmployees);

            Scene scene = new Scene(bpjsView, 1250, 680);
            bpjsStage.setScene(scene);

            // Terapkan Logo App ke Window Baru
            applyAppIcon(bpjsStage);

            bpjsStage.show();

        } catch (Exception e) {
            showAlert("Error", "Gagal membuka modul BPJS TK: " + e.getMessage());
            e.printStackTrace(); // Bermanfaat untuk debug di console
        }
    }

    private List<Employee> getEmployeeListFromDatabase() {
        // JIKA MENGGUNAKAN LIST VARIABLE YANG SUDAH ADA DI MAIN CONTROLLER:
        if (this.employeeLists != null && !this.employeeLists.isEmpty()) {
            return this.employeeLists;
        }
        // JIKA MASIH CONTOH / MOCKUP DATA SEMENTARA:
        List<Employee> dummyList = new ArrayList<>();
        // dummyList.add(new Employee("E001", "3171000000000001", "HENNY KOESOEMAHARJATI", "HO MBP", 4901798.0));
        // dummyList.add(new Employee("E002", "3171000000000002", "YANUAR DWI PUTRANTO", "HO MBP", 4901798.0));
        return dummyList;
    }

    private void applyAppIcon(Stage stage) {
        try {
            InputStream iconStream = getClass().getResourceAsStream("/com/paralel/hrismbp/LOGO.png");
            if (iconStream != null) {
                stage.getIcons().clear();
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
            // Icon optional, abaikan jika tidak ditemukan
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateLanguageUI() {
        if (lblLanguage != null) lblLanguage.setText(LanguageManager.get("lbl.language"));
        if (lblEmployeeId != null) lblEmployeeId.setText(LanguageManager.get("lbl.emp_id"));
        if (lblNik != null) lblNik.setText(LanguageManager.get("lbl.nik"));
        if (lblFamilyCard != null) lblFamilyCard.setText(LanguageManager.get("lbl.family_card"));
        if (lblFullName != null) lblFullName.setText(LanguageManager.get("lbl.name"));
        if (lblAddress != null) lblAddress.setText(LanguageManager.get("lbl.address"));
        if (lblProject != null) lblProject.setText(LanguageManager.get("lbl.project"));
        if (lblSalary != null) lblSalary.setText(LanguageManager.get("lbl.salary"));

        if (btnSave != null) btnSave.setText(LanguageManager.get("btn.save"));
        if (btnUpdate != null) btnUpdate.setText(LanguageManager.get("btn.update"));
        if (btnDelete != null) btnDelete.setText(LanguageManager.get("btn.delete"));
        if (btnClear != null) btnClear.setText(LanguageManager.get("btn.clear"));
        if (btnExport != null) btnExport.setText(LanguageManager.get("btn.export"));
        if (btnImport != null) btnImport.setText(LanguageManager.get("btn.import"));
        if (btnReminders != null) btnReminders.setText(LanguageManager.get("btn.reminders"));
        if (btnLogout != null) btnLogout.setText(LanguageManager.get("btn.logout"));

        // Header TableView 1 Kata
        if (colEmployeeId != null) colEmployeeId.setText(LanguageManager.get("column.employee_id"));
        if (colNik != null) colNik.setText(LanguageManager.get("column.nik"));
        if (colFamilyCard != null) colFamilyCard.setText(LanguageManager.get("column.family_card"));
        if (colName != null) colName.setText(LanguageManager.get("column.name"));
        if (colAddress != null) colAddress.setText(LanguageManager.get("column.address"));
        if (colProject != null) colProject.setText(LanguageManager.get("column.project"));
        if (colGender != null) colGender.setText(LanguageManager.get("column.gender"));
        if (colPhone != null) colPhone.setText(LanguageManager.get("column.phone"));
        if (colEducation != null) colEducation.setText(LanguageManager.get("column.education"));
        if (colSalary != null) colSalary.setText(LanguageManager.get("column.salary"));
        if (colBpjsTk != null) colBpjsTk.setText(LanguageManager.get("column.bpjs_tk"));
        if (colBpjsKes != null) colBpjsKes.setText(LanguageManager.get("column.bpjs_kes"));
    }

    private void setLanguage(String langCode) {
        if ("en".equalsIgnoreCase(langCode)) {
            bundle = ResourceBundle.getBundle("messages", new Locale("en", "US"));
        } else {
            bundle = ResourceBundle.getBundle("messages", new Locale("id", "ID"));
        }

        btnSave.setText(bundle.getString("btn.save"));
        btnUpdate.setText(bundle.getString("btn.update"));
        btnDelete.setText(bundle.getString("btn.delete"));
        btnClear.setText(bundle.getString("btn.clear"));
        btnExport.setText(bundle.getString("btn.export"));
        btnImport.setText(bundle.getString("btn.import"));
        btnReminders.setText(bundle.getString("btn.reminders"));
        btnLogout.setText(bundle.getString("btn.logout"));

        colEmployeeId.setText(bundle.getString("lbl.emp_id"));
        colNik.setText(bundle.getString("lbl.nik"));
        colFamilyCard.setText(bundle.getString("lbl.family_card"));
        colName.setText(bundle.getString("lbl.name"));
        colAddress.setText(bundle.getString("lbl.address"));
        colProject.setText(bundle.getString("lbl.project"));
        colSalary.setText(bundle.getString("lbl.salary"));
    }



    @FXML
    public void initialize() {
        if (cbBirthYear != null) {
            int currentYear = LocalDate.now().getYear();
            for (int year = currentYear; year >= 1940; year--) {
                cbBirthYear.getItems().add(year);
            }
        }

        if (dpBirthDate != null) {
            dpBirthDate.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null && cbBirthYear != null) {
                    cbBirthYear.setValue(newDate.getYear());
                }
            });
        }

        if (cbLanguage != null) {
            cbLanguage.getItems().addAll("Bahasa Indonesia", "English");
            cbLanguage.setValue("Bahasa Indonesia");
            cbLanguage.setOnAction(e -> {
                String selected = cbLanguage.getValue();
                LanguageManager.setLanguage("English".equals(selected) ? "en" : "id");
                updateLanguageUI();
            });
        }

        tableEmployees.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Aktifkan mode pilih banyak baris di TableView
        if (tableEmployees != null) {
            tableEmployees.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        if (cbProjectName != null) cbProjectName.setItems(FXCollections.observableArrayList("PT Airfast Indonesia", "PT Salim Ivomas Pratama", "PT Mandom Indonesia", "PT Astra Credit Companies"));
        if (cbGender != null) cbGender.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));
        if (cbEducation != null) cbEducation.setItems(FXCollections.observableArrayList("Paket", "SMP", "SMA/SMK", "D3", "S1", "S2", "S3"));

        // BINDING TABLEVIEW (HANYA 1 KALI)
        if (colEmployeeId != null) colEmployeeId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().employeeId()));
        if (colNik != null) colNik.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nik()));
        if (colFamilyCard != null) colFamilyCard.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().familyCardNumber()));
        if (colName != null) colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fullName()));
        if (colBirthDate != null) colBirthDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().birthDate()));
        if (colAddress != null) colAddress.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().address()));
        if (colProject != null) colProject.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().projectName()));
        if (colGender != null) colGender.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().gender()));
        if (colPhone != null) colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().phoneNumber()));
        if (colEducation != null) colEducation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().education()));
        if (colBpjsTk != null) colBpjsTk.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().bpjsTk()));
        if (colBpjsKes != null) colBpjsKes.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().bpjsKesehatan()));

        if (colSalary != null) {
            colSalary.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().salary()));
            colSalary.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        setText(formatRupiah.format(price));
                    }
                }
            });
        }

        // FILTER SEARCH
        filteredData = new FilteredList<>(employeeList, p -> true);
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(emp -> {
                    if (newVal == null || newVal.isBlank()) return true;
                    String lower = newVal.toLowerCase();
                    return (emp.nik() != null && emp.nik().toLowerCase().contains(lower)) ||
                            (emp.fullName() != null && emp.fullName().toLowerCase().contains(lower)) ||
                            (emp.projectName() != null && emp.projectName().toLowerCase().contains(lower)) ||
                            (emp.employeeId() != null && emp.employeeId().toLowerCase().contains(lower));
                });
            });
        }

        // LISTEN SELEKSI BARIS TABLEVIEW
        if (tableEmployees != null) {
            tableEmployees.setItems(filteredData);
            tableEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedEmployee = newVal;
                    if (txtEmployeeId != null) txtEmployeeId.setText(newVal.employeeId());
                    if (txtNik != null) txtNik.setText(newVal.nik());
                    if (txtFamilyCardNumber != null) txtFamilyCardNumber.setText(newVal.familyCardNumber());
                    if (txtFullName != null) txtFullName.setText(newVal.fullName());
                    if (cbProjectName != null) cbProjectName.setValue(newVal.projectName());
                    if (txtBirthPlace != null) txtBirthPlace.setText(newVal.birthPlace());
                    // Ganti logika set dpBirthDate menjadi:
                    if (txtBirthDate != null) txtBirthDate.setText(newVal.birthDate());
                    if (txtAddress != null) txtAddress.setText(newVal.address());
                    if (txtPhone != null) txtPhone.setText(newVal.phoneNumber());
                    if (cbGender != null) cbGender.setValue(newVal.gender());
                    if (cbEducation != null) cbEducation.setValue(newVal.education());
                    if (txtBankAccount != null) txtBankAccount.setText(newVal.bankAccount());
                    if (txtBpjsTk != null) txtBpjsTk.setText(newVal.bpjsTk());
                    if (txtBpjsKesehatan != null) txtBpjsKesehatan.setText(newVal.bpjsKesehatan());
                    if (txtSalary != null) txtSalary.setText(String.valueOf(newVal.salary()));
                }
            });
        }

        loadDataFromDatabase();
    }


    @FXML
    private void handleBulkDelete() {
        ObservableList<Employee> selectedEmployees = tableEmployees.getSelectionModel().getSelectedItems();

        if (selectedEmployees == null || selectedEmployees.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih/sorot minimal satu karyawan yang ingin dihapus! (Gunakan Ctrl/Shift untuk memilih banyak)");
            return;
        }

        int totalCount = selectedEmployees.size();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus Massal");
        confirmAlert.setHeaderText("PERINGATAN: Hapus " + totalCount + " Data Karyawan!");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus " + totalCount + " data karyawan yang dipilih (termasuk karyawan resign)?\n\nData yang dihapus tidak dapat dikembalikan.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM employees WHERE id = ?";

            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                conn.setAutoCommit(false);

                for (Employee emp : selectedEmployees) {
                    pstmt.setInt(1, emp.id());
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();

                loadDataFromDatabase();
                clearForm();

                showAlert(Alert.AlertType.INFORMATION, "Sukses Hapus Massal", totalCount + " data karyawan berhasil dihapus dari sistem.");

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Gagal Hapus Massal", "Terjadi kesalahan database: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleYearChange() {
        if (cbBirthYear == null || dpBirthDate == null) return;
        Integer selectedYear = cbBirthYear.getValue();
        if (selectedYear != null) {
            LocalDate currentDate = dpBirthDate.getValue();
            if (currentDate != null) {
                dpBirthDate.setValue(LocalDate.of(selectedYear, currentDate.getMonth(), currentDate.getDayOfMonth()));
            } else {
                dpBirthDate.setValue(LocalDate.of(selectedYear, 1, 1));
            }
        }
    }

    public void loadDataFromDatabase() {
        employeeList.clear();
        String sql = "SELECT * FROM employees ORDER BY id DESC";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employeeList.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("employee_id") != null ? rs.getString("employee_id") : "",
                        rs.getString("nik") != null ? rs.getString("nik") : "",
                        rs.getString("family_card_number") != null ? rs.getString("family_card_number") : "",
                        rs.getString("full_name") != null ? rs.getString("full_name") : "",
                        rs.getString("project_name") != null ? rs.getString("project_name") : "",
                        rs.getString("birth_place") != null ? rs.getString("birth_place") : "",
                        rs.getString("birth_date") != null ? rs.getString("birth_date") : "",
                        rs.getString("address") != null ? rs.getString("address") : "",
                        rs.getString("phone_number") != null ? rs.getString("phone_number") : "",
                        rs.getString("gender") != null ? rs.getString("gender") : "",
                        rs.getString("education") != null ? rs.getString("education") : "",
                        rs.getString("bank_account") != null ? rs.getString("bank_account") : "",
                        rs.getString("bpjs_tk") != null ? rs.getString("bpjs_tk") : "",
                        rs.getString("bpjs_kesehatan") != null ? rs.getString("bpjs_kesehatan") : "",
                        rs.getDouble("salary")
                ));
            }

            if (tableEmployees != null) {
                tableEmployees.refresh();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (isNikInvalid()) return;

        String sql = """
            INSERT INTO employees (employee_id, nik, family_card_number, full_name, project_name, birth_date, address, phone_number, gender, education, bank_account, bpjs_tk, bpjs_kesehatan, salary)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtEmployeeId != null ? txtEmployeeId.getText().trim() : "");
            pstmt.setString(2, txtNik.getText().trim());
            pstmt.setString(3, txtFamilyCardNumber != null ? txtFamilyCardNumber.getText().trim() : "");
            pstmt.setString(4, txtFullName != null && txtFullName.getText() != null ? txtFullName.getText().trim() : "");
            pstmt.setString(5, cbProjectName != null && cbProjectName.getValue() != null ? cbProjectName.getValue() : "");
            // Gunakan teks dari txtBirthDate
            pstmt.setString(6, txtBirthDate != null && txtBirthDate.getText() != null ? txtBirthDate.getText().trim() : "");
            pstmt.setString(7, txtAddress != null && txtAddress.getText() != null ? txtAddress.getText().trim() : "");
            pstmt.setString(8, txtPhone != null && txtPhone.getText() != null ? txtPhone.getText().trim() : "");
            pstmt.setString(9, cbGender != null && cbGender.getValue() != null ? cbGender.getValue() : "");
            pstmt.setString(10, cbEducation != null && cbEducation.getValue() != null ? cbEducation.getValue() : "");
            pstmt.setString(11, txtBankAccount != null && txtBankAccount.getText() != null ? txtBankAccount.getText().trim() : "");
            pstmt.setString(12, txtBpjsTk != null && txtBpjsTk.getText() != null ? txtBpjsTk.getText().trim() : "");
            pstmt.setString(13, txtBpjsKesehatan != null && txtBpjsKesehatan.getText() != null ? txtBpjsKesehatan.getText().trim() : "");
            pstmt.setDouble(14, parseSalary(txtSalary != null ? txtSalary.getText() : "0"));

            pstmt.executeUpdate();
            loadDataFromDatabase();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Karyawan berhasil disimpan.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Terjadi kesalahan simpan: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data karyawan yang ingin diubah!");
            return;
        }
        if (isNikInvalid()) return;

        String sql = """
            UPDATE employees SET 
                employee_id=?, 
                nik=?, 
                family_card_number=?, 
                full_name=?, 
                project_name=?, 
                birth_date=?, 
                address=?, 
                phone_number=?, 
                gender=?, 
                education=?, 
                bank_account=?, 
                bpjs_tk=?, 
                bpjs_kesehatan=?, 
                salary=?
            WHERE id=?
        """;

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtEmployeeId != null ? txtEmployeeId.getText().trim() : "");
            pstmt.setString(2, txtNik.getText().trim());
            pstmt.setString(3, txtFamilyCardNumber != null ? txtFamilyCardNumber.getText().trim() : "");
            pstmt.setString(4, txtFullName != null && txtFullName.getText() != null ? txtFullName.getText().trim() : "");
            pstmt.setString(5, cbProjectName != null && cbProjectName.getValue() != null ? cbProjectName.getValue() : "");
            // Gunakan teks dari txtBirthDate
            pstmt.setString(6, txtBirthDate != null && txtBirthDate.getText() != null ? txtBirthDate.getText().trim() : "");
            pstmt.setString(7, txtAddress != null && txtAddress.getText() != null ? txtAddress.getText().trim() : "");
            pstmt.setString(8, txtPhone != null && txtPhone.getText() != null ? txtPhone.getText().trim() : "");
            pstmt.setString(9, cbGender != null && cbGender.getValue() != null ? cbGender.getValue() : "");
            pstmt.setString(10, cbEducation != null && cbEducation.getValue() != null ? cbEducation.getValue() : "");
            pstmt.setString(11, txtBankAccount != null && txtBankAccount.getText() != null ? txtBankAccount.getText().trim() : "");
            pstmt.setString(12, txtBpjsTk != null && txtBpjsTk.getText() != null ? txtBpjsTk.getText().trim() : "");
            pstmt.setString(13, txtBpjsKesehatan != null && txtBpjsKesehatan.getText() != null ? txtBpjsKesehatan.getText().trim() : "");
            pstmt.setDouble(14, parseSalary(txtSalary != null ? txtSalary.getText() : "0"));
            pstmt.setInt(15, selectedEmployee.id());

            pstmt.executeUpdate();
            loadDataFromDatabase();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data karyawan berhasil diperbarui.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Update", "Terjadi kesalahan update: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data karyawan yang ingin dihapus!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus data KTP: " + selectedEmployee.nik() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM employees WHERE id=?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedEmployee.id());
                pstmt.executeUpdate();

                loadDataFromDatabase();
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil dihapus.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Gagal Hapus", e.getMessage());
            }
        }
    }

    @FXML
    private void handleImportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Data Karyawan (.csv / .xlsx)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File CSV/Excel (*.csv, *.xlsx)", "*.csv", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV File (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("Excel File (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(txtNik.getScene().getWindow());
        if (file == null) return;

        if (file.getName().toLowerCase().endsWith(".xlsx")) {
            importFromExcel(file);
        } else {
            importFromCsv(file);
        }
    }

    private void importFromCsv(File file) {
        String sql = """
    INSERT OR REPLACE INTO employees (
        employee_id, nik, family_card_number, full_name, project_name, 
        birth_date, address, phone_number, gender, education, 
        bank_account, bpjs_tk, bpjs_kesehatan, salary
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""";

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
             Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                showAlert(Alert.AlertType.ERROR, "Gagal Import", "File CSV kosong!");
                return;
            }

            String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String cleanHeader = cleanQuotes(headers[i]).toLowerCase();
                colMap.put(cleanHeader, i);
            }

            if (!colMap.containsKey("nik") && !colMap.containsKey("no. ktp") && !colMap.containsKey("no ktp")) {
                showAlert(Alert.AlertType.ERROR, "Header Tidak Ditemukan", "File harus memiliki kolom 'NIK' atau 'No. KTP'!");
                return;
            }

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String nik = getVal(data, colMap, "nik", "no. ktp", "no ktp");
                if (nik.isBlank()) continue;

                String employeeId = getVal(data, colMap, "id karyawan", "no. id", "id", "emp id");
                String familyCard = getVal(data, colMap, "no. kk", "no kk", "kartu keluarga");
                String fullName = getVal(data, colMap, "nama lengkap", "nama");
                String projectName = getVal(data, colMap, "nama project", "project");
                String birthDate = getVal(data, colMap, "tanggal lahir", "tgl lahir");
                String address = getVal(data, colMap, "alamat");
                String phone = getVal(data, colMap, "no telp", "no. telp", "no hp", "telepon");
                String gender = getVal(data, colMap, "gender", "jenis kelamin");
                String education = getVal(data, colMap, "pendidikan");
                String bankAcc = getVal(data, colMap, "no rekening", "rekening");
                String bpjsTk = getVal(data, colMap, "bpjs tk", "bpjs ketenagakerjaan");
                String bpjsKes = getVal(data, colMap, "bpjs kes", "bpjs kesehatan");
                String salaryStr = getVal(data, colMap, "gaji");

                pstmt.setString(1, employeeId);
                pstmt.setString(2, nik);
                pstmt.setString(3, familyCard);
                pstmt.setString(4, fullName);
                pstmt.setString(5, projectName);
                pstmt.setString(6, birthDate);
                pstmt.setString(7, address);
                pstmt.setString(8, phone);
                pstmt.setString(9, gender);
                pstmt.setString(10, education);
                pstmt.setString(11, bankAcc);
                pstmt.setString(12, bpjsTk);
                pstmt.setString(13, bpjsKes);
                pstmt.setDouble(14, parseSalary(salaryStr));

                pstmt.addBatch();
                count++;
            }

            pstmt.executeBatch();
            loadDataFromDatabase();
            showAlert(Alert.AlertType.INFORMATION, "Import Berhasil", count + " data karyawan berhasil dimasukkan.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Import", "Terjadi kesalahan saat membaca CSV: " + e.getMessage());
        }
    }

    private void importFromExcel(File file) {
        String sql = """
    INSERT OR REPLACE INTO employees (
        employee_id, nik, family_card_number, full_name, project_name, 
        birth_date, address, phone_number, gender, education, 
        bank_account, bpjs_tk, bpjs_kesehatan, salary
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""";

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                showAlert(Alert.AlertType.ERROR, "Gagal Import", "File Excel kosong!");
                return;
            }

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String cleanHeader = getCellValueAsString(cell).toLowerCase().trim();
                colMap.put(cleanHeader, cell.getColumnIndex());
            }

            if (!colMap.containsKey("nik") && !colMap.containsKey("no. ktp") && !colMap.containsKey("nik / no. ktp")) {
                showAlert(Alert.AlertType.ERROR, "Header Tidak Ditemukan", "Excel harus memiliki kolom 'NIK' atau 'No. KTP'!");
                return;
            }

            int count = 0;
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nik = getExcelVal(row, colMap, formatter, "nik", "no. ktp", "nik / no. ktp");
                if (nik.isBlank()) continue;

                pstmt.setString(1, getExcelVal(row, colMap, formatter, "id karyawan", "no. id", "id", "emp id"));
                pstmt.setString(2, nik);
                pstmt.setString(3, getExcelVal(row, colMap, formatter, "no. kk", "no kk", "kartu keluarga"));
                pstmt.setString(4, getExcelVal(row, colMap, formatter, "nama lengkap", "nama"));
                pstmt.setString(5, getExcelVal(row, colMap, formatter, "nama project", "project"));
                pstmt.setString(6, getExcelVal(row, colMap, formatter, "tanggal lahir", "tgl lahir"));
                pstmt.setString(7, getExcelVal(row, colMap, formatter, "alamat"));
                pstmt.setString(8, getExcelVal(row, colMap, formatter, "no. telepon", "no telp", "telepon"));
                pstmt.setString(9, getExcelVal(row, colMap, formatter, "jenis kelamin", "gender"));
                pstmt.setString(10, getExcelVal(row, colMap, formatter, "pendidikan"));
                pstmt.setString(11, getExcelVal(row, colMap, formatter, "no. rekening", "rekening"));
                pstmt.setString(12, getExcelVal(row, colMap, formatter, "bpjs tk"));
                pstmt.setString(13, getExcelVal(row, colMap, formatter, "bpjs kesehatan", "bpjs kes"));
                pstmt.setDouble(14, parseSalary(getExcelVal(row, colMap, formatter, "gaji (rp)", "gaji")));

                pstmt.addBatch();
                count++;
            }

            pstmt.executeBatch();
            loadDataFromDatabase();
            showAlert(Alert.AlertType.INFORMATION, "Import Berhasil", count + " data karyawan berhasil dimasukkan dari Excel!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Import Excel", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private String getExcelVal(Row row, Map<String, Integer> colMap, DataFormatter formatter, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            Integer index = colMap.get(header.toLowerCase());
            if (index != null) {
                Cell cell = row.getCell(index);
                if (cell != null) {
                    return formatter.formatCellValue(cell).trim();
                }
            }
        }
        return "";
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private String getVal(String[] data, Map<String, Integer> colMap, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            Integer index = colMap.get(header.toLowerCase());
            if (index != null && index < data.length) {
                return cleanQuotes(data[index]);
            }
        }
        return "";
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Data Karyawan (Excel)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Data_Karyawan_HRIS.xlsx");

        File file = fileChooser.showSaveDialog(txtNik.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data Karyawan");
            sheet.setDisplayGridlines(true);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(headerStyle);

            CellStyle leftStyle = workbook.createCellStyle();
            leftStyle.setAlignment(HorizontalAlignment.LEFT);
            leftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(leftStyle);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(centerStyle);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            currencyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(currencyStyle);

            CellStyle textFormatStyle = workbook.createCellStyle();
            textFormatStyle.setDataFormat(format.getFormat("@"));
            textFormatStyle.setAlignment(HorizontalAlignment.CENTER);
            textFormatStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(textFormatStyle);

            String[] headers = {
                    "ID Karyawan", "NIK / No. KTP", "No. KK", "Nama Lengkap", "Alamat",
                    "Nama Project", "Tanggal Lahir", "No. Telepon", "Jenis Kelamin",
                    "Pendidikan", "No. Rekening", "BPJS TK", "BPJS Kesehatan", "Gaji (Rp)"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Employee emp : employeeList) {
                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, emp.employeeId(), textFormatStyle);
                createCell(row, 1, emp.nik(), textFormatStyle);
                createCell(row, 2, emp.familyCardNumber(), textFormatStyle);
                createCell(row, 3, emp.fullName(), leftStyle);
                createCell(row, 4, emp.address(), leftStyle);
                createCell(row, 5, emp.projectName(), leftStyle);
                createCell(row, 6, emp.birthDate(), centerStyle);
                createCell(row, 7, emp.phoneNumber(), textFormatStyle);
                createCell(row, 8, emp.gender(), leftStyle);
                createCell(row, 9, emp.education(), leftStyle);
                createCell(row, 10, emp.bankAccount(), textFormatStyle);
                createCell(row, 11, emp.bpjsTk(), textFormatStyle);
                createCell(row, 12, emp.bpjsKesehatan(), textFormatStyle);

                Cell salaryCell = row.createCell(13);
                salaryCell.setCellValue(emp.salary());
                salaryCell.setCellStyle(currencyStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Berhasil", "Data berhasil di-export ke Excel (.xlsx) secara rapi!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Export", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void setBorders(CellStyle headerStyle) {
    }

    @FXML
    private void clearForm() {
        if (txtNik != null) txtNik.clear();
        if (txtEmployeeId != null) txtEmployeeId.clear();
        if (txtFamilyCardNumber != null) txtFamilyCardNumber.clear();
        if (txtFullName != null) txtFullName.clear();
        if (cbProjectName != null) cbProjectName.setValue(null);
        if (txtBirthPlace != null) txtBirthPlace.clear();
        if (txtBirthDate != null) txtBirthDate.clear();
        if (cbBirthYear != null) cbBirthYear.setValue(null);
        if (txtAddress != null) txtAddress.clear();
        if (txtPhone != null) txtPhone.clear();
        if (cbGender != null) cbGender.setValue(null);
        if (cbEducation != null) cbEducation.setValue(null);
        if (txtBankAccount != null) txtBankAccount.clear();
        if (txtBpjsTk != null) txtBpjsTk.clear();
        if (txtBpjsKesehatan != null) txtBpjsKesehatan.clear();
        if (txtSalary != null) txtSalary.clear();
        if (tableEmployees != null) tableEmployees.getSelectionModel().clearSelection();
        selectedEmployee = null;
    }

    private boolean isNikInvalid() {
        if (txtNik == null || txtNik.getText() == null || txtNik.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "No. KTP / NIK wajib diisi! Silakan masukkan nomor KTP terlebih dahulu.");
            if (txtNik != null) txtNik.requestFocus();
            return true;
        }
        return false;
    }

    private String cleanQuotes(String text) {
        return text != null ? text.replace("\"", "").trim() : "";
    }

    private double parseSalary(String salaryText) {
        if (salaryText == null || salaryText.isBlank()) return 0.0;
        try {
            return Double.parseDouble(salaryText.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        applyDialogIcon(alert);

        alert.showAndWait();
    }

    @FXML
    private void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ubah Password Admin");
        dialog.setHeaderText("Silakan masukkan password lama dan password baru Anda.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField oldPassword = new PasswordField();
        oldPassword.setPromptText("Password saat ini");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Password baru");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Ulangi password baru");

        grid.add(new Label("Password Lama:"), 0, 0);
        grid.add(oldPassword, 1, 0);
        grid.add(new Label("Password Baru:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Konfirmasi Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        dialog.getDialogPane().setContent(grid);

        applyDialogIcon(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String oldPass = oldPassword.getText().trim();
            String newPass = newPassword.getText().trim();
            String confirmPass = confirmPassword.getText().trim();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Gagal Ubah Password", "Semua kolom password wajib diisi!");
                return;
            }

            if (!Database.checkAdminPassword(oldPass)) {
                showAlert(Alert.AlertType.ERROR, "Gagal Ubah Password", "Password lama yang Anda masukkan salah!");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showAlert(Alert.AlertType.ERROR, "Gagal Ubah Password", "Konfirmasi password baru tidak cocok!");
                return;
            }

            if (newPass.length() < 4) {
                showAlert(Alert.AlertType.ERROR, "Gagal Ubah Password", "Password baru minimal terdiri dari 4 karakter!");
                return;
            }

            if (Database.updateAdminPassword(newPass)) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Password Admin berhasil diperbarui! Silakan gunakan password baru saat login berikutnya.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal memperbarui password di database.");
            }
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Log Out");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Apakah Anda yakin ingin keluar dari akun?");

        applyDialogIcon(confirmAlert);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Stage stage = (Stage) tableEmployees.getScene().getWindow();

                FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("login-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 420, 480);

                stage.setTitle("Login - HRIS Manpower");
                stage.setScene(scene);

                stage.setMaximized(false);
                stage.setResizable(true);
                stage.setWidth(450);
                stage.setHeight(520);
                stage.centerOnScreen();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal kembali ke halaman login: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleManageReminders() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Kelola Pengingat Operasional & HRIS");
        dialog.setHeaderText("Daftar Pengingat Rutin & Agenda Kantor");

        ButtonType closeButton = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        TableView<Reminder> tableReminders = new TableView<>();
        ObservableList<Reminder> reminderList = FXCollections.observableArrayList();

        TableColumn<Reminder, String> colTitle = new TableColumn<>("Nama Pengingat");
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title()));
        colTitle.setPrefWidth(220);

        TableColumn<Reminder, String> colType = new TableColumn<>("Tipe");
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                "RECURRING_DAYS".equalsIgnoreCase(c.getValue().type()) ? "Setiap " + c.getValue().intervalDays() + " Hari" : "Setiap Tgl " + c.getValue().monthlyDay()
        ));
        colType.setPrefWidth(120);

        TableColumn<Reminder, String> colStart = new TableColumn<>("Mulai / Patokan");
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().startDate()));
        colStart.setPrefWidth(110);

        TableColumn<Reminder, String> colNotice = new TableColumn<>("Info (H-X)");
        colNotice.setCellValueFactory(c -> new SimpleStringProperty("H-" + c.getValue().daysBeforeNotice()));
        colNotice.setPrefWidth(80);

        TableColumn<Reminder, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Aktif" : "Non-Aktif"));
        colStatus.setPrefWidth(80);

        tableReminders.getColumns().addAll(colTitle, colType, colStart, colNotice, colStatus);
        tableReminders.setPrefHeight(200);

        Runnable loadReminders = () -> {
            reminderList.clear();
            String sql = "SELECT * FROM reminders ORDER BY id DESC";
            try (Connection conn = Database.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    reminderList.add(new Reminder(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("type"),
                            rs.getString("start_date"),
                            rs.getInt("interval_days"),
                            rs.getInt("monthly_day"),
                            rs.getInt("days_before_notice"),
                            rs.getString("notes"),
                            rs.getInt("is_active") == 1
                    ));
                }
                tableReminders.setItems(reminderList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        loadReminders.run();

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("misal: Memo Gereja Bonaventura");

        ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList("Berulang Hari (e.g. 14 Hari)", "Bulanan (Tanggal)"));
        cbType.setValue("Berulang Hari (e.g. 14 Hari)");

        TextField txtIntervalOrDay = new TextField();
        txtIntervalOrDay.setPromptText("Jumlah hari (misal: 14) atau Tanggal (1-31)");

        DatePicker dpStartDate = new DatePicker(LocalDate.now());

        TextField txtNoticeDays = new TextField("3");
        txtNoticeDays.setPromptText("Hari Peringatan (misal: 3)");

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Catatan/Instruksi (opsional)");

        CheckBox chkIsActive = new CheckBox("Aktifkan Pengingat");
        chkIsActive.setSelected(true);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        formGrid.add(new Label("Nama Pengingat:"), 0, 0);
        formGrid.add(txtTitle, 1, 0);

        formGrid.add(new Label("Tipe Pengingat:"), 0, 1);
        formGrid.add(cbType, 1, 1);

        formGrid.add(new Label("Interval / Tanggal:"), 0, 2);
        formGrid.add(txtIntervalOrDay, 1, 2);

        formGrid.add(new Label("Tanggal Acuan/Mulai:"), 0, 3);
        formGrid.add(dpStartDate, 1, 3);

        formGrid.add(new Label("Peringatan (H-X):"), 0, 4);
        formGrid.add(txtNoticeDays, 1, 4);

        formGrid.add(new Label("Catatan:"), 0, 5);
        formGrid.add(txtNotes, 1, 5);

        formGrid.add(chkIsActive, 1, 6);

        Button btnAdd = new Button("Tambah Pengingat");
        btnAdd.setStyle("-fx-background-color: #319795; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnDelete = new Button("Hapus Terpilih");
        btnDelete.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white;");

        btnAdd.setOnAction(e -> {
            String title = txtTitle.getText().trim();
            if (title.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Gagal", "Nama Pengingat tidak boleh kosong!");
                return;
            }

            String selectedType = cbType.getValue().contains("Berulang") ? "RECURRING_DAYS" : "MONTHLY_DATE";
            int val = parseSalary(txtIntervalOrDay.getText()) > 0 ? (int) parseSalary(txtIntervalOrDay.getText()) : 14;
            int intervalDays = "RECURRING_DAYS".equals(selectedType) ? val : 0;
            int monthlyDay = "MONTHLY_DATE".equals(selectedType) ? val : 0;
            int notice = parseSalary(txtNoticeDays.getText()) > 0 ? (int) parseSalary(txtNoticeDays.getText()) : 3;
            String startDate = dpStartDate.getValue() != null ? dpStartDate.getValue().toString() : LocalDate.now().toString();

            String sqlInsert = "INSERT INTO reminders (title, type, start_date, interval_days, monthly_day, days_before_notice, notes, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setString(1, title);
                pstmt.setString(2, selectedType);
                pstmt.setString(3, startDate);
                pstmt.setInt(4, intervalDays);
                pstmt.setInt(5, monthlyDay);
                pstmt.setInt(6, notice);
                pstmt.setString(7, txtNotes.getText().trim());
                pstmt.setInt(8, chkIsActive.isSelected() ? 1 : 0);

                pstmt.executeUpdate();
                loadReminders.run();
                txtTitle.clear();
                txtIntervalOrDay.clear();
                txtNotes.clear();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pengingat baru berhasil ditambahkan!");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error Database", ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            Reminder selected = tableReminders.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Pilih Data", "Silakan pilih pengingat yang akan dihapus!");
                return;
            }

            String sqlDelete = "DELETE FROM reminders WHERE id = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                pstmt.setInt(1, selected.id());
                pstmt.executeUpdate();
                loadReminders.run();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error Database", ex.getMessage());
            }
        });

        HBox actionBox = new HBox(10, btnAdd, btnDelete);
        VBox contentBox = new VBox(10, tableReminders, formGrid, actionBox);
        contentBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contentBox);

        applyDialogIcon(dialog);
        dialog.showAndWait();

        checkAllDatabaseReminders();
    }

    private void checkAllDatabaseReminders() {
    }

    @FXML
    private void handleManageLongLeaves() {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Pencatatan Sakit & Cuti Jangka Panjang");
            dialog.setHeaderText("Rekapitulasi Karyawan Sakit / Cuti Durasi Panjang (> 1 Bulan)");

            ButtonType closeButton = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            TableView<EmployeeLeave> tableLeaves = new TableView<>();
            ObservableList<EmployeeLeave> leaveList = FXCollections.observableArrayList();

            TableColumn<EmployeeLeave, String> colEmpName = new TableColumn<>("Nama");
            colEmpName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().employeeName()));
            colEmpName.setPrefWidth(140);

            TableColumn<EmployeeLeave, String> colType = new TableColumn<>("Tipe");
            colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().leaveType()));
            colType.setPrefWidth(120);

            TableColumn<EmployeeLeave, String> colStart = new TableColumn<>("Mulai");
            colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().startDate()));
            colStart.setPrefWidth(90);

            TableColumn<EmployeeLeave, String> colEnd = new TableColumn<>("Selesai");
            colEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().endDate()));
            colEnd.setPrefWidth(90);

            TableColumn<EmployeeLeave, String> colDays = new TableColumn<>("Durasi");
            colDays.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().totalDays() + " Hari"));
            colDays.setPrefWidth(80);

            TableColumn<EmployeeLeave, String> colNotes = new TableColumn<>("Catatan");
            colNotes.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().notes()));
            colNotes.setPrefWidth(180);

            TableColumn<EmployeeLeave, String> colStatus = new TableColumn<>("Status");
            colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
            colStatus.setPrefWidth(80);

            tableLeaves.getColumns().addAll(colEmpName, colType, colStart, colEnd, colDays, colNotes, colStatus);
            tableLeaves.setPrefHeight(200);

            Runnable loadLeaves = () -> {
                leaveList.clear();
                String sql = "SELECT * FROM employee_leaves ORDER BY id DESC";
                try (Connection conn = Database.connect();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        leaveList.add(new EmployeeLeave(
                                rs.getInt("id"),
                                rs.getString("employee_id"),
                                rs.getString("employee_name"),
                                rs.getString("leave_type"),
                                rs.getString("start_date"),
                                rs.getString("end_date"),
                                rs.getInt("total_days"),
                                rs.getString("notes"),
                                rs.getString("status")
                        ));
                    }
                    tableLeaves.setItems(leaveList);
                } catch (Exception e) {
                    System.err.println("Gagal memuat data employee_leaves: " + e.getMessage());
                }
            };

            loadLeaves.run();

            TextField txtSearchEmployee = new TextField();
            txtSearchEmployee.setPromptText("Ketik NIK atau Nama Karyawan...");

            ContextMenu suggestionsPopup = new ContextMenu();
            final Employee[] selectedEmpHolder = new Employee[1];

            txtSearchEmployee.textProperty().addListener((obs, oldText, newText) -> {
                if (newText == null || newText.trim().length() < 2) {
                    suggestionsPopup.hide();
                    return;
                }

                String filter = newText.toLowerCase().trim();
                List<MenuItem> items = new ArrayList<>();

                for (Employee emp : employeeList) {
                    if (emp.nik().toLowerCase().contains(filter) || emp.fullName().toLowerCase().contains(filter)) {
                        MenuItem item = new MenuItem(emp.nik() + " - " + emp.fullName() + " (" + emp.projectName() + ")");
                        item.setOnAction(e -> {
                            txtSearchEmployee.setText(emp.nik() + " - " + emp.fullName());
                            selectedEmpHolder[0] = emp;
                            suggestionsPopup.hide();
                        });
                        items.add(item);
                        if (items.size() >= 10) break;
                    }
                }

                if (!items.isEmpty()) {
                    suggestionsPopup.getItems().setAll(items);
                    if (!suggestionsPopup.isShowing()) {
                        suggestionsPopup.show(txtSearchEmployee, Side.BOTTOM, 0, 0);
                    }
                } else {
                    suggestionsPopup.hide();
                }
            });

            ComboBox<String> cbLeaveType = new ComboBox<>(FXCollections.observableArrayList(
                    "Sakit Panjang / Rawat Inap",
                    "Cuti Melahirkan (3 Bulan)",
                    "Cuti Besar / Tahunan",
                    "Cuti Diluar Tanggungan"
            ));
            cbLeaveType.setValue("Sakit Panjang / Rawat Inap");

            DatePicker dpStart = new DatePicker(LocalDate.now());
            DatePicker dpEnd = new DatePicker(LocalDate.now().plusMonths(1));

            TextArea txtNotes = new TextArea();
            txtNotes.setPromptText("Nomor Surat Dokter / Catatan Medis / Keterangan Alasan...");
            txtNotes.setPrefRowCount(2);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 0, 10, 0));

            grid.add(new Label("Cari Karyawan:"), 0, 0);
            grid.add(txtSearchEmployee, 1, 0);

            grid.add(new Label("Kategori:"), 0, 1);
            grid.add(cbLeaveType, 1, 1);

            grid.add(new Label("Tgl Mulai:"), 0, 2);
            grid.add(dpStart, 1, 2);

            grid.add(new Label("Tgl Selesai:"), 0, 3);
            grid.add(dpEnd, 1, 3);

            grid.add(new Label("Catatan / Medis:"), 0, 4);
            grid.add(txtNotes, 1, 4);

            Button btnSimpan = new Button("Simpan Catatan");
            btnSimpan.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold;");

            Button btnSelesai = new Button("Tandai Selesai");
            btnSelesai.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-weight: bold;");

            Button btnHapus = new Button("Hapus Catatan");
            btnHapus.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white;");

            btnSimpan.setOnAction(e -> {
                String searchText = txtSearchEmployee.getText().trim();
                if (searchText.isEmpty() || dpStart.getValue() == null || dpEnd.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Input Gagal", "Ketik nama/NIK karyawan dan tentukan tanggal mulai/selesai!");
                    return;
                }

                String empNik = "";
                String empName = "";

                if (selectedEmpHolder[0] != null) {
                    empNik = selectedEmpHolder[0].nik();
                    empName = selectedEmpHolder[0].fullName();
                } else {
                    String[] parts = searchText.split(" - ");
                    empNik = parts[0];
                    empName = parts.length > 1 ? parts[1] : parts[0];
                }

                LocalDate start = dpStart.getValue();
                LocalDate end = dpEnd.getValue();

                if (end.isBefore(start)) {
                    showAlert(Alert.AlertType.ERROR, "Input Gagal", "Tanggal selesai tidak boleh sebelum tanggal mulai!");
                    return;
                }

                long totalDays = ChronoUnit.DAYS.between(start, end) + 1;

                String sqlInsert = "INSERT INTO employee_leaves (employee_id, employee_name, leave_type, start_date, end_date, total_days, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'AKTIF')";
                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    pstmt.setString(1, empNik);
                    pstmt.setString(2, empName);
                    pstmt.setString(3, cbLeaveType.getValue());
                    pstmt.setString(4, start.toString());
                    pstmt.setString(5, end.toString());
                    pstmt.setLong(6, totalDays);
                    pstmt.setString(7, txtNotes.getText().trim());

                    pstmt.executeUpdate();
                    loadLeaves.run();

                    txtSearchEmployee.clear();
                    selectedEmpHolder[0] = null;
                    txtNotes.clear();

                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Catatan sakit/cuti berhasil disimpan!");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
                }
            });

            btnSelesai.setOnAction(e -> {
                EmployeeLeave selected = tableLeaves.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    showAlert(Alert.AlertType.WARNING, "Pilih Data", "Pilih data karyawan yang masa cuti/sakitnya sudah selesai!");
                    return;
                }

                String sqlUpdate = "UPDATE employee_leaves SET status = 'SELESAI' WHERE id = ?";
                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                    pstmt.setInt(1, selected.id());
                    pstmt.executeUpdate();
                    loadLeaves.run();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Status berhasil diubah menjadi SELESAI.");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
                }
            });

            btnHapus.setOnAction(e -> {
                EmployeeLeave selected = tableLeaves.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    showAlert(Alert.AlertType.WARNING, "Pilih Data", "Pilih data riwayat yang ingin dihapus!");
                    return;
                }

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Konfirmasi Hapus");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("Apakah Anda yakin ingin menghapus catatan untuk: " + selected.employeeName() + "?");

                applyDialogIcon(confirmAlert);

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String sqlDelete = "DELETE FROM employee_leaves WHERE id = ?";
                    try (Connection conn = Database.connect();
                         PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                        pstmt.setInt(1, selected.id());
                        pstmt.executeUpdate();
                        loadLeaves.run();
                        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Catatan berhasil dihapus permanen.");
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
                    }
                }
            });

            HBox actionBox = new HBox(10, btnSimpan, btnSelesai, btnHapus);
            VBox mainLayout = new VBox(10, tableLeaves, grid, actionBox);
            mainLayout.setPadding(new Insets(10));

            dialog.getDialogPane().setContent(mainLayout);

            applyDialogIcon(dialog);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka jendela pop-up: " + e.getMessage());
        }
    }

    private void applyDialogIcon(Dialog<?> dialog) {
        try {
            DialogPane dialogPane = dialog.getDialogPane();
            if (dialogPane != null && dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                Stage stage = (Stage) dialogPane.getScene().getWindow();
                InputStream iconStream = MainApp.class.getResourceAsStream("/com/paralel/hrismbp/LOGO.png");
                if (iconStream != null) {
                    stage.getIcons().clear();
                    stage.getIcons().add(new Image(iconStream));
                }
            } else {
                // Alternatif aman jika window belum sepenuhnya terbentuk
                dialog.setOnShowing(evt -> {
                    try {
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        InputStream iconStream = MainApp.class.getResourceAsStream("/com/paralel/hrismbp/LOGO.png");
                        if (iconStream != null) {
                            stage.getIcons().clear();
                            stage.getIcons().add(new Image(iconStream));
                        }
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon dialog: " + e.getMessage());
        }
    }
}