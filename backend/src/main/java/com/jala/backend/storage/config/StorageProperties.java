package com.jala.backend.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds Supabase Storage configuration from application.yml / environment variables.
 *
 * Expected properties:
 *   supabase.url
 *   supabase.service-role-key
 *   supabase.storage-bucket
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "supabase")
public class StorageProperties {

    /**
     * Base URL of the Supabase project, e.g. https://xxxxxxxx.supabase.co
     */
    private String url;

    /**
     * Supabase service role key used to authenticate storage requests.
     */
    private String serviceRoleKey;

    /**
     * Name of the Supabase Storage bucket used for JALA file uploads.
     */
    private String storageBucket;
}
