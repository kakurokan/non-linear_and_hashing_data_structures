package aed.tables;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

class UAlshBucket<Key, Value> implements IUAlshBucket<Key, Value> {

    Key key;
    Value value;
    int maxSharedTable;
    int hc1;
    int hc2;

    public UAlshBucket() {
    }

    public void initUAlshBucket(Value value, Key key, int khc1, int khc2, int maxSharedTable) {
        this.value = value;
        this.key = key;
        this.maxSharedTable = maxSharedTable;
        this.hc1 = khc1;
        this.hc2 = khc2;
    }

    public int getMaxSharedTable() {
        return maxSharedTable;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return key == null;
    }

    @Override
    public boolean isDeleted() {
        return this.value == null;
    }

    void delete() {
        this.value = null;
    }
}

public class UAlshTable<Key, Value> {

    //mudei de ideais relativamente aos primos iniciais, iremos usar
    //37, 17, 11, 7, e 5. Esta mudança não tem qualquer impacto significativo
    private static final int[] primes = {5, 7, 11, 17, 37, 79, 163, 331, 673, 1361, 2729, 5471, 10949, 21911, 43853, 87719, 175447, 350899, 701819, 1403641, 2807303, 5614657, 11229331, 22458671, 44917381, 89834777, 179669557};
    private final Function<Key, Integer> hc2;
    //Contadores
    public int nOfCompares; //incrementa antes de comparar as keys no findBuckets
    public int nOfSearches;  //incrementa no inicio do findBuckets antes de verificar qualquer coisa
    public int nOfPutCalls; //incrementa no inicio do put
    //Variáveis
    private int primeIndex;
    private int size;
    private int deletedKeys;
    //Tabelas
    private UAlshBucket<Key, Value>[] t1;
    private UAlshBucket<Key, Value>[] t2;
    private UAlshBucket<Key, Value>[] t3;
    private UAlshBucket<Key, Value>[] t4;
    private UAlshBucket<Key, Value>[] t5;


    public UAlshTable(Function<Key, Integer> hc2) {
        this(hc2, 4);
    }

    private UAlshTable(Function<Key, Integer> hc2, int primeIndex) {
        this.hc2 = hc2;
        this.size = 0;
        this.primeIndex = primeIndex;
        this.deletedKeys = 0;

        initTables(primeIndex);
    }

    public void resetCount() {
        nOfSearches = 0;
        nOfCompares = 0;
        nOfPutCalls = 0;
    }

    @SuppressWarnings("unchecked")
    private void initTables(int primeIndex) {
        this.t1 = (UAlshBucket<Key, Value>[]) new UAlshBucket[primes[primeIndex]];
        Arrays.setAll(t1, i -> new UAlshBucket<>());
        this.t2 = (UAlshBucket<Key, Value>[]) new UAlshBucket[primes[primeIndex - 1]];
        Arrays.setAll(t2, i -> new UAlshBucket<>());
        this.t3 = (UAlshBucket<Key, Value>[]) new UAlshBucket[primes[primeIndex - 2]];
        Arrays.setAll(t3, i -> new UAlshBucket<>());
        this.t4 = (UAlshBucket<Key, Value>[]) new UAlshBucket[primes[primeIndex - 3]];
        Arrays.setAll(t4, i -> new UAlshBucket<>());
        this.t5 = (UAlshBucket<Key, Value>[]) new UAlshBucket[primes[primeIndex - 4]];
        Arrays.setAll(t5, i -> new UAlshBucket<>());
    }

    public int size() {
        return this.size - this.deletedKeys;
    }

    private int UAsh(int i, int kch1, int kch2) {
        int h = (kch1 + (i * kch2)) & 0x7fffffff;
        return h % getSubTable(i).length;
    }

    public int getMainCapacity() {
        return primes[this.primeIndex];
    }

    public int getTotalCapacity() {
        int sum = 0;
        for (int i = 0; i < 5; i++)
            sum += primes[primeIndex - i];
        return sum;
    }

    public float getLoadFactor() {
        return this.size / (float) getTotalCapacity();
    }

    public int getDeletedNotRemoved() {
        return this.deletedKeys;
    }

    private void resize(int new_primeIndex) {
        UAlshTable<Key, Value> new_table = new UAlshTable<>(this.hc2, new_primeIndex);

        for (int i = 1; i <= 5; i++) {
            UAlshBucket<Key, Value>[] sub_table = (UAlshBucket<Key, Value>[]) getSubTable(i);
            for (UAlshBucket<Key, Value> bucket : sub_table) {
                if (!bucket.isEmpty() && !bucket.isDeleted()) {
                    new_table.fastPut(bucket.getKey(), bucket.getValue(), bucket.hc1, bucket.hc2);
                }
            }
        }

        this.t1 = new_table.t1;
        this.t2 = new_table.t2;
        this.t3 = new_table.t3;
        this.t4 = new_table.t4;
        this.t5 = new_table.t5;
        this.deletedKeys = new_table.deletedKeys;
        this.size = new_table.size;
        this.primeIndex = new_table.primeIndex;
    }

