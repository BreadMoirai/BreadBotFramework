package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.event.ArgumentSplitter;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DefaultArgumentSplitterImpl implements ArgumentSplitter {

    @Override
    public Iterator<String> getArguments(String content) {
        if (content == null) {
            return new Iterator<String>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public String next() {
                    throw new NoSuchElementException();
                }
            };
        }
        if (content.startsWith("```") && content.endsWith("```")) {
            final String code = content.substring(content.indexOf('\n'), content.length() - 3).trim();
            return new Iterator<String>() {
                private final String s = code;
                private boolean r = true;

                @Override
                public boolean hasNext() {
                    if (r) {
                        r = false;
                        return true;
                    }
                    return false;
                }

                @Override
                public String next() {
                    return s;
                }
            };
        }
        return new LazyIterator(content);
    }

    private static class LazyIterator implements Iterator<String> {

        private final String content;
        private int idx;

        public LazyIterator(String content) {
            this.content = content;
        }

        @Override
        public boolean hasNext() {
            return idx >= 0;
        }

        @Override
        public String next() {
            if (content.charAt(idx) == '"') {
                idx++;
                final int y = content.indexOf('"', idx);
                if (y > 0) {
                    final String next = content.substring(idx, y);
                    this.idx = nextNonWhitespace(content, y + 1);
                    return next;
                }
            } else if (content.charAt(idx) == '`') {
                idx++;
                final int y = content.indexOf('`', idx);
                if (y > 0) {
                    final String next = content.substring(idx, y);
                    this.idx = nextNonWhitespace(content, y + 1);
                    return next;
                }
            }
            int w = nextWhitespace(content, idx);
            if (w == -1) {
                final String next = content.substring(idx);
                idx = -1;
                return next;
            }
            int x = nextNonWhitespace(content, w);
            final String next = content.substring(idx, w);
            idx = x;
            return next;
        }

        private int nextWhitespace(String content, int idx) {
            for (int i = idx; i < content.length(); i++) {
                if (Character.isWhitespace(content.charAt(i))) {
                    return i;
                }
            }
            return -1;
        }

        private int nextNonWhitespace(String content, int idx) {
            for (int i = idx; i < content.length(); i++) {
                if (!Character.isWhitespace(content.charAt(i))) {
                    return i;
                }
            }
            return -1;
        }

    }
}
