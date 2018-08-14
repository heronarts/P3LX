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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import heronarts.lx.LXUtils;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * An ItemList is a scrollable list of elements with a focus state
 * and action handling when the elements are focused or clicked on.
 */
public interface UIItemList {

  public interface Listener {
    public void onItemFocused(Item item);
    public void onItemActivated(Item item);
    public void onItemDeactivated(Item item);
  }

  /**
   * Interface to which items in the list must conform
   */
  public static abstract class Item {

    /**
     * Whether this item is in a special active state
     *
     * @return If this item is active
     */
    public boolean isActive() {
      return false;
    }

    /**
     * Whether the item is checked, applies only if checkbox mode set on the list.
     *
     * @return If this item is checked
     */
    public boolean isChecked() {
      return false;
    }

    /**
     * Active background color for this item
     *
     * @param ui UI context
     * @return Background color
     */
    public int getActiveColor(UI ui) {
      return ui.theme.getControlBackgroundColor();
    }

    /**
     * String label that displays on this item
     *
     * @return Label for the item
     */
    public abstract String getLabel();

    /**
     * Action handler, invoked when item is activated
     */
    public void onActivate() {}

    /**
     * Action handler invoked when item is checked
     *
     * @param checked If checked
     */
    public void onCheck(boolean checked) {
      throw new UnsupportedOperationException("Item does not implement checkbox operation");
    }

    /**
     * Action handler, invoked when item is deactivated. Only applies when setMomentary(true)
     */
    public void onDeactivate() {}

    /**
     * Action handler, invoked when an item is renamed. Only applies when setRenamable(true)
     *
     * @param name New name for item
     */
    public void onRename(String name) {
      throw new UnsupportedOperationException("Item does not implement renaming");
    }

    /**
     * Action handler, invoked when an item is reordered. Only applies to the item that the action
     * was taken upon, not other items affected by the reordering. Only applies when setReorderable(true)
     *
     * @param order New position for item
     */
    public void onReorder(int order) {
      throw new UnsupportedOperationException("Item does not implement reordering");
    }

    /**
     * Action handler, invoked when item is deleted
     */
    public void onDelete() {}

    /**
     * Action handler, invoked when item is focused
     */
    public void onFocus() {}

    /**
     * Section that this item belongs to
     *
     * @return section or null
     */
    public Section getSection() {
      return null;
    }
  }

  /**
   * A section is an item in the list that indents the items beneath it.
   */
  public static abstract class Section extends Item {

    private final List<Item> items = new ArrayList<Item>();

    private boolean expanded = true;

    private void addItem(Item item) {
      this.items.add(item);
    }

    private void removeItem(Item item) {
      this.items.remove(item);
    }

    private void toggle() {
      this.expanded = !this.expanded;
    }

    @Override
    public final Section getSection() {
      // We're not supporting nested sections!
      return null;
    }

    @Override
    public void onActivate() {
      this.expanded = true;
    }

  }

  public static class Impl {

    private static final int PADDING = 2;
    private static final int SCROLL_BAR_WIDTH = 8;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_MARGIN = 2;
    private static final int ROW_SPACING = ROW_HEIGHT + ROW_MARGIN;
    private static final int CHECKBOX_SIZE = 8;
    private static final int SECTION_CHEVRON_WIDTH = 14;

    private final UI2dContainer list;

    private final List<Item> items = new CopyOnWriteArrayList<Item>();

    private final List<Listener> listeners = new ArrayList<Listener>();

    private int focusIndex = -1;

    private boolean singleClickActivate = false;

    private boolean isMomentary = false;

    private boolean isRenamable = false;

    private boolean isReorderable = false;

    private boolean showCheckboxes = false;

    private boolean renaming = false;

    private String renameBuffer = "";

    private boolean dragging;

    private boolean mouseChevronPress = false;

    private int mouseActivate = -1;

    private int keyActivate = -1;

    private int controlSurfaceFocusIndex = -1;
    private int controlSurfaceFocusLength = -1;

    private Impl(UI ui, UI2dContainer list) {
      this.list = list;
      list.setBackgroundColor(ui.theme.getDarkBackgroundColor());
      list.setBorderRounding(4);
    }

    private void addListener(Listener listener) {
      this.listeners.add(listener);
    }

    private void removeListener(Listener listener) {
      this.listeners.remove(listener);
    }

