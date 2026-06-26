package org.sample.javaspecialist.issue336;

import java.util.AbstractList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImmutableSubList<E> extends AbstractList<E> {
    private final Object[] elements;
    
    public ImmutableSubList(CopyOnWriteArrayList<E> list, int fromIndex, int toIndex) {
        Objects.requireNonNull(list, "list");
        if(fromIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        
        this.elements = new Object[toIndex - fromIndex];
        
        var index = 0;
        
        for (var iterator = list.listIterator(fromIndex); iterator.hasNext() && index + fromIndex < toIndex; ) {
            var next = iterator.next();
            elements[index++] = next;
        }
        
        if (index < elements.length)
            throw new IndexOutOfBoundsException();
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, elements.length);
        
        @SuppressWarnings("unchecked")
        var element = (E) elements[index];
        
        return element;
    }

    @Override
    public int size() {
        return elements.length;
    }
    
    
}
