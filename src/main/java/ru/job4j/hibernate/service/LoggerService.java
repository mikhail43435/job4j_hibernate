package ru.job4j.hibernate.service;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ThreadSafe
public class LoggerService {

    public static final Logger LOGGER = LoggerFactory.getLogger(LoggerService.class);

    private LoggerService() {
    }
}