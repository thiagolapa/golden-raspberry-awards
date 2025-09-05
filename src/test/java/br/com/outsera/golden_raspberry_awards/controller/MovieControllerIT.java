package br.com.outsera.golden_raspberry_awards.controller;

import br.com.outsera.golden_raspberry_awards.config.DataLoader;
import br.com.outsera.golden_raspberry_awards.dto.ProducerIntervalDTO;
import br.com.outsera.golden_raspberry_awards.dto.ProducerIntervalResponse;
import br.com.outsera.golden_raspberry_awards.repository.MovieRepository;
import br.com.outsera.golden_raspberry_awards.service.ProducerIntervalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovieControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DataLoader dataLoader;

	@Autowired
	private ProducerIntervalService intervalService;

	@BeforeEach
	void setUp() throws Exception {
		// Limpa e recarrega os dados de teste
		movieRepository.deleteAll();
		dataLoader.loadData();
	}

	@Test
	void deveRetornarIntervalosMinimoEMaximo() throws Exception {
		// 1. Executa a requisição HTTP
		ResponseEntity<ProducerIntervalResponse> response = restTemplate.exchange(
				"/api/movies/producers-intervals",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				}
		);

		// 2. Verifica respostas se estão de acordo com o arquivo.
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(Objects.requireNonNull(response.getBody()).getMax().stream().map(ProducerIntervalDTO::getProducer).findFirst()
				.orElseThrow()).isEqualTo("Matthew Vaughn");
		assertThat(Objects.requireNonNull(response.getBody()).getMax().stream().map(ProducerIntervalDTO::getInterval).findFirst()
				.orElseThrow()).isEqualTo(13);
		assertThat(Objects.requireNonNull(response.getBody()).getMax().stream().map(ProducerIntervalDTO::getPreviousWin).findFirst()
				.orElseThrow()).isEqualTo(2002);
		assertThat(Objects.requireNonNull(response.getBody()).getMax().stream().map(ProducerIntervalDTO::getFollowingWin).findFirst()
				.orElseThrow()).isEqualTo(2015);
		assertThat(Objects.requireNonNull(response.getBody()).getMin().stream().map(ProducerIntervalDTO::getProducer).findFirst()
				.orElseThrow()).isEqualTo("Joel Silver");
		assertThat(Objects.requireNonNull(response.getBody()).getMin().stream().map(ProducerIntervalDTO::getInterval).findFirst()
				.orElseThrow()).isEqualTo(1);
		assertThat(Objects.requireNonNull(response.getBody()).getMin().stream().map(ProducerIntervalDTO::getPreviousWin).findFirst()
				.orElseThrow()).isEqualTo(1990);
		assertThat(Objects.requireNonNull(response.getBody()).getMin().stream().map(ProducerIntervalDTO::getFollowingWin).findFirst()
				.orElseThrow()).isEqualTo(1991);

	}

	@Test
	void deveValidarSeOsResultadosSaoValidos() {
		// 1. Executa a requisição HTTP
		ResponseEntity<ProducerIntervalResponse> response = restTemplate.exchange(
				"/api/movies/producers-intervals",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<ProducerIntervalResponse>() {}
		);

		// 2. Verifica o status da resposta
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// 3. Obtém a resposta
		ProducerIntervalResponse apiResponse = response.getBody();
		assertThat(apiResponse).isNotNull();

		// 4. Verifica se as listas não estão vazias
		assertThat(apiResponse.getMin()).isNotEmpty();
		assertThat(apiResponse.getMax()).isNotEmpty();

		// 5. Verifica se os intervalos são positivos
		apiResponse.getMin().forEach(interval -> {
			assertThat(interval.getInterval()).isPositive();
		});
		apiResponse.getMax().forEach(interval -> {
			assertThat(interval.getInterval()).isPositive();
		});
	}

	@Test
	void deveRetornarResultadosConsistentesComAEstrutura() {
		// Executa o serviço diretamente
		ProducerIntervalResponse serviceResponse = intervalService.getProducersIntervals();

		// Executa a API
		ResponseEntity<ProducerIntervalResponse> apiResponse = restTemplate.exchange(
				"/api/movies/producers-intervals",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<ProducerIntervalResponse>() {}
		);

		// Verifica se as respostas têm a mesma estrutura
		assertThat(apiResponse.getBody()).isNotNull();
		assertThat(apiResponse.getBody().getMin()).hasSameSizeAs(serviceResponse.getMin());
		assertThat(apiResponse.getBody().getMax()).hasSameSizeAs(serviceResponse.getMax());
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
	void deveRetornarAEstruturaJsonCorreta() throws Exception {
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
}
