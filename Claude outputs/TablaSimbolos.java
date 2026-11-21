package pdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Una Tabla de Simbolos (global o local de una funcion).
 * Organizacion: lista de entradas en orden de insercion (la posicion es el
 * indice) + tabla hash lexema -> posicion para buscar en tiempo constante.
 */
public class TablaSimbolos {
    private final int numero;     // identificador unico (orden de creacion)
    private final String nombre;  // titulo para el volcado
    private final List<EntradaTS> entradas = new ArrayList<>();
    private final Map<String, Integer> indice = new HashMap<>();

    public TablaSimbolos(int numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
    }

    public int getNumero() { return numero; }

    /** Devuelve la posicion del lexema o -1 si no esta. */
    public int buscar(String lexema) {
        Integer pos = indice.get(lexema);
        return pos == null ? -1 : pos;
    }

    /** Inserta el lexema (si no estaba) y devuelve su posicion. */
    public int insertar(String lexema) {
        int pos = buscar(lexema);
        if (pos >= 0) return pos;
        entradas.add(new EntradaTS(lexema));
        pos = entradas.size() - 1;
        indice.put(lexema, pos);
        return pos;
    }

    public EntradaTS get(int pos) {
        return entradas.get(pos);
    }

    public int size() { return entradas.size(); }

    public String volcar() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre).append(" #").append(numero).append(":\n");
        for (EntradaTS e : entradas) sb.append(e.volcar());
        sb.append("\n");
        return sb.toString();
    }
}
