package aed.tables;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class UAlshTableTest {

    /** Segunda função de dispersão para inteiros (mistura de bits, independente de hashCode). */
    private static final Function<Integer, Integer> HC2_INT = k -> {
        int h = k * 0x9E3779B9;
        return h ^ (h >>> 16);
    };

    /** djb2, a mesma família de função usada em Main para Strings. */
    private static int djb2(String s) {
        int hash = 5381;
        for (int i = 0; i < s.length(); i++) hash = ((hash << 5) + hash) + s.charAt(i);
        return hash;
    }

    private static <K> Set<K> chaves(UAlshTable<K, ?> tabela) {
        Set<K> resultado = new HashSet<>();
        for (K k : tabela.keys()) assertTrue(resultado.add(k), "chave repetida no iterador: " + k);
        return resultado;
    }

    /** Chave com hashCode fixo, para forçar colisões no primeiro hash. */
    private record ChaveColidente(int id) {
        @Override
        public int hashCode() {
            return 42;
        }
    }

    @Test
    void novaTabela_EstaVazia() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        assertEquals(0, tabela.size());
        assertNull(tabela.get(1));
        assertFalse(tabela.containsKey(1));
        assertFalse(tabela.keys().iterator().hasNext());
        assertEquals(37, tabela.getMainCapacity());
        assertEquals(37 + 17 + 11 + 7 + 5, tabela.getTotalCapacity());
    }

    @Test
    void putEGet_DevolvemValores() {
        UAlshTable<String, Integer> tabela = new UAlshTable<>(UAlshTableTest::djb2);
        tabela.put("um", 1);
        tabela.put("dois", 2);
        tabela.put("tres", 3);
        assertEquals(3, tabela.size());
        assertEquals(2, tabela.get("dois"));
        assertTrue(tabela.containsKey("tres"));
        assertNull(tabela.get("quatro"));
    }

    @Test
    void put_ChaveExistente_SubstituiValor() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        tabela.put(1, "b");
        assertEquals(1, tabela.size());
        assertEquals("b", tabela.get(1));
    }

    @Test
    void delete_RemoveEMarcaComoApagada() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        tabela.put(2, "b");
        tabela.delete(1);
        assertEquals(1, tabela.size());
        assertNull(tabela.get(1));
        assertFalse(tabela.containsKey(1));
        assertEquals(1, tabela.getDeletedNotRemoved());
        assertEquals(Set.of(2), chaves(tabela));
    }

    @Test
    void delete_ChaveInexistenteOuJaApagada_NaoAltera() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        tabela.delete(99);
        tabela.delete(1);
        tabela.delete(1);
        assertEquals(0, tabela.size());
        assertEquals(1, tabela.getDeletedNotRemoved());
    }

    @Test
    void put_ValorNull_Remove() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        tabela.put(1, null);
        assertEquals(0, tabela.size());
        assertNull(tabela.get(1));
    }

    @Test
    void put_DepoisDeApagada_Reativa() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        tabela.delete(1);
        tabela.put(1, "b");
        assertEquals(1, tabela.size());
        assertEquals("b", tabela.get(1));
        assertEquals(0, tabela.getDeletedNotRemoved());
    }

    @Test
    void muitasInsercoes_CresceEMantemTodasAsChaves() {
        UAlshTable<Integer, Integer> tabela = new UAlshTable<>(HC2_INT);
        int n = 50000;
        for (int k = 0; k < n; k++) tabela.put(k, k * 2);

        assertEquals(n, tabela.size());
        assertTrue(tabela.getMainCapacity() > 37);
        assertTrue(tabela.getLoadFactor() <= 1.0f);
        for (int k = 0; k < n; k++) assertEquals(k * 2, tabela.get(k), "chave " + k);
        assertEquals(n, chaves(tabela).size());
    }

    @Test
    void muitasRemocoes_EncolheEMantemRestantes() {
        UAlshTable<Integer, Integer> tabela = new UAlshTable<>(HC2_INT);
        for (int k = 0; k < 5000; k++) tabela.put(k, k);
        int capacidadeCheia = tabela.getMainCapacity();

        for (int k = 0; k < 4900; k++) tabela.delete(k);

        assertEquals(100, tabela.size());
        assertTrue(tabela.getMainCapacity() < capacidadeCheia);
        for (int k = 4900; k < 5000; k++) assertEquals(k, tabela.get(k));
        assertNull(tabela.get(0));
    }

    @Test
    void chavesComMesmoHashCode_SaoDistinguidas() {
        UAlshTable<ChaveColidente, Integer> tabela = new UAlshTable<>(c -> c.id() * 31 + 7);
        for (int i = 0; i < 200; i++) tabela.put(new ChaveColidente(i), i);
        assertEquals(200, tabela.size());
        for (int i = 0; i < 200; i++) assertEquals(i, tabela.get(new ChaveColidente(i)));
        assertNull(tabela.get(new ChaveColidente(999)));
    }

    @Test
    void fastPut_ChaveNova_FicaAcessivel() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.fastPut(7, "sete");
        assertEquals(1, tabela.size());
        assertEquals("sete", tabela.get(7));
    }

    @Test
    void contadores_IncrementamEReiniciam() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        tabela.put(2, "b");
        tabela.get(1);
        assertEquals(2, tabela.nOfPutCalls);
        assertEquals(3, tabela.nOfSearches);
        assertTrue(tabela.nOfCompares >= 1);

        tabela.resetCount();
        assertEquals(0, tabela.nOfPutCalls);
        assertEquals(0, tabela.nOfSearches);
        assertEquals(0, tabela.nOfCompares);
    }

    @Test
    void getSubTable_IndicesValidosEInvalidos() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        assertEquals(37, tabela.getSubTable(1).length);
        assertEquals(5, tabela.getSubTable(5).length);
        assertNull(tabela.getSubTable(0));
        assertNull(tabela.getSubTable(6));
    }

    @Test
    void iterador_SemMaisChaves_LancaExcecao() {
        UAlshTable<Integer, String> tabela = new UAlshTable<>(HC2_INT);
        tabela.put(1, "a");
        Iterator<Integer> it = tabela.keys().iterator();
        assertEquals(1, it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    /**
     * Sequência aleatória (semente fixa) de put/get/delete comparada com uma HashMap,
     * passando por vários crescimentos e encolhimentos da tabela.
     */
    @Test
    void operacoesAleatorias_ComportamentoIgualAHashMap() {
        Random random = new Random(7);
        UAlshTable<Integer, Integer> tabela = new UAlshTable<>(HC2_INT);
        Map<Integer, Integer> modelo = new HashMap<>();

        for (int passo = 0; passo < 50000; passo++) {
            // Fases alternadas de crescimento e de remoção para forçar resizes nos dois sentidos
            boolean faseRemocao = (passo / 10000) % 2 == 1;
            int k = random.nextInt(5000);
            int op = random.nextInt(10);
            String contexto = "passo " + passo + ", chave " + k;

            if (op < (faseRemocao ? 2 : 6)) {
                int v = random.nextInt();
                tabela.put(k, v);
                modelo.put(k, v);
            } else if (op < 8) {
                assertEquals(modelo.get(k), tabela.get(k), contexto);
            } else {
                tabela.delete(k);
                modelo.remove(k);
            }

            assertEquals(modelo.size(), tabela.size(), contexto);
            if (passo % 1000 == 0) assertEquals(modelo.keySet(), chaves(tabela), contexto);
        }
        assertEquals(modelo.keySet(), chaves(tabela));
        for (Map.Entry<Integer, Integer> e : modelo.entrySet())
            assertEquals(e.getValue(), tabela.get(e.getKey()));
    }
}
