package heronarts.p3lx.ui.component;

import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITimerTask;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIFunctionalKnob extends UIParameterControl implements UIFocus {

  private double lastParameterValue = 0;

  private final UITimerTask checkRedrawTask = new UITimerTask(30, UITimerTask.Mode.FPS) {
    @Override
    public void run() {
      double parameterValue = getNormalized();
      if (parameterValue != lastParameterValue) {
        redraw();
      }
      lastParameterValue = parameterValue;
    }
  };

  public UIFunctionalKnob(LXNormalizedParameter parameter) {
    this();
    setParameter(parameter);
  }

  public UIFunctionalKnob() {
    this(0, 0);
  }

  public UIFunctionalKnob(float x, float y) {
    this(x, y, UIKnob.WIDTH, UIKnob.KNOB_SIZE);
  }

  public UIFunctionalKnob(float x, float y, float w, float h) {
    super(x, y, w, h);
    this.keyEditable = true;
    enableImmediateEdit(true);
    addLoopTask(checkRedrawTask);
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    // value refers to the current, function-derived value of the control's parameter.
    float value = (float) getNormalized();
    float valueEnd = UIKnob.ARC_START + value * UIKnob.ARC_RANGE;
    float valueStart;
    switch (this.polarity) {
    case BIPOLAR: valueStart = UIKnob.ARC_START + UIKnob.ARC_RANGE/2; break;
    default: case UNIPOLAR: valueStart = UIKnob.ARC_START; break;
    }

    float arcSize = UIKnob.KNOB_SIZE;
    pg.noStroke();
    pg.ellipseMode(PConstants.CENTER);

    // Outer fill
    pg.noStroke();
    pg.fill(ui.theme.getControlBackgroundColor());
    pg.arc(UIKnob.ARC_CENTER_X, UIKnob.ARC_CENTER_Y, arcSize, arcSize, UIKnob.ARC_START, UIKnob.ARC_END);

    // Compute color for value fill
    int valueColor;
    if (isEnabled()) {
      valueColor = getModulatedValueColor(ui.theme.getPrimaryColor());
    } else {
      int disabled = ui.theme.getControlDisabledColor();
      valueColor = disabled;
    }

    // Value indication
    pg.fill(valueColor);
    pg.arc(UIKnob.ARC_CENTER_X, UIKnob.ARC_CENTER_Y, arcSize, arcSize, Math.min(valueStart, valueEnd), Math.max(valueStart, valueEnd));

    // Center tick mark for bipolar knobs
    if (this.polarity == LXParameter.Polarity.BIPOLAR) {
      pg.stroke(0xff333333);
      pg.line(UIKnob.ARC_CENTER_X, UIKnob.ARC_CENTER_Y, UIKnob.ARC_CENTER_X, UIKnob.ARC_CENTER_Y - arcSize/2);
    }

    // Center dot
    pg.noStroke();
    pg.fill(0xff333333);
    pg.ellipse(UIKnob.ARC_CENTER_X, UIKnob.ARC_CENTER_Y, 8, 8);

    super.onDraw(ui,  pg);
  }

}