    private void focusNext(int increment) {
      int index = this.focusIndex + increment;
      while (index >= 0 && index < this.items.size()) {
        Section section = this.items.get(index).getSection();
        if (section == null || section.expanded) {
          setFocusIndex(index);
          return;
        }
        index += increment;
      }
    }

    private void setFocusIndex(int focusIndex) {
      setFocusIndex(focusIndex, true);
    }

    private void setFocusIndex(int focusIndex, boolean scroll) {
      focusIndex = LXUtils.constrain(focusIndex, -1, this.items.size() - 1);
      if (this.focusIndex != focusIndex) {
        if (focusIndex >= 0 && scroll && this.list instanceof ScrollList) {
          UI2dScrollContext scrollList = (UI2dScrollContext) this.list;
          float yp = ROW_SPACING * focusIndex + scrollList.getScrollY();
          if (yp < 0) {
            scrollList.setScrollY(-ROW_SPACING * focusIndex);
          } else if (yp >= list.getHeight() - ROW_SPACING) {
            scrollList.setScrollY(list.getHeight() - ROW_SPACING * (focusIndex+1) - ROW_MARGIN);
          }
        }
        this.focusIndex = focusIndex;
        if (this.focusIndex >= 0) {
          Item item = this.items.get(this.focusIndex);
          item.onFocus();
          for (Listener listener : this.listeners) {
            listener.onItemFocused(item);
          }
        }
        this.list.redraw();
      }
    }

    private int getFocusedIndex() {
      return this.focusIndex;
    }

    /**
     * Retrieves the currently focused item in the list.
     *
     * @return Focused item, or null if none is focused
     */
    private UIItemList.Item getFocusedItem() {
      if (this.focusIndex >= 0 && this.focusIndex < this.items.size()) {
        return this.items.get(this.focusIndex);
      }
      return null;
    }

    private void recomputeContentHeight() {
      int itemCount = 0;
      for (Item item : this.items) {
        Section section = item.getSection();
        if (section == null || section.expanded) {
          ++itemCount;
        }
      }
      setContentHeight(ROW_SPACING * itemCount + ROW_MARGIN);

    }

    private void addSection(Section section) {
      addItem(section);
    }

    /**
     * Adds an item to the list
     *
     * @param item Item to remove
     * @return this
     */
    private void addItem(Item item) {
      Section section = item.getSection();
      if (section != null) {
        section.addItem(item);
        int sectionIndex = this.items.indexOf(section);
        this.items.add(sectionIndex + section.items.size(), item);
      } else {
        this.items.add(item);
      }
      recomputeContentHeight();
      this.list.redraw();
    }

    /**
     * Removes an item from the list
     *
     * @param item Item to remove
     * @return this
     */
    private void removeItem(Item item) {
      Section section = item.getSection();
      if (section != null) {
        section.removeItem(item);
      }

      int itemIndex = this.items.indexOf(item);
      if (itemIndex < 0) {
        throw new IllegalArgumentException("Item is not in UIItemList: " + item);
      }
      this.items.remove(itemIndex);
      if (this.focusIndex >= this.items.size()) {
        setFocusIndex(items.size() - 1);
      } else if (this.focusIndex >= 0) {
        Item focusItem = this.items.get(this.focusIndex);
        focusItem.onFocus();
        for (Listener listener : this.listeners) {
          listener.onItemFocused(focusItem);
        }
      }
      recomputeContentHeight();
      this.list.redraw();
    }

    /**
     * Sets the items in the list and redraws it
     *
     * @param items Items
     * @return this
     */
    private void setItems(List<? extends Item> items) {
      this.items.clear();
      this.items.addAll(items);
      if (this.focusIndex >= items.size()) {
        setFocusIndex(items.size() - 1);
      } else if (this.focusIndex >= 0) {
        Item item = this.items.get(this.focusIndex);
        item.onFocus();
        for (Listener listener : this.listeners) {
          listener.onItemFocused(item);
        }
      }
      setContentHeight(ROW_SPACING * items.size() + ROW_MARGIN);
      this.list.redraw();
    }

    private void setSingleClickActivate(boolean singleClickActivate) {
      this.singleClickActivate = singleClickActivate;
    }

    private void setShowCheckboxes(boolean showCheckboxes) {
      if (this.showCheckboxes != showCheckboxes) {
        this.showCheckboxes = showCheckboxes;
        this.list.redraw();
      }
    }

