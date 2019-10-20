package ru.kbakaras.jpa.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.kbakaras.jpa.Regset;
import ru.kbakaras.sugar.entity.IReg;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class RegsetRepositoryImpl<R extends IReg> implements RegsetRepository<R> {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void save(Regset<R> regset) {
        if (regset.hasDeleted()) {
            for (R reg: regset.getDeleted()) {
                em.remove(em.merge(reg));
            }
        }
        if (!regset.isEmpty()) {
            for (R reg: regset) {
                if (reg.getId() == null) {
                    em.persist(reg);
                } else {
                    em.merge(reg);
                }
            }
        }
    }

    @Override
    @Transactional
    public void save(Regset<R> regset, int portion) {
        if (regset.hasDeleted()) {
            for (R reg: regset.getDeleted()) {
                em.remove(em.merge(reg));
            }
        }

        if (!regset.isEmpty()) {
            int i = 0;
            for (R reg: regset) {
                if (i >= portion) {
                    i = 0;
                    em.flush();
                    em.clear();
                } else {
                    i++;
                }

                if (reg.getId() == null) {
                    em.persist(reg);
                } else {
                    em.merge(reg);
                }
            }
        }
    }
}
