package br.com.outsera.golden_raspberry_awards.config;

import br.com.outsera.golden_raspberry_awards.model.Movie;
import br.com.outsera.golden_raspberry_awards.service.MovieService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private static final String REGEX_SEPARATED_QUOTES_AND_WITHE_SPACE = "(,| and )\\s*";

    private final MovieService movieService;
    private final String csvPath;
    private final String delimiter;
    private final char quoteChar;

    public DataLoader(MovieService movieService,
                      @Value("${app.data.csv.path}") String csvPath,
                      @Value("${app.data.csv.delimiter:;}") String delimiter,
                      @Value("${app.data.csv.quote-char:\"}") char quoteChar) {
        this.movieService = movieService;
        this.csvPath = csvPath;
        this.delimiter = delimiter;
        this.quoteChar = quoteChar;
    }

    // Carrega os dados do arquivo CSV.
    @PostConstruct
    public void loadData() throws Exception {
        log.info("Carregando dados de filmes do arquivo CSV: {}", csvPath);
        List<Movie> movies = readMoviesFromCSV(new ClassPathResource(csvPath.replace("classpath:", "")));
        movieService.saveAll(movies);
        log.info("Quantidade de filmes carregados do arquivo CSV: {}", movies.size());
    }

    // Leitura do arquivo CSV.
    private List<Movie> readMoviesFromCSV(Resource resource) throws Exception {
        List<Movie> movies = new ArrayList<>();
        // Abre o arquivo CSV.
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            // Se for nula, retorna a lista vazia.
            String line = br.readLine();
            if (line == null) {
                return movies;
            }

            // Pula a primeira linha do arquivo CSV.
            while ((line = br.readLine()) != null) {

                // Verifica se a linha esta vazia.
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Separa os valores da linha.
                String[] values = parseCSVLine(line, delimiter, quoteChar);
                // Verifica se a linha tem pelo menos 5 valores.
                if (values.length >= 5) {
                    try {
                        Movie movie = new Movie();
                        movie.setYear(Integer.parseInt(values[0].trim()));
                        movie.setTitle(values[1].trim());
                        movie.setStudios(values[2].trim());
                        movie.setProducers(parseProducers(values[3]));
                        movie.setWinner("yes".equalsIgnoreCase(values[4].trim()));
                        movies.add(movie);
                    } catch (Exception e) {
                        log.error("Erro ao analisar a linha: {}", line, e);
                    }
                }
            }
        }
        return movies;
    }

    // Separa os valores da linha.
    private String[] parseCSVLine(String line, String delimiter, char quoteChar) {
        log.info("Iniciando a separação da linha: {}", line);
        List<String> values = new ArrayList<>();
        // Verifica se a linha esta vazia.
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        // Percorre a linha.
        for (char c : line.toCharArray()) {
            // Verifica se esta entre aspas e ignora.
            if (c == quoteChar) {
                // Inverte o estado.
                inQuotes = !inQuotes;
                // Se for aspas duplas, adiciona mais um.
            } else if (c == delimiter.charAt(0) && !inQuotes) {
                // Adiciona o valor.
                values.add(sb.toString().trim());
                // Limpa o buffer.
                sb = new StringBuilder();
            } else {
                // Adiciona o caractere.
                sb.append(c);
            }
        }
        // Adiciona o valor.
        values.add(sb.toString().trim());
        // Retorna os valores.
        log.info("Retornando valores separados: {}", values);
        return values.toArray(new String[0]);
    }

    // Separa os produtores para salvar no banco de dados.
    private List<String> parseProducers(String producersStr) {
        // Remove aspas e espaços em branco.
        return Arrays.stream(producersStr.split(REGEX_SEPARATED_QUOTES_AND_WITHE_SPACE))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
