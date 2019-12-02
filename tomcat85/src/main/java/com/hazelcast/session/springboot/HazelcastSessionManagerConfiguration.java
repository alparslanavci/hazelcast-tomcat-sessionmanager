package com.hazelcast.session.springboot;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.session.HazelcastSessionManager;
import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(
        name = "useHazelcastSessionManager",
        havingValue = "true")
public class HazelcastSessionManagerConfiguration {
    private final Log log = LogFactory.getLog(HazelcastSessionManager.class);

    @Bean
    @ConditionalOnMissingBean(type = "com.hazelcast.config.Config")
    public Config hazelcastConfig() {
        try {
            return getHazelcastConfigInClasspath();
        } catch (IllegalArgumentException e) {
            log.warn("Exception when getting the configuration from classpath", e);
        } catch (HazelcastException e) {
            log.warn("Exception when getting the configuration from classpath", e);
        }

        log.info("No suitable classpath configuration exists. Using default configuration.");

        Config config = new Config();
        config.setProperty( "hazelcast.logging.type", "slf4j" );
        config.setInstanceName("hazelcastInstance");
        return config;
    }

    private Config getHazelcastConfigInClasspath()
            throws IllegalArgumentException, HazelcastException {
        Config config = new XmlConfigBuilder().build();
        if (config.getInstanceName() == null){
            throw new IllegalArgumentException("No instance name configured.");
        }
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return Hazelcast.getOrCreateHazelcastInstance(hazelcastConfig);
    }

    @Bean(name = "hazelcastTomcatSessionManagerCustomizer")
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizeTomcat(HazelcastInstance hazelcastInstance) {
        return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {
            @Override
            public void customize(TomcatServletWebServerFactory factory) {
                factory.addContextCustomizers(new TomcatContextCustomizer() {
                    @Override
                    public void customize(Context context) {
                        HazelcastSessionManager manager = new HazelcastSessionManager();
                        manager.setSticky(false);
                        manager.setHazelcastInstanceName("hazelcastInstance");
                        context.setManager(manager);
                    }
                });
            }
        };
    }
}
