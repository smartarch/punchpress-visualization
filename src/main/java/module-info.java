module ers.d3s.punchpressvisualization {
    requires javafx.controls;
    requires javafx.base;
    requires com.fazecast.jSerialComm;

    opens d3s.ers.punchpressvisualization to javafx.fxml;
    exports d3s.ers.punchpressvisualization;
}