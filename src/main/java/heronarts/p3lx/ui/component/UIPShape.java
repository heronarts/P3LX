/**
 * Copyright 2021- Mark C. Slee, Heron Arts LLC
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

import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import processing.core.PShape;

public class UIPShape extends UI3dComponent {

  private final PShape shape;
  private float x = 0;
  private float y = 0;
  private float w = 0;
  private float h = 0;

  private boolean hasSize = false;
  private boolean hasPosition = false;

  public UIPShape(P3LX lx, String filename) {
    this.shape = lx.applet.loadShape(filename);
  }

  public UIPShape setPosition(float x, float y) {
    this.x = x;
    this.y = y;
    this.hasPosition = (this.x != 0) || (this.y != 0);
    return this;
  }

  public UIPShape setSize(float w, float h) {
    this.hasSize = true;
    this.w = w;
    this.h = h;
    return this;
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    if (this.hasSize) {
      pg.shape(this.shape, this.x, this.y, this.w, this.h);
    } else if (this.hasPosition) {
      pg.shape(this.shape, this.x, this.y);
    } else {
      pg.shape(this.shape);
    }
  }
}
