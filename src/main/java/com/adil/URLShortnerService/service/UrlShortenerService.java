package com.adil.URLShortnerService.service;

import com.adil.URLShortnerService.entity.ShortUrl;
import com.adil.URLShortnerService.repository.ShortUrlRepository;
import com.adil.URLShortnerService.util.ShortCodeGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private static final int MAX_COLLISION_RETRIES = 10;

    public UrlShortenerService(ShortUrlRepository shortUrlRepository, ShortCodeGenerator shortCodeGenerator) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Transactional
    public ShortUrl createShortUrl(String originalUrl) {
        // Check if URL already exists
        Optional<ShortUrl> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        // Generate unique code (handle collisions)
        String shortCode = generateUniqueCode();

        // Create and save new ShortUrl
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);

        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public Optional<String> getOriginalUrl(String shortCode) {
        Optional<ShortUrl> shortUrl = shortUrlRepository.findByShortCode(shortCode);
        
        if (shortUrl.isEmpty()) {
            return Optional.empty();
        }

        ShortUrl url = shortUrl.get();
        
        // Check if URL has expired
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return Optional.empty();
        }

        return Optional.of(url.getOriginalUrl());
    }

    private String generateUniqueCode() {
        int attempts = 0;
        String shortCode;
        
        do {
            if (attempts >= MAX_COLLISION_RETRIES) {
                throw new RuntimeException("Unable to generate unique short code after " + MAX_COLLISION_RETRIES + " attempts");
            }
            
            shortCode = shortCodeGenerator.generate();
            attempts++;
        } while (shortUrlRepository.findByShortCode(shortCode).isPresent());
        
        return shortCode;
    }
}
