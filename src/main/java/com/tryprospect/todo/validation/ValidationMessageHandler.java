package com.tryprospect.todo.validation;

import java.util.ResourceBundle;


public class ValidationMessageHandler {

    public static String getMessageFromPropertiesFile(String propertiesFileKey) {
        return ResourceBundle.getBundle(ValidationMessages.class.getSimpleName()).getString(propertiesFileKey);
    }
}
