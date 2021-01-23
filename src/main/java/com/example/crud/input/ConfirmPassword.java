package com.example.crud.input;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ConfirmPassword {
    @NotEmpty(message = "*Please provide your confirm code")
    private String confirmCode;
    @Length(min = 5)
    @NotEmpty(message = "*Please pro")
    private String newPassword;

    private String username;
    public ConfirmPassword(String confirmCode, String newPassword, String username) {
        this.confirmCode = confirmCode;
        this.newPassword = newPassword;
        this.username= username;
    }


    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
