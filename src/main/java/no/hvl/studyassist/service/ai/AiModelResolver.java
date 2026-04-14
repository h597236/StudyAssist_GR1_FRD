package no.hvl.studyassist.service.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AiModelResolver {

    private final Map<String, AiModelClient> clientMap;

    public AiModelResolver(List<AiModelClient> clients) {
        this.clientMap = clients.stream()
                .collect(Collectors.toMap(AiModelClient::getModelName, c -> c));
    }

    public AiModelClient resolve(String modelName) {
        AiModelClient client = clientMap.get(modelName);
        if (client == null) {
            throw new IllegalArgumentException("Ukjent KI-modell: " + modelName);
        }
        return client;
    }

    public AiModelClient resolveOrDefault(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return clientMap.values().iterator().next();
        }
        return resolve(modelName);
    }

    public List<String> getAvailableModels() {
        return List.copyOf(clientMap.keySet());
    }
}