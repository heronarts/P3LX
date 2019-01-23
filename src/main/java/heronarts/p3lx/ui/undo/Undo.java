/**
 * Copyright 2019- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.ui.undo;

import java.util.Stack;

import heronarts.lx.clipboard.LXNormalizedValue;
import heronarts.lx.parameter.LXNormalizedParameter;

public class Undo {

  public interface Action {
    public void undo();

    public static class SetNormalized implements Action {

      private final LXNormalizedParameter parameter;
      private final LXNormalizedValue value;

      public SetNormalized(LXNormalizedParameter parameter) {
        this.parameter = parameter;
        this.value = new LXNormalizedValue(parameter);
      }

      public void undo() {
        this.parameter.setNormalized(this.value.getValue());
      }
    }

  }

  private final Stack<Action> actions = new Stack<Action>();

  public Undo push(Action action) {
    this.actions.push(action);
    return this;
  }

  public Undo undo() {
    if (!this.actions.empty()) {
      Action action = this.actions.pop();
      if (action != null) {
        action.undo();
      }
    }
    return this;
  }

  // TODO(mcslee): add more advanced hooks for components, parameters that
  // have been disposed of, etc.
}
