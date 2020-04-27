/**
 * Copyright 2018- Mark C. Slee, Heron Arts LLC
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
import heronarts.p3lx.ui.UIContextActions;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIContextMenu extends UI2dComponent {

  private static final float DEFAULT_ROW_HEIGHT = 18;
  public static final float DEFAULT_WIDTH = 120;

  private UIContextActions.Action[] actions = new UIContextActions.Action[0];;

  private int highlight = -1;
  private float rowHeight = DEFAULT_ROW_HEIGHT;
  private float padding = 0;

  public UIContextMenu(float x, float y, float w, float h) {
    super(x, y, w, h);
    setVisible(false);
    setBackgroundColor(UI.get().theme.getContextBackgroundColor());
    setBorderColor(UI.get().theme.getContextBorderColor());
  }

  public UIContextMenu setPadding(float padding) {
    if (this.padding != padding) {
      this.padding = padding;
      updateHeight();
    }
    return this;
  }

  public UIContextMenu setRowHeight(float rowHeight) {
    if (this.rowHeight != rowHeight) {
      this.rowHeight = rowHeight;
      updateHeight();
    }
    return this;
  }

  public UIContextMenu setActions(UIContextActions.Action[] actions) {
    this.actions = actions;
    updateHeight();
    return this;
  }

  private void updateHeight() {
    setHeight(this.actions.length * this.rowHeight + 2 * this.padding + 2);
  }

  public UIContextMenu setHighlight(int highlight) {
    if (this.highlight != highlight) {
      if (highlight >= 0 && highlight < this.actions.length) {
        this.highlight = highlight;
      } else {
        this.highlight = -1;
      }
      redraw();
    }
    return this;
  }

  /**
   * Subclasses may override to draw some other kind of drop menu
   *
   * @param ui UI context
   * @param pg PGraphics context
   */
  @Override
  public void onDraw(UI ui, PGraphics pg) {
    if (this.padding > 0) {
      pg.noStroke();
      pg.fill(ui.theme.getDeviceFocusedBackgroundColor());
      pg.rect(0, 0, this.width, this.height, getBorderRounding());
      pg.fill(getBackgroundColor());
      pg.rect(this.padding, this.padding, this.width - 2 * this.padding, this.height - 2*this.padding, 2);
    }

    if (this.highlight >= 0) {
      pg.noStroke();
      pg.fill(ui.theme.getContextHighlightColor());
      pg.rect(this.padding + 2, this.padding + 2 + this.highlight * this.rowHeight, this.width - 2 * this.padding - 4, this.rowHeight - 2, 2);
    }

    float yp = 0;
    for (UIContextActions.Action action : this.actions) {
      pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
      pg.fill(ui.theme.getControlTextColor());
      pg.textAlign(PConstants.LEFT, PConstants.CENTER);
      pg.text(clipTextToWidth(pg, action.getLabel(), this.width - 6 - 2 * this.padding), this.padding + 4, this.padding + yp + this.rowHeight / 2);
      yp += this.rowHeight;
    }
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (keyCode == java.awt.event.KeyEvent.VK_UP) {
      consumeKeyEvent();
      setHighlight((this.highlight + this.actions.length - 1) % this.actions.length);
    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
      consumeKeyEvent();
      setHighlight((this.highlight + 1) % this.actions.length);
    } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
      consumeKeyEvent();
      if (this.highlight >= 0) {
        this.actions[this.highlight].onContextAction(getUI());
      }
      getUI().hideContextOverlay();
    } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
      consumeKeyEvent();
      getUI().hideContextOverlay();
    }
  }

  @Override
  public void onMouseOut(MouseEvent mouseEvent) {
    setHighlight(-1);
  }

  @Override
  public void onMouseMoved(MouseEvent mouseEvent, float x, float y) {
    setHighlight((int) ((y - this.padding - 1) / this.rowHeight));
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float x, float y) {
    int index = (int) ((y - this.padding - 1) / this.rowHeight);
    if (index >= 0 && index < this.actions.length) {
      this.actions[index].onContextAction(getUI());
    }
    getUI().hideContextOverlay();
  }

}
