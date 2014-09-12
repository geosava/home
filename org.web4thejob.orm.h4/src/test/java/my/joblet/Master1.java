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

package my.joblet;

import org.hibernate.validator.constraints.NotBlank;
import org.web4thejob.orm.AbstractHibernateEntity;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Veniamin Isaias
 * @since 1.0.0
 */

public class Master1 extends AbstractHibernateEntity {
    public static final String FLD_ID = "id";
    public static final String FLD_NAME = "name";
    public static final String FLD_DETAILS = "details";
    public static final String FLD_REFERENCE1 = "reference1";

    private long id;
    @NotBlank
    private String name;
    @NotNull
    private Reference1 reference1;
    private int version;
    private Set<Detail> details = new HashSet<Detail>(0);

    public Set<Detail> getDetails() {
        return details;
    }

    public void setDetails(Set<Detail> details) {
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Reference1 getReference1() {
        return reference1;
    }

    public void setReference1(Reference1 reference1) {
        this.reference1 = reference1;
    }

    public int getVersion() {
        return version;
    }

    public Serializable getIdentifierValue() {
        return id;
    }

    public void setAsNew() {
        id = 0;
    }
}
