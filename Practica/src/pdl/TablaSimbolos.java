package pdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// lista de entradas en orden + hash para buscar rapido
public class TablaSimbolos {
    private final int numero;
    private final String nombre;
    private final List<EntradaTS> entradas = new ArrayList<>();
    private final Map<String, Integer> indice = new HashMap<>();

    public TablaSimbolos(int numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
    }

    public int getNumero() { return numero; }

    // -1 si no esta
    public int buscar(String lexema) {
        Integer pos = indice.get(lexema);
        return pos == null ? -1 : pos;
    }

    // si ya estaba no la duplica
    public int insertar(String lexema) {
        int pos = buscar(lexema);
        if (pos >= 0) return pos;
        entradas.add(new EntradaTS(lexema));
        pos = entradas.size() - 1;
        indice.put(lexema, pos);
        return pos;
    }

    public EntradaTS get(int pos) { return entradas.get(pos); }
    public int size()             { return entradas.size(); }

    public String volcar() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre).append(" #").append(numero).append(":\n");
        for (EntradaTS e : entradas) sb.append(e.volcar());
        sb.append("\n");
        return sb.toString();
    }
}