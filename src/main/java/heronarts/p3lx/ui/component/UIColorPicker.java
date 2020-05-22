/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.ui.component;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITimerTask;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.utils.LXUtils;

public class UIColorPicker extends UI2dComponent {

  public enum Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT
  };

  private Corner corner = Corner.BOTTOM_RIGHT;

  private final ColorParameter color;
  private final UIColorOverlay uiColorOverlay;

  public UIColorPicker(ColorParameter color) {
    this(UIKnob.WIDTH, UIKnob.WIDTH, color);
  }

  public UIColorPicker(float w, float h, ColorParameter color) {
    this(0, 0, w, h, color);
  }

  public UIColorPicker(float x, float y, float w, float h, ColorParameter color) {
    super(x, y, w, h);
    setBorderColor(UI.get().theme.getControlBorderColor());
    setBackgroundColor(color.getColor());

    this.color = color;
    this.uiColorOverlay = new UIColorOverlay();

    // Redraw with color in real-time, if modulated
    addLoopTask(new UITimerTask(30, UITimerTask.Mode.FPS) {
      @Override
      protected void run() {
        setBackgroundColor(LXColor.hsb(
          color.hue.getValuef(),
          color.saturation.getValuef(),
          color.brightness.getValuef()
        ));
      }
    });
  }

  public UIColorPicker setCorner(Corner corner) {
    this.corner = corner;
    return this;
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    final float overlap = 6;

    switch (this.corner) {
    case BOTTOM_LEFT:
      this.uiColorOverlay.setPosition(this, overlap - this.uiColorOverlay.getWidth(), this.height - overlap);
      break;
    case BOTTOM_RIGHT:
      this.uiColorOverlay.setPosition(this, this.width - overlap, this.height - overlap);
      break;
    case TOP_LEFT:
      this.uiColorOverlay.setPosition(this, overlap - this.uiColorOverlay.getWidth(), overlap - this.uiColorOverlay.getHeight());
      break;
    case TOP_RIGHT:
      this.uiColorOverlay.setPosition(this, this.width - overlap, overlap - this.uiColorOverlay.getHeight());
      break;
    }
    getUI().showContextOverlay(this.uiColorOverlay);
  }

  private class UIColorOverlay extends UI2dContainer {
    UIColorOverlay() {
      super(0, 0, 240, UISwatch.HEIGHT + 8);
      setBackgroundColor(UI.get().theme.getDeviceBackgroundColor());
      setBorderColor(UI.get().theme.getControlBorderColor());
      setBorderRounding(6);

      new UISwatch().addToContainer(this);

      float xp = UISwatch.WIDTH;
      float yp = 16;
      new UIDoubleBox(xp, yp, 60, color.hue).addToContainer(this);
      new UILabel(xp, yp + 16, 60, "Hue").setTextAlignment(PConstants.CENTER).addToContainer(this);

      yp += 40;

      new UIDoubleBox(xp, yp, 60, color.saturation).addToContainer(this);
      new UILabel(xp, yp + 16, 60, "Sat").setTextAlignment(PConstants.CENTER).addToContainer(this);

      yp += 40;

      new UIDoubleBox(xp, yp, 60, color.brightness).addToContainer(this);
      new UILabel(xp, yp + 16, 60, "Bright").setTextAlignment(PConstants.CENTER).addToContainer(this);

    }

    private class UISwatch extends UI2dComponent implements UIFocus {

      private static final float PADDING = 8;

      private static final float GRID_X = PADDING;
      private static final float GRID_Y = PADDING;

      private static final float GRID_WIDTH = 120;
      private static final float GRID_HEIGHT = 120;

      private static final float BRIGHT_SLIDER_X = 140;
      private static final float BRIGHT_SLIDER_Y = PADDING;
      private static final float BRIGHT_SLIDER_WIDTH = 16;
      private static final float BRIGHT_SLIDER_HEIGHT = GRID_HEIGHT;

      private static final float WIDTH = BRIGHT_SLIDER_X + BRIGHT_SLIDER_WIDTH + 2*PADDING;
      private static final float HEIGHT = GRID_HEIGHT + 2*PADDING;

      public UISwatch() {
        super(4, 4, WIDTH, HEIGHT);
        color.addListener((p) -> { redraw(); });
        setFocusCorners(false);
      }

      @Override
      public void onDraw(UI ui, PGraphics pg) {

        float hue = color.hue.getBaseValuef();
        float saturation = color.saturation.getBaseValuef();
        float brightness = color.brightness.getBaseValuef();

        // Main color grid
        for (int x = 0; x < GRID_WIDTH; ++x) {
          for (int y = 0; y < GRID_HEIGHT; ++y) {
            pg.stroke(LXColor.hsb(
              360 * (x / GRID_WIDTH),
              100 * (1 - y / GRID_HEIGHT),
              brightness
            ));
            pg.point(GRID_X + x, GRID_Y + y);
          }
        }

        // Brightness slider
        for (int y = 0; y < BRIGHT_SLIDER_HEIGHT; ++y) {
          pg.stroke(LXColor.hsb(hue, saturation, 100 - 100 * (y / BRIGHT_SLIDER_HEIGHT)));
          pg.line(BRIGHT_SLIDER_X, BRIGHT_SLIDER_Y + y, BRIGHT_SLIDER_X + BRIGHT_SLIDER_WIDTH - 1, BRIGHT_SLIDER_Y + y);
        }

        // Color square
        pg.noFill();
        pg.stroke(brightness < 50 ? 0xffffffff : 0xff000000);
        pg.ellipseMode(PConstants.CORNER);
        pg.ellipse(
          GRID_X + hue / 360 * GRID_WIDTH,
          GRID_Y + (1 - saturation / 100) * GRID_HEIGHT,
          4,
          4
        );

        // Brightness triangle
        pg.fill(0xffcccccc);
        pg.noStroke();

        pg.beginShape(PConstants.TRIANGLES);
        float xp = BRIGHT_SLIDER_X;
        float yp = BRIGHT_SLIDER_Y + (1 - brightness / 100) * BRIGHT_SLIDER_HEIGHT;
        pg.vertex(xp, yp);
        pg.vertex(xp - 6, yp - 4);
        pg.vertex(xp - 6, yp + 4);

        pg.vertex(xp + BRIGHT_SLIDER_WIDTH, yp);
        pg.vertex(xp + BRIGHT_SLIDER_WIDTH + 6, yp + 4);
        pg.vertex(xp + BRIGHT_SLIDER_WIDTH + 6, yp - 4);

      }

      private boolean draggingBrightness = false;

      @Override
      public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.draggingBrightness = (mx > GRID_X + GRID_WIDTH);
        if (!this.draggingBrightness) {
          setHueSaturation(mx, my);
        }
      }

      private void setHueSaturation(float mx, float my) {
        mx = LXUtils.clampf(mx - GRID_X, 0, GRID_WIDTH);
        my = LXUtils.clampf(my - GRID_Y, 0, GRID_WIDTH);
        color.hue.setValue(mx / GRID_WIDTH * 360);
        color.saturation.setValue(100 - my / GRID_HEIGHT * 100);
      }

      @Override
      public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (this.draggingBrightness) {
          if (dy != 0) {
            float brightness = color.brightness.getBaseValuef();
            brightness = LXUtils.clampf(brightness - 100 * dy / BRIGHT_SLIDER_HEIGHT, 0, 100);
            color.brightness.setValue(brightness);
          }
        } else {
          setHueSaturation(mx, my);
        }
      }

      @Override
      public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        float inc = keyEvent.isShiftDown() ? 10 : 2;
        if (keyCode == java.awt.event.KeyEvent.VK_UP) {
          consumeKeyEvent();
          color.saturation.setValue(LXUtils.clampf(color.saturation.getBaseValuef() + inc, 0, 100));
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
          consumeKeyEvent();
          color.saturation.setValue(LXUtils.clampf(color.saturation.getBaseValuef() - inc, 0, 100));
        } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
          consumeKeyEvent();
          color.hue.setValue(LXUtils.clampf(color.hue.getBaseValuef() - 3*inc, 0, 360));
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
          consumeKeyEvent();
          color.hue.setValue(LXUtils.clampf(color.hue.getBaseValuef() + 3*inc, 0, 360));
        }
      }

    }
  }


}
