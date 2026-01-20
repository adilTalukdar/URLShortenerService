package com.adil.URLShortnerService.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortenUrlRequest {

    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String url;
}
