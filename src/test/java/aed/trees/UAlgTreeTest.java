package aed.trees;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class UAlgTreeTest {

    private static <K> List<K> lista(Iterable<K> it) {
        List<K> resultado = new ArrayList<>();
        for (K k : it) resultado.add(k);
        return resultado;
    }

    private static UAlgTree<Integer, String> arvoreCom(int... chaves) {
        UAlgTree<Integer, String> arvore = new UAlgTree<>();
        for (int k : chaves) arvore.put(k, "v" + k);
        return arvore;
    }

    /**
     * Verifica recursivamente a ordem de pesquisa e os campos size/weight de cada nó
     * (weight = número de ponteiros null na sub-árvore = size + 1).
     * Devolve o tamanho da sub-árvore.
     */
    private static <K extends Comparable<K>, V> int verificaNo(IUAlgTreeNode<K, V> no, K min, K max) {
        if (no == null) return 0;
        K k = no.getKey();
        if (min != null) assertTrue(k.compareTo(min) > 0, "ordem violada em " + k);
        if (max != null) assertTrue(k.compareTo(max) < 0, "ordem violada em " + k);

        int tamanho = 1 + verificaNo(no.getLeft(), min, k) + verificaNo(no.getRight(), k, max);
        assertEquals(tamanho, no.getSize(), "size errado no nó " + k);
        assertEquals(tamanho + 1, no.getWeight(), "weight errado no nó " + k);
        return tamanho;
    }

    private static <K extends Comparable<K>, V> void verificaInvariantes(UAlgTree<K, V> arvore) {
        assertEquals(arvore.size(), verificaNo(arvore.getRoot(), null, null));
    }

    @Test
    void novaArvore_EstaVazia() {
        UAlgTree<Integer, String> arvore = new UAlgTree<>();
        assertEquals(0, arvore.size());
        assertNull(arvore.getRoot());
        assertNull(arvore.get(1));
        assertFalse(arvore.contains(1));
        assertEquals(0, arvore.maxDepth());
        assertEquals(0, arvore.minDepth());
        assertFalse(arvore.keys().iterator().hasNext());
    }

    @Test
    void putEGet_DevolvemValores() {
        UAlgTree<Integer, String> arvore = arvoreCom(5, 2, 8, 1, 9);
        assertEquals(5, arvore.size());
        assertEquals("v8", arvore.get(8));
        assertEquals("v1", arvore.get(1));
        assertNull(arvore.get(7));
        assertTrue(arvore.contains(9));
        assertFalse(arvore.contains(3));
        verificaInvariantes(arvore);
    }

    @Test
    void put_ChaveExistente_SubstituiValorSemAlterarTamanho() {
        UAlgTree<Integer, String> arvore = arvoreCom(1, 2, 3);
        arvore.put(2, "novo");
        assertEquals(3, arvore.size());
        assertEquals("novo", arvore.get(2));
    }

    @Test
    void put_ValorNull_RemoveChave() {
        UAlgTree<Integer, String> arvore = arvoreCom(1, 2, 3);
        arvore.put(2, null);
        assertEquals(2, arvore.size());
        assertFalse(arvore.contains(2));
        verificaInvariantes(arvore);
    }

    @Test
    void keysEValues_EmOrdemCrescenteDeChave() {
        UAlgTree<Integer, String> arvore = arvoreCom(50, 20, 80, 10, 30, 70, 90, 60);
        assertEquals(List.of(10, 20, 30, 50, 60, 70, 80, 90), lista(arvore.keys()));
        assertEquals(List.of("v10", "v20", "v30", "v50", "v60", "v70", "v80", "v90"), lista(arvore.values()));
    }

    @Test
    void delete_FolhaNoComUmFilhoENoComDoisFilhos() {
        UAlgTree<Integer, String> arvore = arvoreCom(50, 20, 80, 10, 30, 70, 90, 60);
        arvore.delete(10);
        arvore.delete(70);
        arvore.delete(50);
        assertEquals(List.of(20, 30, 60, 80, 90), lista(arvore.keys()));
        assertEquals(5, arvore.size());
        assertNull(arvore.get(50));
        verificaInvariantes(arvore);
    }

    @Test
    void delete_ChaveInexistente_NaoAltera() {
        UAlgTree<Integer, String> arvore = arvoreCom(1, 2, 3);
        arvore.delete(42);
        assertEquals(List.of(1, 2, 3), lista(arvore.keys()));
    }

    @Test
    void delete_TodasAsChaves_FicaVazia() {
        UAlgTree<Integer, String> arvore = arvoreCom(4, 2, 6, 1, 3, 5, 7);
        for (int k = 1; k <= 7; k++) arvore.delete(k);
        assertEquals(0, arvore.size());
        assertNull(arvore.getRoot());
    }

    @Test
    void get_PodeRodarMasMantemConteudoEInvariantes() {
        UAlgTree<Integer, String> arvore = new UAlgTree<>();
        for (int k = 0; k < 200; k++) arvore.put(k, "v" + k);
        List<Integer> antes = lista(arvore.keys());

        for (int k = 0; k < 200; k += 7) {
            assertEquals("v" + k, arvore.get(k));
            verificaInvariantes(arvore);
        }
        assertEquals(antes, lista(arvore.keys()));
    }

    @Test
    void get_ChaveEncontrada_SobeEmDirecaoARaiz() {
        UAlgTree<Integer, String> arvore = arvoreCom(2, 1, 3);
        arvore.get(1);
        assertEquals(1, arvore.getRoot().getKey());
    }

    @Test
    void insercaoSequencial_MantemArvoreEquilibrada() {
        UAlgTree<Integer, Integer> arvore = new UAlgTree<>();
        int n = 10000;
        for (int k = 0; k < n; k++) arvore.put(k, k);
        verificaInvariantes(arvore);

        double log2 = Math.log(n) / Math.log(2);
        assertTrue(arvore.maxDepth() <= 3 * log2,
                "profundidade máxima " + arvore.maxDepth() + " demasiado grande para n=" + n);
        assertTrue(arvore.minDepth() >= 1);
        assertTrue(arvore.minDepth() <= arvore.maxDepth());
    }

    @Test
    void shallowCopy_EstruturaIndependenteValoresPartilhados() {
        StringBuilder partilhado = new StringBuilder("x");
        UAlgTree<Integer, StringBuilder> original = new UAlgTree<>();
        original.put(1, partilhado);
        original.put(2, new StringBuilder("y"));

        UAlgTree<Integer, StringBuilder> copia = original.shallowCopy();
        verificaInvariantes(copia);
        assertSame(partilhado, copia.get(1));

        copia.delete(2);
        copia.put(3, new StringBuilder("z"));
        assertEquals(List.of(1, 2), lista(original.keys()));
        assertEquals(List.of(1, 3), lista(copia.keys()));
    }

    @Test
    void minDepthEMaxDepth_ArvoreSoComRaiz() {
        UAlgTree<Integer, String> arvore = arvoreCom(1);
        assertEquals(1, arvore.minDepth());
        assertEquals(1, arvore.maxDepth());
    }

    /**
     * Sequência aleatória (semente fixa) de put/get/delete comparada com uma TreeMap,
     * verificando conteúdo e invariantes ao longo do caminho.
     */
    @Test
    void operacoesAleatorias_ComportamentoIgualATreeMap() {
        Random random = new Random(2024);
        UAlgTree<Integer, Integer> arvore = new UAlgTree<>();
        Map<Integer, Integer> modelo = new TreeMap<>();

        for (int passo = 0; passo < 20000; passo++) {
            int k = random.nextInt(2000);
            int op = random.nextInt(10);
            String contexto = "passo " + passo + ", chave " + k;

            if (op < 5) {
                int v = random.nextInt();
                arvore.put(k, v);
                modelo.put(k, v);
            } else if (op < 8) {
                assertEquals(modelo.get(k), arvore.get(k), contexto);
            } else {
                arvore.delete(k);
                modelo.remove(k);
            }

            assertEquals(modelo.size(), arvore.size(), contexto);
            if (passo % 500 == 0) {
                verificaInvariantes(arvore);
                assertEquals(new ArrayList<>(modelo.keySet()), lista(arvore.keys()), contexto);
            }
        }
        verificaInvariantes(arvore);
        assertEquals(new ArrayList<>(modelo.keySet()), lista(arvore.keys()));
        assertEquals(new ArrayList<>(modelo.values()), lista(arvore.values()));
    }
}
