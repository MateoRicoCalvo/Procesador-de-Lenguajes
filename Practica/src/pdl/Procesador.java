package pdl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Procesador de PdL-JS - Grupo 131.
 *
 * Uso: java -jar Procesador.jar fichero.javascript [directorio_salida]
 *
 * Genera en el directorio de salida (por defecto, el del fichero fuente):
 *   tokens.txt   - lista de tokens
 *   ts.txt       - volcado de las Tablas de Simbolos
 *   errores.txt  - listado de errores
 *
 * Primera entrega: el Analizador Sintactico todavia no existe, asi que el
 * programa principal pide tokens al Analizador Lexico hasta llegar a EOF.
 */
public class Procesador {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java -jar Procesador.jar <fichero_fuente> [directorio_salida]");
            System.exit(1);
        }
        Path fuente = Paths.get(args[0]);
        Path dirSalida = args.length > 1 ? Paths.get(args[1])
                : (fuente.toAbsolutePath().getParent());
        try {
            Files.createDirectories(dirSalida);
            String texto = leerFuente(fuente);

            GestorErrores errores = new GestorErrores();
            GestorTS gestorTS = new GestorTS(dirSalida.resolve("ts.txt"));
            AnalizadorLexico lexico = new AnalizadorLexico(texto, gestorTS, errores,
                    dirSalida.resolve("tokens.txt"));

            Token t;
            do {
                t = lexico.siguienteToken();
            } while (t.tipo != TipoToken.EOF);

            lexico.cerrar();
            gestorTS.cerrar();
            errores.volcar(dirSalida.resolve("errores.txt"));

            System.out.println("Analisis terminado: " + errores.numErrores() + " error(es). Ficheros en "
                    + dirSalida.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("No se puede procesar el fichero '" + fuente + "': " + e.getMessage());
            System.exit(2);
        }
    }

    /** Lee el fuente en UTF-8; si no es UTF-8 valido, lo lee como Windows-1252 (ANSI). */
    private static String leerFuente(Path p) throws IOException {
        byte[] bytes = Files.readAllBytes(p);
        try {
            String s = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
            if (!s.isEmpty() && s.charAt(0) == '﻿') s = s.substring(1); // BOM
            return s;
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }
}
