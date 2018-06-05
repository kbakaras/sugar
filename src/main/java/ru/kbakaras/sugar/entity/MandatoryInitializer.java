package org.butu.sugar.entity;

import java.util.List;

/**
 * Вспомогательный класс, облегчающий создание обязательных элементов.
 * @author kbakaras
 */
public abstract class MandatoryInitializer<E extends IEntity> {
	
	private MandatoryInitializerCustomizable<E, Description> initializer; 
	
    public MandatoryInitializer(Class<?> clazz, String patternStr) {
    	initializer = new MandatoryInitializerCustomizable<E, Description>(clazz, patternStr, Description.class) {
    		protected E createNew(String code, Description an) {
    			return MandatoryInitializer.this.createNew(code, an.value());
    		}
			protected String getCode(E mandatory) {
				return MandatoryInitializer.this.getCode(mandatory);
			}
			protected Object[] getAttributes(E mandatory) {
				return new Object[]{MandatoryInitializer.this.getDescription(mandatory)};
			}
			protected Object[] getAttributes(Description an) {
				return new Object[]{an.value()};
			}
			protected void setAttributes(E mandatory, Description an) {
				MandatoryInitializer.this.setDescription(mandatory, an.value());
			}
		};
    }

    /**
     * Выполняет инициализацию (обновление) таблицы обязательными элементами
     * @param existing Список существующих в БД на данный момент элементов
     * @param doUpdateDescriptions Флаг обновления описаний. Если флаг установлен, будет производиться
     * проверка и обновление описаний для существующих в БД элементов
     */
    public void initMandatory(List<E> existing, boolean doUpdateDescriptions) {
        initializer.initMandatory(existing, doUpdateDescriptions);
    }

    protected abstract String getCode(E mandatory);
    protected abstract String getDescription(E mandatory);

    /**
     * Метод выполняющий создание, наполнение и персистирование нового полномочия.
     * @param code Ключ нового полномочия
     * @param description Описание для нового полномочия
     * @return Ссылку на созданный объект-сущность полномочия
     */
    protected abstract E createNew(String code, String description);
    protected abstract void setDescription(E mandatory, String description);
}