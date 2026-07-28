package com.paralel.hrismbp;

import javafx.beans.property.*;

public class EmployeeBpjs {
    private final IntegerProperty no = new SimpleIntegerProperty();
    private final StringProperty namaProject = new SimpleStringProperty(); // Contoh: UNIT BSD II
    private final StringProperty lokasiProject = new SimpleStringProperty(); // Contoh: Club House Eonna
    private final StringProperty nama = new SimpleStringProperty();
    private final StringProperty jabatan = new SimpleStringProperty();
    private final DoubleProperty gapok = new SimpleDoubleProperty();
    private final BooleanProperty jpActive = new SimpleBooleanProperty(true);

    public EmployeeBpjs(int no, String namaProject, String lokasiProject, String nama, String jabatan, double gapok, boolean jpActive) {
        setNo(no);
        setNamaProject(namaProject);
        setLokasiProject(lokasiProject);
        setNama(nama);
        setJabatan(jabatan);
        setGapok(gapok);
        setJpActive(jpActive);
    }

    // --- Properties & Getters/Setters ---
    public IntegerProperty noProperty() { return no; }
    public int getNo() { return no.get(); }
    public void setNo(int no) { this.no.set(no); }

    public StringProperty namaProjectProperty() { return namaProject; }
    public String getNamaProject() { return namaProject.get(); }
    public void setNamaProject(String namaProject) { this.namaProject.set(namaProject); }

    public StringProperty lokasiProjectProperty() { return lokasiProject; }
    public String getLokasiProject() { return lokasiProject.get(); }
    public void setLokasiProject(String lokasiProject) { this.lokasiProject.set(lokasiProject); }

    public StringProperty namaProperty() { return nama; }
    public String getNama() { return nama.get(); }
    public void setNama(String nama) { this.nama.set(nama); }

    public StringProperty jabatanProperty() { return jabatan; }
    public String getJabatan() { return jabatan.get(); }
    public void setJabatan(String jabatan) { this.jabatan.set(jabatan); }

    public DoubleProperty gapokProperty() { return gapok; }
    public double getGapok() { return gapok.get(); }
    public void setGapok(double gapok) { this.gapok.set(gapok); }

    public BooleanProperty jpActiveProperty() { return jpActive; }
    public boolean isJpActive() { return jpActive.get(); }
    public void setJpActive(boolean jpActive) { this.jpActive.set(jpActive); }
}