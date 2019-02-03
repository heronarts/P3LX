/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UIModulationTarget;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDoubleBox extends UINumberBox implements UIControlTarget, UIModulationSource, UIModulationTarget {

  private double minValue = 0;
  private double maxValue = Double.MAX_VALUE;
  private double value = 0;
  private BoundedParameter parameter = null;

  private boolean normalizedMouseEditing = true;
  protected double editMultiplier = 1;

  private final LXParameterListener parameterListener = new LXParameterListener() {
    public void onParameterChanged(LXParameter p) {
      setValue(p);
    }
  };

  public UIDoubleBox() {
    this(0, 0, 0, 0);
  }

  public UIDoubleBox(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  public UIDoubleBox setEditMultiplier(double editMultiplier) {
    this.editMultiplier = editMultiplier;
    return this;
  }

  public UIDoubleBox setNormalizedMouseEditing(boolean normalizedMouseEditing) {
    this.normalizedMouseEditing = normalizedMouseEditing;
    return this;
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  @Override
  public BoundedParameter getParameter() {
    return this.parameter;
  }

  public UIDoubleBox setParameter(final BoundedParameter parameter) {
    if (this.parameter != null) {
      this.parameter.removeListener(this.parameterListener);
    }
    this.parameter = parameter;
    if (parameter != null) {
      this.minValue = parameter.range.min;
      this.maxValue = parameter.range.max;
      this.parameter.addListener(this.parameterListener);
      setValue(parameter);
    }
    return this;
  }

  public UIDoubleBox setRange(double minValue, double maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    setValue(LXUtils.constrain(this.value, minValue, maxValue));
    return this;
  }

  protected double getNormalized() {
    if (this.parameter != null) {
      if (this.parameter instanceof CompoundParameter) {
        return ((CompoundParameter) this.parameter).getBaseNormalized();
      }
      return this.parameter.getNormalized();
    }
    return (this.value - this.minValue) / (this.maxValue - this.minValue);
  }

  public UIDoubleBox setNormalized(double normalized) {
    if (this.parameter != null) {
      setNormalizedCommand(normalized);
    } else {
      setValue(this.minValue + normalized * (this.maxValue - this.minValue));
    }
    return this;
  }

  @Override
  protected double getFillWidthNormalized() {
    return getNormalized();
  }

  public double getValue() {
    return this.value;
  }

  protected UIDoubleBox setValue(LXParameter p) {
    if (p instanceof CompoundParameter) {
      return setValue(((CompoundParameter) p).getBaseValue(), false);
    } else {
      return setValue(p.getValue(), false);
    }
  }

  public UIDoubleBox setValue(double value) {
    return setValue(value, true);
  }

  protected UIDoubleBox setValue(double value, boolean pushToParameter) {
    value = LXUtils.constrain(value, this.minValue, this.maxValue);
    if (this.value != value) {
      this.value = value;
      if (this.parameter != null && pushToParameter) {
        setValueCommand(value);
      }
      this.onValueChange(this.value);
      redraw();
    }
    return this;
  }

  @Override
  protected String getValueString() {
    if (this.parameter != null) {
      return this.parameter.getFormatter().format(this.value);
    }
    return LXParameter.Units.NONE.format(this.value);
  }

  /**
   * Invoked when value changes, subclasses may override to handle.
   *
   * @param value New value that is being set
   */
  protected /* abstract */ void onValueChange(double value) {}

  @Override
  protected void saveEditBuffer() {
    try {
      // Hacky solution for handling minutes + hours
      String[] parts = this.editBuffer.split(":");
      double value = 0;
      for (String part : parts) {
        value = value * 60 + Double.parseDouble(part);
      }
      setValue(this.editMultiplier * value);
    } catch (NumberFormatException nfx) {}
  }

  public static boolean isValidInputCharacter(char keyChar) {
    return (keyChar >= '0' && keyChar <= '9') || (keyChar == '.') || (keyChar == '-') || (keyChar == ':');
  }

  @Override
  protected boolean isValidCharacter(char keyChar) {
    return isValidInputCharacter(keyChar);
  }

  private LXParameter.Units getUnits() {
    if (this.parameter != null) {
      return this.parameter.getUnits();
    }
    return LXParameter.Units.NONE;
  }

  private double getBaseIncrement() {
    double range = this.maxValue - this.minValue;
    if (this.parameter != null) {
      range = Math.abs(this.parameter.range.max - this.parameter.range.min);
    }
    switch (getUnits()) {
    case MILLISECONDS:
      if (range > 10000) {
        return 1000;
      } else if (range > 1000) {
        return 10;
      }
      return 1;
    default:
      return (range > 100) ? 1 : (range / 100.);
    }
  }

  private double getIncrement(Event inputEvent) {
    double increment = getBaseIncrement();
    if (inputEvent.isShiftDown()) {
      if (this.hasShiftMultiplier) {
        increment *= this.shiftMultiplier;
      } else if (this.parameter != null) {
        increment = (float) (this.parameter.getRange() / 10.);
      } else {
        increment *= .1;
      }
    }
    return increment;
  }

  @Override
  protected void decrementValue(KeyEvent keyEvent) {
    consumeKeyEvent();
    setValue(getValue() - getIncrement(keyEvent));
  }

  @Override
  protected void incrementValue(KeyEvent keyEvent) {
    consumeKeyEvent();
    setValue(getValue() + getIncrement(keyEvent));
  }

  @Override
  protected void incrementMouseValue(MouseEvent mouseEvent, int offset) {
    setValue(this.value + offset * getIncrement(mouseEvent));
  }

  @Override
  public LXParameter getControlTarget() {
    return getMappableParameter();
  }

  @Override
  public LXNormalizedParameter getModulationSource() {
    return getMappableParameter();
  }

  @Override
  public CompoundParameter getModulationTarget() {
    if (this.parameter instanceof CompoundParameter) {
      return (CompoundParameter) getMappableParameter();
    }
    return null;
  }

  private BoundedParameter getMappableParameter() {
    if (isMappable() && this.parameter != null && this.parameter.getComponent() != null) {
      return this.parameter;
    }
    return null;
  }

  @Override
  protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    if (this.enabled && this.editable && !this.editing && this.normalizedMouseEditing && this.parameter != null) {
      float delta = dy / 100.f;
      if (mouseEvent.isShiftDown()) {
        delta /= 10;
      }
      setNormalized(LXUtils.constrain(getNormalized() - delta, 0, 1));
    } else {
      super.onMouseDragged(mouseEvent, mx, my, dx, dy);
    }
  }

}
