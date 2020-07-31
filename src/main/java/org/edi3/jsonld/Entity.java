package org.edi3.jsonld;

import org.apache.commons.lang3.StringUtils;

public class Entity {
    String id;
    String type;
    String name;
    String description;
    String objectClassTermQualifier;
    String objectClassTerm;
    String propertyTermQualifier;
    String propertyTerm;
    String dataTypeQualifier;
    String representationTerm;
    String qualifiedDataTypeId;
    String associatedObjectClassTermQualifier;
    String associatedObjectClassTerm;
    String businessTerm;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectClassTermQualifier() {
        return objectClassTermQualifier;
    }

    public void setObjectClassTermQualifier(String objectClassTermQualifier) {
        this.objectClassTermQualifier = StringUtils.defaultIfEmpty(objectClassTermQualifier, "");
    }

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
    }

    public String getPropertyTermQualifier() {
        return propertyTermQualifier;
    }

    public void setPropertyTermQualifier(String propertyTermQualifier) {
        this.propertyTermQualifier = StringUtils.defaultIfEmpty(propertyTermQualifier, "");;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getDataTypeQualifier() {
        return dataTypeQualifier;
    }

    public void setDataTypeQualifier(String dataTypeQualifier) {
        this.dataTypeQualifier = dataTypeQualifier;
    }

    public String getRepresentationTerm() {
        return representationTerm;
    }

    public void setRepresentationTerm(String representationTerm) {
        this.representationTerm = representationTerm;
    }

    public String getQualifiedDataTypeId() {
        return qualifiedDataTypeId;
    }

    public void setQualifiedDataTypeId(String qualifiedDataTypeId) {
        this.qualifiedDataTypeId = qualifiedDataTypeId;
    }

    public String getAssociatedObjectClassTermQualifier() {
        return associatedObjectClassTermQualifier;
    }

    public void setAssociatedObjectClassTermQualifier(String associatedObjectClassTermQualifier) {
        this.associatedObjectClassTermQualifier = StringUtils.defaultIfEmpty(associatedObjectClassTermQualifier, "");;
    }

    public String getAssociatedObjectClassTerm() {
        return associatedObjectClassTerm;
    }

    public void setAssociatedObjectClassTerm(String associatedObjectClassTerm) {
        this.associatedObjectClassTerm = StringUtils.defaultIfEmpty(associatedObjectClassTerm, "");;
    }

    public String getBusinessTerm() {
        return businessTerm;
    }

    public void setBusinessTerm(String businessTerm) {
        this.businessTerm = businessTerm;
    }

    public String getClassTermWithQualfier(){
        return this.objectClassTermQualifier.concat(this.objectClassTerm);
    }

    public String getAssociatedClassTermWithQualifier(){
        return this.associatedObjectClassTermQualifier.concat(this.associatedObjectClassTerm);
    }

    public String getPropertyTermWithQualifier(){
        return this.propertyTermQualifier.concat(this.propertyTerm);
    }
}
