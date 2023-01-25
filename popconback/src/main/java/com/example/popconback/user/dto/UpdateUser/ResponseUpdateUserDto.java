package com.example.popconback.user.dto.UpdateUser;

import lombok.Data;

import java.util.Objects;
@Data
public class ResponseUpdateUserDto {
    private String email;
    private String social;
    private String Token;
    private int alarm;
    private int Nday;
    private int term;
    private int timezone;
    private int manner_temp;

    @Override
    public int hashCode() {
        return Objects.hash(email,social);
    }
}