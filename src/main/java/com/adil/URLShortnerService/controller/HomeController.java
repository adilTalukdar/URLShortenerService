package com.adil.URLShortnerService.controller;

import com.adil.URLShortnerService.dto.ShortenUrlRequest;
import com.adil.URLShortnerService.dto.ShortenUrlResponse;
import com.adil.URLShortnerService.entity.ShortUrl;
import com.adil.URLShortnerService.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;

    public HomeController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("request", new ShortenUrlRequest());
        return "index";
    }

    @PostMapping("/shorten")
    public String shortenUrl(@Valid ShortenUrlRequest request, BindingResult bindingResult, 
                             Model model, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        ShortUrl shortUrl = urlShortenerService.createShortUrl(request.getUrl());
        
        String baseUrl = getBaseUrl(httpRequest);
        String shortUrlString = baseUrl + "/" + shortUrl.getShortCode();
        
        ShortenUrlResponse response = new ShortenUrlResponse(
            shortUrl.getShortCode(),
            shortUrlString,
            shortUrl.getOriginalUrl(),
            shortUrl.getCreatedAt(),
            shortUrl.getExpiresAt()
        );
        
        model.addAttribute("request", new ShortenUrlRequest());
        model.addAttribute("response", response);
        return "index";
    }

    @GetMapping("/{shortCode}")
    public String redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrl = urlShortenerService.getOriginalUrl(shortCode);
        
        if (originalUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found or expired");
        }
        
        return "redirect:" + originalUrl.get();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        url.append(contextPath);
        
        return url.toString();
    }
}
