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
import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UIControlTarget;
import processing.core.PConstants;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIIntegerBox extends UINumberBox implements UIControlTarget {

  private int minValue = 0;
  private int maxValue = PConstants.MAX_INT;
  private int value = 0;
  protected DiscreteParameter parameter = null;
  protected int editMultiplier = 1;

  private final LXParameterListener parameterListener = new LXParameterListener() {
    public void onParameterChanged(LXParameter p) {
      setValue(parameter.getValuei(), false);
    }
  };

  public UIIntegerBox() {
    this(0, 0, 0, 0);
  }

  public UIIntegerBox(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  public UIIntegerBox setParameter(final DiscreteParameter parameter) {
    if (this.parameter != parameter) {
      if (this.parameter != null) {
        this.parameter.removeListener(this.parameterListener);
      }
      this.parameter = parameter;
      if (parameter != null) {
        this.minValue = parameter.getMinValue();
        this.maxValue = parameter.getMaxValue();
        this.value = parameter.getValuei();
        this.parameter.addListener(this.parameterListener);
      }
      redraw();
    }
    return this;
  }

  public UIIntegerBox setRange(int minValue, int maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    setValue(LXUtils.constrain(this.value, minValue, maxValue));
    return this;
  }

  public UIIntegerBox setEditMultiplier(int editMultiplier) {
    this.editMultiplier = editMultiplier;
    return this;
  }

  @Override
  protected double getFillWidthNormalized() {
    if (this.parameter != null) {
      return this.parameter.getNormalized();
    }
    return (this.value - this.minValue) / (this.maxValue - this.minValue);
  }

  public int getValue() {
    return this.value;
  }

  @Override
  public String getValueString() {
    if (this.parameter != null) {
      return this.parameter.getFormatter().format(this.value);
    }
    return Integer.toString(this.value);
  }

  public UIIntegerBox setValue(int value) {
    return setValue(value, true);
  }

  protected UIIntegerBox setValue(int value, boolean pushToParameter) {
    if (this.value != value) {
      int range = (this.maxValue - this.minValue + 1);
      while (value < this.minValue) {
        value += range;
      }
      this.value = this.minValue + (value - this.minValue) % range;
      if (this.parameter != null && pushToParameter) {
        getLX().command.perform(new LXCommand.Parameter.SetValue(this.parameter, this.value));
      }
      this.onValueChange(this.value);
      redraw();
    }
    return this;
  }

  /**
   * Subclasses may override to handle value changes
   *
   * @param value New value being set
   */
  protected void onValueChange(int value) {}

  @Override
  protected void saveEditBuffer() {
    try {
      setValue(this.editMultiplier * Integer.parseInt(this.editBuffer));
    } catch (NumberFormatException nfx) {}
  }

  @Override
  protected boolean isValidCharacter(char keyChar) {
    return (keyChar >= '0' && keyChar <= '9');
  }

  private int getIncrement(Event inputEvent) {
    int increment = 1;
    if (inputEvent.isShiftDown()) {
      if (this.hasShiftMultiplier) {
        increment *= this.shiftMultiplier;
      } else if (this.parameter != null) {
        increment = Math.max(1, this.parameter.getRange() / 10);
      } else {
        increment *= 10;
      }
    }
    return increment;
  }

  @Override
  protected void incrementValue(KeyEvent keyEvent) {
    consumeKeyEvent();
    setValue(this.value + getIncrement(keyEvent));
  }

  @Override
  protected void decrementValue(KeyEvent keyEvent) {
    consumeKeyEvent();
    setValue(this.value - getIncrement(keyEvent));
  }

  @Override
  protected void incrementMouseValue(MouseEvent mouseEvent, int offset) {
    setValue(this.value + getIncrement(mouseEvent) * offset);
  }

  @Override
  public LXParameter getControlTarget() {
    if (isMappable() && this.parameter != null && this.parameter.isMappable() && this.parameter.getParent() != null) {
      return this.parameter;
    }
    return null;
  }

}
