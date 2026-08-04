/********************************************************************************************************
 * File: NonAcademicClub.java Course materials CST 8277
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("0")
public class NonAcademic extends StudentClub implements Serializable {
    private static final long serialVersionUID = 1L;

    public NonAcademic() {
        super(false);
    }
}
