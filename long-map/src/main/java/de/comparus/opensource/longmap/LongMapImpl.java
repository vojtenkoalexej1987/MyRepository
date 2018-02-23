package de.comparus.opensource.longmap;

import java.util.Objects;

public class LongMapImpl<V> implements LongMap<V> {
    private int capacity = 1 << 4;
    private float loadFactor = 0.75f;
    private Node<V>[] nodesArray = new Node[capacity];
    private long size = 0;
    private int threshold = 12;
    private int MAXIMUM_CAPACITY = 1 << 30;

    public LongMapImpl(int capacity, float loadFactor) {
        if (capacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    capacity);
        if (capacity > MAXIMUM_CAPACITY)
            capacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.loadFactor = loadFactor;
        this.capacity = tableSizeFor(capacity);
        this.threshold = (int) (loadFactor * capacity);
    }

    public LongMapImpl() {
    }

    private int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static class Node<V> {
        private final int hashKey;
        private final long key;
        private V value;
        private Node<V> next;

        Node(int hashKey, long key, V value, Node<V> next) {
            this.hashKey = hashKey;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public long getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node<?> node = (Node<?>) o;
            return key == node.key &&
                    Objects.equals(value, node.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }


    }

    @Override
    public V put(long key, V value) {
        int hashKey = hash(key);
        Node<V> firstNodeByIndexInArray;
        int nodesLength, indexInArray;
        if (nodesArray == null || (nodesLength = nodesArray.length) == 0) {
            nodesLength = (nodesArray = resize()).length;
        }
        V result;
        if ((firstNodeByIndexInArray = nodesArray[indexInArray = hashKey % nodesLength]) == null) {
            nodesArray[indexInArray] = new Node<>(hashKey, key, value, null);
            size++;
            result = null;
        } else {
            result = putByFirstNode(firstNodeByIndexInArray, hashKey, key, value);
        }
        resize();
        return result;
    }

    private V putByFirstNode(Node<V> firstNodeByIndexInArray, int hashKey, long key, V value) {
        Node<V> tempNode = firstNodeByIndexInArray;
        while (true) {
            if (tempNode.key == key) {
                if (tempNode.value.equals(value)) {
                    return value;
                } else {
                    V valueRes = tempNode.value;
                    tempNode.value = value;
                    size++;
                    return valueRes;
                }
            }
            if (tempNode.next == null) {
                tempNode.next = new Node<>(hashKey, key, value, null);
                size++;
                return null;
            }
            tempNode = tempNode.next;
        }
    }

    private Node<V>[] resize() {
        if (threshold == size) {
            if (capacity >= MAXIMUM_CAPACITY) {
                if (capacity != Integer.MAX_VALUE) {
                    capacity = Integer.MAX_VALUE;
                    threshold = Integer.MAX_VALUE;
                    nodesArray = arrayDoublingForResize(nodesArray);
                }
            } else {
                capacity = capacity << 1;
                threshold = (int) (capacity * loadFactor);
                nodesArray = arrayDoublingForResize(nodesArray);
            }
        }
        return nodesArray;
    }

    private Node<V>[] arrayDoublingForResize(Node<V>[] nodes) {
        Node<V>[] newArray = new Node[capacity];
        for (Node<V> nodeInOldArray : nodes) {
            if (nodeInOldArray != null) {
                putInArrayNodesForDoublingChangesArgument(newArray, nodeInOldArray);
            }
        }
        return newArray;
    }

    private void putInArrayNodesForDoublingChangesArgument(Node<V>[] newArray, Node<V> firstNodeInOldArray) {
        Node<V> childNodeInOldArray;
        Node<V> nodeInOldArray = firstNodeInOldArray;
        do {
            int indexInNewArray = nodeInOldArray.hashKey % capacity;
            Node<V> nodeByIndexInNewArray = newArray[indexInNewArray];
            if (nodeByIndexInNewArray == null) {
                newArray[indexInNewArray] = nodeInOldArray;
            } else {
                Node<V> tempNode = nodeByIndexInNewArray;
                while (true) {
                    if (tempNode.next == null) {
                        tempNode.next = nodeInOldArray;
                        break;
                    }
                    tempNode = tempNode.next;
                }
            }
            childNodeInOldArray = nodeInOldArray.next;
            nodeInOldArray.next = null;
            nodeInOldArray = childNodeInOldArray;
        } while (nodeInOldArray != null);
    }

    @Override
    public V get(long key) {
        int hashKey = hash(key);
        Node<V> firstNodeByIndexInArray;
        if (nodesArray != null && nodesArray.length > 0
                && (firstNodeByIndexInArray = nodesArray[hashKey % nodesArray.length]) != null) {
            if (firstNodeByIndexInArray.key == key) {
                return firstNodeByIndexInArray.value;
            }
            Node<V> returnNode = getByFirstNode(firstNodeByIndexInArray, key);
            return returnNode == null ? null : returnNode.value;
        }
        return null;
    }

    private Node<V> getByFirstNode(Node<V> firstNodeByIndexInArray, long key) {
        Node<V> tempNode = firstNodeByIndexInArray.next;
        while (true) {
            if (tempNode == null) {
                return null;
            }
            if (tempNode.key == key) {
                return tempNode;
            }
            tempNode = tempNode.next;
        }
    }

    private int hash(long key) {
        return Math.abs(Objects.hashCode(key));
    }

    @Override
    public V remove(long key) {
        int hashKey = hash(key);
        Node<V> firstNodeByIndexInArray;
        int indexFirstNodeInArray;
        if (nodesArray != null && nodesArray.length > 0
                && (firstNodeByIndexInArray = nodesArray[indexFirstNodeInArray = hashKey % nodesArray.length]) != null) {
            V tempValue;
            if (firstNodeByIndexInArray.key == key) {
                tempValue = firstNodeByIndexInArray.value;
                nodesArray[indexFirstNodeInArray] = firstNodeByIndexInArray.next;
                size--;
                return tempValue;
            } else {
                Node<V> tempNode = removeByFirstNodeInArray(firstNodeByIndexInArray, key);
                return tempNode == null ? null : tempNode.value;
            }

        }
        return null;
    }

    private Node<V> removeByFirstNodeInArray(Node<V> firstNodeByIndexInArray, long key) {
        Node<V> previousNode = firstNodeByIndexInArray;
        Node<V> currentNode = firstNodeByIndexInArray.next;
        while (true) {
            if (currentNode == null) {
                return null;
            }
            if (currentNode.key == key) {
                previousNode.next = currentNode.next;
                size--;
                return currentNode;
            }
            previousNode = currentNode;
            currentNode = currentNode.next;
        }
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(long key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(V value) {
        if (nodesArray != null && size > 0) {
            for (Node<V> nodeInArray : nodesArray) {
                V valueForFoundNode;
                for (Node<V> node = nodeInArray; node != null; node = node.next) {
                    if ((valueForFoundNode = node.value) == value ||
                            (value != null && value.equals(valueForFoundNode)))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public long[] keys() {
        if (size > Integer.MAX_VALUE) throw new ManyNodesException("Keys in the map =" + size);
        long[] result = new long[(int) size];
        if (nodesArray != null && size > 0) {
            int resultCount = 0;
            for (Node<V> nodeInArray : nodesArray) {
                if (nodeInArray == null) continue;
                Node<V> tempNode = nodeInArray;
                while (true) {
                    result[resultCount] = tempNode.key;
                    resultCount++;
                    tempNode = tempNode.next;
                    if (tempNode == null) break;
                }
            }
        }
        return result;
    }

    @Override
    public V[] values() {
        if (size > Integer.MAX_VALUE) throw new ManyNodesException("Values in the map =" + size);
        V[] result = (V[]) new Object[(int) size];
        if (nodesArray != null && size > 0) {
            int resultCount = 0;
            for (Node<V> nodeInArray : nodesArray) {
                if (nodeInArray == null) continue;
                Node<V> tempNode = nodeInArray;
                while (true) {
                    result[resultCount] = tempNode.value;
                    resultCount++;
                    tempNode = tempNode.next;
                    if (tempNode == null) break;
                }
            }
        }
        return result;
    }

    static class ManyNodesException extends RuntimeException {
        ManyNodesException(String msg) {
            super(msg);
        }
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear() {
        if (nodesArray != null && size > 0) {
            size = 0;
            for (int i = 0; i < nodesArray.length; ++i)
                nodesArray[i] = null;
        }
    }
}
