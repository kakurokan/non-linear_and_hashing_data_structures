package aed.trees;
//W(n) = nº de ponteiros "null" existentes na sub-árvore que começa em "n"
//w(null) = 1
//w(n) = w(left(n)) + w(right(n))

import java.util.Iterator;
import java.util.Stack;

class UAlgTreeNode<Key extends Comparable<Key>, Value> implements IUAlgTreeNode<Key, Value> {
    private static final int INITIAL_WEIGHT = 2;
    Key key;
    Value value;
    int size;
    int weight;
    UAlgTreeNode<Key, Value> left;
    UAlgTreeNode<Key, Value> right;

    UAlgTreeNode(Value value, Key key) {
        this.size = 1;
        this.value = value;
        this.key = key;
        this.weight = INITIAL_WEIGHT;
    }

    UAlgTreeNode(int size, Value value, Key key) {
        this.size = size;
        this.value = value;
        this.key = key;
        this.weight = INITIAL_WEIGHT;
    }

    @Override
    public IUAlgTreeNode<Key, Value> getLeft() {
        return left;
    }

    @Override
    public IUAlgTreeNode<Key, Value> getRight() {
        return right;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Value getValue() {
        return value;
    }
}

public class UAlgTree<Key extends Comparable<Key>, Value> {
    private UAlgTreeNode<Key, Value> root;
    private boolean wasRotated;
    private Value foundValue;

    public UAlgTree() {
        this.root = null;
    }

    public IUAlgTreeNode<Key, Value> getRoot() {
        return root;
    }

    public int size() {
        return UAlgTreeNodeSize(this.root);
    }

    private int UAlgTreeNodeWeight(UAlgTreeNode<Key, Value> node) {
        return (node == null) ? 1 : node.getWeight();
    }

    private int UAlgTreeNodeSize(UAlgTreeNode<Key, Value> node) {
        return (node == null) ? 0 : node.getSize();
    }

    private int updateUAlgTreeNodeSize(UAlgTreeNode<Key, Value> node) {
        return UAlgTreeNodeSize(node.left) + UAlgTreeNodeSize(node.right) + 1;
    }

    private int updateUAlgTreeNodeWeight(UAlgTreeNode<Key, Value> node) {
        return UAlgTreeNodeWeight(node.left) + UAlgTreeNodeWeight(node.right);
    }

    private boolean isSafeLeftRotation(UAlgTreeNode<Key, Value> node) {
        if (node.right == null) return false;

        long weight = node.getWeight();
        long weightLeftKid = UAlgTreeNodeWeight(node.right.left);
        long weightRightKid = UAlgTreeNodeWeight(node.right.right);

        return (2 * weight <= 7 * weightRightKid) && (2 * weight <= 7 * weightLeftKid + 2 * weightRightKid);
    }

    private boolean isSafeRightRotation(UAlgTreeNode<Key, Value> node) {
        if (node.left == null) return false;

        long weight = node.getWeight();
        long weightLeftKid = UAlgTreeNodeWeight(node.left.left);
        long weightRightKid = UAlgTreeNodeWeight(node.left.right);

        return (2 * weight <= 7 * weightLeftKid) && (2 * weight <= 7 * weightRightKid + 2 * weightLeftKid);
    }

    public Value get(Key k) {
        this.wasRotated = false;
        this.foundValue = null;

        this.root = get(this.root, k);

        return foundValue;
    }

    private UAlgTreeNode<Key, Value> get(UAlgTreeNode<Key, Value> node, Key k) {
        if (node == null) return null;

        int cmp = k.compareTo(node.getKey());
        if (cmp > 0) {
            node.right = get(node.right, k);

            if (this.foundValue != null && !this.wasRotated) {
                if (node == this.root || isSafeLeftRotation(node)) {
                    node = rotateLeft(node);
                    this.wasRotated = true;
                }
            }
        } else if (cmp < 0) {
            node.left = get(node.left, k);

            if (this.foundValue != null && !this.wasRotated) {
                if (node == this.root || isSafeRightRotation(node)) {
                    node = rotateRight(node);
                    this.wasRotated = true;
                }
            }
        } else {
            this.foundValue = node.value;
        }

        return node;
    }

    public boolean contains(Key k) {
        return get(k) != null;
    }

    private UAlgTreeNode<Key, Value> fixWeights(UAlgTreeNode<Key, Value> node) {
        long weightLeft = UAlgTreeNodeWeight(node.left);
        long weightRight = UAlgTreeNodeWeight(node.right);

        if (2 * weightLeft > 5 * weightRight) {
            long weightKidLeft = UAlgTreeNodeWeight(node.left.left);
            long weightKidRight = UAlgTreeNodeWeight(node.left.right);

            if (2 * weightKidRight > 3 * weightKidLeft) {
                node.left = rotateLeft(node.left);
            }

            node = rotateRight(node);
        } else if (2 * weightRight > 5 * weightLeft) {
            long weightKidLeft = UAlgTreeNodeWeight(node.right.left);
            long weightKidRight = UAlgTreeNodeWeight(node.right.right);

            if (2 * weightKidLeft > 3 * weightKidRight) {
                node.right = rotateRight(node.right);
            }

            node = rotateLeft(node);
        }

        return node;
    }

