package pdl;

import java.util.ArrayList;
import java.util.List;

/**
 * Entrada de una Tabla de Simbolos. El Analizador Lexico solo rellena el lexema;
 * el resto de atributos los completara el Analizador Semantico.
 */
public class EntradaTS {
    public final String lexema;
    public String tipo;                // 'int', 'float', 'boolean', 'string', 'funcion'
    public Integer despl;              // desplazamiento (variables)
    public Integer numParam;           // numero de parametros (funciones)
    public final List<String> tipoParam = new ArrayList<>(); // TipoParam01..XX
    public String tipoRetorno;         // tipo devuelto (funciones)
    public String etiqFuncion;         // etiqueta (funciones)

    public EntradaTS(String lexema) {
        this.lexema = lexema;
    }

    /** Vuelca la entrada con el formato obligatorio del fichero de TS. */
    public String volcar() {
        StringBuilder sb = new StringBuilder();
        sb.append("* LEXEMA : '").append(lexema).append("'\n");
        boolean hayAtributos = tipo != null || despl != null || numParam != null
                || tipoRetorno != null || etiqFuncion != null;
        if (hayAtributos) {
            sb.append("  ATRIBUTOS :\n");
            if (tipo != null)        sb.append("  + Tipo : '").append(tipo).append("'\n");
            if (despl != null)       sb.append("  + Despl : ").append(despl).append("\n");
            if (numParam != null) {
                sb.append("  + numParam : ").append(numParam).append("\n");
                for (int i = 0; i < tipoParam.size(); i++) {
                    sb.append(String.format("    + TipoParam%02d : '%s'%n", i + 1, tipoParam.get(i)));
                }
            }
            if (tipoRetorno != null) sb.append("  + TipoRetorno : '").append(tipoRetorno).append("'\n");
            if (etiqFuncion != null) sb.append("  + EtiqFuncion : '").append(etiqFuncion).append("'\n");
        }
        sb.append("  --------- ----------\n");
        return sb.toString();
    }
}
