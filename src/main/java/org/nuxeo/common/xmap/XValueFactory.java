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

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

/**
 * Value factories are used to decode values from XML strings.
 * <p>
 * To register a new factory for a given XMap instance use the method
 * {@link XMap#setValueFactory(Class, XValueFactory)}.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public abstract class XValueFactory<T> {

    private static final Log log = LogFactory.getLog(XValueFactory.class);

    static final Map<Class<?>, XValueFactory<?>> defaultFactories = new Hashtable<Class<?>, XValueFactory<?>>();

	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		ParameterizedType type = (ParameterizedType)getClass().getGenericSuperclass();
		return (Class<T>)type.getActualTypeArguments()[0];
	}

    public abstract  T deserialize(Context context, String value);

    public abstract String serialize(Context context, T value);

    public final T getElementValue(Context context, Node element,
            boolean trim) {
        String text = element.getTextContent();
        return deserialize(context, trim ? text.trim() : text);
    }

    public final T getAttributeValue(Context context, Node element,
            String name) {
        Node at = element.getAttributes().getNamedItem(name);
        return at != null ? deserialize(context, at.getNodeValue()) : null;
    }

    public static <T> void addFactory(Class<T> klass, XValueFactory<T> factory) {
        defaultFactories.put(klass, factory);
    }

    @SuppressWarnings("unchecked")
	public static <T> XValueFactory<T> getFactory(Class<T> type) {
        return (XValueFactory<T>)defaultFactories.get(type);
    }

    public static <T> T getValue(Context context, Class<T> klass, String value) {
        XValueFactory<T> factory = getFactory(klass);
        if (factory == null) {
            return null;
        }
        return factory.deserialize(context, value);
    }

    public static final XValueFactory<String> STRING = new XValueFactory<String>() {
        @Override
        public String deserialize(Context context, String value) {
            return value;
        }

        @Override
        public String serialize(Context context, String value) {
            return value.toString();
        }

    };

    public static final XValueFactory<Integer> INTEGER = new XValueFactory<Integer>() {
        @Override
        public Integer deserialize(Context context, String value) {
            return Integer.valueOf(value);
        }

        @Override
        public String serialize(Context context, Integer value) {
            return Integer.toString(value);
        }

		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}

    };

    public static final XValueFactory<Long> LONG = new XValueFactory<Long>() {
        @Override
        public Long deserialize(Context context, String value) {
            return Long.valueOf(value);
        }

        @Override
        public String serialize(Context context, Long value) {
            return Long.toString(value);
        }

		@Override
		public Class<Long> getType() {
			return Long.class;
		}
    };

    public static final XValueFactory<Double> DOUBLE = new XValueFactory<Double>() {
        @Override
        public Double deserialize(Context context, String value) {
            return Double.valueOf(value);
        }

        @Override
        public String serialize(Context context, Double value) {
            return Double.toString(value);
        }

		@Override
		public Class<Double> getType() {
			return Double.class;
		}
    };

    public static final XValueFactory<Float> FLOAT = new XValueFactory<Float>() {
        @Override
        public Float deserialize(Context context, String value) {
            return Float.valueOf(value);
        }

        @Override
        public String serialize(Context context, Float value) {
            return Float.toString(value);
        }

		@Override
		public Class<Float> getType() {
			return Float.class;
		}
    };

    public static final XValueFactory<Boolean> BOOLEAN = new XValueFactory<Boolean>() {
        @Override
        public Boolean deserialize(Context context, String value) {
            return Boolean.valueOf(value);
        }

        @Override
        public String serialize(Context context, Boolean value) {
            return Boolean.toString(value);
        }

		@Override
		public Class<Boolean> getType() {
			return Boolean.class;
		}
    };

    public static final XValueFactory<Date> DATE = new XValueFactory<Date>() {
        private final DateFormat df = DateFormat.getDateInstance();

        @Override
        public Date deserialize(Context context, String value) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                return null;
            }
        }

        @Override
        public String serialize(Context context, Date value) {
            Date date = value;
            return df.format(date);
        }

		@Override
		public Class<Date> getType() {
			return Date.class;
		}
    };

    public static final XValueFactory<File> FILE = new XValueFactory<File>() {
        @Override
        public File deserialize(Context context, String value) {
            return new File(value);
        }

        @Override
        public String serialize(Context context, File value) {
            File file = value;
            return file.getName();
        }

		@Override
		public Class<File> getType() {
			return File.class;
		}
    };

    public static final XValueFactory<URL> URL = new XValueFactory<URL>() {
        @Override
        public URL deserialize(Context context, String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        @Override
        public String serialize(Context context, URL value) {
            return value.toExternalForm();
        }

		@Override
		public Class<java.net.URL> getType() {
			return URL.class;
		}
    };

    @SuppressWarnings("rawtypes")
	public static final XValueFactory<Class> CLASS = new XValueFactory<Class>() {
        @Override
        public Class deserialize(Context context, String value) {
            try {
                return context.loadClass(value);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load class " + value, e);
            }
        }

        @Override
        public String serialize(Context context, Class value) {
            Class<?> clazz = value;
            return clazz.getName();
        }

        @Override
        public Class<Class> getType() {
            return Class.class;
        }
    };

    public static final XValueFactory<Resource> RESOURCE = new XValueFactory<Resource>() {
        @Override
        public Resource deserialize(Context context, String value) {
            return new Resource(context.getResource(value));
        }

        @Override
        public String serialize(Context context, Resource value) {
            return value.toString();
        }

		@Override
		public Class<Resource> getType() {
			return Resource.class;
		}
    };

    static {
        addFactory(String.class, STRING);
        addFactory(Integer.class, INTEGER);
        addFactory(Long.class, LONG);
        addFactory(Double.class, DOUBLE);
        addFactory(Date.class, DATE);
        addFactory(Boolean.class, BOOLEAN);
        addFactory(File.class, FILE);
        addFactory(URL.class, URL);

        addFactory(int.class, INTEGER);
        addFactory(long.class, LONG);
        addFactory(double.class, DOUBLE);
        addFactory(float.class, FLOAT);
        addFactory(boolean.class, BOOLEAN);

        addFactory(Class.class, CLASS);
        addFactory(Resource.class, RESOURCE);
    }


}
