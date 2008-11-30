/**
 Copyright (C) 2007 Mark Utting
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

package nz.ac.waikato.modeljunit;

import nz.ac.waikato.modeljunit.Transition;

/** An implementation of ModelListener that prints
 *  event messages to the Model's <code>getOutput()</code> stream.
 */
public class VerboseListener extends AbstractListener
{
  public VerboseListener()
  {
  }

  /** @deprecated Use the null constructor instead. */
  public VerboseListener(Model model)
  {
  }

  @Override
  public String getName()
  {
    return "verbose";
  }

  @Override
  public void doneReset(String reason, boolean testing)
  {
    model_.printMessage("done " + reason + " reset("+testing+")");
  }

  @Override
  public void doneTransition(int action, Transition tr)
  {
    model_.printMessage("done " + tr.toString());
  }

  @Override
  public void failure(TestFailureException ex)
  {
    model_.printMessage("FAILURE: "+ex.getLocalizedMessage());
  }
}
