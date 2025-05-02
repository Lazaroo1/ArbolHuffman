import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class HuffmanCompressorTest {

    private final Path entrada = Path.of("entrada.txt");
    private final Path comprimido = Path.of("compressed.huf");
    private final Path descomprimido = Path.of("descomprimido.txt");
    private final Path bits = Path.of("compressed_bits.txt");
    private final Path decimales = Path.of("compressed_decimals.txt");



    @Test
    public void TestCompressGeneraArchivos() throws IOException {
        Files.writeString(entrada, "abc abc");
        HuffmanCompressor compresor = new HuffmanCompressor();
        compresor.compress();

        assertTrue(Files.exists(comprimido), "compressed.huf no fue creado");
        assertTrue(Files.exists(bits), "compressed_bits.txt no fue creado");
        assertTrue(Files.exists(decimales), "compressed_decimals.txt no fue creado");
    }


    @Test
    public void ArchivoVacioTest() throws IOException {
        Files.writeString(entrada, "");
        HuffmanCompressor compresor = new HuffmanCompressor();
        compresor.compress();
        compresor.decompress();

        String resultado = Files.readString(descomprimido);
        assertEquals("", resultado, "El archivo vacío no se manejó correctamente");
    }

    @Test
    public void ArchivoComprimidoValido() throws IOException {
        String input = "Texto de prueba";
        Files.writeString(entrada, input);
        HuffmanCompressor compresor = new HuffmanCompressor();
        compresor.compress();

        assertTrue(Files.exists(comprimido), "El archivo comprimido no fue creado");
        assertTrue(Files.size(comprimido) > 0, "El archivo comprimido está vacío");
    }


    
}
