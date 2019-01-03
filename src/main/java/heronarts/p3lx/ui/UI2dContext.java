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

package heronarts.p3lx.ui;

import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * A UIContext is a container that owns a graphics buffer. This buffer is
 * persistent across frames and is only redrawn as necessary. It is simply
 * bitmapped onto the UI that is a part of.
 */
public class UI2dContext extends UI2dContainer {

  /**
   * Graphics context for this container.
   */
  private final PGraphics pg;

  /**
   * Constructs a new UI2dContext
   *
   * @param ui the UI to place it in
   * @param x x-position
   * @param y y-position
   * @param w width
   * @param h height
   */
  public UI2dContext(UI ui, float x, float y, float w, float h) {
    super(x, y, w, h);
    this.pg = ui.applet.createGraphics((int) w, (int) h, PConstants.JAVA2D);
    this.pg.smooth();
  }

  @Override
  protected void onResize() {
    this.pg.setSize((int) this.width, (int) this.height);
    redraw();
  }

  @Override
  void draw(UI ui, PGraphics pg) {
    if (!isVisible()) {
      return;
    }
    if (this.needsRedraw || this.childNeedsRedraw) {
      this.pg.beginDraw();
      super.draw(ui, this.pg);
      this.pg.endDraw();
    }
    pg.image(this.pg, 0, 0);
  }

  protected PGraphics getGraphics() {
    return this.pg;
  }
}
