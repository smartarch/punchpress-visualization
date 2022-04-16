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

import java.util.Objects;

public class Position {

	public final int x;
	public final int y;
	
	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}

	public Position(){
		this.x = 0;
		this.y = 0;
	}
	
	public int getCornerX(int width){
		return x - width/2;
	}
	
	public int getCornerY(int height){
		return y - height/2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Position position = (Position) o;
		return x == position.x && y == position.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[%d:%d]", x, y);
	}
}
