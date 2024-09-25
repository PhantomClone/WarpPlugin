package me.phantomclone.warpplugin.injection;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PhoenixDependencyInjector {

    private final String injectorConfigFileName;
    private final Set<Object> instances;

    public PhoenixDependencyInjector(String injectorConfigFileName) {
        this.injectorConfigFileName = injectorConfigFileName;
        this.instances = new HashSet<>();
    }

    public void load(File configFolder, Object startInstance) {
        String packageName = startInstance.getClass().getPackage().getName();
        load(packageName, configFolder, startInstance);
    }

    public void load(String packageName, File configFolder, Object startInstance) {
        Map<String, Object> config = loadConfig(configFolder);

        instances.add(startInstance);

        Reflections reflections = scanPackages(packageName);

        loadMethodBeans(reflections);
        loadTypBeans(reflections);
        injectAutowire(reflections);
        injectConfig(config, reflections);
        injectPostConstructor(reflections);
    }

    public void clean() {
        instances.clear();
    }

    private void injectPostConstructor(Reflections reflections) {
        reflections.getMethodsAnnotatedWith(PostConstructor.class)
                .forEach(this::invokeMethod);
    }

    @NotNull
    private static Reflections scanPackages(String packageName) {
        return new Reflections(
                packageName,
                Scanners.TypesAnnotated,
                Scanners.MethodsAnnotated,
                Scanners.FieldsAnnotated
        );
    }

    private void injectConfig(Map<String, Object> config, Reflections reflections) {
        reflections.getFieldsAnnotatedWith(Configuration.class)
                .forEach(field -> handleConfigurationField(field, config));
    }

    private void injectAutowire(Reflections reflections) {
        reflections.getFieldsAnnotatedWith(Autowire.class)
                .forEach(this::handleAutowireField);
    }

    private void loadMethodBeans(Reflections reflections) {
        reflections.getMethodsAnnotatedWith(Bean.class).stream()
                .map(this::invokeMethod)
                .forEach(instances::add);
    }

    private void loadTypBeans(Reflections reflections) {
        List<Constructor<?>> list = reflections.getTypesAnnotatedWith(Bean.class).stream()
                .map(Class::getConstructors)
                .map(this::getConstructor)
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(ArrayList::new));

        tryToLoadTypBeans(list);
    }

    private void tryToLoadTypBeans(List<Constructor<?>> constructorList) {
        boolean couldFindOne = constructorList.removeIf(this::removeIfCreated);

        if (!couldFindOne) {
            throw new RuntimeException(String.format("Could not resovle all Beans! [%s] [%s]", constructorList.stream()
                    .map(constructor -> constructor.getDeclaringClass().getSimpleName())
                    .collect(Collectors.joining(", ")),
                    instances.stream().map(o -> o.getClass().getSimpleName()).collect(Collectors.joining(", "))));
        }
        if (!constructorList.isEmpty()) {
            tryToLoadTypBeans(constructorList);
        }
    }

    private boolean removeIfCreated(Constructor<?> constructor) {
        if (Arrays.stream(constructor.getParameterTypes()).allMatch(this::containsBeanWithClass)) {
            getCreateBean(constructor);
            return true;
        }

        return false;
    }

    private void handleConfigurationField(Field field, Map<String, Object> config) {
        String path = field.getAnnotation(Configuration.class).path();

        String[] split = path.split("\\.");

        Object value = getObjectFromConfig(config, split[split.length - 1], Arrays.copyOfRange(split, 0, split.length - 1));

        getOrCreate(field.getDeclaringClass())
                .ifPresentOrElse(object -> setBean(field, object, value), () -> throwException(field));
    }

    @SneakyThrows
    private Object invokeMethod(Method method) {
        return method.invoke(getBean(method.getDeclaringClass()));
    }

    @SuppressWarnings({"unchecked"})
    private Object getObjectFromConfig(Map<String, Object> config, String key, String... path) {
        for (String string : path) {
            Object object = config.get(string);
            if (object instanceof Map<?, ?>) {
                config = (Map<String, Object>) config.get(string);
            }
        }
        return config.get(key);
    }

    private void handleAutowireField(Field field) {
        getOrCreate(field.getDeclaringClass())
                .ifPresentOrElse(object -> setBean(field, object), () -> throwException(field));
    }

    private void setBean(Field field, Object bean) {
        setBean(field, bean, getBean(field.getType()));
    }

    @SneakyThrows
    private void setBean(Field field, Object bean, Object toSet) {
        if (!field.canAccess(bean)) {
            field.setAccessible(true);
            field.set(bean, toSet);
            field.setAccessible(false);
        } else {
            field.set(bean, toSet);
        }
    }

    private void throwException(Field field) {
        throw new RuntimeException(String.format("No bean found for %s", field.getDeclaringClass()));
    }

    private Object getBean(Class<?> clazz) {
        return instances.stream()
                .filter(object -> clazz.isAssignableFrom(object.getClass()))
                .findFirst().orElseThrow(() -> new RuntimeException("No bean found " + clazz.getSimpleName() +  " | " +
                        instances.stream().map(o -> o.getClass().getSimpleName()).collect(Collectors.joining(", "))));
    }

    private Optional<Object> getOrCreate(Class<?> clazz) {
        return getConstructor(clazz.getConstructors())
                .map(this::getCreateBean);
    }

    private Optional<Constructor<?>> getConstructor(Constructor<?>[] constructors) {
        return Arrays.stream(constructors)
                .findFirst();
    }

    private boolean containsBeanWithClass(Class<?> aClass) {
        return instances.stream().anyMatch(o -> o.getClass().equals(aClass) || aClass.isAssignableFrom(o.getClass()));
    }

    private Object getCreateBean(Constructor<?> constructor) {
        return instances.stream()
                .filter(object -> object.getClass().equals(constructor.getDeclaringClass()))
                .findFirst()
                .orElseGet(() -> createMissingBean(constructor));

    }

    @SneakyThrows
    private Object createMissingBean(Constructor<?> constructor) {
        Object object = constructor.newInstance(findParameters(constructor));
        instances.add(object);
        return object;
    }

    private Object[] findParameters(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameterTypes())
                .map(this::getBean)
                .toArray();
    }

    private Map<String, Object> loadConfig(File configFolder) {
        Path path = Path.of(configFolder.getPath(), injectorConfigFileName);
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
            return yaml.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
