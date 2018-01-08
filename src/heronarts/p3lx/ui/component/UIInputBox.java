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
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITimerTask;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIInputBox extends UI2dComponent implements UIFocus {

  public interface ProgressIndicator {
    public boolean hasProgress();
    public double getProgress();
  }

  private static final int TEXT_MARGIN = 2;

  protected boolean enabled = true;

  protected boolean editing = false;
  protected String editBuffer = "";

  protected boolean hasFill = false;
  protected int fillColor = 0;

  private boolean immediateEdit = false;

  private ProgressIndicator progressMeter;
  private int progressPixels = 0;

  protected UIInputBox() {
    this(0, 0, 0, 0);
  }

  protected UIInputBox(float x, float y, float w, float h) {
    super(x, y, w, h);
    setBorderColor(UI.get().theme.getControlBorderColor());
    setBackgroundColor(UI.get().theme.getControlBackgroundColor());
    setTextAlignment(PConstants.CENTER);
  }

  public UIInputBox setProgressIndicator(ProgressIndicator meter) {
    boolean addLoopTask = this.progressMeter == null;
    this.progressMeter = meter;
    if (addLoopTask) {
      addLoopTask(new UITimerTask(30, UITimerTask.Mode.FPS) {
        @Override
        public void run() {
          if (progressMeter != null && progressMeter.hasProgress()) {
            int newProgress = (int) (progressMeter.getProgress() * (width-5));
            if (newProgress != progressPixels) {
              progressPixels = newProgress;
              redraw();
            }
          } else {
            if (progressPixels != 0) {
              progressPixels = 0;
              redraw();
            }
          }
        }
      });
    }
    return this;
  }

  public UIInputBox enableImmediateEdit(boolean immediateEdit) {
    this.immediateEdit = immediateEdit;
    return this;
  }

  protected abstract String getValueString();

  protected abstract void saveEditBuffer();

  public UIInputBox setEnabled(boolean enabled) {
    if (this.enabled != enabled) {
      this.enabled = enabled;
      if (this.editing && !this.enabled) {
        this.editing = false;
      }
      redraw();
    }
    return this;
  }

  public void edit() {
    if (this.enabled && !this.editing) {
      this.editing = true;
      this.editBuffer = "";
    }
    redraw();
  }

  @Override
  protected void onBlur() {
    super.onBlur();
    if (this.editing) {
      this.editing = false;
      saveEditBuffer();
      redraw();
    }
  }

  protected double getFillWidthNormalized() {
    return 0;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    if (this.progressPixels > 0) {
      pg.noFill();
      pg.stroke(ui.theme.getPrimaryColor());
      pg.line(2, this.height-2, 2 + this.progressPixels, this.height-2);
    }

    pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
    if (this.editing) {
      pg.fill(UI.BLACK);
      pg.noStroke();
      pg.rect(0, 0, this.width, this.height);
    } else {
      if (!this.enabled) {
        pg.fill(ui.theme.getControlDisabledColor());
        pg.noStroke();
        pg.rect(1, 1, this.width-2, this.height-2);
      }
      if (this.hasFill) {
        int fillWidth = (int) (getFillWidthNormalized() * (this.width-5));
        if (fillWidth > 0) {
          pg.stroke(this.fillColor);
          pg.line(2, this.height-2, 2 + fillWidth, this.height-2);
        }
      }
    }

    pg.fill(this.editing ? ui.theme.getPrimaryColor() : ui.theme.getControlTextColor());

    String displayString = clipTextToWidth(pg, this.editing ? this.editBuffer : getValueString(), this.width - TEXT_MARGIN);

    if (this.textAlignHorizontal == PConstants.LEFT) {
      pg.textAlign(PConstants.LEFT, PConstants.CENTER);
      pg.text(displayString, 2, this.height / 2);
    } else {
      pg.textAlign(PConstants.CENTER, PConstants.CENTER);
      pg.text(displayString, this.width / 2, this.height / 2);
    }

  }

  protected abstract boolean isValidCharacter(char keyChar);

  /**
   * Subclasses may optionally override to decrement value in response to arrows.
   * Decrement is invoked for the left or down arrow keys.
   *
   * @param keyEvent
   */
  protected void decrementValue(KeyEvent keyEvent) {}

  /**
   * Subclasses may optionally override to decrement value in response to arrows.
   * Increment is invoked for the right or up keys.
   *
   * @param keyEvent
   */
  protected void incrementValue(KeyEvent keyEvent) {}

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (this.editing) {
      // Editing!
      if (isValidCharacter(keyChar)) {
        consumeKeyEvent();
        this.editBuffer += keyChar;
        redraw();
      } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
        consumeKeyEvent();
        this.editing = false;
        saveEditBuffer();
        redraw();
      } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
        consumeKeyEvent();
        if (this.editBuffer.length() > 0) {
          this.editBuffer = this.editBuffer.substring(0, this.editBuffer.length() - 1);
          redraw();
        }
      } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
        consumeKeyEvent();
        this.editing = false;
        redraw();
      }
    } else if (this.enabled) {
      // Not editing
      if (this.immediateEdit && isValidCharacter(keyChar)) {
        consumeKeyEvent();
        this.editing = true;
        this.editBuffer = Character.toString(keyChar);
        redraw();
      } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
        consumeKeyEvent();
        this.editing = true;
        this.editBuffer = "";
        redraw();
      } else if ((keyCode == java.awt.event.KeyEvent.VK_LEFT) || (keyCode == java.awt.event.KeyEvent.VK_DOWN)) {
        decrementValue(keyEvent);
      } else if ((keyCode == java.awt.event.KeyEvent.VK_RIGHT) || (keyCode == java.awt.event.KeyEvent.VK_UP)) {
        incrementValue(keyEvent);
      }
    }
  }

  /**
   * Subclasses may optionally implement to change value based upon mouse click+drag in the box.
   *
   * @param offset Units of mouse movement, positive or negative
   */
  protected void incrementMouseValue(MouseEvent mouseEvent, int offset) {}

  private float dAccum = 0;

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    this.dAccum = 0;
  }

  @Override
  protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    if (this.enabled) {
      this.dAccum -= dy;
      int offset = (int) (this.dAccum / 5);
      this.dAccum = this.dAccum - (offset * 5);
      if (!this.editing) {
        incrementMouseValue(mouseEvent, offset);
      }
    }
  }

}
