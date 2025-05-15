package com.fpt.bbusbe.model.mqtt;

import lombok.Getter;

@Getter
public enum OperationType {

    ADD("AddPersons"),
    ADD_RETURN("AddPersons-Ack"),
    EDIT("EditPersons"),
    EDIT_RETURN("EditPersons-Ack"),
    DELETE("DeletePersons"),
    DELETE_RETURN("DeletePersons-Ack");

    private String operationType;

    OperationType(String operationType) {
        this.operationType = operationType;
    }
}
