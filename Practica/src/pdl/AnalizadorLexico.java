package pdl;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/*
 * Analizador lexico de PdL-JS (grupo 131).
 * Cada siguienteToken() recorre el AFD desde el estado 0 hasta un estado final.
 * Si hay error se lo pasa al GestorErrores y sigue con el siguiente token.
 * Opciones del grupo: comentarios de bloque, cadenas con ', -- y do-while.
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
        PAL_RESERVADAS.put("do", TipoToken.DO);
        PAL_RESERVADAS.put("while", TipoToken.WHILE);
        PAL_RESERVADAS.put("return", TipoToken.RETURN);
        PAL_RESERVADAS.put("echo", TipoToken.ECHO);
        PAL_RESERVADAS.put("prompt", TipoToken.PROMPT);
        PAL_RESERVADAS.put("true", TipoToken.TRUE);
        PAL_RESERVADAS.put("false", TipoToken.FALSE);
    }

    private final String fuente;
    private int pos = 0;
    private int car;
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

    // avanza al siguiente caracter
    private void leer() {
        if (car == '\n') linea++;
        pos++;
        car = pos < fuente.length() ? fuente.charAt(pos) : EOF;
    }

    private void error(int codigo, int lin, String msg) {
        errores.error(GestorErrores.Tipo.LEXICO, codigo, lin, msg);
    }

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
        if (c == EOF)  return "fin de fichero";
        if (c == '\n') return "salto de linea";
        if (c == '\t') return "tabulador";
        if (c == ' ')  return "espacio en blanco";
        return "'" + (char) c + "'";
    }

    public Token siguienteToken() {
        while (true) {

            // estado 0: blancos
            if (car == ' ' || car == '\t' || car == '\r' || car == '\n') {
                leer();
                continue;
            }
            int lin = linea;

            if (car == EOF) return genToken(TipoToken.EOF, null, lin);

            // estado 1: identificadores y palabras reservadas
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
                int posTS = gestorTS.tratarIdentificador(lexema);
                return genToken(TipoToken.ID, posTS, lin);
            }

            // estados 2,3,4: enteros y reales
            if (esDigito(car)) {
                StringBuilder lex = new StringBuilder();
                int valor = car - '0';
                boolean desbordado = false;
                lex.append((char) car);
                leer();

                while (esDigito(car)) {         // estado 2
                    if (!desbordado) {
                        valor = valor * 10 + (car - '0');
                        if (valor > MAX_ENTERO) desbordado = true;
                    }
                    lex.append((char) car);
                    leer();
                }

                if (car != '.') {               // es entero
                    if (desbordado) {
                        error(107, lin, "la constante entera " + lex + " supera el maximo (" + MAX_ENTERO + ")");
                        continue;
                    }
                    return genToken(TipoToken.ENTERO, valor, lin);
                }

                lex.append('.');
                leer();                         // estado 3
                if (!esDigito(car)) {
                    error(108, lin, "la constante real '" + lex
                            + "' esta mal formada: falta algun digito tras el punto (ej: 3.0)");
                    continue;
                }
                while (esDigito(car)) {         // estado 4
                    lex.append((char) car);
                    leer();
                }
                BigDecimal real = new BigDecimal(lex.toString());
                if (real.compareTo(MAX_REAL) > 0) {
                    error(109, lin, "la constante real " + lex + " supera el maximo (" + MAX_REAL + ")");
                    continue;
                }
                return genToken(TipoToken.REAL, lex.toString(), lin);
            }

            switch (car) {

                // estado 5: cadena con '
                case '\'': {
                    StringBuilder lex = new StringBuilder();
                    leer();
                    while (car != '\'' && car != '\n' && car != EOF) {
                        lex.append((char) car);
                        leer();
                    }
                    if (car != '\'') {
                        error(105, lin, "la cadena no esta cerrada: falta la comilla simple (') antes del "
                                + mostrar(car));
                        continue;
                    }
                    leer();
                    if (lex.length() > MAX_CADENA) {
                        error(106, lin, "la cadena tiene " + lex.length()
                                + " caracteres y el maximo es " + MAX_CADENA);
                        continue;
                    }
                    return genToken(TipoToken.CADENA, lex.toString(), lin);
                }

                // estados 7,8,9: comentario de bloque
                case '/': {
                    leer();
                    if (car != '*') {
                        error(103, lin, "caracter '/' no valido: los comentarios empiezan por '/*'");
                        continue;
                    }
                    leer();
                    boolean cerrado = false;
                    while (car != EOF) {
                        if (car == '*') {       // estado 9
                            leer();
                            while (car == '*') leer();
                            if (car == '/') {
                                leer();
                                cerrado = true;
                                break;
                            }
                        } else {                // estado 8
                            leer();
                        }
                    }
                    if (!cerrado) {
                        error(104, lin, "el comentario '/*' no se ha cerrado con '*/' antes del fin de fichero");
                    }
                    continue;
                }

                // estado 10: '-' o '--'
                case '-':
                    leer();
                    if (car == '-') {
                        leer();
                        return genToken(TipoToken.DECREMENTO, null, lin);
                    }
                    return genToken(TipoToken.RESTA, null, lin);

                // estado 11: '=' o '=='
                case '=':
                    leer();
                    if (car == '=') {
                        leer();
                        return genToken(TipoToken.IGUAL, null, lin);
                    }
                    return genToken(TipoToken.ASIG, null, lin);

                // estado 12: '&&'
                case '&':
                    leer();
                    if (car == '&') {
                        leer();
                        return genToken(TipoToken.AND, null, lin);
                    }
                    error(102, lin, "se esperaba '&&' (y logico) pero hay '&' seguido de " + mostrar(car));
                    continue;

                // simbolos de un solo caracter
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
                    String extra = (c == '"') ? " (las cadenas van con comilla simple)" : "";
                    error(101, lin, "el caracter " + mostrar(c) + " no pertenece al lenguaje" + extra);
                }
            }
        }
    }

    public void cerrar() {
        ficheroTokens.close();
    }
}