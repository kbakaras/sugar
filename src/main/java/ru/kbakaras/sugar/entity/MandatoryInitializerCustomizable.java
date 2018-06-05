package org.butu.sugar.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Вспомогательный класс, облегчающий создание обязательных элементов.
 * @author kirillov
 */
public abstract class MandatoryInitializerCustomizable<E extends IEntity, A extends Annotation> {
    private Map<String, A> values = new HashMap<String, A>();

    public MandatoryInitializerCustomizable(Class<?> clazz, String patternStr, Class<A> annotationClass) {
        Pattern pattern = Pattern.compile(patternStr);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType().equals(String.class)) {
                Matcher matcher = pattern.matcher(field.getName());
                if (matcher.matches()) {
                    try {
                        String value = (String) field.get(clazz);
                        A annotation = field.getAnnotation(annotationClass);
                        if (values.containsKey(value)) {
                            duplication(value);
                        } else {
                            values.put(value, annotation);
                        }
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public Map<String, A> getValues() {
        return values;
    }

    /**
     * Выполняет инициализацию (обновление) таблицы обязательными элементами
     * @param existing Список существующих в БД на данный момент элементов
     * @param doUpdate Флаг обновления описаний. Если флаг установлен, будет производиться
     * проверка и обновление описаний для существующих в БД элементов
     */
    public void initMandatory(List<E> existing, boolean doUpdate) {
        Map<String, E> map = new HashMap<String, E>(existing.size());
        for (E mandatory: existing) {
            map.put(getCode(mandatory), mandatory);
        }

        for (Map.Entry<String, A> entry: values.entrySet()) {
            E oldMandatory = map.get(entry.getKey());
            if (oldMandatory == null) {
                createNew(entry.getKey(), entry.getValue());
                //log.warn("Добавление нового элемента " + entry.getKey());
            } else if (doUpdate) {
                if (!Arrays.equals(
                		entry.getValue() != null ? getAttributes(entry.getValue()) : null, 
                		getAttributes(oldMandatory))) {
                	setAttributes(oldMandatory, entry.getValue());
                    //log.warn("Обновление атрибутов для элемента " + entry.getKey());
                }
            }
        }
    }

    protected abstract String getCode(E mandatory);
    protected abstract Object[] getAttributes(E mandatory);
    protected abstract Object[] getAttributes(A an);

    /**
     * Метод выполняющий создание, наполнение и персистирование нового полномочия.
     * @param code Ключ нового полномочия
     * @param annotation Описание для нового полномочия
     * @return Ссылку на созданный объект-сущность полномочия
     */
    protected abstract E createNew(String code, A annotation);
    protected abstract void setAttributes(E mandatory, A an);

    protected void duplication(String value) {
        //log.warn("Обнаружена константа с дублирующим ключём [" + value + "]!");
    }
}