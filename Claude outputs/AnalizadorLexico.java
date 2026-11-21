package pdl;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Analizador Lexico de PdL-JS (grupo 131).
 *
 * Implementa el Automata Finito Determinista descrito en la memoria.
 * Cada llamada a siguienteToken() recorre el automata desde el estado 0
 * hasta un estado final y devuelve un token; los errores se comunican al
 * Gestor de Errores y el analisis continua (no se genera token erroneo).
 *
 * Opciones del grupo: comentarios de bloque, cadenas con comilla simple,
 * pre-auto-decremento (--), do-while.
 */
public class AnalizadorLexico {

    public static final int MAX_ENTERO = 32767;
    public static final BigDecimal MAX_REAL = new BigDecimal("117549436.0");
    public static final int MAX_CADENA = 64;
    private static final int EOF = -1;

    private static final Map<String, TipoToken> PAL_RESERVADAS = new HashMap<>();
    static {
        PAL_RESERVADAS.put("let", TipoToken.LET);
        PAL_RESERVADAS.put("function", TipoToken.FUNCTION);
        PAL_RESERVADAS.put("void", TipoToken.VOID);
        PAL_RESERVADAS.put("int", TipoToken.INT);
        PAL_RESERVADAS.put("float", TipoToken.FLOAT);
        PAL_RESERVADAS.put("boolean", TipoToken.BOOLEAN);
        PAL_RESERVADAS.put("string", TipoToken.STRING);
        PAL_RESERVADAS.put("if", TipoToken.IF);
        PAL_RESERVADAS.put("else", TipoToken.ELSE);
        PAL_RESERVADAS.put("do", TipoToken.DO);
        PAL_RESERVADAS.put("while", TipoToken.WHILE);
        PAL_RESERVADAS.put("return", TipoToken.RETURN);
        PAL_RESERVADAS.put("echo", TipoToken.ECHO);
        PAL_RESERVADAS.put("prompt", TipoToken.PROMPT);
        PAL_RESERVADAS.put("true", TipoToken.TRUE);
        PAL_RESERVADAS.put("false", TipoToken.FALSE);
    }

    private final String fuente;
    private int pos = 0;          // indice del caracter actual
    private int car;              // caracter actual (leido pero no consumido)
    private int linea = 1;

    private final GestorTS gestorTS;
    private final GestorErrores errores;
    private final PrintWriter ficheroTokens;

    public AnalizadorLexico(String fuente, GestorTS gestorTS, GestorErrores errores, Path ficheroTokens)
            throws IOException {
        this.fuente = fuente;
        this.gestorTS = gestorTS;
        this.errores = errores;
        this.ficheroTokens = new PrintWriter(Files.newBufferedWriter(ficheroTokens, StandardCharsets.UTF_8));
        this.car = fuente.isEmpty() ? EOF : fuente.charAt(0);
    }

    // ---------------------------------------------------------------
    // Acciones semanticas basicas
    // ---------------------------------------------------------------

    /** leer: avanza al siguiente caracter del fichero fuente. */
    private void leer() {
        if (car == '\n') linea++;
        pos++;
        car = pos < fuente.length() ? fuente.charAt(pos) : EOF;
    }

    private void error(int codigo, int lin, String msg) {
        errores.error(GestorErrores.Tipo.LEXICO, codigo, lin, msg);
    }

    /** genToken: crea el token y lo escribe en el fichero de tokens. */
    private Token genToken(TipoToken tipo, Object atributo, int lin) {
        Token t = new Token(tipo, atributo, lin);
        ficheroTokens.println(t);
        return t;
    }

