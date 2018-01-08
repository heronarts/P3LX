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

package heronarts.p3lx;

import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class P3LXGraphicsPattern extends P3LXPattern {

  private final PGraphics pg;

  protected P3LXGraphicsPattern(P3LX lx) {
    super(lx);
    this.pg = this.applet.createGraphics(lx.width, lx.height, PConstants.P2D);
  }

  @Override
  final protected void run(double deltaMs) {
    this.pg.beginDraw();
    this.run(deltaMs, this.pg);
    this.pg.endDraw();
    this.pg.loadPixels();
    for (int i = 0; i < this.lx.total; ++i) {
      this.colors[i] = this.pg.pixels[i];
    }
  }

  abstract protected void run(double deltaMs, PGraphics pg);

}
