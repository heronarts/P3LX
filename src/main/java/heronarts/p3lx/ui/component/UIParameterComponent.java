package heronarts.p3lx.ui.component;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.command.LXCommand;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIContextActions;

public abstract class UIParameterComponent extends UI2dComponent implements UIContextActions {
  protected UIParameterComponent(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  protected void pushUndoCommand() {
    pushUndoCommand(getParameter());
  }

  protected void pushUndoCommand(LXParameter parameter) {
    if (parameter != null && parameter.getComponent() != null) {
      if (parameter instanceof LXNormalizedParameter) {
        getLX().command.push(new LXCommand.Parameter.SetNormalized((LXNormalizedParameter) parameter));
      }
    }
  }

  public LXParameter getParameter() {
    return null;
  }

  public String getOscAddress() {
    LXParameter parameter = getParameter();
    if (parameter != null) {
      return LXOscEngine.getOscAddress(parameter);
    }
    return null;
  }

  @Override
  public List<Action> getContextActions() {
    List<Action> actions = new ArrayList<Action>();
    LXParameter parameter = getParameter();
    if (parameter != null) {
      actions.add(new UIContextActions.Action.ResetParameter(parameter));
    }
    String oscAddress = getOscAddress();
    if (oscAddress != null) {
      actions.add(new UIContextActions.Action.CopyOscAddress(oscAddress));
    }
    return actions;
  }

}