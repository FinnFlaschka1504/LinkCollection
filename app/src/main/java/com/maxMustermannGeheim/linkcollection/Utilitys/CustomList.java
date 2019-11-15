package com.maxMustermannGeheim.linkcollection.Utilitys;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class CustomList<E> extends ArrayList<E> {

//  ----- Constructors ----->
    public CustomList() {
    }

    public CustomList(@NonNull Collection<? extends E> c) {
        super(c);
    }

    public CustomList(int initialCapacity) {
        super(initialCapacity);
    }

    public CustomList(Object[] objects) {
        addAll((List<E>) Arrays.asList(objects));
    }
//  <----- Constructors -----

    public E getRandom() {
        return get((int) (Math.random() * size()));
    }

    public E getLast() {
        if (isEmpty())
            return null;
        return get(size() - 1);
    }

    @Override
    public E get(int index) {
        if (index < 0)
            return super.get(size() - index);
        else
            return super.get(index);
    }

    public CustomList add(E... e) {
        Collections.addAll(this, e);
        return this;
    }

    public boolean isLast(E e) {
        if (e == null)
            return false;
        return e.equals(getLast());
    }
    public boolean isLast(int i) {
        return get(i).equals(getLast());
    }

    public E next(E e) {
        if (isLast(e))
            return get(0);
        else
            return get(indexOf(e) + 1);
    }

    public void forEachCount(ForEachCount<E> forEachCount) {
        int count = 0;
        for (E e : this) {
            if (forEachCount.runForeEachCount(e, count)) {
                break;
            }
            count++;
        }
    }
    public interface ForEachCount<E> {
        boolean runForeEachCount(E e, int count);
    }

    public Integer indexOf(Predicate<? super E> predicate) {
        final Integer[] foundAt = new Integer[1];
        forEachCount((e, count) -> {
            if (predicate.test(e)) {
                foundAt[0] = count;
                return true;
            }
            return false;
        });
        return foundAt[0];
    }
}
