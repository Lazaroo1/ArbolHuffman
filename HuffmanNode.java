/**
 * Clase que representa un nodo del árbol de Huffman para guardar la información del archivo txt
 */

public class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left, right;

    /**
     * Constructor de nodo
     * @param character Caracter guardado 
     * @param frequency Frecuencia de aparición dentro del archivo txt
     */
    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return Integer.compare(this.frequency, other.frequency);
    }

    /**
     * Comprueba si el nodo es hoja
     * @return true si no tiene hijos, false si tiene hijos
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }
}