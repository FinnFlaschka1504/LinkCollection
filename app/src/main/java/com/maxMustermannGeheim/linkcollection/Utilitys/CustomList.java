package com.maxMustermannGeheim.linkcollection.Utilitys;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (objects != null)
            addAll((List<E>) Arrays.asList(objects));
    }
//  <----- Constructors -----

    public E getRandom() {
        return get((int) (Math.random() * size()));
    }

    public E getLast() {
        if (isEmpty())
            return null;
        return get(- 1);
    }

    @Override
    public E get(int index) {
        if (index < 0)
            return super.get(size() + index);
        else
            return super.get(index);
    }

    public CustomList<E> add(E... e) {
        Collections.addAll(this, e);
        return this;
    }

    public boolean isFirst(E e) {
        if (e == null || isEmpty())
            return false;
        return e.equals(get(0));
    }

    public boolean isLast(E e) {
        if (e == null || isEmpty())
            return false;
        return e.equals(getLast());
    }

    public boolean isLast(int i) {
        return i == size() - 1;
//        return get(i).equals(getLast());
    }

    //  --------------- Recycle --------------->
    public E next(E e) {
        if (isLast(e))
            return get(0);
        else
            return get(indexOf(e) + 1);
    }
    public E previous(E e) {
//        if (get(0).equals(e))
//            return getLast();
//        else
        return get(indexOf(e) - 1);
    }
    //  <--------------- Recycle ---------------


//  ----- forEach ----->
    public void forEachCount(ForEachCount_breakable<E> forEachCount_breakable) {
        int count = 0;
        for (E e : this) {
            if (forEachCount_breakable.runForeEachCount(e, count)) {
                break;
            }
            count++;
        }
    }
    public interface ForEachCount_breakable<E> {
        boolean runForeEachCount(E e, int count);
    }

    public void forEachCount(ForEachCount<E> forEachCount) {
        int count = 0;
        for (E e : this) {
            forEachCount.runForeEachCount(e, count);
            count++;
        }
    }
    public interface ForEachCount<E> {
        void runForeEachCount(E e, int count);
    }
//  <----- forEach -----

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


//    @NonNull
//    @Override
//    public E[] toArray() {
//        return (E[]) toArray((E[]) new Object[0]);
//    }

    //  ----- Stream ----->
    public static <E,R> CustomList<R> map(List<E> list, Function<? super E, ? extends R> mapper) {
        return list.stream().map(mapper).collect(Collectors.toCollection(CustomList::new));
    }

    public <R> CustomList<R> map(Function<? super E, ? extends R> mapper) {
        return stream().map(mapper).collect(Collectors.toCollection(CustomList::new));
    }

    public CustomList<E> filter(Predicate<? super E> mapper) {
        return stream().filter(mapper).collect(Collectors.toCollection(CustomList::new));
    }
    //  <----- Stream -----
}
