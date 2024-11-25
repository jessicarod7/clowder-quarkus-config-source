package com.redhat.cloud.common.clowder.configsource.handlers;

import com.redhat.cloud.common.clowder.configsource.ClowderConfig;
import com.redhat.cloud.common.clowder.configsource.ClowderConfigSource;

public class QuarkusRedisClowderPropertyHandler extends ClowderPropertyHandler {
    private static final String QUARKUS_REDIS = "quarkus.redis.";

    public QuarkusRedisClowderPropertyHandler(ClowderConfig clowderConfig) {
        super(clowderConfig);
    }

    @Override
    public boolean handles(String property) {
        return property.startsWith(QUARKUS_REDIS);
    }

    @Override
    public String handle(String property, ClowderConfigSource configSource) {
        if (clowderConfig.inMemoryDb == null) {
            throw new IllegalStateException("No inMemoryDb section found");
        }

        String sub = property.substring(QUARKUS_REDIS.length());

        return switch (sub) {
            case "hosts" -> "redis://" + getURIAuthority();
            case "password" ->
                    clowderConfig.inMemoryDb.password; // Note: This value will be overriden by a password provided in `quarkus.redis.hosts`.
            default ->
                    configSource.getExistingValue(property); // fallback to fetching the value from application.properties
        };
    }

    private String getURIAuthority() {
        String hosts = clowderConfig.inMemoryDb.hostname;

        if (clowderConfig.inMemoryDb.username != null && clowderConfig.inMemoryDb.password != null) {
            hosts = clowderConfig.inMemoryDb.username + ":" + clowderConfig.inMemoryDb.password + "@" + hosts;
        } else if (clowderConfig.inMemoryDb.password != null) {
            hosts = ":" + clowderConfig.inMemoryDb.password + "@" + hosts;
        } else if (clowderConfig.inMemoryDb.username != null) {
            throw new IllegalStateException("In-memory DB password must be specified if username is provided");
        }

        if (clowderConfig.inMemoryDb.port != null) {
            hosts += ":" + clowderConfig.inMemoryDb.port;
        }
        return hosts;
    }
}
