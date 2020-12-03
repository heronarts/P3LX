package heronarts.p3lx.ui.component;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.command.LXCommand;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIContextActions;
import processing.event.MouseEvent;

public abstract class UIParameterComponent extends UI2dComponent implements UIContextActions {

  public static final float DEFAULT_HEIGHT = 16;

  protected boolean useCommandEngine = true;

  private boolean enableContextActions = true;


  protected UIParameterComponent(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  public abstract LXParameter getParameter();

  public UIParameterComponent setEnableContextActions(boolean enableContextActions) {
    this.enableContextActions = enableContextActions;
    return this;
  }

  public String getOscAddress() {
    LXParameter parameter = getParameter();
    if (parameter != null) {
      return LXOscEngine.getOscAddress(parameter);
    }
    return null;
  }

  public UIParameterComponent setUseCommandEngine(boolean useCommandEngine) {
    this.useCommandEngine = useCommandEngine;
    return this;
  }

  @Override
  public List<Action> getContextActions() {
    if (!this.enableContextActions) {
      return null;
    }
    List<Action> actions = new ArrayList<Action>();
    LXParameter parameter = getParameter();
    if (parameter != null && !(parameter instanceof BooleanParameter)) {
      actions.add(new UIContextActions.Action.ResetParameter(parameter));
    }
    String oscAddress = getOscAddress();
    if (oscAddress != null) {
      actions.add(new UIContextActions.Action.CopyOscAddress(oscAddress));
    }
    return actions;
  }

  private LXCommand.Parameter.SetNormalized mouseEditCommand = null;

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    super.onMousePressed(mouseEvent, mx, my);
    LXParameter parameter = getParameter();
    if (parameter != null && parameter instanceof LXNormalizedParameter) {
      if (this.useCommandEngine) {
        this.mouseEditCommand = new LXCommand.Parameter.SetNormalized((LXNormalizedParameter) parameter);
      }
    }
  }

  @Override
  protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
    super.onMouseReleased(mouseEvent, mx, my);
    this.mouseEditCommand = null;
  }

  protected void setNormalizedCommand(double newValue) {
    if (this.mouseEditCommand != null) {
      getLX().command.perform(this.mouseEditCommand.update(newValue));
    } else {
      LXParameter parameter = getParameter();
      if (parameter != null && parameter instanceof LXNormalizedParameter) {
        if (this.useCommandEngine) {
          getLX().command.perform(new LXCommand.Parameter.SetNormalized((LXNormalizedParameter) parameter, newValue));
        } else {
          ((LXNormalizedParameter) parameter).setNormalized(newValue);
        }
      }
    }
  }

}