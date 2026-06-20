package com.example.demo.service;

import com.example.demo.dto.SalasEEstacoesDisponiveisDTO;
import com.example.demo.model.EntEstacao;
import com.example.demo.model.EntSala;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiWorkspaceService {

    // ✅ Chave lida do application.yaml (gemini.api.key) — não fica mais hardcoded no código
    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Recebe a lista de SalasEEstacoesDisponiveisDTO (já retornada pelo ReservaService),
     * envia ao Gemini e retorna a sugestão de estações mais próximas no mesmo formato.
     *
     * @param salasDisponiveis  lista vinda de consultarTodasSalasEEstacoesLivres()
     * @param qtdDev            quantidade de estações do tipo "dev"
     * @param qtdDesign         quantidade de estações do tipo "design"
     * @param qtdSimples        quantidade de estações do tipo "simples"
     * @return lista filtrada pelo Gemini com as estações mais próximas
     */
    public List<SalasEEstacoesDisponiveisDTO> selecionarEstacoesParaEquipe(
            List<SalasEEstacoesDisponiveisDTO> salasDisponiveis,
            int qtdDev,
            int qtdDesign,
            int qtdSimples) throws Exception {

        // ✅ Monta a URL em tempo de execução usando a chave injetada do yaml
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;

        // 1. Converte a lista de DTOs para JSON — usa ObjectMapper para respeitar
        //    os nomes dos campos já existentes nas entidades do projeto
        String jsonEstacoes = objectMapper.writeValueAsString(salasDisponiveis);

        // 2. Monta o prompt dinâmico com as quantidades recebidas
        String prompt = "Você é um assistente que retorna apenas JSON puro, sem explicações, sem markdown, sem blocos de código.\n\n"
                + "Abaixo está um JSON com salas e suas estações de trabalho disponíveis. Cada estação possui coordenadas x (coordx) e y (coordy).\n\n"
                + "Selecione exatamente:\n"
                + "- " + qtdDev + " estações do tipo \"dev\"\n"
                + "- " + qtdDesign + " estações do tipo \"design\"\n"
                + "- " + qtdSimples + " estações do tipo \"simples\"\n\n"
                + "Regras obrigatórias:\n"
                + "1. As estações selecionadas devem estar o mais próximas possíveis entre si (minimize a distância euclidiana total).\n"
                + "2. Prefira estações da mesma sala quando possível.\n"
                + "3. Retorne APENAS um JSON, no EXATO mesmo formato do input (lista de objetos com campos 'sala' e 'estacoesDisponiveis').\n"
                + "4. Inclua no retorno APENAS as salas que tiverem pelo menos uma estação selecionada.\n"
                + "5. Dentro de cada sala, inclua APENAS as estações selecionadas.\n"
                + "6. Não adicione nenhum campo extra, não altere os valores dos campos existentes.\n\n"
                + "JSON de entrada:\n"
                + jsonEstacoes;

        // 3. Monta o body da requisição para a API do Gemini
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.1);   // mais determinístico
        generationConfig.addProperty("maxOutputTokens", 8192); // ✅ aumentado para JSONs grandes

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);
        requestBody.add("generationConfig", generationConfig);

        // 4. Executa a requisição HTTP
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // 5. Log para debug — remove em produção se desejar
        System.out.println("[Gemini] Status: " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro na API Gemini: " + response.statusCode() + " - " + response.body());
        }

        // 6. Extrai o texto da resposta da estrutura do Gemini
        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String textoResposta = responseJson
                .getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        // 7. Remove possível markdown residual (```json ... ```)
        textoResposta = textoResposta
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        System.out.println("[Gemini] Resposta: " + textoResposta);

        // 8. Desserializa o JSON de volta para List<SalasEEstacoesDisponiveisDTO>
        //    usando a mesma estrutura já existente no projeto
        return deserializarResposta(textoResposta);
    }

    /**
     * Converte o JSON retornado pelo Gemini de volta para
     * List<SalasEEstacoesDisponiveisDTO>, reconstruindo EntSala e EntEstacao
     * manualmente para garantir compatibilidade com as entidades do projeto.
     */
    private List<SalasEEstacoesDisponiveisDTO> deserializarResposta(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<SalasEEstacoesDisponiveisDTO> resultado = new ArrayList<>();

        for (JsonNode itemNode : root) {
            // --- Reconstrói EntSala ---
            JsonNode salaNode = itemNode.get("sala");
            EntSala sala = EntSala.builder()
                    .idsala(salaNode.get("idsala").asLong())
                    .endereco(salaNode.get("endereco").asText())
                    .disponibilidade(salaNode.get("disponibilidade").asBoolean())
                    .lot_max(salaNode.get("lot_max").asInt())
                    .build();

            // --- Reconstrói lista de EntEstacao ---
            List<EntEstacao> estacoes = new ArrayList<>();
            JsonNode estacoesNode = itemNode.get("estacoesDisponiveis");
            if (estacoesNode != null && estacoesNode.isArray()) {
                for (JsonNode estacaoNode : estacoesNode) {
                    EntEstacao estacao = EntEstacao.builder()
                            .idestacao(estacaoNode.get("idestacao").asLong())
                            .idsala(estacaoNode.get("idsala").asLong())
                            .descricao(estacaoNode.get("descricao").asText())
                            .coordx(estacaoNode.get("coordx").asInt())
                            .coordy(estacaoNode.get("coordy").asInt())
                            .build();
                    estacoes.add(estacao);
                }
            }

            resultado.add(new SalasEEstacoesDisponiveisDTO(sala, estacoes));
        }

        return resultado;
    }
}
