package com.adil.URLShortnerService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/urlshortener}")
    private String defaultUrl;

    @Value("${spring.datasource.username:root}")
    private String defaultUsername;

    @Value("${spring.datasource.password:root}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSourceProperties properties = new DataSourceProperties();
        
        // Priority 1: Parse MYSQL_PUBLIC_URL if available (Railway)
        String mysqlPublicUrl = System.getenv("MYSQL_PUBLIC_URL");
        if (mysqlPublicUrl != null && !mysqlPublicUrl.isEmpty()) {
            try {
                // Replace mysql:// with http:// temporarily for URI parsing
                String urlForParsing = mysqlPublicUrl.replaceFirst("^mysql://", "http://");
                URI uri = new URI(urlForParsing);
                
                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 3306;
                String database = uri.getPath().replaceFirst("^/", "");
                
                String[] userInfo = uri.getUserInfo().split(":", 2);
                String username = userInfo.length > 0 ? userInfo[0] : "root";
                String password = userInfo.length > 1 ? userInfo[1] : "";
                
                // Build JDBC URL
                String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=true&requireSSL=false&allowPublicKeyRetrieval=true", 
                    host, port, database);
                
                properties.setUrl(jdbcUrl);
                properties.setUsername(username);
                properties.setPassword(password);
                
                return properties.initializeDataSourceBuilder().build();
            } catch (Exception e) {
                System.err.println("Failed to parse MYSQL_PUBLIC_URL: " + e.getMessage());
                // Fall through to individual variables
            }
        }
        
        // Priority 2: Use individual environment variables if available
        String mysqlUser = System.getenv("MYSQLUSER");
        String mysqlPassword = System.getenv("MYSQLPASSWORD");
        String mysqlDatabase = System.getenv("MYSQL_DATABASE");
        String mysqlPort = System.getenv("MYSQLPORT");
        
        if (mysqlUser != null && !mysqlUser.isEmpty() && 
            mysqlPassword != null && !mysqlPassword.isEmpty() &&
            mysqlDatabase != null && !mysqlDatabase.isEmpty()) {
            
            // Try to get host from MYSQLHOST or extract from MYSQL_URL
            String host = System.getenv("MYSQLHOST");
            if (host == null || host.isEmpty()) {
                String mysqlUrl = System.getenv("MYSQL_URL");
                if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
                    try {
                        String urlForParsing = mysqlUrl.replaceFirst("^mysql://", "http://");
                        URI uri = new URI(urlForParsing);
                        host = uri.getHost();
                    } catch (Exception e) {
                        host = "localhost";
                    }
                } else {
                    host = "localhost";
                }
            }
            
            int port = 3306;
            if (mysqlPort != null && !mysqlPort.isEmpty()) {
                try {
                    port = Integer.parseInt(mysqlPort);
                } catch (NumberFormatException e) {
                    port = 3306;
                }
            }
            
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=true&requireSSL=false&allowPublicKeyRetrieval=true", 
                host, port, mysqlDatabase);
            
            properties.setUrl(jdbcUrl);
            properties.setUsername(mysqlUser);
            properties.setPassword(mysqlPassword);
            
            return properties.initializeDataSourceBuilder().build();
        }
        
        // Priority 3: Use application.properties defaults
        properties.setUrl(defaultUrl + "?useSSL=true&requireSSL=false&allowPublicKeyRetrieval=true");
        properties.setUsername(defaultUsername);
        properties.setPassword(defaultPassword);
        
        return properties.initializeDataSourceBuilder().build();
    }
}
