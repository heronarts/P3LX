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

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * A simple text label object. Draws a string aligned top-left to its x-y
 * position.
 */
public class UILabel extends UI2dComponent {

  public static final float DEFAULT_HEIGHT = 12;

  private int topPadding = 0;
  private int rightPadding = 0;
  private int leftPadding = 0;
  private int bottomPadding = 0;
  private boolean breakLines = false;
  private boolean autoHeight = false;

  /**
   * Label text
   */
  private String label = "";

  public UILabel() {
    this(0, 0, 0, 0);
  }

  public UILabel(float w, String label) {
    this(w, DEFAULT_HEIGHT, label);
  }

  public UILabel(float w, float h, String label) {
    this(0, 0, w, h);
    setLabel(label);
  }

  public UILabel(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  /**
   * Sets the label to render text multi-line
   *
   * @param breakLines Whether to break lines
   * @return this
   */
  public UILabel setBreakLines(boolean breakLines) {
    return setBreakLines(breakLines, false);
  }


  /**
   * Sets the label to render text multi-line
   *
   * @param breakLines Whether to break lines
   * @param autoHeight Whether to resize automatically based upon line height
   * @return this
   */
  public UILabel setBreakLines(boolean breakLines, boolean autoHeight) {
    if (this.breakLines != breakLines || this.autoHeight != autoHeight) {
      this.breakLines = breakLines;
      this.autoHeight = autoHeight;
      redraw();
    }
    return this;
  }

  /**
   * Sets padding on all 4 sides
   *
   * @param padding Padding
   * @return this
   */
  public UILabel setPadding(int padding) {
    return setPadding(padding, padding, padding, padding);
  }

  /**
   * Sets padding on top and sides, CSS style
   *
   * @param topBottom Top bottom padding
   * @param leftRight Left right padding
   * @return this
   */
  public UILabel setPadding(int topBottom, int leftRight) {
    return setPadding(topBottom, leftRight, topBottom, leftRight);
  }

  /**
   * Sets padding on all 4 sides
   *
   * @param topPadding Top padding
   * @param rightPadding Right padding
   * @param bottomPadding Bottom padding
   * @param leftPadding Left padding
   * @return this
   */
  public UILabel setPadding(int topPadding, int rightPadding, int bottomPadding, int leftPadding) {
    boolean redraw = false;
    if (this.topPadding != topPadding) {
      this.topPadding = topPadding;
      redraw = true;
    }
    if (this.rightPadding != rightPadding) {
      this.rightPadding = rightPadding;
      redraw = true;
    }
    if (this.bottomPadding != bottomPadding) {
      this.bottomPadding = bottomPadding;
      redraw = true;
    }
    if (this.leftPadding != leftPadding) {
      this.leftPadding = leftPadding;
      redraw = true;
    }
    if (redraw) {
      redraw();
    }
    return this;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    pg.textFont(hasFont() ? getFont() : ui.theme.getLabelFont());
    pg.fill(hasFontColor() ? getFontColor() : ui.theme.getLabelColor());
    float tx = this.leftPadding, ty = this.topPadding;
    switch (this.textAlignHorizontal) {
    case PConstants.CENTER:
      tx = this.width / 2;
      break;
    case PConstants.RIGHT:
      tx = this.width - this.rightPadding;
      break;
    }
    switch (this.textAlignVertical) {
    case PConstants.BASELINE:
      ty = this.height - this.bottomPadding;
      break;
    case PConstants.BOTTOM:
      ty = this.height - this.bottomPadding;
      break;
    case PConstants.CENTER:
      ty = this.height / 2;
      break;
    }
    String str;
    if (this.breakLines) {
      str = breakTextToWidth(pg, this.label, this.width - this.leftPadding - this.rightPadding);
      if (this.autoHeight) {
        int numLines = (int) str.chars().filter(ch -> ch == '\n').count();
        setHeight(this.bottomPadding + this.topPadding + pg.textLeading * (numLines + 1) - pg.textDescent());
      }
    } else {
      str = clipTextToWidth(pg, this.label, this.width - this.leftPadding - this.rightPadding);
    }
    pg.textAlign(this.textAlignHorizontal, this.textAlignVertical);
    pg.text(str, tx + this.textOffsetX, ty + this.textOffsetY);
  }

  public UILabel setLabel(String label) {
    if (this.label != label) {
      this.label = label;
      redraw();
    }
    return this;
  }

  @Override
  public String getDescription() {
    return this.label;
  }
}
