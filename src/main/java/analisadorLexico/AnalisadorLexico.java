package analisadorLexico;

import java.io.*;
import java.util.*;

public class AnalisadorLexico {

    private static final Set<String> PALAVRAS_RESERVADAS = new HashSet<>(Arrays.asList(
            "inicio", "fim", "var", "leia", "escreva", "se", "senao"
    ));

    private static final Map<String, String> TOKENS = new HashMap<>();
    static {
        TOKENS.put("inicio", "id");
        TOKENS.put("fim", "id");
        TOKENS.put("var", "nu");
        TOKENS.put("leia", "fr");
        TOKENS.put("escreva", "fr");
        TOKENS.put("se", "or");
        TOKENS.put("senao", "or");
        TOKENS.put("(", "om");
        TOKENS.put(")", "om");
        TOKENS.put(";", "ol");
    }

    private String entrada;
    private int posicao;
    private int linha;
    private int coluna;
    private List<Token> tokensLista;

    public AnalisadorLexico(String entrada) {
        this.entrada = entrada;
        this.posicao = 0;
        this.linha = 1;
        this.coluna = 1;
        this.tokensLista = new ArrayList<>();
    }

    static class Token {
        String lexema;
        String tipo;
        int linha;
        int coluna;

        Token(String lexema, String tipo, int linha, int coluna) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.linha = linha;
            this.coluna = coluna;
        }

