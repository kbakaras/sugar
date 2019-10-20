package ru.kbakaras.jpa.repository;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SugarRepositoryExe<T, ID>
        extends SimpleJpaRepository<T, ID>
        implements  SugarRepository<T, ID> {

    private EntityManager em;

    public SugarRepositoryExe(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
    }

    public SugarRepositoryExe(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <D extends T> D findCreate(ID id, Supplier<D> instanceSupplier, Consumer<D> instanceSetup) {
        return findById(id).map(element -> (D) element)
                .orElseGet(() -> {
                    D instance = instanceSupplier.get();

                    Class<?> idClass = em.getMetamodel().entity(instance.getClass()).getIdType().getJavaType();
                    Field field = (Field) em.getMetamodel().entity(instance.getClass()).getId(idClass).getJavaMember();
                    try {
                        field.set(instance, id);
                        instanceSetup.accept(instance);
                        return save(instance);

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}