    private void setRenamable(boolean isRenamable) {
      this.isRenamable = isRenamable;
    }

    private void setMomentary(boolean momentary) {
      this.isMomentary = momentary;
    }

    private void setReorderable(boolean isReorderable) {
      this.isReorderable = isReorderable;
    }

    private void setControlSurfaceFocus(int index, int length) {
      this.controlSurfaceFocusIndex = index;
      this.controlSurfaceFocusLength = length;
      this.list.redraw();
    }

    private void activate() {
      if (this.focusIndex >= 0) {
        Item item = this.items.get(this.focusIndex);
        if (item instanceof Section) {
          ((Section) item).toggle();
          recomputeContentHeight();
          this.list.redraw();
        } else {
          item.onActivate();
          for (Listener listener : this.listeners) {
            listener.onItemActivated(item);
          }
        }
      }
    }

    private void delete() {
      if (this.focusIndex >= 0) {
        this.items.get(this.focusIndex).onDelete();
      }
    }

    private void check() {
      if (this.focusIndex >= 0) {
        Item item = this.items.get(this.focusIndex);
        if (!(item instanceof Section)) {
          item.onCheck(!item.isChecked());
          this.list.redraw();
        }
      }
    }

    private float getScrollY() {
      if (this.list instanceof ScrollList) {
        return ((UI2dScrollContext) this.list).getScrollY();
      }
      return 0;
    }

    private float getScrollHeight() {
      if (this.list instanceof ScrollList) {
        return ((UI2dScrollContext) this.list).getScrollHeight();
      }
      return this.list.getHeight();
    }

    private void setContentHeight(float height) {
      this.list.setContentHeight(height);
    }

    private void setScrollY(float scrollY) {
      if (this.list instanceof ScrollList) {
        ((UI2dScrollContext) this.list).setScrollY(scrollY);
      }
    }

    private float getHeight() {
      return this.list.getHeight();
    }

    private float getWidth() {
      return this.list.getWidth();
    }

    private float getRowWidth() {
      return (getScrollHeight() > this.list.getHeight()) ? this.list.getWidth() - SCROLL_BAR_WIDTH - PADDING : this.list.getWidth();
    }

    private int getVisibleFocusIndex() {
      int counter = 0;
      int itemIndex = -1;
      for (Item item : this.items) {
        Section section = item.getSection();
        if (section == null || section.expanded) {
          ++itemIndex;
        }
        if (counter++ >= this.focusIndex) {
          break;
        }
      }
      return itemIndex;
    }

    private void drawFocus(UI ui, PGraphics pg) {
      float yp = ROW_MARGIN + getScrollY() + ROW_SPACING * getVisibleFocusIndex();
      UI2dComponent.drawFocus(ui, pg, ui.theme.getFocusColor(), PADDING, yp, getRowWidth() - 2*PADDING, ROW_HEIGHT, 2);
    }

