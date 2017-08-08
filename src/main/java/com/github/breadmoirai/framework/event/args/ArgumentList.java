package com.github.breadmoirai.framework.event.args;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ArgumentList implements List<EventArgument> {

    final private String[] strings;
    final private EventArgument[] arguments;

    public ArgumentList(String[] strings) {
        this.strings = strings;
        arguments = new EventArgument[strings.length];
    }

    //list
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<EventArgument> iterator() {
        return null;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return null;
    }

    @Override
    public boolean add(EventArgument eventArgument) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends EventArgument> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends EventArgument> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public EventArgument get(int index) {
        return null;
    }

    @Override
    public EventArgument set(int index, EventArgument element) {
        return null;
    }

    @Override
    public void add(int index, EventArgument element) {

    }

    @Override
    public EventArgument remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @NotNull
    @Override
    public ListIterator<EventArgument> listIterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<EventArgument> listIterator(int index) {
        return null;
    }

    @NotNull
    @Override
    public List<EventArgument> subList(int fromIndex, int toIndex) {
        return null;
    }

    //spliterator
    @Override
    public boolean tryAdvance(Consumer<? super EventArgument> action) {
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super EventArgument> action) {

    }

    @Override
    public Spliterator<EventArgument> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return SIZED | IMMUTABLE;
    }

    @Override
    public Stream<EventArgument> stream() {
        return StreamSupport.stream()
    }
}
