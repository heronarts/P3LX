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
import processing.core.PGraphics;
import processing.core.PImage;

public class UIImage extends UI2dComponent {

  private final PImage image;

  private boolean hasTint = false;

  private int tint = UI.WHITE;

  public UIImage(PImage image) {
    this(image, 0, 0);
  }

  public UIImage(PImage image, float x, float y) {
    super(x, y, image.width, image.height);
    this.image = image;
  }

  public UIImage setTint(boolean hasTint) {
    if (this.hasTint != hasTint) {
      this.hasTint = hasTint;
      redraw();
    }
    return this;
  }

  public UIImage setTintColor(int tint) {
    if (!this.hasTint || (this.tint != tint)) {
      this.hasTint = true;
      this.tint = tint;
      redraw();
    }
    return this;
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    if (this.hasTint) {
      pg.tint(this.tint);
    }
    pg.image(this.image, 0, 0, this.width, this.height);
    pg.noTint();
  }

}
