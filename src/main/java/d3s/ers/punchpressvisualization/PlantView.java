package d3s.ers.punchpressvisualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;


public class PlantView extends ScrollPane {
	private static final int HOLE_WIDTH = 10;
	private static final int HOLE_HEIGHT = 10;

	private static final Color PLANNED_STROKE_COLOR = Color.GREEN;
	private static final Color HOLE_FILL_COLOR = Color.BLACK;
	private static final double LINE_WIDTH = 2;

	public static final int BASE_WIDTH = 1500; // in mm
	public static final int BASE_HEIGHT = 1000; // in mm
	public static final int SAFE_ZONE_WIDTH = 20;

	private final HeadView head;
	private final Positions plannedPositions;
	private final Positions holes;
	private boolean failed;

	private final Canvas canvas;
	private double zoom;

	public PlantView(Positions plannedPositions, Positions punchedPositions) {
		this.plannedPositions = plannedPositions;
		holes = punchedPositions;
		failed = false;
		head = new HeadView();

		canvas = new Canvas(BASE_WIDTH + 2 * SAFE_ZONE_WIDTH, BASE_HEIGHT + 2 * SAFE_ZONE_WIDTH);
		Pane pane = new Pane();
		pane.getChildren().add(canvas);
		setContent(pane);

		setZoom(1);
	}

	public boolean isFailed() {
		return failed;
	}

	public void fail() {
		failed = true;
	}

	public void restart() {
		failed = false;
		head.setUp(true);
	}

	public HeadView getHead() {
		return head;
	}

	public void update() {
		double zoomedSZWidth = zoom * SAFE_ZONE_WIDTH;
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, zoom * BASE_WIDTH + 2 * SAFE_ZONE_WIDTH, zoom * BASE_HEIGHT + 2 * SAFE_ZONE_WIDTH);

		drawSafeZones(gc);
		drawPlan(gc);
		drawHoles(gc);
		head.draw(gc, zoomedSZWidth, zoomedSZWidth);
	}

	public void drawPlan(GraphicsContext gc) {
		double zoomedWidth = zoom * HOLE_WIDTH;
		double zoomedHeight = zoom * HOLE_HEIGHT;
		double zoomedSZWidth = zoom * SAFE_ZONE_WIDTH;
		
		gc.setStroke(PLANNED_STROKE_COLOR);
		gc.setLineWidth(LINE_WIDTH);
		for (Position p : plannedPositions.getPositions()) {
			double zoomedX = zoom * p.getCornerX(HOLE_WIDTH);
			double zoomedY = zoom * p.getCornerY(HOLE_HEIGHT);
			
			gc.strokeRect(zoomedX + zoomedSZWidth, zoomedY + zoomedSZWidth, zoomedWidth, zoomedHeight);
		}
	}

	public void drawHoles(GraphicsContext gc) {
		double zoomedWidth = zoom * HOLE_WIDTH;
		double zoomedHeight = zoom * HOLE_HEIGHT;
		double zoomedSZWidth = zoom * SAFE_ZONE_WIDTH;
		
		gc.setFill(HOLE_FILL_COLOR);
		for (Position p : holes.getPositions()) {
			double zoomedX = zoom * p.getCornerX(HOLE_WIDTH);
			double zoomedY = zoom * p.getCornerY(HOLE_HEIGHT);
			
			gc.fillRect(zoomedX + zoomedSZWidth, zoomedY + zoomedSZWidth, zoomedWidth, zoomedHeight);
		}
	}

	private void drawSafeZones(GraphicsContext gc) {
		double zoomedBaseWidth = zoom * BASE_WIDTH;
		double zoomedBaseHeight = zoom * BASE_HEIGHT;
		double zoomedSZWidth = zoom * SAFE_ZONE_WIDTH;
		
		gc.setFill(Color.RED);
		gc.fillRect(0, 0, zoomedBaseWidth + 2 * zoomedSZWidth, zoomedSZWidth);
		gc.fillRect(0, 0, zoomedSZWidth, zoomedBaseHeight + 2 * zoomedSZWidth);
		gc.fillRect(0, zoomedBaseHeight + zoomedSZWidth, zoomedBaseWidth + 2 * zoomedSZWidth, zoomedSZWidth);
		gc.fillRect(zoomedBaseWidth + zoomedSZWidth, 0, zoomedSZWidth, zoomedBaseHeight + 2 * zoomedSZWidth);
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
		head.setZoom(zoom);
		canvas.setWidth(zoom * BASE_WIDTH + 2 * SAFE_ZONE_WIDTH);
		canvas.setHeight(zoom * BASE_HEIGHT + 2 * SAFE_ZONE_WIDTH);
	}

}
