module com.paralel.hrismbp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires itextpdf;

    opens com.paralel.hrismbp to javafx.fxml, javafx.base;
    exports com.paralel.hrismbp;
}