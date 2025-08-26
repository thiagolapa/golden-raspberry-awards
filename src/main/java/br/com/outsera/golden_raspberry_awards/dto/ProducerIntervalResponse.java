package br.com.outsera.golden_raspberry_awards.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProducerIntervalResponse {
    
    private List<ProducerIntervalDTO> min;
    
    private List<ProducerIntervalDTO> max;
    

}