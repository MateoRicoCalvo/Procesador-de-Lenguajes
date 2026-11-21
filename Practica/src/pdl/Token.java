package pdl;

// un token: tipo + atributo opcional + linea
public class Token {
    public final TipoToken tipo;
    public final Object atributo;
    public final int linea;

    public Token(TipoToken tipo, Object atributo, int linea) {
        this.tipo = tipo;
        this.atributo = atributo;
        this.linea = linea;
    }

    // <TIPO, atributo>
    @Override
    public String toString() {
        String attr = "";
        if (atributo != null) {
            attr = (tipo == TipoToken.CADENA) ? "\"" + atributo + "\"" : atributo.toString();
        }
        return "<" + tipo.name() + ", " + attr + ">";
    }
}