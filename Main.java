
//Árbol de Huffman
// Este algoritmo se basa en la frecuencia de aparición de los caracteres en el archivo de texto y se van haciendo nodos
// para cada caracter, luego se van uniendo los nodos de menor frecuencia hasta formar un árbol binario, que en su raíz
// tendrá el nodo con la mayor frecuencia de aparición. Luego se asignan códigos binarios a cada caracter, donde los nodos
// a la izquierda se les asigna un 0 y los nodos a la derecha un 1 para que de esta forma, los caracteres que aparecen con mayor
// frecuencia tendrán códigos binarios más cortos, y los que aparecen con menor frecuencia tendrán códigos binarios más largos
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        //creamos una instancia de la clase HuffmanCompressor
        //y llamamos a los métodos compress y decompress para comprimir y descomprimir el archivo
        HuffmanCompressor hc = new HuffmanCompressor();
        try {
            //comprimimos el archivo de texto y ese metodo genera 2 archivos, primero guardamos el texto en version 
            // comprimida como "compressed.huf", y tambien generamos un archivo de texto con los bits en bloques de 8 ("compressed_bits.txt")
            hc.compress();
            //descomprimimos el archivo comprimido y lo guardamos como "decomprimido.txt"
            hc.decompress();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}