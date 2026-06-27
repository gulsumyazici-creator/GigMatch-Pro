/**
 * A max binary heap implementation used by the platform to store
 * freelancer score entries. Uses 1-based indexing and percolation
 * operations for insert and deleteMax.
 *
 * @param <AnyType> type that must implement Comparable
 */
public class BinaryHeap<AnyType extends Comparable<? super AnyType>> {

    private static final int DEFAULT_CAPACITY = 10;

    private int currentSize = 0;
    private AnyType[] array;

    /**
     * Creates an empty heap with default capacity.
     */
    public BinaryHeap() {
        array = (AnyType[]) new Comparable[DEFAULT_CAPACITY + 1];
    }

    /**
     * Creates an empty heap with the given capacity.
     */
    public BinaryHeap(int capacity) {
        if (capacity < 1) capacity = DEFAULT_CAPACITY;
        array = (AnyType[]) new Comparable[capacity + 1];
    }

    /**
     * Builds a heap from an array of items.
     */
    public BinaryHeap(AnyType[] items) {
        array = (AnyType[]) new Comparable[Math.max(items.length + 1, DEFAULT_CAPACITY + 1)];
        currentSize = items.length;

        for (int i = 0; i < items.length; i++) {
            array[i + 1] = items[i];
        }
        buildHeap();
    }

    /**
     * Restores heap order from bottom-up.
     */
    private void buildHeap() {
        for (int i = currentSize / 2; i >= 1; i--) {
            percolateDown(i);
        }
    }

    /**
     * Inserts a new element into the heap.
     */
    public void insert(AnyType x) {
        if (currentSize == array.length - 1)
            enlargeArray(array.length * 2 + 1);

        int hole = ++currentSize;
        array[0] = x;

        while (x.compareTo(array[hole / 2]) > 0) {
            array[hole] = array[hole / 2];
            hole /= 2;
        }
        array[hole] = x;
    }

    /**
     * Moves an element down to restore heap order.
     */
    private void percolateDown(int hole) {
        int child;
        AnyType tmp = array[hole];

        while (hole * 2 <= currentSize) {
            child = hole * 2;

            if (child != currentSize &&
                    array[child + 1].compareTo(array[child]) > 0) {
                child++;
            }

            if (array[child].compareTo(tmp) > 0) {
                array[hole] = array[child];
                hole = child;
            } else {
                break;
            }
        }
        array[hole] = tmp;
    }

    /**
     * Removes and returns the maximum element.
     */
    public AnyType deleteMax() {
        if (isEmpty()) return null;

        AnyType maxItem = array[1];
        array[1] = array[currentSize];
        array[currentSize] = null;
        currentSize--;

        percolateDown(1);
        return maxItem;
    }

    /** @return true if the heap is empty */
    public boolean isEmpty() {
        return currentSize == 0;
    }

    /**
     * Doubles array size when capacity is exceeded.
     */
    private void enlargeArray(int newSize) {
        AnyType[] old = array;
        array = (AnyType[]) new Comparable[newSize];
        System.arraycopy(old, 0, array, 0, old.length);
    }
}