    public void put(Key k, Value v) {
        if (v == null) {
            delete(k);
            return;
        }
        this.root = put(this.root, k, v);
    }

    private UAlgTreeNode<Key, Value> put(UAlgTreeNode<Key, Value> node, Key k, Value v) {
        if (node == null) return new UAlgTreeNode<>(v, k);

        int cmp = k.compareTo(node.getKey());
        if (cmp > 0) node.right = put(node.right, k, v);
        else if (cmp < 0) node.left = put(node.left, k, v);
        else {
            node.value = v;
        }

        node.size = updateUAlgTreeNodeSize(node);
        node.weight = updateUAlgTreeNodeWeight(node);
        return fixWeights(node);
    }

    private UAlgTreeNode<Key, Value> rotateLeft(UAlgTreeNode<Key, Value> node) {
        UAlgTreeNode<Key, Value> right = node.right;

        node.right = right.left;
        right.left = node;

        node.weight = updateUAlgTreeNodeWeight(node);
        node.size = updateUAlgTreeNodeSize(node);

        right.weight = updateUAlgTreeNodeWeight(right);
        right.size = updateUAlgTreeNodeSize(right);

        return right;
    }

    private UAlgTreeNode<Key, Value> rotateRight(UAlgTreeNode<Key, Value> node) {
        UAlgTreeNode<Key, Value> left = node.left;

        node.left = left.right;
        left.right = node;

        node.weight = updateUAlgTreeNodeWeight(node);
        node.size = updateUAlgTreeNodeSize(node);

        left.weight = updateUAlgTreeNodeWeight(left);
        left.size = updateUAlgTreeNodeSize(left);

        return left;
    }

    private UAlgTreeNode<Key, Value> getSucessor(UAlgTreeNode<Key, Value> node) {
        UAlgTreeNode<Key, Value> suc = node.right;
        while (suc != null && suc.left != null) {
            suc = suc.left;
        }
        return suc;
    }

    public void delete(Key k) {
        this.root = delete(this.root, k);
    }

    private UAlgTreeNode<Key, Value> delete(UAlgTreeNode<Key, Value> node, Key k) {
        if (node == null) return null;

        int cmp = k.compareTo(node.getKey());
        if (cmp > 0) {
            node.right = delete(node.right, k);
        } else if (cmp < 0) {
            node.left = delete(node.left, k);
        } else {
            //0/1 filho
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            //2 filhos
            UAlgTreeNode<Key, Value> suc = getSucessor(node);
            node.key = suc.getKey();
            node.value = suc.getValue();

            node.right = delete(node.right, suc.key);
        }

        node.size = updateUAlgTreeNodeSize(node);
        node.weight = updateUAlgTreeNodeWeight(node);
        return fixWeights(node);
    }

    public Iterable<Key> keys() {
        return KeyIterator::new;
    }

    public Iterable<Value> values() {
        return ValueIterator::new;
    }

    public UAlgTree<Key, Value> shallowCopy() {
        UAlgTree<Key, Value> copy = new UAlgTree<>();
        copy.root = shallowCopyNode(this.root);
        return copy;
    }

    private UAlgTreeNode<Key, Value> shallowCopyNode(UAlgTreeNode<Key, Value> node) {
        if (node == null) return null;

        UAlgTreeNode<Key, Value> copy = new UAlgTreeNode<>(node.getSize(), node.getValue(), node.getKey());
        copy.weight = node.weight;

        copy.left = shallowCopyNode(node.left);
        copy.right = shallowCopyNode(node.right);

        return copy;
    }

    public int maxDepth() {
        return maxDepth(this.root);
    }

    private int maxDepth(UAlgTreeNode<Key, Value> node) {
        if (node == null) return 0;
        return 1 + Math.max(maxDepth(node.left), maxDepth(node.right));
    }

    public int minDepth() {
        return minDepth(this.root);
    }

    private int minDepth(UAlgTreeNode<Key, Value> node) {
        if (node == null) return 0;

        if (node.left == null && node.right == null) return 1;

        if (node.left == null) return 1 + minDepth(node.right);

        if (node.right == null) return 1 + minDepth(node.left);

        return 1 + Math.min(minDepth(node.left), minDepth(node.right));
    }

    public class ValueIterator implements Iterator<Value> {
        private final Stack<UAlgTreeNode<Key, Value>> stack = new Stack<>();

        public ValueIterator() {
            pushLeft((UAlgTreeNode<Key, Value>) getRoot());
        }

        private void pushLeft(UAlgTreeNode<Key, Value> node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        public boolean hasNext() {
            return !stack.isEmpty();
        }

        public Value next() {
            UAlgTreeNode<Key, Value> node = stack.pop();
            if (node.right != null) {
                pushLeft(node.right);
            }
            return node.getValue();

        }
    }

    public class KeyIterator implements Iterator<Key> {
        private final Stack<UAlgTreeNode<Key, Value>> stack = new Stack<>();

        public KeyIterator() {
            pushLeft((UAlgTreeNode<Key, Value>) getRoot());
        }

        public boolean hasNext() {
            return (!stack.isEmpty());
        }

        private void pushLeft(UAlgTreeNode<Key, Value> node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        @Override
        public Key next() {
            UAlgTreeNode<Key, Value> node = stack.pop();
            if (node.right != null) {
                pushLeft(node.right);
            }
            return node.getKey();
        }
    }

}