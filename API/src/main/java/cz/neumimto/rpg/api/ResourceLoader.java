package cz.neumimto.rpg.api;

import javax.naming.ConfigurationException;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Map;

public interface ResourceLoader {

    void init();

    void loadJarFile(File f, boolean main);

    Object loadClass(Class<?> clazz) throws IllegalAccessException, InstantiationException, ConfigurationException;

    URLClassLoader getConfigClassLoader();

    Map<String, URLClassLoader> getClassLoaderMap();

    void reloadLocalizations(Locale locale);

    void loadExternalJars();

    void initializeComponents();

    //Set<RpgAddon> discoverGuiceModules();

    @Retention(RetentionPolicy.RUNTIME)
    @interface Skill {

        String value();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Command {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface ModelMapper {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface ListenerClass {

    }
}
