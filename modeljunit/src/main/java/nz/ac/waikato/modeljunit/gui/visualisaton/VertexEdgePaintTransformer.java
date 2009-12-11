/**
 Copyright (C) 2008 Jerramy Winchester
 This file is part of the CZT project.

 The CZT project contains free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published
 by the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 The CZT project is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with CZT; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nz.ac.waikato.modeljunit.gui.visualisaton;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * @author Jerramy Winchester
 *
 */
public class VertexEdgePaintTransformer<V,E> implements Transformer<V, Paint> {	
	
  private Color DEFAULT_LINE_COLOR = ColorUtil.EXPLORED;
  private Color PICKED = ColorUtil.PICKED;
  
	private PickedInfo<Object> pi;
	private ArrayList<Object> vertecies_;
	
	public VertexEdgePaintTransformer(PickedInfo<Object> pi, ArrayList<Object> v){
		if (pi == null)
			throw new IllegalArgumentException("PickedInfo instance must be non-null");
		this.pi = pi;
		this.vertecies_ = v;
	}	
	
	@Override
	public Paint transform(V v){
		
		try{
			if (v instanceof Graph){	
				if(pi.isPicked(v)){
					return PICKED;
				}				
				return DEFAULT_LINE_COLOR;						
			}
			if(v instanceof VertexInfo){
				if (pi.isPicked(vertecies_.get(vertecies_.indexOf(v)))){					
					return PICKED;
				}
				else
				{				
					if(vertecies_.contains(v)){
						VertexInfo vert = (VertexInfo)vertecies_.get(vertecies_.indexOf(v));
						if(vert.getIsDisplayed() || vert.getIsVisited()){							
							return DEFAULT_LINE_COLOR;
						}
					}
				}
			}							
			return DEFAULT_LINE_COLOR;
						
		} catch (Exception e){						
			return DEFAULT_LINE_COLOR;
		}
	}

}
