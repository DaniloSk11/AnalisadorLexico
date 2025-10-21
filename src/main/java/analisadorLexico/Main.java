package analisadorLexico;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String arquivoEntrada = "src/teste.txt";
        String arquivoSaida = "saida_tokens.txt";

        System.out.println("=== ANALISADOR LÉXICO ===\n");

        try {
            File arquivo = new File(arquivoEntrada);
            if (!arquivo.exists()) {
                arquivoEntrada = "teste.txt";
                arquivo = new File(arquivoEntrada);
                if (!arquivo.exists()) {
                    System.err.println("ERRO: Arquivo 'teste.txt' não encontrado!");
                    System.err.println("Caminho atual: " + new File(".").getAbsolutePath());
                    System.err.println("\nTentei procurar em:");
                    System.err.println("  - src/teste.txt");
                    System.err.println("  - teste.txt");
                    System.err.println("\nColoque o arquivo 'teste.txt' em um desses locais.");
                    return;
                }
            }

            StringBuilder conteudo = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    conteudo.append(linha).append("\n");
                }
            }

            String codigoOriginal = conteudo.toString();

            System.out.println("PASSO 1: Pré-processamento");
            System.out.println("---------------------------");
            String codigoPreprocessado = AnalisadorLexico.preprocessar(codigoOriginal);
            System.out.println("Código sem comentários e espaços excessivos:");
            System.out.println(codigoPreprocessado);
            System.out.println();

            System.out.println("PASSO 2: Análise Léxica");
            System.out.println("------------------------");
            AnalisadorLexico analisador = new AnalisadorLexico(codigoPreprocessado);
            List<AnalisadorLexico.Token> tokens = analisador.analisar();

            System.out.println("Tokens reconhecidos:");
            for (AnalisadorLexico.Token token : tokens) {
                System.out.println(token);
            }

            analisador.salvarSaida(arquivoSaida);
            System.out.println("\n✓ Análise concluída!");
            System.out.println("✓ Saída salva em: " + arquivoSaida);
            System.out.println("✓ Total de tokens: " + tokens.size());

        } catch (IOException e) {
            System.err.println("ERRO ao ler/escrever arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}