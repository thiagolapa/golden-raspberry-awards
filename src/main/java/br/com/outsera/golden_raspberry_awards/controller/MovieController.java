package br.com.outsera.golden_raspberry_awards.controller;

import br.com.outsera.golden_raspberry_awards.dto.ProducerIntervalResponse;
import br.com.outsera.golden_raspberry_awards.service.ProducerIntervalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final ProducerIntervalService producerIntervalService;

    @Autowired
    public MovieController(ProducerIntervalService producerIntervalService) {
        this.producerIntervalService = producerIntervalService;
    }

    @GetMapping("/producers-intervals")
    public ResponseEntity<ProducerIntervalResponse> getProducersIntervals() {
        ProducerIntervalResponse result = producerIntervalService.getProducersIntervals();
        return ResponseEntity.ok(result);
    }
}
