import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Compresor y descompresor Huffman.
 */
public class HuffmanCompressor {
    private HuffmanNode root;
    private Map<Character, String> codeMap;
    private Map<String, Character> decodeMap;

    /**
     * Comprime el archivo de entrada y guarda:
     * - "compressed.huf": encabezado + bits crudos
     * - "compressed_bits.txt": bits en bloques de 8
     * @throws IOException si hay errores de entrada o salida
     * @throws IllegalArgumentException si el archivo de entrada no existe o no es un archivo de texto
     */
    public void compress() throws IOException {
        // Leemos el archivo de texto y guardamos su contenido en un StringBuilder, separando líneas
        // y leyendo linea por línea, y conservamos los saltos de línea
        // para que el texto comprimido sea igual al original
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("entrada.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        String text = sb.toString();

        // calculamos frecuencias contando las apariciones de los caracteres y construimos el árbol de Huffman con esas mismas frecuencias
        // y guardamos el árbol en un mapa de frecuencias y con "buidcodes" generamos y guardamos los códigos a partir del arbol resultante
        Map<Character, Integer> freq = countFrequencies(text);
        buildTree(freq);
        buildCodes();

        // se busca su código Huffman para cada caracter en codeMap y se acumula todo en un string de bits
        StringBuilder bits = new StringBuilder();
        for (char c : text.toCharArray()) {
            bits.append(codeMap.get(c));
        }

        // para rellenar con 0s si no es un múltiplo de 8
        while (bits.length() % 8 != 0) {
            bits.append("0"); // agregamos ceros al final si no tiene 8 bits
        }

        // Guardar archivo comprimido con el header y los bits para interpretar el resto del contenido
        //y descomprimirlo si solo se tiene el contenido comprimido guardado 
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("compressed.huf"))) {
            for (Map.Entry<Character, Integer> e : freq.entrySet()) {
                char c = e.getKey();
                String repr = switch (c) {
                    case '\n' -> "\\n";//para representar el salto de línea como un caracter
                    case '\t' -> "\\t";// para representar el tabulador como un caracter
                    case ' ' -> "␣";// para representar el espacio " " como un caracter
                    default -> String.valueOf(c);
                };
                bw.write(repr + ":" + e.getValue());
                bw.newLine();
            }
            bw.newLine();
            bw.write(bits.toString());
        }

