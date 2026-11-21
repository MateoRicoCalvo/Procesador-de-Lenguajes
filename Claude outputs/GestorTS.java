package pdl;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestor de Tablas de Simbolos.
 * - Existe siempre una tabla global (#1) y, como PdL-JS no permite funciones
 *   anidadas, como mucho una tabla local activa a la vez.
 * - Cada tabla se vuelca al fichero justo antes de destruirse.
 * - zonaDeclaracion lo activara el Analizador Sintactico/Semantico mientras se
 *   procesa una declaracion (let, parametros, nombre de funcion).
 */
public class GestorTS {
    private final PrintWriter salida;
    private int contadorTablas = 0;
    private final TablaSimbolos global;
    private TablaSimbolos local = null;
    public boolean zonaDeclaracion = false;

    public GestorTS(Path fichero) throws IOException {
        salida = new PrintWriter(Files.newBufferedWriter(fichero, StandardCharsets.UTF_8));
        global = new TablaSimbolos(++contadorTablas, "TABLA PRINCIPAL");
    }

    public TablaSimbolos getGlobal() { return global; }

    public TablaSimbolos getActual() { return local != null ? local : global; }

    /** Se llamara al empezar la definicion de una funcion. */
    public TablaSimbolos crearTablaLocal(String nombreFuncion) {
        local = new TablaSimbolos(++contadorTablas, "TABLA DE LA FUNCION " + nombreFuncion);
        return local;
    }

    /** Se llamara al terminar la definicion de una funcion: vuelca y destruye. */
    public void destruirTablaLocal() {
        if (local != null) {
            salida.print(local.volcar());
            local = null;
        }
    }

    /**
     * Operacion que usa el Analizador Lexico al reconocer un identificador.
     * - En zona de declaracion: se inserta en la tabla actual.
     * - Fuera de ella: se busca en la local y despues en la global; si no esta
     *   (uso sin declarar = variable global entera implicita) se inserta en la global.
     * @return {tabla, posicion}
     */
    public Object[] tratarIdentificador(String lexema) {
        if (zonaDeclaracion) {
            TablaSimbolos t = getActual();
            return new Object[] { t, t.insertar(lexema) };
        }
        if (local != null) {
            int p = local.buscar(lexema);
            if (p >= 0) return new Object[] { local, p };
        }
        return new Object[] { global, global.insertar(lexema) };
    }

    /** Al terminar el analisis: vuelca (y destruye) las tablas que queden. */
    public void cerrar() {
        destruirTablaLocal();
        salida.print(global.volcar());
        salida.close();
    }
}
