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

import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.utils.LXUtils;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIToggleSet extends UIParameterComponent implements UIFocus, UIControlTarget, LXParameterListener {

  private String[] options = null;

  private int[] boundaries = null;

  private int selectedIndex = -1;

  private boolean evenSpacing = false;

  private DiscreteParameter parameter = null;

  public UIToggleSet() {
    this(0, 0, 0, 0);
  }

  public UIToggleSet(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  @Override
  protected void onResize() {
    computeBoundaries();
  }

  public UIToggleSet setOptions(String[] options) {
    if (this.options != options) {
      this.options = options;
      this.selectedIndex = 0;
      this.boundaries = new int[options.length];
      computeBoundaries();
      redraw();
    }
    return this;
  }

  @Override
  public DiscreteParameter getParameter() {
    return this.parameter;
  }

  public UIToggleSet setParameter(DiscreteParameter parameter) {
    if (this.parameter != parameter) {
      if (this.parameter != null) {
        this.parameter.removeListener(this);
      }
      this.parameter = parameter;
      if (this.parameter != null) {
        this.parameter.addListener(this);
        String[] options = this.parameter.getOptions();
        if (options == null) {
          options = new String[this.parameter.getRange()];
          for (int i = 0; i < options.length; ++i) {
            options[i] = Integer.toString(this.parameter.getMinValue() + i);
          }
        }
        setOptions(options);
        setSelectedIndex(this.parameter.getValuei() - this.parameter.getMinValue(), false);
      }
    }
    return this;
  }

  public void onParameterChanged(LXParameter parameter) {
    if (parameter == this.parameter) {
      setSelectedIndex(this.parameter.getValuei() - this.parameter.getMinValue(), false);
    }
  }

  private void computeBoundaries() {
    if (this.boundaries == null) {
      return;
    }
    if (this.evenSpacing) {
      for (int i = 0; i < this.boundaries.length; ++i) {
        this.boundaries[i] = (int) ((i + 1) * (this.width-1) / this.boundaries.length);
      }
    } else {
      int totalLength = 0;
      for (String s : this.options) {
        totalLength += s.length();
      }
      int lengthSoFar = 0;
      for (int i = 0; i < this.options.length; ++i) {
        lengthSoFar += this.options[i].length();
        this.boundaries[i] = (int) (lengthSoFar * (this.width-1) / totalLength);
      }
    }
  }

  public UIToggleSet setEvenSpacing() {
    if (!this.evenSpacing) {
      this.evenSpacing = true;
      computeBoundaries();
      redraw();
    }
    return this;
  }

  /**
   * Gets the index of the currently selected value in the toggle set
   *
   * @return currently selected index
   */
  public int getSelectedIndex() {
    return this.selectedIndex;
  }

  /**
   * Get the currently selected option in the toggle set
   *
   * @return Currently selected option
   */
  public String getSelectedOption() {
    return this.options[this.selectedIndex];
  }

  /**
   * Sets the control to select the given option in the toggle set
   *
   * @param option String value, must be one of the options in the toggle set
   * @return this
   */
  public UIToggleSet setSelectedOption(String option) {
    for (int i = 0; i < this.options.length; ++i) {
      if (this.options[i].equals(option)) {
        return setSelectedIndex(i);
      }
    }

    // That string doesn't exist
    String optStr = "{";
    for (String str : this.options) {
      optStr += str + ",";
    }
    optStr = optStr.substring(0, optStr.length() - 1) + "}";
    throw new IllegalArgumentException("Not a valid option in UIToggleSet: "
        + option + " " + optStr);
  }

  /**
   * Sets the control to the given index in the options
   *
   * @param index Index in the toggle set, from 0 to range-1
   * @return this
   */
  public UIToggleSet setSelectedIndex(int index) {
    return setSelectedIndex(index, true);
  }

  private UIToggleSet setSelectedIndex(int index, boolean pushToParameter) {
    if (this.selectedIndex != index) {
      if (index < 0 || index >= this.options.length) {
        throw new IllegalArgumentException("Invalid index to setValue(): "
            + index);
      }
      this.selectedIndex = index;
      if (this.parameter != null && pushToParameter) {
        getLX().command.perform(new LXCommand.Parameter.SetValue(this.parameter, this.parameter.getMinValue() + index));
      }
      onToggle(this.selectedIndex);
      redraw();
    }
    return this;
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    pg.stroke(ui.theme.getControlBorderColor());
    pg.fill(ui.theme.getControlBackgroundColor());
    pg.rect(0, 0, this.width-1, this.height-1);
    for (int b : this.boundaries) {
      pg.line(b, 1, b, this.height - 2);
    }

    pg.noStroke();
    pg.textAlign(PConstants.CENTER, PConstants.CENTER);
    pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
    int leftBoundary = 0;

    for (int i = 0; i < this.options.length; ++i) {
      boolean isActive = (i == this.selectedIndex);
      if (isActive) {
        pg.fill(ui.theme.getPrimaryColor());
        pg.rect(leftBoundary + 1, 1, this.boundaries[i] - leftBoundary - 2, this.height - 2);
      }
      pg.fill(isActive ? UI.WHITE : ui.theme.getControlTextColor());
      pg.text(this.options[i], (leftBoundary + this.boundaries[i]) / 2.f, this.height/2);
      leftBoundary = this.boundaries[i];
    }
  }

  /**
   * Invoked when value is toggled. Subclasses may override.
   *
   * @param value Selected index in the toggle set
   */
  protected void onToggle(int value) {
    onToggle(getSelectedOption());
  }

  /**
   * Invoked when value is toggled, if subclass did not override the integer
   * version of this method. Subclasses may override.
   *
   * @param option Selected option in the toggle set
   */
  protected void onToggle(String option) {
  }

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    for (int i = 0; i < this.boundaries.length; ++i) {
      if (mx < this.boundaries[i]) {
        setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if ((keyCode == java.awt.event.KeyEvent.VK_LEFT)
        || (keyCode == java.awt.event.KeyEvent.VK_DOWN)) {
      consumeKeyEvent();
      setSelectedIndex(LXUtils.constrain(this.selectedIndex - 1, 0, this.options.length - 1));
    } else if ((keyCode == java.awt.event.KeyEvent.VK_RIGHT)
        || (keyCode == java.awt.event.KeyEvent.VK_UP)) {
      consumeKeyEvent();
      setSelectedIndex(LXUtils.constrain(this.selectedIndex + 1, 0, this.options.length - 1));
    }
  }

  @Override
  public LXParameter getControlTarget() {
    if (isMappable() && this.parameter != null && this.parameter.isMappable() && this.parameter.getParent() != null) {
      return this.parameter;
    }
    return null;
  }

}