    private void onDraw(UI ui, PGraphics pg) {
      float yp = ROW_MARGIN;
      pg.textFont(ui.theme.getControlFont());
      pg.textAlign(PConstants.LEFT, PConstants.TOP);
      pg.noStroke();
      int i = 0;

      float rowWidth = getRowWidth();

      if (getScrollHeight() > getHeight()) {
        pg.noStroke();
        pg.fill(0xff333333);
        float percentCovered = getHeight() / getScrollHeight();
        float barHeight = percentCovered * getHeight() - 2*PADDING;
        float startPosition = -getScrollY() / getScrollHeight();
        float barY = -getScrollY() + startPosition * getHeight() + PADDING;
        pg.rect(getWidth() - PADDING-SCROLL_BAR_WIDTH, barY, SCROLL_BAR_WIDTH, barHeight, 4);
      }

      for (Item item : this.items) {
        Section section = item.getSection();
        boolean isSection = item instanceof Section;

        // Skip rendering items that are in a collapsed section
        if (section != null && !section.expanded) {
          ++i;
          continue;
        }

        boolean renameItem = this.renaming && (this.focusIndex == i);

        int backgroundColor, textColor;
        if (item.isActive()) {
          backgroundColor = item.getActiveColor(ui);
          textColor = UI.WHITE;
        } else {
          backgroundColor = (i == this.focusIndex) ? 0xff333333 : ui.theme.getControlBackgroundColor();
          textColor = isSection ? 0xffaaaaaa : ((i == this.focusIndex) ? UI.WHITE : ui.theme.getControlTextColor());
        }
        pg.noStroke();
        pg.fill(backgroundColor);
        pg.rect(PADDING, yp, rowWidth-2*PADDING, ROW_HEIGHT, 4);

        int textX = 6;
        if (isSection) {
          pg.noStroke();
          pg.fill(0xff666666);
          pg.beginShape();
          if (((Section) item).expanded) {
            pg.vertex(textX-1, yp + 6);
            pg.vertex(textX+5, yp + 6);
            pg.vertex(textX+2, yp + 12);
          } else {
            pg.vertex(textX, yp + 5);
            pg.vertex(textX, yp + 11);
            pg.vertex(textX + 6, yp + 8);
          }
          pg.endShape(PConstants.CLOSE);
          textX += 8;
        } else if (section != null) {
          textX += 14;
        }

        if (this.showCheckboxes && !isSection) {
          pg.stroke(textColor);
          pg.noFill();
          pg.rect(textX, yp+4, CHECKBOX_SIZE-1, CHECKBOX_SIZE-1);
          if (item.isChecked()) {
            pg.noStroke();
            pg.fill(textColor);
            pg.rect(textX+2, yp+6, CHECKBOX_SIZE/2, CHECKBOX_SIZE/2);
          }
          textX += CHECKBOX_SIZE + 4;
        }
        if (renameItem) {
          pg.noStroke();
          pg.fill(UI.BLACK);
          pg.rect(textX-2, yp+1, rowWidth - PADDING - textX + 2, ROW_HEIGHT-2, 4);
          pg.fill(UI.WHITE);
          pg.text(UI2dComponent.clipTextToWidth(pg, this.renameBuffer, rowWidth - textX - 2), textX, yp + 4);
        } else {
          pg.fill(textColor);
          pg.text(UI2dComponent.clipTextToWidth(pg, item.getLabel(), rowWidth - textX - 2), textX, yp + 4);
        }
        yp += ROW_SPACING;

        ++i;
      }

      if (this.controlSurfaceFocusIndex >= 0 && this.controlSurfaceFocusLength > 0) {
        pg.noFill();
        pg.stroke(ui.theme.getSurfaceColor());
        pg.rect(
          PADDING,
          ROW_MARGIN + this.controlSurfaceFocusIndex * ROW_SPACING,
          rowWidth - 2*PADDING,
          Math.min(this.controlSurfaceFocusLength, this.items.size() - this.controlSurfaceFocusIndex) * ROW_SPACING - ROW_MARGIN,
          4
        );
      }
    }

    private int getMouseItemIndex(float my) {
      if ((my % (ROW_HEIGHT + ROW_MARGIN)) < ROW_MARGIN) {
        // Don't detect clicks on strip between rows
        return -1;
      }

      int visibleIndex = (int) (my / (ROW_HEIGHT + ROW_MARGIN));
      int counter = 0;
      int itemIndex = -1;
      for (Item item : this.items) {
        ++itemIndex;
        Section section = item.getSection();
        if (section == null || section.expanded) {
          if (counter++ >= visibleIndex) {
            break;
          }
        }
      }
      return itemIndex;
    }

    private void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
      if (!this.mouseChevronPress && !this.isMomentary && !this.singleClickActivate && (mouseEvent.getCount() == 2)) {
        int index = getMouseItemIndex(my);
        if (index >= 0) {
          setFocusIndex(index);
          activate();
        }
      }
    }

