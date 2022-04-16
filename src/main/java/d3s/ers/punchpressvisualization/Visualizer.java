package d3s.ers.punchpressvisualization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Visualizer extends Application {
	private static String punchInFileName = "punches.in";
	private static String punchesOutFileName = "punches.out";
	private static String commPort;
	private static double zoom = 0.42;

	private static Pattern commMsgRe = Pattern.compile("S(-?[0-9]+),(-?[0-9]+),([01]),([01])\n");

	private BufferedWriter outWriter;

	private final PlannedPositions plannedPositions;
	private final Positions punchedPositions;

	private boolean updatePunches;
	private boolean updateStatus;

	private Timeline timeline;

	private SerialPort comm = null;

	// main part
	private PlantView plantView;
	// right panel
	private PunchStatusView punchStatusView;

	// bottom status bar
	private Label connectionStatus;
	private Label positionStatus;
	private Label status;

	public Visualizer() {
		plannedPositions = new PlannedPositions();
		punchedPositions = new Positions();
		updatePunches = true;
		updateStatus = true;
		outWriter = null;
	}

	public static void main(String[] args) {
		if (!parseArgs(args)) {
			System.exit(0);
		}
		if (Files.notExists(Paths.get(punchInFileName))) {
			System.err.println(String.format("The file %s doesn't exist.", punchInFileName));
			System.exit(1);
		}

		launch(args);
	}

	private static boolean parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String option = args[i];
			if (option.equals("-h")) {
				printUsage();
				return false;
			}
			i++;
			if (i < args.length) {
				String param = args[i];
				if (option.equals("-i")) {
					punchInFileName = param;
				} else if (option.equals("-o")) {
					punchesOutFileName = param;
				} else if (option.equals("-c")) {
					commPort = param;
				} else {
					printUsage();
					return false;
				}
			} else {
				printUsage();
				return false;
			}
		}

		return true;
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println(
				"punchpressvis [-h] [-i <punch in file>] [-o <punch out file>] [-c <serial port>]");
		System.out.println("-i <punch in file> - file with planned punches, planned"
				+ " punches are displayed in the work area; on each line of the"
				+ " file a pair of coordinates separated by a semi-colon is expected");
		System.out.println("-o <punch out file> - file used for logging the punches");
		System.out.println("-c <serial port> - communication port to which the simulator is connected (e.g. COM16, /dev/ttyUSB0)");
		System.out.println("-h - print this help and immediately exits the program");
	}

	private final class CommMessageListener implements SerialPortMessageListener
	{
		@Override
		public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED; }

		@Override
		public byte[] getMessageDelimiter() { return new byte[] { (byte)0xA }; }

		@Override
		public boolean delimiterIndicatesEndOfMessage() { return true; }

		@Override
		public void serialEvent(SerialPortEvent event)
		{
			var eventType = event.getEventType();

			if (eventType == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
				String data = new String(event.getReceivedData(), StandardCharsets.ISO_8859_1);
				Matcher matcher = commMsgRe.matcher(data);

				if (matcher.matches()) {
					int posX = Integer.parseInt(matcher.group(1));
					int posY = Integer.parseInt(matcher.group(2));
					boolean headUp = Integer.parseInt(matcher.group(3)) != 0;
					boolean failed = Integer.parseInt(matcher.group(4)) != 0;
//					System.out.println("posX=" + posX + " posY=" + posY + " headUp=" + headUp + " failed=" + failed);


					if (failed && !plantView.isFailed()) {
						plantView.fail();
						updateStatus = true;
					}

					var head = plantView.getHead();

					Position pos = new Position(posX, posY);
					if (!pos.equals(head.getPosition())) {
						head.setPosition(pos);
						updateStatus = true;
					}

					if (head.isUp() != headUp) {
						head.setUp(headUp);
						if (!headUp) {
							punchedPositions.addPosition(pos);
							writeOut(pos.toString());
							updatePunches = true;
						}
					}
				}

			} else if (eventType == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
				releaseComm();
			}

		}
	}

	private void ensureComm() {
		if (comm == null) {
			try {
				comm = SerialPort.getCommPort(commPort);
				if (comm.openPort(0)) {
					CommMessageListener listener = new CommMessageListener();
					comm.addDataListener(listener);

					plantView.restart();
					punchedPositions.getPositions().clear();
					openOutWriter();

					updatePunches = true;
					updateStatus = true;
				} else {
					comm = null;
				}
			} catch (SerialPortInvalidPortException ex) {
				comm = null;
			}
		}
	}

	private void releaseComm() {
		if (comm != null) {
			comm.removeDataListener();
			comm.closePort();
			comm = null;
			updateStatus = true;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		if (punchInFileName != null) {
			plannedPositions.loadPositions(punchInFileName);
		}

		BorderPane root = new BorderPane();
		root.setBottom(createStatusBar());
		root.setRight(createPunchPanel());
		root.setCenter(createPlantView());

		Scene scene = new Scene(root, 800, 500);

		primaryStage.setTitle("Punchpress Visualizer");
		primaryStage.setScene(scene);
		primaryStage.show();

		openOutWriter();

		timeline = new Timeline(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				update();
			}
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();

	}

	@Override
	public void stop() {
		if (timeline != null) {
			timeline.stop();
		}

		releaseComm();

		closeOutWriter();
	}

	private void openOutWriter() {
		closeOutWriter();
		try {
			outWriter = new BufferedWriter(new FileWriter(punchesOutFileName));
		} catch (IOException e) {
			closeOutWriter();
			System.err.println(e.getMessage());
		}
	}

	private void closeOutWriter() {
		if (outWriter != null) {
			try {
				outWriter.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		outWriter = null;
	}

	private Node createPunchPanel() {
		BorderPane pane = new BorderPane();

		this.punchStatusView = new PunchStatusView(plannedPositions, punchedPositions);
		pane.setCenter(this.punchStatusView);

		return pane;
	}

	private Node createStatusBar() {
		HBox bar = new HBox();
		bar.setPadding(new Insets(5, 5, 5, 5));
		bar.setSpacing(5);
		bar.setStyle("-fx-background-color: #999999;");

		Label connectionLabel = new Label("Connected: ");
		connectionStatus = new Label();
		Label positionLabel = new Label("Position: ");
		positionStatus = new Label();
		Label statusLabel = new Label("Status: ");
		status = new Label();

		Separator s = new Separator(Orientation.VERTICAL);
		Separator s2 = new Separator(Orientation.VERTICAL);

		bar.getChildren().addAll(connectionLabel, connectionStatus, s, statusLabel, status, s2, positionLabel,
				positionStatus);

		return bar;
	}

	private Node createPlantView() {
		BorderPane pane = new BorderPane();

		plantView = new PlantView(plannedPositions, punchedPositions);
		plantView.setZoom(zoom);
		pane.setCenter(this.plantView);

		Slider slider = new Slider();
		slider.setMin(0);
		slider.setMax(100);
		slider.setValue(50);
		slider.setShowTickLabels(false);
		slider.setShowTickMarks(true);
		slider.setBlockIncrement(10);
		pane.setBottom(slider);

		slider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				double position = (newValue.doubleValue() - 50) / 20;
				double zoom = Math.pow(Math.abs(position), Math.log(10) / Math.log(5));
				if (position > 0) {
					zoom += 1;
				} else {
					zoom = 1 / (zoom + 1);
				}

				plantView.setZoom(zoom);
			}
		});

		return pane;
	}

	private void update() {
		ensureComm();

		this.plantView.update();
		if (updatePunches) {
			punchStatusView.update();
			updatePunches = false;
		}
		if (updateStatus) {
			updateStatusBar();
			updateStatus = false;
		}

	}

	private void updateStatusBar() {
		connectionStatus.setText(comm != null ? "yes" : "no");
		positionStatus.setText(plantView.getHead().getPosition().toString());
		status.setText(plantView.isFailed() ? "FAILED" : "OK");
	}

	private void writeOut(String message) {
		if (outWriter != null) {
			try {
				outWriter.write(message);
				outWriter.newLine();
				outWriter.flush();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
