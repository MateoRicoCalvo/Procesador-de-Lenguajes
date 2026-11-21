package pdl;

/**
 * Codigos de los tokens del lenguaje PdL-JS (grupo 131).
 * El nombre del enumerado es el codigo que se escribe en el fichero de tokens.
 */
public enum TipoToken {
    // Palabras reservadas (sin atributo)
    LET, FUNCTION, VOID, INT, FLOAT, BOOLEAN, STRING,
    IF, ELSE, DO, WHILE, RETURN, ECHO, PROMPT, TRUE, FALSE,

    // Identificadores y constantes (con atributo)
    ID,         // atributo: posicion en la Tabla de Simbolos
    ENTERO,     // atributo: valor numerico
    REAL,       // atributo: valor numerico
    CADENA,     // atributo: contenido de la cadena

    // Operadores
    SUMA,       // +
    RESTA,      // -
    DECREMENTO, // --  (pre-auto-decremento)
    ASIG,       // =
    IGUAL,      // ==
    MAYOR,      // >
    AND,        // &&
    NOT,        // !

    // Separadores
    PARABIERTO,   // (
    PARCERRADO,   // )
    LLAVEABIERTA, // {
    LLAVECERRADA, // }
    PUNTOYCOMA,   // ;
    COMA,         // ,

    EOF
}
