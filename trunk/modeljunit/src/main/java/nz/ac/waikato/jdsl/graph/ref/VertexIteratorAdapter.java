/*
  Copyright (c) 1999, 2000 Brown University, Providence, RI
  
                            All Rights Reserved
  
  Permission to use, copy, modify, and distribute this software and its
  documentation for any purpose other than its incorporation into a
  commercial product is hereby granted without fee, provided that the
  above copyright notice appear in all copies and that both that
  copyright notice and this permission notice appear in supporting
  documentation, and that the name of Brown University not be used in
  advertising or publicity pertaining to distribution of the software
  without specific, written prior permission.
  
  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN
  UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
  DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
  PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
  TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  PERFORMANCE OF THIS SOFTWARE.
*/

package nz.ac.waikato.jdsl.graph.ref;

import nz.ac.waikato.jdsl.core.api.*;
import nz.ac.waikato.jdsl.graph.api.*;

/**
 * Takes an ObjectIterator known to be iterating over things that  
 * are Vertices, and makes it appear as an VertexIterator.
 *   
 * @version JDSL 2.1.1 
 * @author Mark Handy
 */
public class VertexIteratorAdapter implements VertexIterator {

    private ObjectIterator iter_m;
    public VertexIteratorAdapter( ObjectIterator pi ) {
	iter_m = pi;
    }
    
    public boolean hasNext() { return iter_m.hasNext(); }
    
    public Object nextObject() { return nextVertex(); }
    public Position nextPosition() { return nextVertex(); }
    public Vertex nextVertex() { return (Vertex)iter_m.nextObject(); }

    public Object object() { return vertex(); }
    public Position position() { return vertex(); }
    public Vertex vertex() { return (Vertex)iter_m.object(); }
    
    public Object element() { return vertex().element(); }

    public void reset() { iter_m.reset(); }
    
}
