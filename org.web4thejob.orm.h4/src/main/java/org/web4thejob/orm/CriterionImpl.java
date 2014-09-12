/*
 * Copyright (c) 2012-2014 Veniamin Isaias.
 *
 * This file is part of web4thejob.
 *
 * Web4thejob is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Web4thejob is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.web4thejob.orm;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.StringUtils;
import org.web4thejob.context.ContextUtil;
import org.web4thejob.orm.query.Condition;
import org.web4thejob.orm.query.Criterion;
import org.web4thejob.orm.query.Query;

import javax.validation.constraints.NotNull;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author Veniamin Isaias
 * @since 1.0.0
 */

/*package*/class CriterionImpl extends AbstractHibernateEntity implements Criterion {
    public CriterionImpl(boolean isMaster) {
        master = isMaster;
    }

    public CriterionImpl() {
        this(false);
    }

    private final boolean master;
    private Condition condition;
    private long id;
    private int index;
    @NotNull
    private Query query;
    @NotBlank
    private String flatPropertyPath;
    private String flatValue;
    private boolean fixed;
    private PathMetadata propertyPath;
    private Object value;

    @SuppressWarnings("rawtypes")
    private Object deserializeValue(String flatValue) {
        final Unmarshaller unmarshaller = ContextUtil.getBean(Unmarshaller.class);
        try {
            Object value = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(flatValue.getBytes
                    ("UTF-8"))));
            if (value instanceof Entity) {

                try {
                    value = ContextUtil.getDRS().refresh((Entity) value);
                } catch (HibernateObjectRetrievalFailureException e) {
                    //probably the record has been deleted.
                    return null;
                }

            } else if (value instanceof List) {
                List list = (List) value;
                if (!list.isEmpty() && list.get(0) instanceof Entity) {
                    for (Object item : list) {

                        try {
                            ContextUtil.getDRS().refresh((Entity) item);
                        } catch (HibernateObjectRetrievalFailureException e) {
                            //probably the record has been deleted.
                            return list.remove(item);
                        }


                    }
                }
            }
            return value;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeValue(Object value) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Marshaller marshaller = ContextUtil.getBean(Marshaller.class);
        final Result result = new StreamResult(out);
        try {
            marshaller.marshal(value, result);
            return out.toString("UTF-8");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getFlatCondition() {
        if (condition != null) {
            return condition.getKey();
        }
        return null;
    }

    public void setFlatCondition(String flatCondition) {
        condition = Condition.fromKey(flatCondition);
    }

    public String getFlatPropertyPath() {
        if (flatPropertyPath == null && propertyPath != null) {
            flatPropertyPath = propertyPath.getPath();
        }
        return flatPropertyPath;
    }

    public void setFlatPropertyPath(String flatPropertyPath) {
        this.flatPropertyPath = flatPropertyPath;
        this.propertyPath = null;
    }

    public String getFlatValue() {
        if (flatValue == null && value != null) {
            flatValue = serializeValue(value);
        }
        return flatValue;
    }

    public void setFlatValue(String flatValue) {
        this.flatValue = flatValue;
        this.value = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int ordering) {
        this.index = ordering;
    }

    public PathMetadata getPropertyPath() {
        if (propertyPath == null && flatPropertyPath != null) {
            if (getQuery() == null) {
                throw new IllegalStateException("query cannot be null.");
            }

            propertyPath = ContextUtil.getMRS().getPropertyPath(query.getTargetType(), StringUtils
                    .delimitedListToStringArray(flatPropertyPath, Path.DELIMITER));
        }
        return propertyPath;
    }

    public void setPropertyPath(PathMetadata pathMetadata) {
        this.propertyPath = pathMetadata;
        this.flatPropertyPath = null;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Object getValue() {
        if (getPropertyPath() == null) {
            throw new IllegalStateException("property path cannot be null.");
        }
        if (value == null && flatValue != null) {
            value = deserializeValue(flatValue);
        }

        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.flatValue = null;
    }

    public boolean isLocal() {
        return propertyPath == null || !propertyPath.isMultiStep();
    }

    public Serializable getIdentifierValue() {
        return id;
    }

    public void setAsNew() {
        id = 0;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isMaster() {
        return master;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getFlatPropertyPath() != null) {
            sb.append(getFlatPropertyPath());
        } else {
            sb.append("null");
        }
        sb.append(" ");
        if (getFlatCondition() != null) {
            sb.append(getFlatCondition());
        } else {
            sb.append("null");
        }
        sb.append(" ");
        if (getFlatValue() != null) {
            sb.append(getFlatValue());
        } else {
            sb.append("null");
        }

        return sb.toString();
    }
}
