package pdl;

/** Token generado por el Analizador Lexico: codigo + atributo opcional + linea. */
public class Token {
    public final TipoToken tipo;
    public final Object atributo; // Integer (ID, ENTERO), String (REAL, CADENA) o null
    public final int linea;

    public Token(TipoToken tipo, Object atributo, int linea) {
        this.tipo = tipo;
        this.atributo = atributo;
        this.linea = linea;
    }

    /** Formato obligatorio del fichero de tokens: < codigo , atributo > */
    @Override
    public String toString() {
        String attr = "";
        if (atributo != null) {
            attr = (tipo == TipoToken.CADENA) ? "\"" + atributo + "\"" : atributo.toString();
        }
        return "<" + tipo.name() + ", " + attr + ">";
    }
}
