package Trees;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class BTree<Element> {

    /**
     * Node in which the BTree stores the array of elements and the children nodes
     */
    private class BTreeNode<V extends Element> {
        final V[] values;
        final BTreeNode<Element>[] children;
        BTreeNode<Element> parent;
        boolean allChildrenNull = true;

        @SuppressWarnings("unchecked")
        BTreeNode(BTreeNode<Element> parent) {
            this.values = (V[]) new Object[elementCount];
            this.children = new BTreeNode[order];
            this.parent = parent;
        }
    }

    /**
     * Root of the BTree
     */
    private BTreeNode<Element> bTreeRootNode = null;

    /**
     * Order of the BTree (Maximum number of children of any B-Tree Element) and data
     * that derives their value from order.
     */
    private final int order;
    private final int elementCount;
    private final int midIndex;

    /**
     * Comparator that is used to compare the elements while inserting them
     * into the BTree. If {@code null} then natural ordering of elements
     * is used.
     */
    private final Comparator<? super Element> comparator;

    /**
     * Size of the BTree (Number of Elements in the Tree)
     */
    private int size = 0;

    /**
     * Constructs an empty BTree with specified order and {@code null Comparator}.
     *
     * @param order Order of the BTree
     * @throws IllegalArgumentException If the order is less than 2
     */
    public BTree(int order) {
        this(null, order);
    }

    /**
     * Constructs an empty BTree with specified order and Comparator.
     *
     * @param comparator Comparator that should be used to compare elements
     * @param order      Order of the BTree
     * @throws IllegalArgumentException If the order is less than 2
     */
    public BTree(Comparator<? super Element> comparator, int order) {
        this.comparator = comparator;
        if (order > 1) {
            this.order = order;
        } else {
            throw new IllegalArgumentException("Illegal Tree Order : " + order);
        }
        elementCount = order - 1;
        midIndex = order / 2;
    }

    /**
     * Inserts the element in the BTree to the proper position reaching down to the proper
     * leaf node first and then comes back up, while splitting the nodes, as necessary.
     *
     * <p><strong>Running time: - </strong></p>
     * <ol>
     *     <li>If thBTreeRootNode is null : Θ( 1 )</li>
     *     <li>If thBTreeRootNode is not null : Θ( order * log(size) )</li>
     * </ol>
     *
     * @param element Element to be inserted in the BTree
     * @return Element that was inserted
     * @throws NullPointerException If the element is {@code null} and this BTree uses default Comparator
     * @throws ClassCastException   If the class of the element prevents it from adding in the BTree
     */
    public final Element put(Element element) {
        if (bTreeRootNode == null) {
            bTreeRootNode = new BTreeNode<>(null);
            bTreeRootNode.values[0] = element;
            size = 1;
        } else {
            assert put(bTreeRootNode, element) == null; // Assert that there are no upward moving elements
            size++;
        }

        return element;
    }

    /**
     * This helper method is used to recursively reach the correct leaf node under the given
     * subTreeRoot in which the element can be inserted and then call the {@code splittableInsert}
     * function.
     *
     * @param subTreeRoot Root of the subTree under which the element has to be inserted
     * @param element     Element that has to be inserted
     * @throws IllegalArgumentException If the subTreeRoot is {@code null}
     * @throws NullPointerException     If the element is {@code null} and this BTree uses natural ordering
     */
    private UpwardMovingData put(BTreeNode<Element> subTreeRoot, Element element) {
        if (subTreeRoot == null) {
            throw new IllegalArgumentException("Illegal subTreeRoot : Null");
        }

        int newInsertIndex = 0;

        for (int i = 0; i < elementCount; i++) {
            if (subTreeRoot.values[i] == null) {
                break;
            } else if (comparator != null) {
                if (comparator.compare(element, subTreeRoot.values[i]) >= 0) {
                    newInsertIndex += 1;
                } else {
                    break;
                }
            } else {
                Objects.requireNonNull(element);
                @SuppressWarnings("unchecked")
                Comparable<? super Element> comparable = (Comparable<? super Element>) element;
                if (comparable.compareTo(subTreeRoot.values[i]) >= 0) {
                    newInsertIndex += 1;
                } else {
                    break;
                }
            }
        }

        // Check whether the current BTreeNode is a leaf node or not.
        if (subTreeRoot.allChildrenNull) {
            return splittableInsert(subTreeRoot, newInsertIndex, element, null);
        } else {
            // Recursive call till a leaf node is reached
            UpwardMovingData upwardMovingData = put(subTreeRoot.children[newInsertIndex], element);
            if (upwardMovingData != null) {
                return splittableInsert(subTreeRoot, newInsertIndex, upwardMovingData.middleElement, upwardMovingData.rightSplit);
            }
        }

        return null;
    }

    private class UpwardMovingData {
        final Element middleElement;
        final BTreeNode<Element> rightSplit;

        UpwardMovingData(Element middleElement, BTreeNode<Element> rightSplit) {
            this.middleElement = middleElement;
            this.rightSplit = rightSplit;
        }
    }

    /**
     * <P>If the node has less than max number of values, then it inserts the element at
     * insertIndex in values array and the rightSubTreeRoot at insertIndex + 1 in the
     * children array of BTreeNode. No split is performed.</P>
     *
     * <P>Otherwise, values and children array copied to an ArrayList, then the element
     * and rightSubTreeRoot are inserted at insertIndex and insertIndex + 1 respectively
     * in these ArrayLists. Then a new node is created called {@code rightSplit}.
     * First half of values and children is copied into current node (making remaining
     * positions null) and the second half is copied into rightSplit. (Middle element
     * is excluded from both the nodes.</P>
     *
     * <P>If no split is performed, returns null. If BTreeNode is root node, a new node is
     * created and that is set as new root (Where, children[0] <- bTreeNode, children[1] <-
     * rightSplit and values[0] <- middleElements of copied values ArrayList) and returns {@code
     * null}. In all other cases, it returns UpwardMovingData that contains the middle element
     * of the copied values ArrayList and rightSplit</P>
     *
     * @param bTreeNode        Node in which the element and it's right child has to be added
     * @param insertIndex      Index in the {@code values} array of {@code BTreeNode} where element has to be inserted
     * @param element          The element that has to be inserted
     * @param rightSubTreeRoot Right child of the element
     * @return null if no split is performed or if BTreeNode is root otherwise, returns UpwardMovingData
     */
    private UpwardMovingData splittableInsert(BTreeNode<Element> bTreeNode, int insertIndex,
                                              Element element, BTreeNode<Element> rightSubTreeRoot) {

        if (bTreeNode.values[elementCount - 1] == null) {
            // Non-Split Insert

            for (int i = elementCount - 1; i > insertIndex; i--) {
                bTreeNode.values[i] = bTreeNode.values[i - 1];
                bTreeNode.children[i + 1] = bTreeNode.children[i];
            }
            bTreeNode.values[insertIndex] = element;
            bTreeNode.children[insertIndex + 1] = rightSubTreeRoot;

            return null;
        } else {
            // Split Insert

            BTreeNode<Element> rightSplit = new BTreeNode<>(bTreeNode.parent);
            rightSplit.allChildrenNull = bTreeNode.allChildrenNull;
            ArrayList<Element> values = new ArrayList<>(elementCount + 1);
            ArrayList<BTreeNode<Element>> children = new ArrayList<>(order + 1);

            // Fill ArrayLists
            int loc = 0;
            children.add(bTreeNode.children[0]);
            for (int i = 0; i < order; i++) {
                if (i == insertIndex) {
                    values.add(element);
                    children.add(rightSubTreeRoot);
                } else {
                    values.add(bTreeNode.values[loc]);
                    children.add(bTreeNode.children[loc + 1]);
                    loc++;
                }
            }

            // Fill left node
            loc = 0;
            for (int i = 0; i < elementCount; i++) {
                if (i < midIndex) {
                    bTreeNode.values[i] = values.get(loc);
                    bTreeNode.children[i + 1] = children.get(loc + 1);
                    loc++;
                } else {
                    bTreeNode.values[i] = null;
                    bTreeNode.children[i + 1] = null;
                }
            }

            Element upwardMovingElement = values.get(loc);
            rightSplit.children[0] = children.get(loc + 1);
            loc++;

            // Fill right node
            int remainingCount = order - loc;
            for (int i = 0; i < elementCount; i++) {
                if (i < remainingCount) {
                    rightSplit.values[i] = values.get(loc);
                    rightSplit.children[i + 1] = children.get(loc + 1);
                    loc++;
                } else {
                    break;
                }
            }

            // If the current node is root node
            if (bTreeNode.parent == null) {
                assert bTreeNode == bTreeRootNode;
                bTreeRootNode = new BTreeNode<>(null);
                bTreeRootNode.allChildrenNull = false;
                bTreeRootNode.values[0] = upwardMovingElement;
                bTreeRootNode.children[0] = bTreeNode;
                bTreeRootNode.children[1] = rightSplit;
                bTreeNode.parent = bTreeRootNode;
                rightSplit.parent = bTreeNode.parent;
                return null;
            } else {
                return new UpwardMovingData(upwardMovingElement, rightSplit);
            }
        }
    }

    /**
     * Returns an arraylist containing the elements of the BTree in Order.
     *
     * @param bTreeNode Root of the subTree whose inOrderTraversal is required
     * @return ArrayList containing the elements of the BTree in Order.
     */
    public final ArrayList<Element> inOrderTraversal(BTreeNode<Element> bTreeNode) {
        ArrayList<Element> arrayList = new ArrayList<>(elementCount);

        if (bTreeNode == null || bTreeNode.values[0] == null) {
            return arrayList;
        } else if (bTreeNode.allChildrenNull) {
            for (int i = 0; i < elementCount; i++) {
                if (bTreeNode.values[i] != null) {
                    arrayList.add(bTreeNode.values[i]);
                } else {
                    break;
                }
            }
        } else {
            arrayList.addAll(inOrderTraversal(bTreeNode.children[0]));
            for (int i = 0; i < elementCount; i++) {
                if (bTreeNode.values[i] != null) {
                    arrayList.add(bTreeNode.values[i]);
                    arrayList.addAll(inOrderTraversal(bTreeNode.children[i + 1]));
                } else {
                    break;
                }
            }
        }

        return arrayList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ArrayList<Element> inOrder = inOrderTraversal(bTreeRootNode);
        return "Order : " + order() + ", size : " + size() + "\nElements : " + ((inOrder.size() == 0) ? "Empty" : inOrder);
    }

    /**
     * Returns the order of the BTree
     *
     * @return The order of the BTree
     */
    public int order() {
        return order;
    }

    /**
     * Returns the number of elements in the BTree
     *
     * @return The number of elements in the BTree
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if the BTree contains no elements
     *
     * @return {@code true} if the BTree contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }
}