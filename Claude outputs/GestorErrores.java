package pdl;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de Errores: recibe los errores de todos los modulos del procesador,
 * los muestra por pantalla y los vuelca al fichero de errores.
 */
public class GestorErrores {

    public enum Tipo { LEXICO, SINTACTICO, SEMANTICO }

    private final List<String> errores = new ArrayList<>();

    /**
     * Registra un error.
     * @param tipo    modulo que detecta el error
     * @param codigo  codigo del error (100-199 lexicos, 200-299 sintacticos, 300-399 semanticos)
     * @param linea   linea del fichero fuente donde se detecta
     * @param mensaje explicacion en terminos del usuario
     */
    public void error(Tipo tipo, int codigo, int linea, String mensaje) {
        String nombre;
        switch (tipo) {
            case LEXICO:     nombre = "lexico"; break;
            case SINTACTICO: nombre = "sintactico"; break;
            default:         nombre = "semantico"; break;
        }
        String texto = "Error " + nombre + " (" + codigo + ") en linea " + linea + ": " + mensaje;
        errores.add(texto);
        System.err.println(texto);
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public int numErrores() {
        return errores.size();
    }

    public void volcar(Path fichero) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(fichero, StandardCharsets.UTF_8))) {
            if (errores.isEmpty()) {
                pw.println("No se han detectado errores.");
            } else {
                for (String e : errores) pw.println(e);
                pw.println();
                pw.println("Total: " + errores.size() + " error(es).");
            }
        }
    }
}
