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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlannedPositions extends Positions {
	
	public void loadPositions(String file){
		Pattern positionPattern = Pattern.compile("(\\d+);(\\d+)");
		
		positions.clear();
		
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			String line = reader.readLine();
			int lineNumber = 1;
			while(line != null){
				Matcher positionMatcher = positionPattern.matcher(line);
				if(!positionMatcher.matches() || positionMatcher.groupCount() != 2){
					System.err.println(String.format("Unexpected element in file %s on line %d", file, lineNumber));
				} else {
					int x = Integer.parseInt(positionMatcher.group(1));
					int y = Integer.parseInt(positionMatcher.group(2));
					Position position = new Position(x, y);
					positions.add(position);
				}
				
				line = reader.readLine();
				lineNumber++;
			}
		} catch(IOException e){
			System.err.println(e.getMessage());
		}
	}
}
