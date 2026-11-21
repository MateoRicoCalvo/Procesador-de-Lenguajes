@echo off
REM Compila las fuentes y genera Procesador.jar (requiere JDK 8 o superior)
if exist bin rmdir /s /q bin
mkdir bin
javac -encoding UTF-8 -d bin src\pdl\*.java
echo Main-Class: pdl.Procesador> manifest.txt
jar cfm Procesador.jar manifest.txt -C bin .
del manifest.txt
echo Procesador.jar generado.
