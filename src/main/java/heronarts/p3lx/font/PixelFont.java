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

package heronarts.p3lx.font;

import heronarts.p3lx.P3LX;

import processing.core.PConstants;
import processing.core.PImage;

/**
 * This is simple, tiny 5-pixel bitmap font helpful for drawing basic text to
 * low-resolution LED displays. The characters are blitted in a bitmap.
 *
 * Only upper-case alphabetical characters are supported, and extremely basic
 * punctuation. Non-supported characters are skipped.
 */
public class PixelFont {

  private final static char NULL_CHAR = 0;
  private final static int INVALID_CHAR = -1;

  private final PImage alphabet;

  private final int[] offsets = {
      // A B C D E F G H I J K L M N O P Q R S T U V W X Y Z . : - , ' <END>
      0, 5, 10, 15, 20, 25, 30, 35, 40, 42, 46, 51, 56, 62, 68, 73, 78, 83, 88,
      93, 99, 104, 110, 116, 122, 128, 134, 136, 138, 142, 144, 146, 149 };

  /**
   * Constructs an instance of the font.
   *
   * @param lx LX
   */
  public PixelFont(P3LX lx) {
    this.alphabet = lx.applet.loadImage("PixelFont.png");
    this.alphabet.loadPixels();
  }

  private boolean isValidCharacter(char c) {
    return (this.validCharacter(c) > 0);
  }

  private char validCharacter(char c) {
    if ((c >= 'a') && (c <= 'z')) {
      return (char) ('A' + (c - 'a'));
    } else if ((c >= 'A') && (c <= 'Z')) {
      return c;
    }
    switch (c) {
    case '.':
    case ',':
    case '-':
    case ':':
    case '\'':
    case ' ':
      return c;
    }
    return NULL_CHAR;
  }

  private int characterIndex(char c) {
    char valid = this.validCharacter(c);
    if (valid == NULL_CHAR) {
      return INVALID_CHAR;
    }
    switch (valid) {
    case '.':
      return 26;
    case ':':
      return 27;
    case '-':
      return 28;
    case ',':
      return 29;
    case '\'':
      return 30;
    case ' ':
      return 31;
    default:
      return valid - 'A';
    }
  }

  private int characterOffset(char c) {
    int index = this.characterIndex(c);
    if (index == INVALID_CHAR) {
      return INVALID_CHAR;
    }
    return this.offsets[index];
  }

  private int characterWidth(char c) {
    int index = this.characterIndex(c);
    if (index == INVALID_CHAR) {
      return INVALID_CHAR;
    }
    return this.offsets[index + 1] - this.offsets[index] - 1;
  }

  /**
   * Creates a pixel buffer in a PImage with the given string. Unsupported
   * characters are skipped without error or warning. Letters are full-white,
   * the background is black.
   *
   * @param s String to draw
   * @return a new PImage instance with the string
   */
  public PImage drawString(String s) {
    int width = 0, height = 5;
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; ++i) {
      if (this.isValidCharacter(chars[i])) {
        if (i > 0) {
          ++width;
        }
        width += this.characterWidth(chars[i]);
      }
    }

    PImage image = new PImage(width, height, PConstants.RGB);
    image.loadPixels();

    int xPos = 0;
    for (int i = 0; i < chars.length; ++i) {
      if (this.isValidCharacter(chars[i])) {
        if (i > 0) {
          for (int y = 0; y < image.height; ++y) {
            image.pixels[xPos + y * image.width] = 0;
          }
          ++xPos;
        }
        int offset = this.characterOffset(chars[i]);
        int characterWidth = this.characterWidth(chars[i]);
        for (int j = 0; j < characterWidth; ++j) {
          for (int y = 0; y < image.height; ++y) {
            image.pixels[xPos + j + y * image.width] = this.alphabet.pixels[offset
                + j + y * this.alphabet.width];
          }
        }
        xPos += characterWidth;
      }
    }

    image.updatePixels();
    return image;
  }
}
