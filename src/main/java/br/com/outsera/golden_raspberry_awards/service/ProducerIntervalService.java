package br.com.outsera.golden_raspberry_awards.service;

import br.com.outsera.golden_raspberry_awards.dto.ProducerIntervalDTO;
import br.com.outsera.golden_raspberry_awards.dto.ProducerIntervalResponse;
import br.com.outsera.golden_raspberry_awards.model.Movie;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class ProducerIntervalService {

    private final MovieService movieService;

    // Retorna os intervalos entre os anos de vitória de cada produtor.
    public ProducerIntervalResponse getProducersIntervals() {
        // Busca os filmes vencedores da base de dados.
        List<Movie> winners = movieService.findAllWinners();
        log.info("Quantidade de filmes vencedores encontrados: {}", winners.size());

        Map<String, List<Integer>> producerWins = new HashMap<>();

        // Percorre os filmes vencedores.
        for (Movie movie : winners) {
            for (String producer : movie.getProducers()) {
                // Adiciona os anos de vitória do produtor.
                producerWins.computeIfAbsent(producer, k -> new ArrayList<>()).add(movie.getYear());
            }
        }

        log.info("Map com os produtores e o ano da vitória: {}", producerWins);

        // Percorre os produtores.
        List<ProducerIntervalDTO> intervals = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : producerWins.entrySet()) {
            // Ordena os anos de vitória.
            List<Integer> years = entry.getValue().stream().sorted().toList();
            // Verifica se o produtor tem pelo menos 2 vitórias.
            if (years.size() >= 2) {
                // Percorre os anos de vitória.
                for (int i = 1; i < years.size(); i++) {
                    // Ano anterior.
                    int previousWin = years.get(i - 1);
                    // Ano seguinte.
                    int followingWin = years.get(i);
                    // Intervalo.
                    int interval = followingWin - previousWin;

                    ProducerIntervalDTO dto = new ProducerIntervalDTO();
                    dto.setProducer(entry.getKey());
                    dto.setInterval(interval);
                    dto.setPreviousWin(previousWin);
                    dto.setFollowingWin(followingWin);
                    intervals.add(dto);
                }
            }
        }

        log.info("Lista dos produtores e os intervalos das vitórias: {}", intervals);

        // Verifica se a lista de intervalos está vazia.
        if (intervals.isEmpty()) {
            return new ProducerIntervalResponse(Collections.emptyList(), Collections.emptyList());
        }

        // Encontra o intervalo mínimo e mapeia os intervalos para inteiros.
        int minInterval = intervals.stream()
                .mapToInt(ProducerIntervalDTO::getInterval)
                .min()
                .orElse(0);

        // Encontra o intervalo máximo e mapeia os intervalos para inteiros.
        int maxInterval = intervals.stream()
                .mapToInt(ProducerIntervalDTO::getInterval)
                .max()
                .orElse(0);

        // Filtra os intervalos mínimos.
        var minIntervals = intervals.stream()
                .filter(dto -> dto.getInterval() == minInterval)
                .toList();

        // Filtra os intervalos máximos.
        var maxIntervals = intervals.stream()
                .filter(dto -> dto.getInterval() == maxInterval)
                .toList();

        log.info("Fim do mapeamento do menor {} e maior {} intervalo de prêmio.", minInterval, maxInterval);

        return new ProducerIntervalResponse(minIntervals, maxIntervals);
    }
}