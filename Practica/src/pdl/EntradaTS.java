package pdl;

import java.util.ArrayList;
import java.util.List;

// una entrada de la TS. de momento el lexico solo rellena el lexema,
// el resto lo pondra el semantico
public class EntradaTS {
    public final String lexema;
    public String tipo;
    public Integer despl;
    public Integer numParam;
    public final List<String> tipoParam = new ArrayList<>();
    public String tipoRetorno;
    public String etiqFuncion;

    public EntradaTS(String lexema) {
        this.lexema = lexema;
    }

    public String volcar() {
        StringBuilder sb = new StringBuilder();
        sb.append("* LEXEMA : '").append(lexema).append("'\n");

        boolean hayAlgo = tipo != null || despl != null || numParam != null
                || tipoRetorno != null || etiqFuncion != null;
        if (hayAlgo) {
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