    private static boolean esLetra(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean esDigito(int c) {
        return c >= '0' && c <= '9';
    }

    private static String mostrar(int c) {
        if (c == EOF) return "fin de fichero";
        if (c == '\n') return "salto de linea";
        if (c == '\t') return "tabulador";
        if (c == ' ') return "espacio en blanco";
        return "'" + (char) c + "'";
    }

    // ---------------------------------------------------------------
    // Automata
    // ---------------------------------------------------------------

    /** Devuelve el siguiente token del fichero fuente (EOF al terminar). */
    public Token siguienteToken() {
        while (true) {
            // ---------- Estado 0 ----------
            // delimitadores: espacio, tabulador, retorno de carro, salto de linea
            if (car == ' ' || car == '\t' || car == '\r' || car == '\n') {
                leer();
                continue;
            }
            int lin = linea;

            if (car == EOF) {
                return genToken(TipoToken.EOF, null, lin);
            }

            // Estado 1: identificadores y palabras reservadas
            if (esLetra(car) || car == '_') {
                StringBuilder lex = new StringBuilder();
                lex.append((char) car);
                leer();
                while (esLetra(car) || esDigito(car) || car == '_') {
                    lex.append((char) car);
                    leer();
                }
                String lexema = lex.toString();
                TipoToken pr = PAL_RESERVADAS.get(lexema);
                if (pr != null) return genToken(pr, null, lin);
                Object[] r = gestorTS.tratarIdentificador(lexema);
                return genToken(TipoToken.ID, r[1], lin);
            }

            // Estados 2, 3, 4: constantes enteras y reales
            if (esDigito(car)) {
                StringBuilder lex = new StringBuilder();
                int valor = car - '0';
                boolean desbordado = false;
                lex.append((char) car);
                leer();
                while (esDigito(car)) {                         // estado 2
                    if (!desbordado) {
                        valor = valor * 10 + (car - '0');
                        if (valor > MAX_ENTERO) desbordado = true;
                    }
                    lex.append((char) car);
                    leer();
                }
                if (car != '.') {                               // final: entero
                    if (desbordado) {
                        error(108, lin, "la constante entera " + lex
                                + " supera el valor maximo permitido (" + MAX_ENTERO + ")");
                        continue;
                    }
                    return genToken(TipoToken.ENTERO, valor, lin);
                }
                lex.append('.');
                leer();                                         // estado 3
                if (!esDigito(car)) {
                    error(109, lin, "la constante real '" + lex + "' esta mal formada: "
                            + "tras el punto debe haber al menos un digito (por ejemplo 3.0)");
                    continue;
                }
                while (esDigito(car)) {                         // estado 4
                    lex.append((char) car);
                    leer();
                }
                BigDecimal real = new BigDecimal(lex.toString());
                if (real.compareTo(MAX_REAL) > 0) {
                    error(110, lin, "la constante real " + lex
                            + " supera el valor maximo permitido (" + MAX_REAL + ")");
                    continue;
                }
                return genToken(TipoToken.REAL, lex.toString(), lin);
            }

            switch (car) {
                // Estado 5: cadenas con comilla simple
                case '\'': {
                    StringBuilder lex = new StringBuilder();
                    leer();
                    while (car != '\'' && car != '\n' && car != EOF) {
                        lex.append((char) car);
                        leer();
                    }
                    if (car != '\'') {
                        error(105, lin, "la cadena no se ha cerrado: falta la comilla simple (') "
                                + "antes del " + mostrar(car));
                        continue;
                    }
                    leer(); // consume la comilla de cierre
                    if (lex.length() > MAX_CADENA) {
                        error(107, lin, "la cadena tiene " + lex.length()
                                + " caracteres y el maximo permitido es " + MAX_CADENA);
                        continue;
                    }
                    return genToken(TipoToken.CADENA, lex.toString(), lin);
                }

                // Estados 7, 8, 9: comentario de bloque
                case '/': {
                    leer();
                    if (car != '*') {
                        error(103, lin, "caracter '/' no valido: los comentarios deben empezar por '/*'");
                        continue;
                    }
                    leer();
                    boolean cerrado = false;
                    while (car != EOF) {
                        if (car == '*') {          // estado 9
                            leer();
                            while (car == '*') leer();
                            if (car == '/') {
                                leer();
                                cerrado = true;
                                break;
                            }
                        } else {                   // estado 8
                            leer();
                        }
                    }
                    if (!cerrado) {
                        error(104, lin, "el comentario abierto con '/*' no se ha cerrado con '*/' "
                                + "antes del fin de fichero");
                    }
                    continue;
                }

                // Estado 10: '-' o '--'
                case '-':
                    leer();
                    if (car == '-') {
                        leer();
                        return genToken(TipoToken.DECREMENTO, null, lin);
                    }
                    return genToken(TipoToken.RESTA, null, lin);

                // Estado 11: '=' o '=='
                case '=':
                    leer();
                    if (car == '=') {
                        leer();
                        return genToken(TipoToken.IGUAL, null, lin);
                    }
                    return genToken(TipoToken.ASIG, null, lin);

                // Estado 12: '&&'
                case '&':
                    leer();
                    if (car == '&') {
                        leer();
                        return genToken(TipoToken.AND, null, lin);
                    }
                    error(102, lin, "se esperaba '&&' (operador y logico) pero se encontro '&' seguido de "
                            + mostrar(car));
                    continue;

                // Caracteres que forman un token por si solos
                case '+': leer(); return genToken(TipoToken.SUMA, null, lin);
                case '>': leer(); return genToken(TipoToken.MAYOR, null, lin);
                case '!': leer(); return genToken(TipoToken.NOT, null, lin);
                case '(': leer(); return genToken(TipoToken.PARABIERTO, null, lin);
                case ')': leer(); return genToken(TipoToken.PARCERRADO, null, lin);
                case '{': leer(); return genToken(TipoToken.LLAVEABIERTA, null, lin);
                case '}': leer(); return genToken(TipoToken.LLAVECERRADA, null, lin);
                case ';': leer(); return genToken(TipoToken.PUNTOYCOMA, null, lin);
                case ',': leer(); return genToken(TipoToken.COMA, null, lin);

                default: {
                    int c = car;
                    leer();
                    String extra = (c == '"') ? " (las cadenas se escriben entre comillas simples)" : "";
                    error(101, lin, "el caracter " + mostrar(c) + " no pertenece al lenguaje" + extra);
                }
            }
        }
    }

    public void cerrar() {
        ficheroTokens.close();
    }
}
