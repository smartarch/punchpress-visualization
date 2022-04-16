package d3s.ers.punchpressvisualization;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PunchStatusView extends ScrollPane {
	
	private VBox punchedBox;

	private final Positions punchedPositions;

	public PunchStatusView(Positions plannedPositions, Positions punchedPositions) {
		this.punchedPositions = punchedPositions;

		VBox punchBox = new VBox(5);
		this.setContent(punchBox);

		VBox plannedBox = new VBox();
		for (Position p : plannedPositions.getPositions()) {
			Label l = new Label(p.toString());
			plannedBox.getChildren().add(l);
		}

		this.punchedBox = new VBox();

		Separator s = new Separator();
		s.setOrientation(Orientation.HORIZONTAL);
		Label l1 = new Label("Planned punches:");
		l1.setFont(Font.font(null, FontWeight.BOLD, 14));
		Label l2 = new Label("Punched punches:");
		l2.setFont(Font.font(null, FontWeight.BOLD, 14));

		punchBox.getChildren().addAll(l1, plannedBox, s, l2, this.punchedBox);

		update();
	}

	public void update() {
		punchedBox.getChildren().clear();
		for (Position p : punchedPositions.getPositions()) {
			Label l = new Label(p.toString());
			punchedBox.getChildren().add(l);
		}
	}

}
