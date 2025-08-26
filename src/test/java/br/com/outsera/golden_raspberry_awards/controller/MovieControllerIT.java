package br.com.outsera.golden_raspberry_awards.controller;

import br.com.outsera.golden_raspberry_awards.model.Movie;
import br.com.outsera.golden_raspberry_awards.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovieControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		movieRepository.deleteAll();
		loadTestData();
	}

	private void loadTestData() {
		List<Movie> movies = List.of(
				createMovie(1980, "Filme 1", "Estúdio 1", List.of("Produtora A", "Produtora B"), true),
				createMovie(1981, "Filme 2", "Estúdio 2", List.of("Produtora A", "Produtora C"), true),
				createMovie(1985, "Filme 3", "Estúdio 3", List.of("Produtora B"), true),
				createMovie(1990, "Filme 4", "Estúdio 4", List.of("Produtora A"), true),
				createMovie(1991, "Filme 5", "Estúdio 5", List.of("Produtora C"), true),
				createMovie(1995, "Filme 6", "Estúdio 6", List.of("Produtora D"), true),
				createMovie(2000, "Filme 7", "Estúdio 7", List.of("Produtora C"), true),
				createMovie(2005, "Filme 8", "Estúdio 8", List.of("Produtora D"), true),
				createMovie(2010, "Filme 9", "Estúdio 9", List.of("Produtora E"), true),
				createMovie(2015, "Filme 10", "Estúdio 10", List.of("Produtora E"), true),
				createMovie(2020, "Filme 11", "Estúdio 11", List.of("Produtora F"), true),
				createMovie(2021, "Filme 12", "Estúdio 12", List.of("Produtora F"), true),
				createMovie(2022, "Filme 13", "Estúdio 13", List.of("Produtora G"), true),
				createMovie(2023, "Filme 14", "Estúdio 14", List.of("Produtora G"), true)
		);
		movieRepository.saveAll(movies);
	}

	private Movie createMovie(int year, String title, String studios, List<String> producers, boolean winner) {
		Movie movie = new Movie();
		movie.setYear(year);
		movie.setTitle(title);
		movie.setStudios(studios);
		movie.setProducers(producers);
		movie.setWinner(winner);
		return movie;
	}

	@Test
	void deveRetornarIntervalosMinimoEMaximo() throws Exception {
		mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.min", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.max", hasSize(greaterThanOrEqualTo(1))));
	}

	@Test
	void deveRetornarIntervaloMinCorreto() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content).contains("\"interval\":1"); // Produtora F tem 1 ano de intervalo.
	}

	@Test
	void deveRetornarOIntervaloMaximoCorreto() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content).contains("\"interval\":10"); // Produtora E tem 5 anos de intervalo.
	}

	@Test
	void deveRetornarVariosProdutoresComOMesmoIntervalo() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();

		// Verifique se vários produtores podem ter o mesmo intervalo mínimo
		assertThat(content).contains("\"producer\":\"Produtora F\"")
				.contains("\"interval\":1")
				.contains("\"producer\":\"Produtora G\"")
				.contains("\"interval\":1");
	}

	@Test
	void deveManipularCenarioSemDados() throws Exception {
		// Limpar todos os dados de teste
		movieRepository.deleteAll();

		mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.min", hasSize(0)))
				.andExpect(jsonPath("$.max", hasSize(0)));
	}

	@Test
	void deveRetornarAEstruturaJSONCorreta() throws Exception {
		mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.min").isArray())
				.andExpect(jsonPath("$.max").isArray())
				.andExpect(jsonPath("$.min[0].producer").isString())
				.andExpect(jsonPath("$.min[0].interval").isNumber())
				.andExpect(jsonPath("$.min[0].previousWin").isNumber())
				.andExpect(jsonPath("$.min[0].followingWin").isNumber());
	}

	@Test
	void naoDeveIncluirFilmesNaoVencedores() throws Exception {
		// Adicione um filme não vencedor
		Movie nonWinningMovie = createMovie(2024, "Non-Winning Movie", "Estúdio X",
				List.of("Produtora X"), false);
		movieRepository.save(nonWinningMovie);

		MvcResult result = mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content).doesNotContain("Produtora X");
	}

	@Test
	void deveLidarComCenarioDeVitoriaUnica() throws Exception {
		// Limpe os dados existentes e adicione apenas um filme vencedor
		movieRepository.deleteAll();
		Movie singleWin = createMovie(2024, "Single Winner", "Estúdio Y",
				List.of("Produtora Y"), true);
		movieRepository.save(singleWin);

		mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.min", hasSize(0)))
				.andExpect(jsonPath("$.max", hasSize(0)));
	}

	@Test
	void deveLidarComVariasVitoriasNoMesmoAno() throws Exception {
		// Limpar dados existentes
		movieRepository.deleteAll();

		// Adicione vários vencedores do mesmo ano
		List<Movie> sameYearWins = List.of(
				createMovie(2024, "Filme A", "Estúdio A", List.of("Produtora Z"), true),
				createMovie(2024, "Filme B", "Estúdio B", List.of("Produtora Z"), true)
		);
		movieRepository.saveAll(sameYearWins);

		// Adicione outra vitória em um ano diferente
		Movie laterWin = createMovie(2025, "Filme C", "Estúdio C",
				List.of("Produtora Z"), true);
		movieRepository.save(laterWin);

		MvcResult result = mockMvc.perform(get("/api/movies/producers-intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content).contains("\"producer\":\"Produtora Z\"")
				.contains("\"interval\":1");
	}
}