        @Override
        public String toString() {
            return String.format("[%s, ] [%s, %d][%s, ]", lexema, tipo, linha, coluna);
        }
    }

    private boolean fimEntrada() {
        return posicao >= entrada.length();
    }

    private char caracterAtual() {
        if (fimEntrada()) return '\0';
        return entrada.charAt(posicao);
    }

    private void avancar() {
        if (!fimEntrada()) {
            if (entrada.charAt(posicao) == '\n') {
                linha++;
                coluna = 1;
            } else {
                coluna++;
            }
            posicao++;
        }
    }

    private void pularEspacosEComentarios() {
        while (!fimEntrada()) {
            char c = caracterAtual();

            if (Character.isWhitespace(c)) {
                avancar();
            }
            else if (c == '/' && posicao + 1 < entrada.length() && entrada.charAt(posicao + 1) == '/') {
                while (!fimEntrada() && caracterAtual() != '\n') {
                    avancar();
                }
                if (!fimEntrada()) {
                    avancar();
                }
            }
            else if (c == '/' && posicao + 1 < entrada.length() && entrada.charAt(posicao + 1) == '*') {
                avancar();
                avancar();

                while (!fimEntrada()) {
                    if (caracterAtual() == '*' && posicao + 1 < entrada.length() &&
                            entrada.charAt(posicao + 1) == '/') {
                        avancar();
                        avancar();
                        break;
                    }
                    avancar();
                }
            }
            else {
                break;
            }
        }
    }

    private Token reconhecerIdentificador() {
        int inicioLinha = linha;
        int inicioColuna = coluna;
        StringBuilder sb = new StringBuilder();

        while (!fimEntrada() && (Character.isLetterOrDigit(caracterAtual()) || caracterAtual() == '_')) {
            sb.append(caracterAtual());
            avancar();
        }

        String lexema = sb.toString();

        if (PALAVRAS_RESERVADAS.contains(lexema)) {
            return new Token(lexema, TOKENS.get(lexema), inicioLinha, inicioColuna);
        }

        return new Token(lexema, "id", inicioLinha, inicioColuna);
    }

    private Token reconhecerNumero() {
        int inicioLinha = linha;
        int inicioColuna = coluna;
        StringBuilder sb = new StringBuilder();

        while (!fimEntrada() && Character.isDigit(caracterAtual())) {
            sb.append(caracterAtual());
            avancar();
        }

        return new Token(sb.toString(), "nu", inicioLinha, inicioColuna);
    }

    private Token reconhecerString() {
        int inicioLinha = linha;
        int inicioColuna = coluna;
        StringBuilder sb = new StringBuilder();

        avancar();
        sb.append('"');

        while (!fimEntrada() && caracterAtual() != '"') {
            sb.append(caracterAtual());
            avancar();
        }

        if (!fimEntrada()) {
            sb.append(caracterAtual());
            avancar();
        }

        return new Token(sb.toString(), "fr", inicioLinha, inicioColuna);
    }

    private Token reconhecerOperador() {
        int inicioLinha = linha;
        int inicioColuna = coluna;
        char c = caracterAtual();

        if (c == '<' || c == '>' || c == '=' || c == '!') {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            avancar();

            if (!fimEntrada() && caracterAtual() == '=') {
                sb.append(caracterAtual());
                avancar();
            }

            return new Token(sb.toString(), "or", inicioLinha, inicioColuna);
        }

        if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
            String lexema = String.valueOf(c);
            avancar();
            return new Token(lexema, "om", inicioLinha, inicioColuna);
        }

        if (c == '&' || c == '|' || c == '~') {
            String lexema = String.valueOf(c);
            avancar();

            if ((c == '&' || c == '|') && !fimEntrada() && caracterAtual() == c) {
                lexema += caracterAtual();
                avancar();
            }

            return new Token(lexema, "ol", inicioLinha, inicioColuna);
        }

        return null;
    }

    public List<Token> analisar() {
        while (!fimEntrada()) {
            pularEspacosEComentarios();

            if (fimEntrada()) break;

            char c = caracterAtual();

            if (Character.isLetter(c)) {
                tokensLista.add(reconhecerIdentificador());
            }
            else if (Character.isDigit(c)) {
                tokensLista.add(reconhecerNumero());
            }
            else if (c == '"') {
                tokensLista.add(reconhecerString());
            }
            else if (c == '(' || c == ')') {
                tokensLista.add(new Token(String.valueOf(c), "om", linha, coluna));
                avancar();
            }
            else if (c == ';') {
                tokensLista.add(new Token(String.valueOf(c), "ol", linha, coluna));
                avancar();
            }
            else if (c == ':') {
                tokensLista.add(new Token(String.valueOf(c), "om", linha, coluna));
                avancar();
            }
            else {
                Token op = reconhecerOperador();
                if (op != null) {
                    tokensLista.add(op);
                } else {
                    System.err.println("Erro léxico na linha " + linha + ", coluna " + coluna +
                            ": caractere '" + c + "' não reconhecido");
                    avancar();
                }
            }
        }

        return tokensLista;
    }

    public void salvarSaida(String nomeArquivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (Token token : tokensLista) {
                writer.write(token.toString());
                writer.newLine();
            }
        }
    }

    public static String preprocessar(String codigo) {
        StringBuilder resultado = new StringBuilder();
        int i = 0;

        while (i < codigo.length()) {
            char c = codigo.charAt(i);

            if (c == '/' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '/') {
                while (i < codigo.length() && codigo.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            if (c == '/' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '*') {
                i += 2;
                while (i < codigo.length() - 1) {
                    if (codigo.charAt(i) == '*' && codigo.charAt(i + 1) == '/') {
                        i += 2;
                        break;
                    }
                    i++;
                }
                continue;
            }

            if (Character.isWhitespace(c)) {
                if (resultado.length() > 0 && !Character.isWhitespace(resultado.charAt(resultado.length() - 1))) {
                    resultado.append(' ');
                }
                i++;
                continue;
            }

            resultado.append(c);
            i++;
        }

        return resultado.toString().trim();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java AnalisadorLexico <arquivo_entrada> [arquivo_saida]");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args.length > 1 ? args[1] : "saida_tokens.txt";

        try {
            StringBuilder conteudo = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivoEntrada))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    conteudo.append(linha).append("\n");
                }
            }

            String codigoOriginal = conteudo.toString();

            System.out.println("=== PASSO 1: Pré-processamento ===");
            String codigoPreprocessado = preprocessar(codigoOriginal);
            System.out.println("Código sem comentários e espaços excessivos:");
            System.out.println(codigoPreprocessado);
            System.out.println();

            System.out.println("=== PASSO 2: Análise Léxica ===");
            AnalisadorLexico analisador = new AnalisadorLexico(codigoPreprocessado);
            List<Token> tokens = analisador.analisar();

            System.out.println("Tokens reconhecidos:");
            for (Token token : tokens) {
                System.out.println(token);
            }

            analisador.salvarSaida(arquivoSaida);
            System.out.println("\nSaída salva em: " + arquivoSaida);

        } catch (IOException e) {
            System.err.println("Erro ao ler/escrever arquivo: " + e.getMessage());
        }
    }
}