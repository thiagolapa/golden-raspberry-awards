package br.com.outsera.golden_raspberry_awards.service;

import br.com.outsera.golden_raspberry_awards.model.Movie;
import br.com.outsera.golden_raspberry_awards.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    @Transactional
    public void saveAll(List<Movie> movies) {
        movieRepository.saveAll(movies);
    }

    public List<Movie> findAllWinners() {
        return movieRepository.findByWinnerTrueOrderByYearAsc();
    }
}
