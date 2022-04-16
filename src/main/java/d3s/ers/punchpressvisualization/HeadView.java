/*******************************************************************************
 * Copyright 2017 Charles University in Prague
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package d3s.ers.punchpressvisualization;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HeadView {

	private static final int OUTER_WIDTH = 30;
	private static final int OUTER_HEIGHT = 30;
	private static final int INNER_WIDTH = 10;
	private static final int INNER_HEIGHT = 10;
	
	private static final Color STROKE_COLOR = Color.BLUE;
	private static final Color FILL_COLOR = Color.BLUE;
	private static final double LINE_WIDTH = 2;
	
	private Position position;
	private boolean up;
	
	private double zoom;
	
	public HeadView(){
		position = new Position();
		up = true;
	}

	public void setPosition(Position position){
		this.position = position;
	}
	
	public Position getPosition(){
		return position;
	}

	public boolean isUp() {
		return this.up;
	}
	
	public void setUp(boolean up){
		this.up = up;
	}
	
	public void draw(GraphicsContext gc, double offset_x, double offset_y){
		gc.setStroke(STROKE_COLOR);
		gc.setFill(FILL_COLOR);
		gc.setLineWidth(LINE_WIDTH);

		double zoomedOX = zoom * position.getCornerX(OUTER_WIDTH);
		double zoomedOY = zoom * position.getCornerY(OUTER_HEIGHT);
		double zoomedOWidth = zoom * OUTER_WIDTH;
		double zoomedOHeight = zoom * OUTER_HEIGHT;
		double zoomedIX = zoom * position.getCornerX(INNER_WIDTH);
		double zoomedIY = zoom * position.getCornerY(INNER_HEIGHT);
		double zoomedIWidth = zoom * INNER_WIDTH;
		double zoomedIHeight = zoom * INNER_HEIGHT;
		
		gc.strokeRect(zoomedOX + offset_x, zoomedOY + offset_y, zoomedOWidth, zoomedOHeight);
		if(up){
			gc.strokeRect(zoomedIX + offset_x, zoomedIY + offset_y, zoomedIWidth, zoomedIHeight);
		} else {
			gc.fillRect(zoomedIX + offset_x, zoomedIY + offset_y, zoomedIWidth, zoomedIHeight);
		}
	}
	
	public void setZoom(double zoom){
		this.zoom = zoom;
	}
}
