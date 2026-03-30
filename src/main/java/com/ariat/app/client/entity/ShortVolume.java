package com.ariat.app.client.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortVolume {
    private LocalDate dateTime;
    private long shortVolume;
    private long totalVolume;
    private double shortRatio;
}
