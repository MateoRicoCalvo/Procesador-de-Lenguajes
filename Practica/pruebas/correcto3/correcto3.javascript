/* Caso correcto 3: if, comparaciones y llamadas anidadas */
function esIgual boolean (a int, b int)
{
    return a == b;
}

function maximo int (x int, y int)
{
    if (x > y) return x;
    return y;
}

let _total int = 0;
let precio float = 3.0;
let mensaje string = '';
_total = maximo(32767, --_total);
if (esIgual(_total, 7) && !(precio > 117549436.0)) mensaje = 'iguales: (a == b)';
echo mensaje; /* comentario ** con asteriscos **/