        // guardamos la vista de bits en bloques de 8 y los guardamos en un archivo "compressed_bits.txt"
        // para que sea más fácil de leer y visualizar, aunque tambien se imprime en consola
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("compressed_bits.txt"))) {
            for (int i = 0; i < bits.length(); i += 8) {
                int end = Math.min(i + 8, bits.length());
                bw.write(bits.substring(i, end));
                bw.write(' ');
            }
        }

        // Imprimimos bloques de 8 en consola
        System.out.println("Bits (en bloques de 8):");
        for (int i = 0; i < bits.length(); i += 8) {
            int end = Math.min(i + 8, bits.length());
            System.out.print(bits.substring(i, end) + " ");
        }
        System.out.println();
    }

    /**
     * Descomprime "compressed.huf" y guarda en "descomprimido.txt".
     * @throws IOException si hay error de entrada o salida
     * @throws IllegalArgumentException si el archivo comprimido no existe o no es un archivo de texto
     */
    public void decompress() throws IOException {
        // leemos todas las líneas del .huf con cabecera + bits
        //ya que este archivo contiene tanto las frecuencias como los bits comprimidos
        // Necesitamos las líneas completas para procesar ambas partes
        var lines = Files.readAllLines(Path.of("compressed.huf"));
        int idx = 0;
        //Necesitamos reconstruir el árbol Huffman con la información de la cabecera
        // y para eso guardamos las frecuencias de cada caracter en un mapa de frecuencias
        Map<Character, Integer> freq = new LinkedHashMap<>();
        // leemos la cabecera hasta encontrar una línea vacía
        for (; idx < lines.size(); idx++) {
            String l = lines.get(idx);
            if (l.isEmpty()) { idx++; break; }
        //divide cada línea en dos partes: el carácter (o su representación especial) y su frecuencia
        // la frecuencia se convierte a número entero y se guarda en freq
            String[] parts = l.split(":", 2);
            char c = switch (parts[0]) {
                case "\\n" -> '\n';
                case "\\t" -> '\t';
                case "␣" -> ' ';
                default -> parts[0].charAt(0);
            };
            freq.put(c, Integer.parseInt(parts[1]));
        }
        //Llamamos a buildTree para reconstruir el árbol de Huffman con las frecuencias
        buildTree(freq);
        //generamos los códigos binarios para cada carácter usando ese árbol
        // con lo de que si uno se va a la izquierda es 0 y derecha es 1, para cada nodo 
        buildCodes();
        // creamos un mapa inverso de código binario a carácter
        decodeMap = new HashMap<>();
        for (var e : codeMap.entrySet()) decodeMap.put(e.getValue(), e.getKey());
        // leer los bits concatenados ( los cuales son bits) a partir del índice donde terminamos la cabecera (idx) y
        // se concatenan en un solo StringBuilder
        StringBuilder bits = new StringBuilder();
        for (; idx < lines.size(); idx++) bits.append(lines.get(idx));
        // Decodificamos
        //Recorre los bits uno por uno, construyendo un string parcial cur
        // si cur coincide con un código en decodeMap, se agrega el carácter correspondiente al out y se reinicia cur
        StringBuilder out = new StringBuilder();
        String cur = "";
        for (char b : bits.toString().toCharArray()) {
            cur += b;
            if (decodeMap.containsKey(cur)) {
                out.append(decodeMap.get(cur));
                cur = "";
            }
        }
        // Guardamos el texto descomprimido
        Files.writeString(Path.of("descomprimido.txt"), out.toString());
        System.out.println("Texto descomprimido guardado en descomprimido.txt");
    }


    //Creamos un mapa donde se va a guardar cuántas veces aparece cada caracter
    //LinkedhashMap para mantener el orden original en que aparecen los caracteres
    private Map<Character, Integer> countFrequencies(String text) {
        var map = new LinkedHashMap<Character, Integer>();
    //Cuenta las veces que aparece ese caracter y si no estaba en el mapa, lo inicia en 0 y luego le suma 1
        for (char c : text.toCharArray()) map.put(c, map.getOrDefault(c, 0) + 1);
        return map;
    }

    private void buildTree(Map<Character, Integer> freq) {
        //se crea una cola de prioridad (menor frecuencia tiene prioridad)los nodos más frecuentes quedan al final
        var pq = new PriorityQueue<HuffmanNode>();
        // creamos un nodo por cada carácter y su frecuencia, y se agrega a la cola
        for (var e : freq.entrySet()) pq.add(new HuffmanNode(e.getKey(), e.getValue()));
        while (pq.size() > 1) {
            //Saca los dos nodos con menor frecuencia y crea un nuevo nodo padre con la suma de sus frecuencias
            var a = pq.poll();
            var b = pq.poll();
            // el nuevo nodo padre tiene como hijos los dos nodos que sacamos de la cola
            //sin carácter (por eso el '\0') y la suma de las frecuencias de los dos nodos
            var p = new HuffmanNode('\0', a.frequency + b.frequency);
            //Asignamos los nodos como hijos izquierdo y derecho y se vuelve a meter el nuevo nodo a la cola
            p.left = a; p.right = b;
            pq.add(p);
        }
        root = pq.poll();
    }

    private void buildCodes() {
        //Inicializamos un mapa donde se almacenarán los códigos binarios por carácter
        codeMap = new HashMap<>();
        //se comienza el recorrido del árbol desde la raíz, construyendo los códigos con 0s y 1s
        // a la izquierda se agrega un 0 y a la derecha un 1
        dfs(root, "");
    }

    //se recorre el árbol de Huffman y genera los códigos binarios para cada carácter, es como el corazón del algoritmo
    // Si el nodo es una hoja (tiene un carácter), se guarda su código en el mapa
    private void dfs(HuffmanNode node, String code) {
        if (node == null) return;
        if (node.isLeaf()) codeMap.put(node.character, code);
        dfs(node.left, code + '0');
        dfs(node.right, code + '1');
    }
}
