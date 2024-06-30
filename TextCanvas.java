import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A grid of {@link FormattedCharacter}s.
 * Supports the 8 basic colors for both foreground and background.
 */
public class TextCanvas extends AbstractCollection<FormattedCharacter> {
    private FormattedCharacter[][] content; // This array is row-dominant.

    /**
     * Create a new {@link TextCanvas} filled with blanks.
     * 
     * @throws IllegalArgumentException if {@code width <= 0 || height <= 0}
     */
    public TextCanvas(int width, int height) throws IllegalArgumentException {
        if (width <= 0) {
            throw new IllegalArgumentException("width <= 0");
        } else if (height <= 0) {
            throw new IllegalArgumentException("height <= 0");
        } else {
            content = new FormattedCharacter[height][width];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    content[y][x] = FormattedCharacter.blank;
                }
            }
        }
    }

    /**
     * Create a new {@link TextCanvas} from the given source.
     * ANSI color codes are processed here.
     * Missing regions are filled with blanks.
     * 
     * @throws IllegalArgumentException if {@code source} is empty
     */
    public TextCanvas(CharSequence source) throws IllegalArgumentException, NullPointerException {
        if (Objects.requireNonNull(source, "source == null").length() == 0) {
            throw new IllegalArgumentException("source.equals(\"\")");
        } else {
            int curLineWidth = 0;
            int height = 1;
            int width = 0;
            for (int i = 0; i < source.length(); i++) {
                switch (source.charAt(i)) {
                    case '\33':
                        do {
                            i++;
                        } while (source.charAt(i) != 'm');
                        break;
                    case '\n':
                        if (curLineWidth > width) {
                            width = curLineWidth;
                        }
                        curLineWidth = 0;
                        height++;
                        break;
                    default:
                        curLineWidth++;
                }
            }
            if (curLineWidth > width) {
                width = curLineWidth;
            }
            content = new FormattedCharacter[height][width];

            Color curBgColor = null;
            Color curFgColor = null;
            final int[] cursor = { 0, 0 };
            for (int i = 0; i < source.length(); i++) {
                switch (source.charAt(i)) {
                    case '\33':
                        switch (source.charAt(i + 2)) {
                            case '3':
                                curFgColor = Color.values()[Character.getNumericValue(source.charAt(i + 3))];
                                i += 4;
                                break;
                            case '4':
                                curBgColor = Color.values()[Character.getNumericValue(source.charAt(i + 3))];
                                i += 4;
                                break;
                            default:
                                curBgColor = null;
                                curFgColor = null;
                                i += 3;
                        }
                        break;
                    case '\n':
                        while (cursor[0] != width) {
                            content[cursor[1]][cursor[0]] = FormattedCharacter.blank;
                            cursor[0]++;
                        }
                        cursor[1]++;
                        cursor[0] = 0;
                        break;
                    default:
                        content[cursor[1]][cursor[0]] = new FormattedCharacter(source.charAt(i), curFgColor,
                                curBgColor);
                        cursor[0]++;
                }
            }
        }
    }

    public TextCanvas clone() {
        return subSurface(0, 0, getWidth(), getHeight());
    }

    /**
     * Draw another {@link TextCanvas} onto this {@link TextCanvas}.
     * 
     * @param left   the x position to draw the left edge of {@code source} at
     * @param top    the y position to draw the top edge of {@code source} at
     * @param source the {@link TextCanvas} to draw onto this {@link TextCanvas}
     * @return the {@link #subSurface} of this {@link TextCanvas} that was
     *         overwritten
     * @throws ArrayIndexOutOfBoundsException if a part of {@code source} would be
     *                                        drawn outside of this
     *                                        {@link TextCanvas}
     */
    public TextCanvas draw(int left, int top, TextCanvas source)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        Objects.requireNonNull(source, "source == null");
        if (left < 0) {
            throw new ArrayIndexOutOfBoundsException("left < 0");
        } else if (top < 0) {
            throw new ArrayIndexOutOfBoundsException("top < 0");
        } else if (left + source.getWidth() > getWidth()) {
            throw new ArrayIndexOutOfBoundsException("left + source.getWidth() > getWidth()");
        } else if (top + source.getHeight() > getHeight()) {
            throw new ArrayIndexOutOfBoundsException("top + source.getHeight() > getHeight()");
        } else {
            final TextCanvas result = new TextCanvas(source.getWidth(), source.getHeight());
            for (int srcy = 0, desty = top; srcy < source.getHeight(); srcy++, desty++) {
                for (int srcx = 0, destx = left; srcx < source.getWidth(); srcx++, destx++) {
                    result.set(srcx, srcy, set(destx, desty, source.get(srcx, srcy)));
                }
            }
            return result;
        }
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != TextCanvas.class) {
            return false;
        }
        TextCanvas o = (TextCanvas) other;
        if (getHeight() != o.getHeight() || getWidth() != o.getWidth()) {
            return false;
        }
        Iterator<FormattedCharacter> T = iterator();
        Iterator<FormattedCharacter> O = o.iterator();
        do {
            if (T.next() != O.next()) {
                return false;
            }
        } while (T.hasNext());
        return true;
    }

    public FormattedCharacter get(int x, int y) throws ArrayIndexOutOfBoundsException {
        if (x < 0) {
            throw new ArrayIndexOutOfBoundsException("x < 0");
        } else if (y < 0) {
            throw new ArrayIndexOutOfBoundsException("y < 0");
        } else if (x >= getWidth()) {
            throw new ArrayIndexOutOfBoundsException("x >= getWidth()");
        } else if (y >= getHeight()) {
            throw new ArrayIndexOutOfBoundsException("y >= getHeight()");
        } else {
            return content[y][x];
        }
    }

    public int getHeight() {
        return content.length;
    }

    public int getWidth() {
        return content[0].length;
    }

    public int hashCode() {
        return toArray().hashCode();
    }

    public Iterator<FormattedCharacter> iterator() {
        return new TextCanvasIterator(this);
    }

    /**
     * A {@link TextCanvas} cannot be empty, so always returns {@code false}.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @return the {@link FormattedCharacter} that was previously at the specified
     *         position
     */
    public FormattedCharacter set(int x, int y, FormattedCharacter value)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        final FormattedCharacter result = get(x, y);
        content[y][x] = Objects.requireNonNull(value, "value == null");
        return result;
    }

    /**
     * Change the dimensions of this {@link TextCanvas}.
     * This is equivalent to calling
     * {@code setWidth(newWidth); setHeight(newHeight);}.
     * 
     * @throws IllegalArgumentException if {@code newWidth <= 0 || newHeight <= 0}
     */
    public void setDimensions(int newWidth, int newHeight) throws IllegalArgumentException {
        if (newWidth <= 0) {
            throw new IllegalArgumentException("newWidth <= 0");
        } else if (newHeight <= 0) {
            throw new IllegalArgumentException("newHeight <= 0");
        } else {
            final FormattedCharacter[][] newContent = new FormattedCharacter[newHeight][newWidth];
            for (int y = 0; y < newHeight; y++) {
                if (y >= getHeight()) {
                    for (int x = 0; x < newWidth; x++) {
                        newContent[y][x] = FormattedCharacter.blank;
                    }
                } else {
                    for (int x = 0; x < newWidth; x++) {
                        newContent[y][x] = x >= getWidth() ? FormattedCharacter.blank : get(x, y);
                    }
                }
            }
            content = newContent;
        }
    }

    /**
     * Change the height of this {@link TextCanvas}.
     * If {@code newHeight} < {@code getHeight()}, removes characters from the
     * right.
     * If {@code newHeight} > {@code getHeight()}, adds blanks to the right.
     * 
     * @throws IllegalArgumentException if {@code newHeight <= 0}
     */
    public void setHeight(int newHeight) throws IllegalArgumentException {
        if (newHeight <= 0) {
            throw new IllegalArgumentException("newHeight <= 0");
        } else {
            final FormattedCharacter[][] newContent = new FormattedCharacter[newHeight][getWidth()];
            for (int y = 0; y < Math.min(getHeight(), newHeight); y++) {
                newContent[y] = content[y];
            }
            for (int y = getHeight(); y < newHeight; y++) {
                for (int x = 0; x < getWidth(); x++) {
                    newContent[y][x] = FormattedCharacter.blank;
                }
            }
            content = newContent;
        }
    }

    /**
     * Change the width of this {@link TextCanvas}.
     * If {@code newWidth} < {@code getWidth()}, removes characters from the bottom.
     * If {@code newWidth} > {@code getWidth()}, adds blanks to the bottom.
     * 
     * @throws IllegalArgumentException if {@code newWidth <= 0}
     */
    public void setWidth(int newWidth) throws IllegalArgumentException {
        if (newWidth <= 0) {
            throw new IllegalArgumentException("newWidth <= 0");
        } else {
            final FormattedCharacter[][] newContent = new FormattedCharacter[getHeight()][newWidth];
            for (int x = 0; x < newWidth; x++) {
                if (x >= getWidth()) {
                    for (int y = 0; y < getHeight(); y++) {
                        newContent[y][x] = FormattedCharacter.blank;
                    }
                } else {
                    for (int y = 0; y < getHeight(); y++) {
                        newContent[y][x] = get(x, y);
                    }
                }
            }
            content = newContent;
        }
    }

    public int size() {
        return getWidth() * getHeight();
    }

    /**
     * Return a copy of a rectangular area of this {@link TextCanvas}.
     * Any changes made to the returned value will not affect the original
     * {@link TextCanvas}, and any changes made to the original {@link TextCanvas}
     * will not affect this copy.
     * 
     * @throws ArrayIndexOutOfBoundsException if
     *                                        {@code left < 0 ||  top < 0 || left + width > getWidth() || top + height > getHeight()}
     * @throws IllegalArgumentException       if {@code width <= 0 || height <= 0}
     */
    public TextCanvas subSurface(int left, int top, int width, int height)
            throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (width <= 0) {
            throw new IllegalArgumentException("width <= 0");
        } else if (height <= 0) {
            throw new IllegalArgumentException("height <= 0");
        } else if (left < 0) {
            throw new ArrayIndexOutOfBoundsException("left < 0");
        } else if (top < 0) {
            throw new ArrayIndexOutOfBoundsException("top < 0");
        } else if (left + width > getWidth()) {
            throw new ArrayIndexOutOfBoundsException("left + width > getWidth()");
        } else if (top + height > getHeight()) {
            throw new ArrayIndexOutOfBoundsException("top + height > getHeight()");
        } else {
            final TextCanvas result = new TextCanvas(width, height);
            for (int srcy = top, desty = 0; desty < height; srcy++, desty++) {
                for (int srcx = left, destx = 0; destx < width; srcx++, destx++) {
                    result.set(destx, desty, get(srcx, srcy));
                }
            }
            return result;
        }
    }

    private static class TextCanvasIterator implements Iterator<FormattedCharacter> {
        private int[] cursor;
        private TextCanvas source;
    
        public TextCanvasIterator(TextCanvas source) throws NullPointerException {
            cursor = new int[2];
            this.source = Objects.requireNonNull(source, "source == null");
        }
    
        public boolean hasNext() {
            return cursor[1] != source.getHeight();
        }
    
        public FormattedCharacter next() throws NoSuchElementException {
            if (hasNext()) {
                FormattedCharacter result = source.get(cursor[0], cursor[1]);
                if (cursor[0] == source.getWidth() - 1) {
                    cursor[1]++;
                    cursor[0] = 0;
                } else {
                    cursor[0]++;
                }
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public String toString() {
        Color curBgColor = null;
        Color curFgColor = null;
        final StringBuilder result = new StringBuilder((getWidth() + 1) * getHeight());
        for (FormattedCharacter[] row : content) {
            for (FormattedCharacter cell : row) {
                if (cell.getBgColor() == null && curBgColor != null
                        || cell.getFgColor() == null && curFgColor != null) {
                    result.append(Color.reset);
                    curBgColor = null;
                    curFgColor = null;
                }
                if (cell.getBgColor() != curBgColor) {
                    result.append(cell.getBgColor().bgCode());
                    curBgColor = cell.getBgColor();
                }
                if (cell.getFgColor() != curFgColor) {
                    result.append(cell.getFgColor().fgCode());
                    curFgColor = cell.getFgColor();
                }
                result.append(cell.charValue());
            }
            result.append('\n');
        }
        result.deleteCharAt(result.length() - 1);
        if (curBgColor != null || curFgColor != null) {
            result.append(Color.reset);
        }
        return result.toString();
    }
}
