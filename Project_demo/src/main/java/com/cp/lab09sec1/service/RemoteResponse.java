package com.cp.lab09sec1.service;

import org.springframework.http.HttpStatus;

public record RemoteResponse<T>(HttpStatus status, T body) {
}
