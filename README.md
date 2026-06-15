# LFT Compiler & Translators Project

Questo repository contiene il progetto del laboratorio del corso di **Linguaggi Formali e Traduttori (LFT)** (A.A. 2023/2024). Il progetto realizza l'implementazione incrementale di un **compilatore/traduttore** da un linguaggio imperativo personalizzato a bytecode eseguibile sulla JVM (Java Virtual Machine) tramite l'assemblatore **Jasmin**.

---

## Autori
*   **Andrei Miclaus**
*   **Andrea Rosso**

---

## Struttura del Progetto

Il progetto è strutturato nelle seguenti parti, che ripercorrono le fasi classiche di sviluppo di un compilatore:

1.  **DFA (Esercizi Lab 1):**
    *   File da `Es1_1.java` a `Es1_6.java`.
    *   Implementazione di Automi a Stati Finiti Deterministici (DFA) in Java per il riconoscimento di pattern specifici (identificatori, costanti numeriche, stringhe binarie con proprietà particolari, ecc.).
2.  **Lexer (Analisi Lessicale):**
    *   `Lexer.java`, `Lexer2.java`, `Lexer3.java` (supportati da `Token.java`, `Word.java`, `NumberTok.java`, `Tag.java`).
    *   Effettua la scansione dei file sorgente (`.lft`) traducendo i caratteri in token e gestendo costrutti come identificatori, numeri e commenti.
3.  **Parser (Analisi Sintattica):**
    *   `Parser.java` e `Parser2.java`.
    *   Effettua l'analisi sintattica ricorsiva a discesa (LL(1)) dei token generati dal Lexer per verificare la correttezza grammaticale del sorgente.
4.  **Valutatore (Interprete di Espressioni):**
    *   `Valutatore.java`.
    *   Un interprete guidato dalla sintassi per valutare ed eseguire espressioni aritmetiche semplici direttamente a run-time.
5.  **Translator & CodeGenerator (Compilatore Finale):**
    *   `Translator.java`, `CodeGenerator.java`, `Instruction.java`, `OpCode.java`, `SymbolTable.java`.
    *   Esegue la traduzione guidata dalla sintassi dal linguaggio sorgente proprietario ad assembly Jasmin (`Output.j`), compilabile in bytecode JVM con `jasmin.jar`.

---

## Caratteristiche del Linguaggio Sorgente

Il linguaggio sorgente supportato dal compilatore presenta le seguenti caratteristiche (vedi `test_Translator.lft`):
*   **Variabili:** Dichiarazione e assegnamento statico e dinamico.
*   **Assegnamenti Multipli:** Sintassi `assign [expr to idlist]`, che permette di assegnare un valore a più variabili contemporaneamente.
*   **Strutture di Controllo:**
    *   Condizionale: `if (bexpr) stat [else stat] end`
    *   Ciclo: `for (stat1) do stat`
*   **Input/Output:** `read(idlist)` per input da terminale e `print(exprlist)` per visualizzare i risultati a schermo.
*   **Espressioni:** Notazione prefissa per operazioni complesse (es. `+ (a, b, c)` e `* (x, y)`), e infissa classica per le restanti espressioni.

---

## Come Compilare ed Eseguire

### Prerequisiti
*   Java Development Kit (JDK 17 o superiore consigliato) installato e configurato nel PATH.
*   Libreria `jasmin.jar` (inclusa nella root del progetto).

### Passi per l'Esecuzione del Compilatore

1.  **Compila tutte le classi Java:**
    ```bash
    javac *.java
    ```

2.  **Esegui il Traduttore (Translator):**
    *   Il traduttore legge un file sorgente in input (di default configurato su `test_Translator.lft` nel file `Translator.java`) e genera in output il file Jasmin `Output.j`.
    ```bash
    java Translator
    ```

3.  **Genera il bytecode eseguibile (.class):**
    *   Utilizza la libreria Jasmin inclusa per assemblare il file `Output.j` in bytecode eseguibile per la JVM:
    ```bash
    java -jar jasmin.jar Output.j
    ```
    *   Questo comando genererà il file `Output.class`.

4.  **Esegui il programma compilato:**
    *   Esegui l'applicazione risultante:
    ```bash
    java Output
    ```

---

## File di Test Inclusi
*   `test_Lexer.lft` / `test_Lexer3.lft`: File sorgente per il testing lessicale.
*   `test_Valutatore.lft`: Espressioni aritmetiche per testare il valutatore sintattico diretto.
*   `test_Translator.lft`: Programma completo che dimostra l'uso di cicli `for`, costrutti `if-else`, input da utente (`read`) e stampa a terminale (`print`), utile per testare la generazione di codice finale.
