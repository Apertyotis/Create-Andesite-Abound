package net.apertyotis.createandesiteabound.foundation;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public class CircularArray<T> {
    private final T[] data;
    private int head;
    private int tail;
    private int size;

    @SuppressWarnings("unchecked")
    public CircularArray(Class<T> clazz, int capacity) {
        data = (T[]) Array.newInstance(clazz, capacity);
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return data.length;
    }

    public boolean isEmpty() {
        return size <= 0;
    }

    public void clear() {
        Arrays.fill(data, null);
        head = 0;
        tail = 0;
        size = 0;
    }

    public T get(int index) {
        if (index < size)
            return data[(head + index) % data.length];
        else
            throw new NoSuchElementException();
    }

    public void set(int index, T value) {
        if (index < size)
            data[(head + index) % data.length] = value;
        else
            throw new IllegalStateException();
    }

    public T getFirst() {
        if (size > 0)
            return data[head];
        else
            throw new NoSuchElementException();
    }

    public T getLast() {
        if (size > 0)
            return data[(tail + data.length - 1) % data.length];
        else
            throw new NoSuchElementException();
    }

    public void addFirst(T value) {
        if (size < data.length) {
            size++;
            head = (head + data.length - 1) % data.length;
            data[head] = value;
        } else {
            throw new IllegalStateException();
        }
    }

    public void addLast(T value) {
        if (size < data.length) {
            size++;
            data[tail] = value;
            tail = (tail + 1) % data.length;
        } else {
            throw new IllegalStateException();
        }
    }

    public void removeFirst() {
        if (size > 0) {
            size--;
            data[head] = null;
            head = (head + 1) % data.length;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void removeLast() {
        if (size > 0) {
            size--;
            tail = (tail + data.length - 1) % data.length;
            data[tail] = null;
        } else {
            throw new NoSuchElementException();
        }
    }
}
