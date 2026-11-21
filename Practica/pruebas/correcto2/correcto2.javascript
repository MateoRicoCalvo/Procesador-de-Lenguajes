/* Caso correcto 2: funcion con parametros,
   bucle do-while y pre-auto-decremento */
function cuentaAtras int (inicio int, paso int)
{
    let i int = inicio;
    do {
        echo i;
        --i;
        i = i - paso;
    } while (i > 0);
    return i;
}

let resultado int;
resultado = cuentaAtras(20, 2);
echo 'Fin de la cuenta';
