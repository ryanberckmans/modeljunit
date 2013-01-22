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

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

/**
 * @author Jerramy Winchester
 * 
 */
public class EdgeStrokeTransformer<V, E> implements Transformer<Object, Stroke> {

    //Set the lines that the graph should use between vertecies
    private float dash[] = { 5.0f };
    private final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
                    0.0f);

    /**
     * Return the stroke for this edge.
     */
    public Stroke transform(Object o) {
        if (o instanceof EdgeInfo) {
            EdgeInfo e = (EdgeInfo) o;
            if (e.getIsVisited()) {
                if (e.getIsCurrSeq()) {
                    return new BasicStroke(2.0f);
                }
                return new BasicStroke(0.45f);
            }
        }
        return edgeStroke;
    }
}
