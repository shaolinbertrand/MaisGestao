package com.gestao.agro.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiService {

    public static String gerarEstrategiaTows(String tipo, String fatorInterno, String fatorExterno) throws Exception {
        
        // 1. Carrega o .env de forma segura
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao tentar carregar o arquivo .env");
        }

        String apiKey = (dotenv != null) ? dotenv.get("GEMINI_API_KEY") : null;
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("Chave da API (GEMINI_API_KEY) não encontrada no ficheiro .env!");
        }

        // URL base exata conforme o cURL do Google AI Studio (sem a chave na URL)
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

        System.out.println("➡️ Chamando API: " + apiUrl);

        // 2. Montar o Prompt para o Gemini atuar como consultor
        String prompt = "Atue como um consultor especialista em agronegócio e cooperativismo. "
                + "Estou montando uma Matriz TOWS. Crie UMA ÚNICA ação estratégica prática, direta e viável (máximo de 2 frases) "
                + "cruzando os seguintes fatores:\n"
                + "- Tipo de Cruzamento: " + tipo + "\n"
                + "- Fator Interno: " + fatorInterno + "\n"
                + "- Fator Externo: " + fatorExterno + "\n\n"
                + "Retorne APENAS o texto da estratégia, sem introduções, sem marcadores e sem aspas.";

        // 3. Construir o corpo da requisição (JSON - igual ao 'contents' do cURL)
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObj = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textObj = new JSONObject();

        textObj.put("text", prompt);
        parts.put(textObj);
        contentObj.put("parts", parts);
        contents.put(contentObj);
        requestBody.put("contents", contents);

        // 4. Fazer a chamada HTTP para o Google (agora com o Header X-goog-api-key)
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", apiKey) // A CHAVE ENTRA AQUI, IGUAL AO cURL!
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. Ler a resposta e extrair o texto gerado
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            String gerado = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            return gerado.trim();
        } else {
            System.err.println("❌ ERRO COMPLETO DA API: " + response.body());
            throw new Exception("Erro " + response.statusCode() + " na API. Verifique o console do NetBeans (Output) para ver os detalhes.");
        }
    }
}