    private void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
      this.mouseActivate = -1;
      this.mouseChevronPress = false;
      if (getScrollHeight() > getHeight() && mx >= getRowWidth()) {
        this.dragging = true;
      } else {
        this.dragging = false;
        int index = getMouseItemIndex(my);
        if (index >= 0) {
          setFocusIndex(index);
          if (this.showCheckboxes && (mx < (5*PADDING + CHECKBOX_SIZE))) {
            if (mx >= 2*PADDING) {
              check();
            }
          } else {
            this.mouseChevronPress = (mx < SECTION_CHEVRON_WIDTH) && (getFocusedItem() instanceof Section);
            if (this.isMomentary || this.singleClickActivate || this.mouseChevronPress) {
              this.mouseActivate = this.focusIndex;
              activate();
            }
          }
        }
      }
    }

    private void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
      if (this.dragging) {
        setScrollY(getScrollY() - dy * (getScrollHeight() / getHeight()));
      }
    }

    private void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
      this.dragging = false;
      if (this.mouseActivate >= 0 && this.mouseActivate < this.items.size()) {
        Item item = this.items.get(this.mouseActivate);
        item.onDeactivate();
        for (Listener listener : this.listeners) {
          listener.onItemDeactivated(item);
        }
      }
      this.mouseActivate = -1;
    }

    private void onBlur() {
      if (this.renaming) {
        this.renaming = false;
        this.list.redraw();
      }
    }

    private boolean onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      boolean consume = false;
      if (this.renaming) {
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
          consume = true;
          this.renaming = false;
          this.list.redraw();
        } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
          consume = true;
          String newName = this.renameBuffer.trim();
          if (newName.length() > 0) {
            this.items.get(this.focusIndex).onRename(newName);
          }
          this.renaming = false;
          this.list.redraw();
        } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
          consume = true;
          if (this.renameBuffer.length() > 0) {
            this.renameBuffer = this.renameBuffer.substring(0, this.renameBuffer.length() - 1);
            this.list.redraw();
          }
        } else if (UITextBox.isValidTextCharacter(keyChar)) {
          consume = true;
          this.renameBuffer += keyChar;
          this.list.redraw();
        }
      } else {
        if (keyCode == java.awt.event.KeyEvent.VK_UP) {
          consume = true;
          if (this.isReorderable && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
            if (this.focusIndex > 0) {
              Item item = this.items.remove(this.focusIndex);
              this.focusIndex = this.focusIndex - 1;
              this.items.add(this.focusIndex, item);
              item.onReorder(this.focusIndex);
              this.list.redraw();
            }
          } else {
            if (this.focusIndex > 0) {
              focusNext(-1);
              this.list.redraw();
            }
          }
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
          consume = true;
          if (this.isReorderable && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
            if (this.focusIndex < this.items.size() - 1) {
              Item item = this.items.remove(this.focusIndex);
              this.focusIndex = this.focusIndex + 1;
              this.items.add(this.focusIndex, item);
              item.onReorder(this.focusIndex);
              this.list.redraw();
            }
          } else {
            focusNext(1);
            this.list.redraw();
          }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
          consume = true;
          if (this.isMomentary) {
            this.keyActivate = this.focusIndex;
          }
          activate();
        } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
          consume = true;
          if (this.showCheckboxes) {
            check();
          } else {
            if (this.isMomentary) {
              this.keyActivate = this.focusIndex;
            }
            activate();
          }
        } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
          consume = true;
          delete();
        } else if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
          if (keyCode == java.awt.event.KeyEvent.VK_D) {
            consume = true;
            delete();
          } else if (keyCode == java.awt.event.KeyEvent.VK_R) {
            if (this.isRenamable && this.focusIndex >= 0) {
              consume = true;
              this.renaming = true;
              this.renameBuffer = "";
              this.list.redraw();
            }
          }
        }
      }
      return consume;
    }

    private boolean onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
      boolean consume = false;
      if (keyCode == java.awt.event.KeyEvent.VK_ENTER || keyCode == java.awt.event.KeyEvent.VK_SPACE) {
        if (this.isMomentary) {
          if (this.keyActivate >= 0 && this.keyActivate < this.items.size()) {
            consume = true;
            Item item = this.items.get(this.keyActivate);
            item.onDeactivate();
            for (Listener listener : this.listeners) {
              listener.onItemDeactivated(item);
            }
          }
          this.keyActivate = -1;
        }
      }
      return consume;
    }

    private void onFocus() {
      if (this.focusIndex < 0 && this.items.size() > 0) {
        setFocusIndex(0, false);
      }
    }
  }

  public static class ScrollList extends UI2dScrollContext implements UIItemList, UIFocus {

    private final Impl impl;

    public ScrollList(UI ui, float x, float y, float w, float h) {
      super(ui, x, y, w, h);
      setScrollHeight(Impl.ROW_MARGIN);
      this.impl = new Impl(ui, this);
    }

    public UIItemList setFocusIndex(int focusIndex) {
      this.impl.setFocusIndex(focusIndex);
      return this;
    }

    public int getFocusedIndex() {
      return this.impl.getFocusedIndex();
    }

    public UIItemList.Item getFocusedItem() {
      return this.impl.getFocusedItem();
    }

    public UIItemList addSection(Section section) {
      this.impl.addSection(section);
      return this;
    }

    public UIItemList addItem(Item item) {
      this.impl.addItem(item);
      return this;
    }

    public UIItemList removeItem(Item item) {
      this.impl.removeItem(item);
      return this;
    }

    public UIItemList setItems(List<? extends Item> items) {
      this.impl.setItems(items);
      return this;
    }

    public List<? extends Item> getItems() {
      return this.impl.items;
    }

    public UIItemList setSingleClickActivate(boolean singleClickActivate) {
      this.impl.setSingleClickActivate(singleClickActivate);
      return this;
    }

    public UIItemList setShowCheckboxes(boolean showCheckboxes) {
      this.impl.setShowCheckboxes(showCheckboxes);
      return this;
    }

    public UIItemList setRenamable(boolean isRenamable) {
      this.impl.setRenamable(isRenamable);
      return this;
    }

    public UIItemList setMomentary(boolean momentary) {
      this.impl.setMomentary(momentary);
      return this;
    }

    public UIItemList setReorderable(boolean reorderable) {
      this.impl.setReorderable(reorderable);
      return this;
    }

    @Override
    public UIItemList setControlSurfaceFocus(int index, int length) {
      this.impl.setControlSurfaceFocus(index, length);
      return this;
    }

    @Override
    public UIItemList addListener(Listener listener) {
      this.impl.addListener(listener);
      return this;
    }

    @Override
    public UIItemList removeListener(Listener listener) {
      this.impl.removeListener(listener);
      return this;
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {
      this.impl.drawFocus(ui, pg);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
      this.impl.onDraw(ui, pg);
    }

    @Override
    public void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
      this.impl.onMouseClicked(mouseEvent, mx, my);
    }

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
      this.impl.onMouseDragged(mouseEvent, mx, my, dx, dy);
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
      this.impl.onMousePressed(mouseEvent, mx, my);
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
      this.impl.onMouseReleased(mouseEvent, mx, my);
    }

    @Override
    public void onBlur() {
      this.impl.onBlur();
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      boolean consume = this.impl.onKeyPressed(keyEvent, keyChar, keyCode);
      if (consume) {
        consumeKeyEvent();
      }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
      boolean consume = this.impl.onKeyReleased(keyEvent, keyChar, keyCode);
      if (consume) {
        consumeKeyEvent();
      }
    }

    @Override
    public void onFocus() {
      this.impl.onFocus();
    }

  }

  public static class BasicList extends UI2dContainer implements UIItemList, UIFocus {

    private final Impl impl;

    public BasicList(UI ui, float x, float y, float w, float h) {
      super(x, y, w, h);
      setContentHeight(Impl.ROW_MARGIN);
      this.impl = new Impl(ui, this);
    }

    public UIItemList setFocusIndex(int focusIndex) {
      this.impl.setFocusIndex(focusIndex, true);
      return this;
    }

    public int getFocusedIndex() {
      return this.impl.getFocusedIndex();
    }

    public UIItemList.Item getFocusedItem() {
      return this.impl.getFocusedItem();
    }

    public UIItemList addSection(Section section) {
      this.impl.addSection(section);
      return this;
    }

    public UIItemList addItem(Item item) {
      this.impl.addItem(item);
      return this;
    }

    public UIItemList removeItem(Item item) {
      this.impl.removeItem(item);
      return this;
    }

    public UIItemList setItems(List<? extends Item> items) {
      this.impl.setItems(items);
      return this;
    }

    public List<? extends Item> getItems() {
      return this.impl.items;
    }

    public UIItemList setSingleClickActivate(boolean singleClickActivate) {
      this.impl.setSingleClickActivate(singleClickActivate);
      return this;
    }

    public UIItemList setShowCheckboxes(boolean showCheckboxes) {
      this.impl.setShowCheckboxes(showCheckboxes);
      return this;
    }

    public UIItemList setRenamable(boolean isRenamable) {
      this.impl.setRenamable(isRenamable);
      return this;
    }

    public UIItemList setMomentary(boolean momentary) {
      this.impl.setMomentary(momentary);
      return this;
    }

    public UIItemList setReorderable(boolean reorderable) {
      this.impl.setReorderable(reorderable);
      return this;
    }

    public UIItemList setControlSurfaceFocus(int index, int length) {
      this.impl.setControlSurfaceFocus(index, length);
      return this;
    }

    @Override
    public UIItemList addListener(Listener listener) {
      this.impl.addListener(listener);
      return this;
    }

    @Override
    public UIItemList removeListener(Listener listener) {
      this.impl.removeListener(listener);
      return this;
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {
      this.impl.drawFocus(ui, pg);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
      this.impl.onDraw(ui, pg);
    }

    @Override
    public void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
      this.impl.onMouseClicked(mouseEvent, mx, my);
    }

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
      this.impl.onMouseDragged(mouseEvent, mx, my, dx, dy);
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
      this.impl.onMousePressed(mouseEvent, mx, my);
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
      this.impl.onMouseReleased(mouseEvent, mx, my);
    }

    @Override
    public void onBlur() {
      this.impl.onBlur();
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      boolean consume = this.impl.onKeyPressed(keyEvent, keyChar, keyCode);
      if (consume) {
        consumeKeyEvent();
      }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
      boolean consume = this.impl.onKeyReleased(keyEvent, keyChar, keyCode);
      if (consume) {
        consumeKeyEvent();
      }
    }

    @Override
    public void onFocus() {
      this.impl.onFocus();
    }

  }

  /**
   * Sets the index of the focused item in the list. Checks the bounds
   * and adjusts the scroll position if necessary.
   *
   * @param focusIndex Index of item to focus
   * @return this
   */
  public UIItemList setFocusIndex(int focusIndex);

  /**
   * Returns the index of the currently focused item in the list
   *
   * @return Index of focused item
   */
  public int getFocusedIndex();

  /**
   * Retrieves the currently focused item in the list.
   *
   * @return Focused item, or null if none is focused
   */
  public UIItemList.Item getFocusedItem();

  /**
   * Adds an item to the list
   *
   * @param item Item to remove
   * @return this
   */
  public UIItemList addItem(Item item);

  /**
   * Removes an item from the list
   *
   * @param item Item to remove
   * @return this
   */
  public UIItemList removeItem(Item item);

  /**
   * Sets the items in the list and redraws it
   *
   * @param items Items
   * @return this
   */
  public UIItemList setItems(List<? extends Item> items);

  /**
   * Get the items in the list
   *
   * @return list of items
   */
  public List<? extends Item> getItems();

  /**
   * Sets whether single-clicks on an item should activate them. Default behavior
   * requires double-click or ENTER keypress
   *
   * @param singleClickActivate Whether to activate on a single click
   * @return this
   */
  public UIItemList setSingleClickActivate(boolean singleClickActivate);

  /**
   * Sets whether a column of checkboxes should be shown on the item list, to the
   * left of the labels. Useful for a secondary selection state.
   *
   * @param showCheckboxes Whether to show checkboxes
   * @return this
   */
  public UIItemList setShowCheckboxes(boolean showCheckboxes);

  /**
   * Sets whether renaming items is allowed
   *
   * @param isRenamable If items may be renamed
   * @return this
   */
  public UIItemList setRenamable(boolean isRenamable);

  /**
   * Sets whether the item list is momentary. If so, then clicking on an item
   * or pressing ENTER/SPACE sends a deactivate action after the click ends.
   *
   * @param momentary Whether items are momentary
   * @return this
   */
  public UIItemList setMomentary(boolean momentary);

  /**
   * Sets whether the list is reorderable. If so, then pressing the modifier key
   * with the up or down arrows will reorder the items.
   *
   * @param reorderable Whether items are reorderable
   * @return this
   */
  public UIItemList setReorderable(boolean reorderable);

  /**
   * Sets a control focus range that is highlighted in the list
   *
   * @param index Start of the surface focus
   * @param length Length of the surface focus block
   * @return this
   */
  public UIItemList setControlSurfaceFocus(int index, int length);

  /**
   * Adds a listener to receive notifications about list operations
   *
   * @param listener Listener
   * @return this
   */
  public UIItemList addListener(Listener listener);

  /**
   * Removes a listener from receiving notifications about list operations
   *
   * @param listener Listener
   * @return this
   */
  public UIItemList removeListener(Listener listener);

}
