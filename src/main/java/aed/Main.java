package aed;

import aed.tables.UAlshTable;
import aed.trees.UAlgTree;
import aed.utils.TemporalAnalysisUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.function.Consumer;

public class Main {
    private static UAlgTree<Integer, Integer> createUalgTree(int n) {
        UAlgTree<Integer, Integer> tree = new UAlgTree<>();
        for (int i = 0; i < n; i++) {
            tree.put(i, i);
        }
        return tree;
    }

    public static void testRacioDecrescente() {
        UAlgTree<Integer, Object> tree_teste = new UAlgTree<>();
        for (int i = 100000; i > 0; i--) {
            tree_teste.put(i, i);
        }
        System.out.println((double) tree_teste.maxDepth() / tree_teste.minDepth());
    }

    public static void testRacioCrescente() {
        UAlgTree<Integer, Object> tree_teste = new UAlgTree<>();
        for (int i = 0; i < 100000; i++) {
            tree_teste.put(i, i);
        }
        System.out.println((double) tree_teste.maxDepth() / tree_teste.minDepth());
    }

    public static void testRacioAleatorio() {
        int n = 100000;
        ArrayList<Integer> lista_aleatorio = new ArrayList<>();
        UAlgTree<Integer, Object> tree_teste = new UAlgTree<>();
        for (int i = 0; i < n; i++) {
            lista_aleatorio.add(i);
        }
        Collections.shuffle(lista_aleatorio);
        for (int i = 0; i < n; i++) {
            tree_teste.put(lista_aleatorio.get(i), i);
        }
        System.out.println((double) tree_teste.maxDepth() / tree_teste.minDepth());
    }

    public static void testPesquisaAleatoria() {
        System.out.print("TESTE PESQUISA ALEATÓRIA");
        Consumer<UAlgTree<Integer, Integer>> getRandom = (tree) -> {
            Random r = new Random();
            int n = tree.size();
            for (int i = 0; i < n; i++) {
                tree.get(r.nextInt(n));
            }
        };

        TemporalAnalysisUtils.runDoublingRatioTest(Main::createUalgTree, getRandom, 10); //Teste de razão dobrada com logica de Pareto
    }

    public static void testUAlgTreePareto() {
        System.out.print("TESTE PESQUISA DE PARETO");
        Consumer<UAlgTree<Integer, Integer>> getPareto = (tree) -> {
            Random r = new Random();
            int n = tree.size();
            int temp = (int) (0.20 * n);
            for (int i = 0; i < n; i++) {
                double v = r.nextDouble();
                if (v < 0.8) {
                    tree.get(r.nextInt(temp));
                } else {
                    tree.get(r.nextInt(temp, n));
                }
            }
        };
        TemporalAnalysisUtils.runDoublingRatioTest(Main::createUalgTree, getPareto, 10); //Teste de razão dobrada com logica de Pareto
    }

    public static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static int djb2HashString(String key) {
        int hash = 5381;
        for (int i = 0; i < key.length(); i++) {
            hash = ((hash << 5) + hash) + key.charAt(i);
        }
        return hash & 0x7fffffff;
    }

    public static HashSet<String> createRandomStringSet(int n) {
        HashSet<String> keys = new HashSet<>();
        while (keys.size() < n)
            keys.add(getRandomString());
        return keys;
    }

    public static void countCompareBySearchUAshTable() {
        UAlshTable<String, Integer> table = new UAlshTable<>(Main::djb2HashString);
        int size = 10000000;

        HashSet<String> set = createRandomStringSet(size);
        ArrayList<String> keys = new ArrayList<>(set);
        ArrayList<Integer> numero_comp = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            table.put(keys.get(i), i);
        }
        table.resetCount();

        for (int i = 0; i < size; i++) {
            table.get(keys.get(i));
            numero_comp.add(table.nOfCompares);
            table.resetCount();
        }

        double media = 0;
        for (int i : numero_comp) {
            media += i;
        }
        media /= size;

        System.out.println("Número médio de comparações efetuadas por pesquisa de valores existentes: " + media);

        ArrayList<String> nonKeys = new ArrayList<>();
        while (nonKeys.size() < size) {
            String k = getRandomString();
            if (!set.contains(k))
                nonKeys.add(k);
        }

        ArrayList<Integer> numero_comp2 = new ArrayList<>();
        table.resetCount();
        for (int i = 0; i < size; i++) {
            table.get(nonKeys.get(i));
            numero_comp2.add(table.nOfCompares);
            table.resetCount();
        }

        media = 0;
        for (int i : numero_comp2) {
            media += i;
        }
        media /= size;

        System.out.println("Número médio de comparações efetuadas por pesquisa de valores inexistentes: " + media);
    }

    public static void countCompareByInsertionUAshTable() {
        UAlshTable<String, Integer> table = new UAlshTable<>(Main::djb2HashString);
        int size = 1000000;
        ArrayList<String> keys = new ArrayList<>(createRandomStringSet(size));
        ArrayList<Integer> numero_comp = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            table.put(keys.get(i), i);
            numero_comp.add(table.nOfCompares);
            table.resetCount();
        }

        double media = 0;
        for (int i : numero_comp) {
            media += i;
        }
        media /= size;
        System.out.println("Número médio de comparações efetuadas: " + media);
    }

    public static void main(String[] args) {

        System.out.println("TESTE DE RACIO COM INSERÇÃO DE ELEMENTOS ALEATORIOS:");
        for (int i = 1; i <= 10; i++) {
            System.out.print("rácio #" + i + ": ");
            testRacioAleatorio();
        }

        System.out.println("TESTE DE RACIO COM INSERÇÃO DECRESCENTE:");
        for (int i = 1; i <= 10; i++) {
            System.out.print("rácio #" + i + ": ");
            testRacioDecrescente();
        }

        System.out.println("TESTE DE RACIO COM INSERÇÃO CRESCENTE:");
        for (int i = 1; i <= 10; i++) {
            System.out.print("rácio #" + i + ": ");
            testRacioCrescente();
        }

        countCompareByInsertionUAshTable();
        countCompareBySearchUAshTable();
    }
}

