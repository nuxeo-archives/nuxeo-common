/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.common.xmap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class XMethodAccessor implements XAccessor {

    protected final Method setter;

    protected final Field field;

    protected final Method getter;

    protected final Class<?> klass;

    public XMethodAccessor(Method method, Class<?> klass) {
        setter = method;
        setter.setAccessible(true);
        //
        this.klass = klass;
        field = guessMemberField();
        getter = guessGetter();
    }

    @Override
    public Class<?> getType() {
        return setter.getParameterTypes()[0];
    }

    @Override
    public Class<?> getMemberType() {
        if (field != null) {
            return field.getType();
        }
        if (getter != null) {
            return getter.getReturnType();
        }
        return Object.class;
    }

    @Override
    public void setValue(Object instance, Object value) {
        try {
            setter.invoke(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return setter.toString();
    }

    @Override
    public Object getValue(Object instance) {
        if (getter == null) {
            return null;
        }
        try {
            return getter.invoke(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalArgumentException(e);
        }
    }

    protected Field guessMemberField() {
        String fieldName = fieldName();
        if (fieldName == null) {
            return null;
        }
        try {
            return klass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException e) {
            return null;
        }
    }

    private String fieldName() {
        String setterName = setter.getName().toLowerCase();
        int index = setterName.indexOf("set");
        if (index == -1) {
            return null;
        }
        return WordUtils.uncapitalize(setter.getName().substring(index + 3));
    }

    protected Method guessGetter() {
        String fieldName = field != null ? field.getName() : fieldName();
        Class<?>[] classes = setter.getParameterTypes();
        Class<?> clazz = classes[0];
        String prefix;
        // compute the getter name
        if (clazz == Boolean.class || clazz == Boolean.TYPE) {
            prefix = "is";
        } else {
            prefix = "get";
        }
        String getterName = prefix + WordUtils.capitalize(fieldName);
        try {
            return klass.getMethod(getterName, new Class[0]);
        } catch (NoSuchMethodException | SecurityException e) {
            LogFactory.getLog(XMethodAccessor.class).warn(
                    "Cannot guess getter of field " + fieldName + " in class "
                            + clazz.getName());
            return null;
        }
    }

}
