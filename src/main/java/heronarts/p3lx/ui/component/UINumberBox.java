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

public abstract class UINumberBox extends UIInputBox {

  protected boolean hasShiftMultiplier = false;
  protected float shiftMultiplier = 1;

  protected UINumberBox() {
    this(0, 0, 0, 0);
  }

  protected UINumberBox(float x, float y, float w, float h) {
    super(x, y, w, h);
    enableImmediateEdit(true);
  }

  public UINumberBox setFillStyle(FillStyle fillStyle) {
    if (this.fillStyle != fillStyle) {
      this.fillStyle = fillStyle;
      if (this.hasFill) {
        redraw();
      }
    }
    return this;
  }

  public UINumberBox setFill(boolean hasFill) {
    if (this.hasFill != hasFill) {
      this.hasFill = hasFill;
      redraw();
    }
    return this;
  }

  public UINumberBox setFillColor(int fillColor) {
    if (!this.hasFill || (this.fillColor != fillColor)) {
      this.hasFill = true;
      this.fillColor = fillColor;
      redraw();
    }
    return this;
  }

  /**
   * Sets a multiplier by which the amount value changes are modulated
   * when the shift key is down. Either for more precise control or
   * larger jumps, depending on the component.
   *
   * @param shiftMultiplier Amount to multiply by
   * @return this
   */
  public UINumberBox setShiftMultiplier(float shiftMultiplier) {
    this.hasShiftMultiplier = true;
    this.shiftMultiplier = shiftMultiplier;
    return this;
  }

}
