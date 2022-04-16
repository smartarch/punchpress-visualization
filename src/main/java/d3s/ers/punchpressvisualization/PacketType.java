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


public enum PacketType {
	HeadPosition,
	HeadDown,
	HeadUp,
	Restart,
	Fail,
	Unknown;
	
	public static PacketType parse(char type){
		switch(type){
		case 'H':
			return HeadPosition;
		case 'D':
			return HeadDown;
		case 'U':
			return HeadUp;
		case 'R':
			return Restart;
		case 'F':
			return Fail;
		default:
			return Unknown;
		}
	}
	
	public boolean hasPosition(){
		switch(this){
		case HeadPosition:
			return true;
		default:
			return false;
		}
	}
}