    public IUAlshBucket<Key, Value>[] getSubTable(int i) {
        return switch (i) {
            case 1 -> t1;
            case 2 -> t2;
            case 3 -> t3;
            case 4 -> t4;
            case 5 -> t5;
            default -> null;
        };
    }

    public boolean containsKey(Key k) {
        return get(k) != null;
    }

    private UAlshBucket<Key, Value> findBucket(Key k, int khc1, int khc2) {
        int z = Integer.MAX_VALUE;
        nOfSearches++;

        for (int i = 1; i <= 5; i++) {
            UAlshBucket<Key, Value> bucket = (UAlshBucket<Key, Value>) getSubTable(i)[UAsh(i, khc1, khc2)];
            z = Math.min(z, bucket.getMaxSharedTable());
            if (z == 0)
                return null;
        }

        for (int i = z; i > 0; i--) {
            UAlshBucket<Key, Value> bucket = (UAlshBucket<Key, Value>) getSubTable(i)[UAsh(i, khc1, khc2)];
            if (!bucket.isEmpty() && bucket.hc1 == khc1 && bucket.hc2 == khc2) {
                nOfCompares++;
                if (bucket.getKey().equals(k)) {
                    return bucket;
                }
            }
        }

        return null;
    }

    public Value get(Key k) {
        int khc1 = k.hashCode();
        int khc2 = hc2.apply(k);

        UAlshBucket<Key, Value> bucket = findBucket(k, khc1, khc2);

        if (bucket != null && !bucket.isDeleted())
            return bucket.getValue();

        return null;
    }

    public void put(Key k, Value v) {
        if (v == null) {
            delete(k);
            return;
        }
        nOfPutCalls++;
        int khc1 = k.hashCode();
        int khc2 = hc2.apply(k);

        UAlshBucket<Key, Value> bucket = findBucket(k, khc1, khc2);

        if (bucket == null) {
            if (primeIndex < primes.length - 1 && 20L * size() >= 17L * primes[primeIndex]) {
                resize(primeIndex + 1);
            }
            fastPut(k, v, khc1, khc2);
            return;
        }

        if (bucket.isDeleted()) {
            this.deletedKeys--;
        }
        bucket.value = v;
    }

    public void fastPut(Key k, Value v) {
        int khc1 = k.hashCode();
        int khc2 = hc2.apply(k);

        if (primeIndex < primes.length - 1 && 20L * size() >= 17L * primes[primeIndex]) {
            resize(primeIndex + 1);
        }

        fastPut(k, v, khc1, khc2);
    }

    private void fastPut(Key k, Value v, int khc1, int khc2) {
        boolean wasAdded = false;
        int index = 0;

        for (int i = 1; i <= 5; i++) {
            int hash = UAsh(i, khc1, khc2);
            UAlshBucket<Key, Value> bucket = (UAlshBucket<Key, Value>) getSubTable(i)[hash];

            if (bucket.isEmpty()) {
                bucket.initUAlshBucket(v, k, khc1, khc2, i);
                this.size++;
                index = i;
                wasAdded = true;
                break;
            }
        }

        if (wasAdded) {
            for (int j = 1; j <= 5; j++) {
                UAlshBucket<Key, Value> b = (UAlshBucket<Key, Value>) getSubTable(j)[UAsh(j, khc1, khc2)];
                if (index > b.maxSharedTable) {
                    b.maxSharedTable = index;
                }
            }
        } else if (primeIndex < primes.length - 1) {
            resize(primeIndex + 1);
            fastPut(k, v, khc1, khc2);
        }
    }


    public void delete(Key k) {
        int khc1 = k.hashCode();
        int khc2 = hc2.apply(k);

        UAlshBucket<Key, Value> bucket = findBucket(k, khc1, khc2);

        if (bucket == null)
            return;

        if (!bucket.isDeleted()) {
            bucket.delete();
            this.deletedKeys++;

            if (primeIndex > 4 && 4L * size() < primes[primeIndex])
                resize(this.primeIndex - 1);
        }
    }

    public Iterable<Key> keys() {
        return UalshIterator::new;
    }

    public class UalshIterator implements Iterator<Key> {
        private int currentTableIndex;
        private int currentBucketIndex;
        private UAlshBucket<Key, Value> currentBucket;

        public UalshIterator() {
            this.currentTableIndex = 1;
            this.currentBucketIndex = 0;
            findNext();
        }

        private void findNext() {
            currentBucket = null;

            while (currentTableIndex <= 5) {
                UAlshBucket<Key, Value>[] table = (UAlshBucket<Key, Value>[]) getSubTable(currentTableIndex);

                while (currentBucketIndex < table.length) {
                    UAlshBucket<Key, Value> tempBucket = table[currentBucketIndex++];
                    if (!tempBucket.isEmpty() && !tempBucket.isDeleted()) {
                        currentBucket = tempBucket;
                        return;
                    }
                }
                currentTableIndex++;
                currentBucketIndex = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return currentBucket != null;
        }

        @Override
        public Key next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Key k = currentBucket.getKey();
            findNext();
            return k;
        }
    }
}